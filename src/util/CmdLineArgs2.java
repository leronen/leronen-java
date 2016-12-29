package util;

import java.util.*;


/** A simplified version cargo-culted from BC-leronen code base */
public class CmdLineArgs2 {

    /** options with no value */
    public Set<String> flags;
    
    /** options with a value */
    public Map<String,String> opts;
    
    /** non-option arguments */
    public List<String> posargs;
    
    
    public CmdLineArgs2(String[] args) throws IllegalArgumentsException {
        this(args, CollectionUtils.EMPTY_STRING_SET);
    }
    
    public CmdLineArgs2(String[] args, String... flagNames) throws IllegalArgumentsException {
        this(args, new LinkedHashSet<String>(Arrays.asList(flagNames)));       
    }

    public String get(String name) {
        return opts.get(name);
    }
    
    /** Get i:th non-opt argument */
    public String get(int i) {
        return posargs.get(i);
    }
    
    public List<String> getNonOptArgs() {
        return posargs; 
    }
    
    public boolean hasPositionalArgs() {
        return posargs.size() > 0; 
    }
    
    public int numPositionalArgs() {
        return posargs.size(); 
    }
    
    /** 
     * @param flagNames names of options with no value; all other options are considered
     * to have a value.
     */
    public CmdLineArgs2(String[] args, Set<String> flagNames) throws IllegalArgumentsException {
        this.flags = new LinkedHashSet<String>();
        this.opts = new LinkedHashMap<String, String>();
        this.posargs = new ArrayList<String>();
        
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
                this.posargs.add(arg);
            }
        }       
    }

    public Integer getInt(String name) {
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
    
    public Long getLongOpt(String name) {
        String s = opts.get(name);
        if (s != null) {
            return Long.parseLong(s);
        }        
        else {
        	return null;
        }
        
    }
    
    public static class IllegalArgumentsException extends Exception {        
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
        if (posargs.size() == 0) {
            return null;
        }
        else {
            return posargs.remove(0);
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
        return posargs.size();
    }
    
    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }
    
    public String toString() {
        return "flags:\n\t"+StringUtils.collectionToString(flags, "\n\t")+"\n"+
               "opts:\n\t"+StringUtils.format(opts, "=", "\n\t")+"\n"+
               "args:\n\t"+StringUtils.collectionToString(posargs, "\n\t");
    }
    
    public static void main(String[] pArgs) throws Exception {
        CmdLineArgs2 args = new CmdLineArgs2(pArgs, Collections.singleton("flag"));
        System.out.println(""+args);
    }
    
    
    
}
 
