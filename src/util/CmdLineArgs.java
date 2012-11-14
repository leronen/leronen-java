package util;

import java.io.File;
import java.util.*;

import util.dbg.*;

import java.util.regex.*;


/**
 *  Todo: split argParser into "Arguments" and "ArgParser" 
 * 
 * Attention: only requires options of form "-opt val" if argsdef is specified
 * (otherwise, form "opt=val" is required!) 
 */
public class CmdLineArgs implements IArgs {
        
	// for reflection:
	private static final String FIELD_NAME_OPTION_DEFINITIONS = "OPTION_DEFINITIONS";                                   
	private static final String FIELD_NAME_NAMES_OF_NON_OPT_ARGS = "NAMES_OF_NON_OPT_ARGS";		
	
	// other stuff
    private String mOriginalCommandLine;
    private String[] mOriginalArgs;
    private LinkedHashMap mOptsWithVal = new LinkedHashMap();
    private List<String> mOptsWithoutVal = new ArrayList();
    private List<String> mNonOptArgs = new ArrayList();

    private List<String> mParsingErrors = new ArrayList<String>();        
            
    private static final Pattern OPT_WITH_VAL_PATTERN = Pattern.compile("^-([^=]*)=(\\S*?)$");
    private static final Pattern OPT_WITHOUT_VAL_PATTERN = Pattern.compile("^-([^=]*)$");
    
    /**
     * An option with one of these values shall be regarded as undefined, and
     * consequently gets the default value, if one is specified...
     */
    private static final Set DUMMY_VALUES = new HashSet(CollectionUtils.makeList("", "-"));                                               
        
    private ArgsDef mDef;        
        
    /** global flag, which acts as a default for the instance-specific flag */
    private static boolean sLoggingEnabled;
    
    /** instance-specific flag */
    private static boolean mLoggingEnabled;                
        
    
    public static void disableLogging() {
        sLoggingEnabled = false;
    }
    
    public static void enableLogging() {
        sLoggingEnabled = true;
    }
    
    private CmdLineArgs() {
        // for cloning purposes
    }            
    
    /** Arg parser with default logging flag */
    public CmdLineArgs(String[] args) {        
        this(args, sLoggingEnabled);    
    }
    
    /** Arg parser with default logging flag */
    public CmdLineArgs(String[] args,
                       ArgsDef pDef) {        
        init(args, pDef, sLoggingEnabled);
    }
    
    /** Arg parser with default logging flag */
    public CmdLineArgs(String[] args,
                       ArgsDef pDef,
                       boolean pLoggingEnabled) {        
        init(args, pDef, pLoggingEnabled);
    }
    
    public CmdLineArgs(String[] args, boolean pEnableLogging) {  
        init(args, null, pEnableLogging);
    }        
    
    public CmdLineArgs(String[] args, 
      				   boolean pEnableLogging, 
      				   Object[][] pOptionDefinitions, 
      				   String[] pNamesOfNonOptArgs) {
    	this(args, pEnableLogging, pOptionDefinitions, null, pNamesOfNonOptArgs);
    }
    
    /** Uses reflection to find the arg def fields! */
    public CmdLineArgs(String[] pArgs, 
				 	   boolean pEnableLogging,
				 	   Class pExecutableClass,
				 	   Collection<String> pNamesOfOptsToInclude) {
    	Object[][] optDefs = (Object[][])ReflectionUtils.getPublicStaticField(pExecutableClass, FIELD_NAME_OPTION_DEFINITIONS);
    	String[] namesOfNonOptArgs = (String[])ReflectionUtils.getPublicStaticField(pExecutableClass, FIELD_NAME_NAMES_OF_NON_OPT_ARGS);
    	
    	if (pExecutableClass == null) {
    		throw new RuntimeException("WhatWhatWhat!!!");    		
    	}    	
    	
    	init(pArgs, 
    	 	 pEnableLogging, 
    		 optDefs, 
    		 pNamesOfOptsToInclude,
    		 namesOfNonOptArgs);
    	
        if (mDef.getProgName() == null) {
            mDef.setProgName("java "+pExecutableClass.getCanonicalName());
        }
    }
    
    public void setProgName(String pName) {        
        mDef.setProgName(pName);
    }
    	     
    private void init(String[] pArgs, 
                      boolean pEnableLogging,                      
                      Object[][] pOptDefs, 
                      Collection<String> pNamesOfOptionsToInclude, 
                      String[] pNamesOfNonOptArgs) {
        ArgsDef def = new ArgsDef(pOptDefs, pNamesOfNonOptArgs, pNamesOfOptionsToInclude);
        // Logger.info("Created arg def\n"+def.usage());
        init(pArgs, def, pEnableLogging);
    }
    
    /** The real init method */      
    private void init(String[] args,
                      ArgsDef pDef,
    		          boolean pEnableLogging) {                      
    		          // Object pDef, 
    		          // Collection<String> pNamesOfOptionsToInclude, 
    		          // String[] pNamesOfNonOptArgs) {
        mDef = pDef;                             
        
        mLoggingEnabled = pEnableLogging;
        mOriginalCommandLine = StringUtils.arrayToString(args, " ");
        mOriginalArgs = args;
        
        Iterator i = Arrays.asList(args).iterator();
        while (i.hasNext()) {
            Argument arg = parseNextArgument(i);
            arg.store();            
        }
        
        // Logging kludges:
        if (isDefined("hierarchical_logging")) {
            String optVal = getOpt("hierarchical_logging");
            boolean hierLogging;
            if (optVal == null || optVal.equals("true") || optVal.equals("1")) {
                hierLogging = true;
            }
            else {
                hierLogging = false;
            }
            Logger.setHierarchicalLogging(hierLogging);                        
        }
        
        if (isDefined("loglevel")) {
            Logger.setLogLevel(getIntOpt("loglevel"));
        }   
    
        String progName = Logger.getProgramName();
        
        if (progName != null && mDef != null) {
            mDef.setProgName(progName);
        }        
        
        if (mParsingErrors.size() > 0) {
        	System.err.println("There were following parsing errors:\n\t"+
          			     	   StringUtils.listToString(mParsingErrors, "\n\t")+"\n\n"+
          			     	   mDef.usage());
        	System.exit(-1);        
        }
           
    }
    
    public CmdLineArgs(String[] args, 
    			 	boolean pEnableLogging, 
    			 	Object[][] pArgumentDefinitions, 
    			 	Collection<String> pNamesOfOptionsToInclude, 
    			 	String[] pNamesOfNonOptArgs) {    	  
    	init(args, 
             pEnableLogging, 
             pArgumentDefinitions, 
             pNamesOfOptionsToInclude, 
             pNamesOfNonOptArgs);
    }                        
        
    public List getParsingErrors() {
        return mParsingErrors;
    }         
        
    private Argument parseNextArgument(Iterator pArgs) {        
        String arg = (String)pArgs.next();
        // System.err.println("parseNextArgument: "+arg);
        if (isOptWithVal(arg)) {
            String optName = parseOptName(arg);
            if (mDef != null) {
                // OK, we have knowledge on the permitted options...
                if (!mDef.getValidOptionNames().contains(optName)) {
                    mParsingErrors.add("Invalid option: "+optName);                    
                    // System.err.println("Invalid option: "+optName);
                    // mDef.outputValidOptions();
                    // System.exit(-1);
                }
                // else {
                    // System.err.println("A valid option: "+optName);   
                // }
            }
            // opt name ok...
            optName = shortNameToLongNameIfNeeded(optName);            
            String optVal = parseOptVal(arg);
            return new OptionWithValue(optName, optVal);                            
        }
        else if (isOptWithoutVal(arg)) {
            String optName = parseOptName(arg);
            if (mDef != null && !mDef.getValidOptionNames().contains(optName)) {
                mParsingErrors.add("Invalid option: "+optName);                       
            }            
            // opt name ok...
            optName = shortNameToLongNameIfNeeded(optName);
            if (mDef != null && mDef.getNamesOfOptionsRequiringValue().contains(optName)) {
                // requires value, so interpret the next argument as the value...
                if (!(pArgs.hasNext())) {
                    System.err.println("Missing value for option: "+formatOptName(optName));
                    System.exit(-1);
                }
                String optVal = (String)pArgs.next();
                // if (isOptWithVal(optVal) || isOptWithoutVal(optVal)) {
//                    System.err.println("Missing value for option: "+optName);
//                    System.exit(-1);
                // }
                return new OptionWithValue(optName, optVal);
            }
            else {
                // does not require value, to our best knowledge...
                return new OptionWithoutValue(optName);
            }
        }
        else {
            // non-opt arg...
            return new NonOptionArgument(arg);
        }                        
    }
        
    private String shortNameToLongNameIfNeeded(String pName) {
        if (mDef != null && mDef.getShortNameToLongNameMap().containsKey(pName)) {       
            return mDef.getShortNameToLongNameMap().get(pName);
        }
        else {
            return pName;
        }
    }    
     
    public String[] getOriginalArgs() {
        return mOriginalArgs;
    }
     
    /** shift next argument from list (after calling shift, get nonoptargs will return an array
      * that does not contain the shifted element 
      * Return null, if no more args...*/
    public String shift() {
        if (mNonOptArgs.size() > 0) {
            return (String)CollectionUtils.shift(mNonOptArgs);
        }
        else {
            return null;
        }
    }
    
    public boolean hasMoreNonOptArgs() {
        return mNonOptArgs.size()>0;
    }
    
    public int shiftInt() {
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        return Integer.parseInt(val);                
    }
    
    public char shiftChar() {
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        if (val.length() != 1) {
            if (val.equals("\\t")) {
                // a special case
                return '\t';
            }
            else {
                throw new RuntimeException("Not a char: "+val);
            }
        }
        return val.charAt(0);                
    }

    
    public File shiftFile() {
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        return new File(val);
    }
    
    
    public double shiftDouble() { 
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        return Double.parseDouble(val);                
    }
    
    
    public double shiftDouble(String pParamName) { 
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        // dbgMsg("Shifted double: "+pParamName+": "+val);
        return Double.parseDouble(val);                
    }
    
    /** Let's cheat and add an argument afterwards... */
    public void addOption(String pName, String pVal) {
        if (!DUMMY_VALUES.contains(pVal)) {                   
           mOptsWithVal.put(pName, pVal);
        }                        
    }
    
    /** Let's cheat and add an argument afterwards... */
    public void addOption(String pName) {                           
        mOptsWithoutVal.add(pName);                                
    }
    
    /** Let's cheat and set the non-opt arguments anew... */
    public void setArguments(List<String> pNewArgs) {        
        mNonOptArgs = pNewArgs;                 
    }

    public CmdLineArgs createClone() {
        CmdLineArgs clone = new CmdLineArgs();
        clone.mOriginalCommandLine = mOriginalCommandLine;
        clone.mOriginalArgs = Utils.clone(mOriginalArgs);        
        clone.mOptsWithVal = new LinkedHashMap(mOptsWithVal);                
        clone.mOptsWithoutVal = new ArrayList(mOptsWithoutVal);
        clone.mNonOptArgs = new ArrayList(mNonOptArgs);
        
        clone.mParsingErrors = new ArrayList(mParsingErrors);                                                                                       
        clone.mDef = mDef;        
            
        return clone;
    }
    
    /** shift and produce debug message */
    public String shift(String pHopefullyParamName) {
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        // dbgMsg("Shifted arg: "+pHopefullyParamName+": "+val);
        return val;        
    }

    public int shiftInt(String pHopefullyParamName) {
        String val = (String)CollectionUtils.shift(mNonOptArgs);
        // dbgMsg("Shifted int arg: "+pHopefullyParamName+": "+val);
        return Integer.parseInt(val);                
    }              
    
    private boolean isOptWithVal(String pArg) {
        return OPT_WITH_VAL_PATTERN.matcher(pArg).matches();    
    }
    
    private boolean isOptWithoutVal(String pArg) {
        return OPT_WITHOUT_VAL_PATTERN.matcher(pArg).matches();    
    }   
    
    public String getCommandLine() {
        return mOriginalCommandLine;
    }        

    /** For convenience */
    public void logParams(int pLogLevel) {               
        Logger.startSubSection("Params", pLogLevel);
        Logger.startSubSection("opts with val", pLogLevel);
        Logger.log(StringUtils.mapToString(mOptsWithVal, "\n"), pLogLevel);
        Logger.endSubSection(pLogLevel);
        Logger.startSubSection("opts without val", pLogLevel);
        Logger.log(StringUtils.listToString(mOptsWithoutVal, "\n"), pLogLevel);
        Logger.endSubSection(pLogLevel);
        Logger.startSubSection("non-opt args", pLogLevel);
        Logger.log(StringUtils.listToString(mNonOptArgs,  "\n"), pLogLevel);
        Logger.endSubSection(pLogLevel);                
        Logger.endSubSection("Params", pLogLevel);        
    }
            
    private String parseOptName(String pArg) {
        Matcher m = OPT_WITH_VAL_PATTERN.matcher(pArg);
        if (m.matches()) {
        	String val = m.group(1);
        	if (val.charAt(0) == '-') {
        		val = StringUtils.removeFirstCharacter(val);            		
        	}
        	return val;            
        }
        else {
            m = OPT_WITHOUT_VAL_PATTERN.matcher(pArg);
            if (m.matches()) {
            	String val = m.group(1);
            	if (val.charAt(0) == '-') {
            		val = StringUtils.removeFirstCharacter(val);            		
            	}
            	return val;
            }
            else {
                return null;
            }
        }                                 
    }
    
    private String parseOptVal(String pArg) {
        Matcher m = OPT_WITH_VAL_PATTERN.matcher(pArg);
        if (m.matches()) {
            return m.group(2);
        }
        else {
            return null;
        }                
    }
        
    public String getOpt(String pLongName) {
        // Logger.info("Getting option: "+pLongName);
        // Logger.info("mOptsWithVal:\n"+StringUtils.mapToString(mOptsWithVal));
        String result = (String)mOptsWithVal.get(pLongName); 
        if (result == null && mDef != null) {
            result = mDef.getDefaultValue(pLongName); 
        }        
        // Logger.info("Returning: "+result);
        return result;     
    }        
    
    public List<String> getListOpt(String pLongName) {
        String result = (String)mOptsWithVal.get(pLongName); 
        if (result == null && mDef != null) {
            result = mDef.getDefaultValue(pLongName); 
        }
        if (result == null) {
        	return null;
        }
        else {
        	return Arrays.asList(result.split(","));        	
        	
        }
    }
    
    public Set getNamesOfNonOverriddenDefaultValues() {                                        
        Set namesOfOptionsWithDefaultVal = mDef.getNamesOfOptionsWithDefaultValue();
        Set namesOfUserGivenOptions = mOptsWithVal.keySet();
        return CollectionUtils.minus(namesOfOptionsWithDefaultVal, namesOfUserGivenOptions);            
    }
    
    public Map getNonOverriddenDefaultValues() {                                        
        Set names = getNamesOfNonOverriddenDefaultValues();
        return mDef.getDefaultValues(names);            
    }
    
    public ArgsDef getDef() {
        return mDef;
    }
    
    
  /** 
   * Get option value, with our own default value; note that it is an error to use this, 
   * if a ParamsDef is used, as then default values should be specified there!
   */     
    public String getOpt(String pLongName, String pDefaultValue) {        
        if (mDef != null && mDef.getDefaultValue(pLongName)!= null) {
            throw new RuntimeException("Soh, soh! - should not use this method when a params def already specifies a default value!");    
        }
        String result = (String)mOptsWithVal.get(pLongName);
        if (result == null) {
            return pDefaultValue;
        }
        else {
            // dbgMsg("opt val("+pLongName+")="+result);
            return result;
        }
    }
    
    public Map getOptMap(boolean pIncludeDefaultValues) {
        if (pIncludeDefaultValues) {
            Map result = new HashMap(mOptsWithVal);
            result.putAll(getNonOverriddenDefaultValues());
            return Collections.unmodifiableMap(result);
        }
        else {
            return Collections.unmodifiableMap(mOptsWithVal);
        }
    }
    
    public boolean isDefined(String pLongName) {
        boolean result = Arrays.asList(getDefinedOptions()).contains(pLongName);        
        return result;    
    }
    
    public boolean isDefined(OptDef pOptDef) {        
        return isDefined(pOptDef.longname);                
    }
    
    public boolean hasValue(String pOpt) {
        return mOptsWithVal.keySet().contains(pOpt);    
    }
    
    public String[] getNonOptArgs() {
        return mNonOptArgs.toArray(new String[mNonOptArgs.size()]); 
    }
    
    public List<String> getNonOptArgsAsList() {
        return mNonOptArgs;
    }
    
    public String getNonOptArg(int pInd) {
        return mNonOptArgs.get(pInd); 
    }
    
    public String getNonOptArg(String pArgName) {
    	int ind = mDef.getIndOfNonOptArg(pArgName);
    	if (ind == -1) {
    		throw new RuntimeException("No such arg: "+pArgName);
    	}
    	else if (mNonOptArgs.size() <= ind) {
    		throw new RuntimeException("Insufficient number of args; "+
    				                   "missing "+StringUtils.formatOrdinal(ind+1)+" "+
    				                   "argument "+pArgName+"\n\n"+
    				                   mDef.usage()); 
    	}    		
    	else {    		    			    
    		return getNonOptArg(ind);
    	}
    }
    
    public int getIntArg(int pInd) {
        return Integer.parseInt(mNonOptArgs.get(pInd)); 
    }
    
    public double getDoubleArg(int pInd) {
        return Double.parseDouble(mNonOptArgs.get(pInd)); 
    }
    
    /** Return null, if not defined */
    public Double getDoubleOpt(String pLongName) {                
        String val = getOpt(pLongName);
        if (val == null && mDef != null) {
            val = mDef.getDefaultValue(pLongName);
        }
        if (val == null) {
            return null;
            // throw new RuntimeException("Cannot get option "+formatOptName(pLongName)+"; not defined!");
        }
        else {
            return Double.parseDouble(val);
        }        
    }
    
    public int getIntOpt(String pLongName, int pDefaultValue) {
        if (mDef != null && mDef.getDefaultValue(pLongName)!= null) {
            throw new RuntimeException("Soh, soh! - should not use this method when a params def already specifies a default value!");    
        }
        String val = getOpt(pLongName);
        if (val == null) {
            return pDefaultValue;
        }
        else {
            return Integer.parseInt(val);
        }
    }
    
    public boolean getBooleanOpt(String pLongName, boolean pDefaultValue) {
        if (mDef != null ) {
            throw new RuntimeException("Soh, soh! - should not use this method when a params def is used!");    
        }
        String val = getOpt(pLongName);
        if (val == null) {
            return pDefaultValue;
        }
        else {
            return val.equals("true") || 
                   val.equals("True") ||
                   val.equals("TRUE") ||
                   val.equals("1");                        
        }
    }
    
    public Integer getIntOpt(String pLongName) {
        String val = getOpt(pLongName);
        if (val == null && mDef != null) {
            val = mDef.getDefaultValue(pLongName);
        }
        if (val == null) {                    	           
            // throw new RuntimeException("Cannot get option "+formatOptName(pLongName)+"; not defined!");
            return null;
        }
        else {
            return Integer.parseInt(val);
        }
    }
    
    private String formatOptName(String pLongName) {
    	String shortName = null;        	
    	if (mDef != null) {
    		shortName = mDef.getShortName(pLongName);
    	}        	
    	return shortName != null ? "-"+pLongName + " (-"+shortName+")" : "-"+pLongName;
    }
    
    public boolean getBooleanOpt(String pLongName) {
        String val = getOpt(pLongName);
        if (val == null && mDef != null) {
            val = mDef.getDefaultValue(pLongName);
        }
        if (val == null) {            
            throw new RuntimeException("Cannot get option "+pLongName+"; not defined!");
        }
        else {
            return ConversionUtils.anyToBoolean(val);            
        }
    }
                    
    
    public int getNumNonOptArgs() {
        return mNonOptArgs.size(); 
    }
            
    public boolean hasAllOptions(String[] pRequiredOptions) {
        Set definedOptions = new HashSet(Arrays.asList(getDefinedOptions()));
        Set requiredOptions = new HashSet(Arrays.asList(pRequiredOptions));
        return definedOptions.containsAll(requiredOptions); 
    }

    public String[] missingOptions(String[] pRequiredOptions) {
        Set definedOptions = new LinkedHashSet(Arrays.asList(getDefinedOptions()));
        Set requiredOptions = new LinkedHashSet(Arrays.asList(pRequiredOptions));
        Set missingOptions = new LinkedHashSet(requiredOptions);
        missingOptions.removeAll(definedOptions);
        return ConversionUtils.stringCollectionToArray(missingOptions);
    }                        
            
    /** Get the keys of defined options */            
    public String[] getDefinedOptions() {
        ArrayList definedOpts = new ArrayList(mOptsWithVal.keySet());
        definedOpts.addAll(mOptsWithoutVal);
        return ConversionUtils.stringCollectionToArray(definedOpts); 
    }
    
    public String[] getNonValOptions() {
        ArrayList definedOpts = new ArrayList(mOptsWithVal.keySet());
        definedOpts.addAll(mOptsWithoutVal);
        return ConversionUtils.stringCollectionToArray(mOptsWithoutVal); 
    }
    
    private Map makeNonOptParamByNameMap() {
        Logger.info("non-opt args: "+mNonOptArgs);
        
        LinkedHashMap result = new LinkedHashMap();
        List<String> names = mDef.getNonOptParamNames();
        for (int i=0; i<mNonOptArgs.size(); i++) {
            result.put(names.get(i), mNonOptArgs.get(i));                 
        }
        return result;
    }
    
    
    
    public String toString() {
        String tmp0 = ( mOptsWithVal.size() + mOptsWithoutVal.size() ) > 0 ? "Options:" : ""; 
        String tmp1 = mOptsWithVal.size() > 0 ? "\n  "+StringUtils.mapToString(mOptsWithVal, "\n  ") : "";
        String tmp2 = mOptsWithoutVal.size() > 0 ? "\n  "+StringUtils.listToString(mOptsWithoutVal, "\n  "  ): "";
        String tmp3;
        if (mDef != null && mDef.getNonOptParamNames() != null) {            
            tmp3 = mNonOptArgs.size() > 0 ? "\nArguments:\n  "+StringUtils.mapToString(makeNonOptParamByNameMap(), "\n  ") : "";
        }
        else {
            tmp3 = mNonOptArgs.size() > 0 ? "\nArguments:\n  "+StringUtils.listToString(mNonOptArgs,  "\n  ") : "";
        }            
        return tmp0+tmp1+tmp2+tmp3;               
    }
    
    public boolean hasOptions() {
        return mOptsWithVal.size() > 0 || mOptsWithoutVal.size() > 0;
    }
    
    public String optsWithValString() {
        return mOptsWithVal.size() > 0 ? StringUtils.mapToString(mOptsWithVal, "\n  ") : ""; 
    }
    
    public String optsWithoutValString() {    
        return mOptsWithoutVal.size() > 0 ? StringUtils.listToString(mOptsWithoutVal, "\n  "  ): "";
    }
    
    public String toString2(Collection<String> pDefaultParamsNotToDisplay ) {
    	StringBuffer result = new StringBuffer();
    	    
    	result.append(toString());
    	result.append("\n");
    	
    	Map<String, ? extends Object> nonOverriddenDefaultValuesToBeDisplayedToTheUser = new LinkedHashMap(getNonOverriddenDefaultValues());                
    
    	// now maybe these are not worth showing to the user...
    	for (String paramName: pDefaultParamsNotToDisplay) {
    		nonOverriddenDefaultValuesToBeDisplayedToTheUser.remove(paramName);
    	}
        	    	
    	if (nonOverriddenDefaultValuesToBeDisplayedToTheUser.size() > 0 ) {            
    		result.append("Using default values for options not specified on command-line:\n  "+                    
                       	  StringUtils.mapToString(nonOverriddenDefaultValuesToBeDisplayedToTheUser, "=", "\n  ")
                       	  +"\n");
    	}
    	
    	return result.toString();
    		
    			
    }
    
    
    public static void main (String[] args) {
        CmdLineArgs parser = new CmdLineArgs(args);
        dbgMsg("defined options: \n"+StringUtils.arrayToString(parser.getDefinedOptions(), "\n"));
        // String[] nonOptArgs = Utils.arrayToString(parser.getNonOptArgs(), "\n"));
        // dbgMsg("number of non-opt args: \n"+Utils.arrayToString(parser.getNonOptArgs(), "\n"));
        dbgMsg("non-opt args: \n"+StringUtils.arrayToString(parser.getNonOptArgs(), "\n"));
        dbgMsg("opt m: "+parser.getOpt("m"));
        dbgMsg("opt n: "+parser.getOpt("n"));
        dbgMsg("opt ö: "+parser.getOpt("ö"));                      
    }
    
    private abstract class Argument {        
        protected abstract void store();
    }
            
    private class OptionWithValue extends Argument {
        String name;
        String value;
        private OptionWithValue(String pName, String pValue) {
            name = pName;
            value = pValue;
        }
        protected void store() {
            if (!DUMMY_VALUES.contains(value)) {                        
                mOptsWithVal.put(name, value);
            }
        }                            
    }
    
    private class OptionWithoutValue extends Argument {
        String name;
        private OptionWithoutValue(String pName) {
            name = pName;            
        }
        protected void store() {            
            mOptsWithoutVal.add(name);
        }              
    }
    
    private class NonOptionArgument extends Argument{
        String data;
        private NonOptionArgument(String pData) {
            data = pData;            
        }
        protected void store() {            
            mNonOptArgs.add(data);
        }               
    }
    
    
    private static void dbgMsg(String pMsg) {
        if (mLoggingEnabled) {
            Logger.dbg("ArgParser: "+pMsg);
        }
    }
        
}
