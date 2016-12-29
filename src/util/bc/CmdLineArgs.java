package util.bc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.CollectionUtils;
import util.StringUtils;


/** A simple command line argument parser/data structure */
public class CmdLineArgs {

    private final ArgsDef def;

    /** options with no value */
    public Set<String> flags;

    /** options with a value */
    public Map<String,String> opts;

    /** non-option arguments */
    public List<String> args;

    private boolean shifted = false;

    public CmdLineArgs(String[] args) throws IllegalArgumentsException {
        this(args, new ArgsDef());
    }

    public CmdLineArgs(String[] args, String... flagNames) throws IllegalArgumentsException {
        this(args, new LinkedHashSet<String>(Arrays.asList(flagNames)));

    }

    public CmdLineArgs(String[] args, Set<String> flagNames) throws IllegalArgumentsException {
        this(args, new ArgsDef(flagNames));
    }

    public Set<String> getDefinedOptions() {
        return opts.keySet();
    }

    /**
     * @param flagNames names of options with no value; all other options are considered
     * to have a value.
     */
    public CmdLineArgs(String[] args, ArgsDef def) throws IllegalArgumentsException {
        this.flags = new LinkedHashSet<String>();
        this.opts = new LinkedHashMap<String, String>();
        this.args = new ArrayList<String>();
        this.def = def;

        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                // forget about the second '-' ("--" and "-" are equivalent)
                arg = arg.substring(1);
            }
            if (arg.startsWith("-")) {
                String name = arg.substring(1);
                if (def.isFlag(name)) {
                    // flag
                    this.flags.add(name);
                }
                else {
                    // option with value
                    i++;
                    if (i >= args.length) {
                        throw new IllegalArgumentsException("No value for option: "+name);
                    }

                    if (def.hasOptionNames() && !(def.isOptionName(name))) {
                        throw new IllegalArgumentsException("Unrecognized option: -"+name);
                    }
                    else {
                        String val = args[i];
                        this.opts.put(name, val);
                    }
                }

            }
            else {
                // non-opt arg
                this.args.add(arg);
            }
        }

        if (def.hasMandatoryOptionNames()) {
            for (String name: def.getMandatoryOptions()) {
                if (getOpt(name) == null) {
                    throw new IllegalArgumentsException("No value for mandatory option -"+name);
                }
            }
        }

        for (Set<String> optGroup: def.getOptionGroups()) {
            if (CollectionUtils.intersection(opts.keySet(), optGroup).size() != 1) {
                throw new IllegalArgumentsException("Must give exactly one of options in group: -"+StringUtils.colToStr(optGroup,", -"));
            }
        }


        int numArgs = this.args.size();
        if (!(def.isValidNumArgs(numArgs))) {
            throw new IllegalArgumentsException("Not a valid number of arguments: "+numArgs);
        }
    }

    /** get a non-opt arg */
    public String getArg(int ind) {
        return getNonOptArg(ind);
    }

   /**
    * Get a (non-opt) arg by name (naturally requires that names have been registered
    * to argsdef).
    *
    * Not compatible with calling shift().
    */
    public String getArg(String name) {
        if (shifted) {
            throw new RuntimeException("Already shifted args; cannot call getArg(String)");
        }
        int index = def.getArgPos(name, getNumArgs());
        return args.get(index);
    }


    /**
     * Get a (non-opt) arg by name (naturally requires that names have been registered
     * to argsdef).
     *
     * Not compatible with calling shift().
     */
    public Integer getIntArg(String name) {
        String s = getArg(name);
        if (s != null) {
            return Integer.parseInt(s);
        }
        else {
            return null;
        }
    }

    public String getOpt(String name) {
        if (def.hasOptionNames() && !(def.hasOption(name))) {
            System.err.println("Warning: no such option: -"+name);
            return null;
        }

        String val = opts.get(name);
        if (val == null) {
            // try defaults
            val = def.getDefaultValue(name);
            if (val != null) {
                System.err.println("Using default value for option -"+name+": "+val);
            }
        }

        return val;
    }

    public List<String> getNonOptArgs() {
        return args;
    }

    public String getNonOptArg(int ind) {
        return getNonOptArgs().get(ind);
    }


    public Integer getIntOpt(String name) {
        String s = getOpt(name);
        if (s != null) {
            return Integer.parseInt(s);
        }
        else {
            return null;
        }
    }

    public Long getLongOpt(String name) {
        String s = getOpt(name);
        if (s != null) {
            return Long.parseLong(s);
        }
        else {
            return null;
        }
    }

    public Double getDoubleOpt(String name) {
        String s = getOpt(name);
        if (s != null) {
            return Double.parseDouble(s);
        }
        else {
            return null;
        }
    }

    public static class IllegalArgumentsException extends RuntimeException {
        private static final long serialVersionUID = -843421106449840299L;

        public IllegalArgumentsException(String msg) {
            super(msg);
        }
    }


    /**
     * Remove and return the first arg from the list of positional arguments.
     * Return null if no more args.
     */
    public String shift() {
        shifted = true;
        if (args.size() == 0) {
            return null;
        }
        else {
            return args.remove(0);
        }
    }

    /** are there more non-opt args to shift? */
    public boolean hasMoreArgs() {
        return args.size() > 0;
    }

    /**
     * Remove and return the first arg from the list of positional arguments
     * as integer. Return null if no more args. Throw NumberFormatException if next arg is not an integer.
     */
    public Integer shiftInt() {
        String val = shift();
        if (val != null) {
            return Integer.parseInt(val);
        }
        else {
            return null;
        }
    }

    public Double shiftDouble() {
        String val = shift();
        if (val != null) {
            return Double.parseDouble(val);
        }
        else {
            return null;
        }
    }

    /**
     * Get a positional argument by name. Requires that argument names have been registered to the ArgsDef
     * @throws ArgumentE
     * @throws RuntimeException if no argument names specified.

    public String getPositionalArgumentByName()

    /**
     * Get number of positional arguments.
     * Does not include the ones already removed by calling shift().
     */
    public int getNumArgs() {
        return args.size();
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    /**
     * check whether option has non-null value. This may be the case even
     * if used did not specify one, as default values can be specified.
     */
    public boolean hasOpt(String optionName) {
        return getOpt(optionName) != null;
    }

    @Override
    public String toString() {
        return "flags:\n\t"+StringUtils.colToStr(flags, "\n\t")+"\n"+
               "opts:\n\t"+StringUtils.format(opts, "=", "\n\t")+"\n"+
               "args:\n\t"+StringUtils.colToStr(args, "\n\t");
    }

    public static void main(String[] pArgs) throws Exception {
        CmdLineArgs args = new CmdLineArgs(pArgs, Collections.singleton("flag"));
        System.out.println(""+args);
    }



}
