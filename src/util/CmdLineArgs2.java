package util;

import java.util.*;


/** A simplified version cargo-culted from BC-leronen code base */
public class CmdLineArgs2 {

    /** options with no value */
    public Set<String> flags;
    
    /** options with a value */
    public Map<String,String> opts;
    
    /** non-option arguments */
    public List<String> args;
    
    
    public CmdLineArgs2(String[] args) throws IllegalArgumentsException {
        this(args, CollectionUtils.EMPTY_STRING_SET);
    }
    
    public CmdLineArgs2(String[] args, String... flagNames) throws IllegalArgumentsException {
        this(args, new LinkedHashSet<String>(Arrays.asList(flagNames)));       
    }

    public String getOpt(String name) {
        return opts.get(name);
    }
    

    
    public List<String> getNonOptArgs() {
        return args; 
    }
    
    /** 
     * @param flagNames names of options with no value; all other options are considered
     * to have a value.
     */
    public CmdLineArgs2(String[] args, Set<String> flagNames) throws IllegalArgumentsException {
        this.flags = new LinkedHashSet<String>();
        this.opts = new LinkedHashMap<String, String>();
        this.args = new ArrayList<String>();
        
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                // forget about the second '-' ("--" and "-" are equivalent)
                arg = arg.substring(1);
            }
            if (arg.startsWith("-")) {
                String name = arg.substring(1);
                if (flagNames.contains(name)) {
                    // flag
                    this.flags.add(name);
                }
                else {     
                    // option with value
                    i++;
                    if (i >= args.length) {
                        throw new IllegalArgumentsException("No value for option: "+name);
                    }
                    String val = args[i];
                    this.opts.put(name, val);
                }
               
            }
            else {
                // non-opt arg
                this.args.add(arg);
            }
        }       
    }

    public Integer getIntOpt(String name) {
        String s = opts.get(name);
        if (s != null) {
            return Integer.parseInt(s);
        }
        else {
            return null;
        }
        
    }
    
    public Integer getIntOpt(String name, int defaultVal) {
        String s = opts.get(name);
        if (s != null) {
            return Integer.parseInt(s);
        }
        else {
            return defaultVal;
        }
        
    }
    
    public static class IllegalArgumentsException extends Exception {        
        private static final long serialVersionUID = -843421106449840299L;

        private IllegalArgumentsException(String msg) {
            super(msg);
        }
    }
    
    /**
     * Remove and return the first arg from the list of positional arguments.
     * Return null if no more args.
     */
    public String shift() {
        if (args.size() == 0) {
            return null;
        }
        else {
            return args.remove(0);
        }
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
    
    /** Does not include the ones already removed by calling shift() */
    public int getNumArgs() {
        return args.size();
    }
    
    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }
    
    public String toString() {
        return "flags:\n\t"+StringUtils.collectionToString(flags, "\n\t")+"\n"+
               "opts:\n\t"+StringUtils.mapToString(opts, "=", "\n\t")+"\n"+
               "args:\n\t"+StringUtils.collectionToString(args, "\n\t");
    }
    
    public static void main(String[] pArgs) throws Exception {
        CmdLineArgs2 args = new CmdLineArgs2(pArgs, Collections.singleton("flag"));
        System.out.println(""+args);
    }
    
    
    
}
 
