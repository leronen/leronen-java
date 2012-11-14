package util;

import java.util.*;

import util.collections.*;

public class ArgsDef {

    // core data
    private List<OptDef> mArgDefs;
    
    // derived data:
	private String mProgName;
    private Map<String, String> mShortNameToLongNameMap;
    /** here are long names only */
    private Set mNamesOfOptionsRequiringValue;
    private Set mOptionsWithoutShortName;    
    /** Set of valid option names (both short and long!) */
    private Set mValidOptionNames;
    private Map mDefaultValues;
    private MultiMap mPossibleValues;
    private List<String> mNonOptParamNames; 
        
    private void init(List<OptDef> pArgDefs, 
                      Collection<String> pNonOptParamNames, 
                      Collection<String> pNamesOfOptsToInclude) {
        // remember orginal defs (is this necessary) 
        mArgDefs = new ArrayList(pArgDefs);
        
        // init derived data:
        mShortNameToLongNameMap = new LinkedHashMap();        
        mNamesOfOptionsRequiringValue = new LinkedHashSet();
        mOptionsWithoutShortName = new LinkedHashSet();                
        mValidOptionNames = new LinkedHashSet();
        mDefaultValues = new LinkedHashMap();
        mPossibleValues = new MultiMap();
        mNonOptParamNames = new ArrayList(pNonOptParamNames);                
                
        // for (int i=0; i<pArgDefs.length; i++) {
        for (OptDef argDef: pArgDefs) {                        
            if (pNamesOfOptsToInclude != null && !(pNamesOfOptsToInclude.contains(argDef.longname))) {
                // forget about this arg
                continue;
            }
            else {
                addDef(argDef);
            }           
        }
    }
    
    /** Append defs from another arg def */
    public void addDefs(ArgsDef pDefs) { 
        for (OptDef def: pDefs.mArgDefs) {
            addDef(def);
        }
    }
    
    public void addDefs(OptDef... pDefs) {
        for (OptDef def: pDefs) {
            addDef(def);
        }
    }
    
    public void addDefs(Object[]... pDefs) {
        for (Object[] def: pDefs) {            
            addDef(def);
        }
    }
    
    public void addDef(Object[] pDef) {
        addDef(new OptDef(pDef));
    }
    
    public void addDef(OptDef pDef) {        
        
        mArgDefs.add(pDef);
        
        if (mShortNameToLongNameMap.containsKey(pDef.shortname)) {
            throw new RuntimeException("Duplicate short name: "+pDef.shortname);
        }
                                                                  
        mValidOptionNames.add(pDef.longname);
        
        if (pDef.shortname != null) {
            mValidOptionNames.add(pDef.shortname);
            mShortNameToLongNameMap.put(pDef.shortname, pDef.longname);                    
        }
        else {
            mOptionsWithoutShortName.add(pDef.longname);    
        }
        
        if (pDef.hasvalue) {
            mNamesOfOptionsRequiringValue.add(pDef.longname);
        }                   
        
        if (pDef.defaultvalue != null) {
            // a little sanity check, if you permit...
            if (!(pDef.hasvalue)) {
                throw new RuntimeException("Illegal situation: parameter "+
                                           pDef.longname+" has no value, "+
                                           "but still has default value "+pDef.defaultvalue);
            }
            mDefaultValues.put(pDef.longname, pDef.defaultvalue);
        }
        
        if (pDef.possiblevalues != null) {
            // for an "enum" type parameter
            mPossibleValues.putMultiple(pDef.longname, pDef.possiblevalues);                                        
        }
    
    }
    
    public ArgsDef() {
        // no data yet...
        init(Collections.EMPTY_LIST, Collections.EMPTY_LIST, null);
    }
    
    public ArgsDef(String... pNamesOfNonOptArgs) {
        // no option definitions yet
        init(Collections.EMPTY_LIST, CollectionUtils.makeList(pNamesOfNonOptArgs), null);
    }       
    
    /**
     * @param pNamesOfOptsToInclude to enable using only a subset of args
     * in a def table. 
     */
    public ArgsDef(List<OptDef> pArgDefs, 
                   String[] pNonOptParamNames, 
                   Collection<String> pNamesOfOptsToInclude) {
        init(pArgDefs, Arrays.asList(pNonOptParamNames), pNamesOfOptsToInclude);
    }
    
    public ArgsDef(List<OptDef> pArgDefs, 
                   String[] pNonOptParamNames) {                   
        this(pArgDefs, pNonOptParamNames, null);
    }
    
    public ArgsDef(Object[][] pArgDefs, 
                   String[] pNonOptParamNames) {                   
        this(pArgDefs, pNonOptParamNames, null);
    }
    
    /**
     * No positional parameters. 
     */
    public ArgsDef(Object[][] pArgDefs) {                   
        this(pArgDefs, new String[0], null);
    }
    
    /**
     * No positional parameters. 
     */
    public ArgsDef(List<OptDef> pArgDefs) {                   
        this(pArgDefs, new String[0], null);
    }
    
    /** Entries of pArgDefs are as follows: 
      [0] long name (String)
      [1] short name (String)
      [2] has value? (Boolean)
      [3] default value, if any (Object)
      [4] possible (enum) values if any (String[] or collection)
      
      Of these, 0-2 are mandarory.
     */
    public ArgsDef(Object[][] pArgDefs, String[] pNonOptParamNames, Collection<String> pNamesOfOptsToInclude) {
        ArrayList<OptDef> argDefs = new ArrayList();
        for (Object[] raw: pArgDefs) {
            OptDef def = new OptDef(raw);
            argDefs.add(def);           
        }
        
        if (pNonOptParamNames == null) {
            pNonOptParamNames = new String[0];
        }
        
        init(argDefs, Arrays.asList(pNonOptParamNames), pNamesOfOptsToInclude);
        
        
//        mShortNameToLongNameMap = new LinkedHashMap();        
//        mNamesOfOptionsRequiringValue = new LinkedHashSet();
//        mOptionsWithoutShortName = new LinkedHashSet();                
//        mValidOptionNames = new LinkedHashSet();
//        mDefaultValues = new LinkedHashMap();
//        mPossibleValues = new MultiMap();
//        mNonOptParamNames = pNonOptParamNames;                
//                
//        for (int i=0; i<pArgDefs.length; i++) {        	
//            Object[] argDef = pArgDefs[i];
//            String longname = (String)argDef[0];
//            if (pNamesOfOptsToInclude != null && !(pNamesOfOptsToInclude.contains(longname))) {
//            	continue;
//            }
//            String shortname = (String)argDef[1];
//            boolean hasValue = ((Boolean)argDef[2]).booleanValue();
//            if (mShortNameToLongNameMap.containsKey(shortname)) {
//            	throw new RuntimeException("Duplicate short name: "+shortname);
//            }
//            String defaultValue = (argDef.length >= 4 && argDef[3] != null) ? ""+argDef[3] : null;
//            
//            Collection<String> possibleValues = null;
//            if (argDef.length >= 5) {
//            	if (argDef[4] instanceof String[]) {            
//            		possibleValues = Arrays.asList((String[])argDef[4]);
//            	}
//            	else if (argDef[4] instanceof Collection) {
//            		possibleValues = (Collection)argDef[4];
//            	}
//            	else {
//            		throw new RuntimeException("Unrecognized enum def: "+argDef[4]);
//            	}
//            }            
//            
//            mValidOptionNames.add(longname);
//            if (shortname != null) {
//                mValidOptionNames.add(shortname);
//                mShortNameToLongNameMap.put(shortname, longname);                    
//            }
//            else {
//                mOptionsWithoutShortName.add(longname);    
//            }
//            if (hasValue) {
//                mNamesOfOptionsRequiringValue.add(longname);
//            }                          
//            if (defaultValue != null) {
//                // a little sanity check, if you permit...
//                if (!hasValue) {
//                    throw new RuntimeException("Illegal situation: parameter "+longname+" has no value, but still has default value "+defaultValue);
//                }
//                mDefaultValues.put(longname, defaultValue);
//            }
//            if (possibleValues != null) {
//                // for an "enum" type parameter
//                mPossibleValues.putMultiple(longname, possibleValues);                                        
//            }
//        }                                              
    }
    
    public Set getOptions(String pKey) {
        return mPossibleValues.get(pKey);                     
    }
    
    public boolean isEnum(String pKey) {
        return mPossibleValues.containsKey(pKey);    
    }
    
    public List<String> getNonOptParamNames() {
        return mNonOptParamNames;   
    }        
    
    public int getIndOfNonOptArg(String pParamName) {
//        Logger.info("getIndOfNonOptArg("+pParamName+")");
//        Logger.info("nonOptParamNames:("+mNonOptParamNames+")");
    	if (mNonOptParamNames == null) {
    		throw new RuntimeException("No param names defined!");
    	}
    	else {
            int result = mNonOptParamNames.indexOf(pParamName); 
//    		Logger.info("Returning: "+result);
            return result;
    	}
    }
    
    public String getDefaultValue(String pOptName) {
        return (String)mDefaultValues.get(pOptName);    
    }
    
    public Map<String, String> getShortNameToLongNameMap() {
        return mShortNameToLongNameMap;
    }
    
    public String getShortName(String pLongName) {
    	return CollectionUtils.inverseMap(mShortNameToLongNameMap).get(pLongName);
    }
    
    public Set getNamesOfOptionsRequiringValue() {
        return mNamesOfOptionsRequiringValue;
    }
    /** Note: of course long names only */
    public Set getNamesOfOptionsNotRequiringValue() {
        return CollectionUtils.minus(getLongOptionNames(), mNamesOfOptionsRequiringValue);
    }
    
    /** Note: of course long names only */
    public Set getOptionsWithoutShortName() {
        return mOptionsWithoutShortName;
    }
    
    /** Note: of course long names only */
    public Set getOptionsWithShortName() {
        return CollectionUtils.minus(getLongOptionNames(), mOptionsWithoutShortName);
    }
    
    /** Get both short and long names */
    public Set getValidOptionNames() {
        return mValidOptionNames;
    }      
    
    public Set getShortOptionNames() {
        return mShortNameToLongNameMap.keySet();
    }
    
    public Set getLongOptionNames() {
        return CollectionUtils.minus(mValidOptionNames, getShortOptionNames());   
    }    
    
    /** Note: Gets of course long names only */
    public Set getNamesOfOptionsWithDefaultValue() {
        return mDefaultValues.keySet();    
    }
    
    /** 
     * Hmm, bit ugly to do this afterwards, but currently we must handle "dependent" 
     * default values this way, by hand.
     */      
    public void putDefaultValue(String pKey, String pVal) {
        mDefaultValues.put(pKey, pVal);    
    }
    
    public Map getDefaultValues(Set pKeySet) {
        return CollectionUtils.subMap(mDefaultValues, pKeySet, true);    
    }
    
    /*
    public void outputValidOptions() {
        System.err.println("Valid options:");
        // System.err.println("Valid options:\n"+StringUtils.collectionToString(mValidOptionNames));
        Map longnametoshortnamemap = CollectionUtils.inverseMap(mShortNameToLongNameMap);
        // kludge: append possible vals to short names for user-friendliness...
        Iterator longnames = longnametoshortnamemap.keySet().iterator();
        while(longnames.hasNext()) {
            String longname = (String)longnames.next();
            String shortname = (String)longnametoshortnamemap.get(longname);
            String possibleValsString = ""; 
            if (mPossibleValues.keySet().contains(longname)) {                
                possibleValsString = " "+mPossibleValues.get(longname);
            }
            System.err.println("  -"+longname+" (-"+shortname+")"+possibleValsString);            
        }
        // System.err.println("Valid options:\n"+StringUtils.mapToString(longnametoshortnamemap, " (", ")\n")+")");
        System.err.println(StringUtils.collectionToString(mOptionsWithoutShortName));
    }
    */
    
    public String toString() {
        return usage();
    }
    
    public void outputValidOptions() {
        System.err.println(optionsString());        
    }
    
    public String formatOpts(Collection pLongNames) {                 
        StringBuffer result = new StringBuffer(); 
        Map longnametoshortnamemap = CollectionUtils.inverseMap(mShortNameToLongNameMap);                
        Iterator longnames = pLongNames.iterator();        
        while(longnames.hasNext()) {
            String longname = (String)longnames.next();
            result.append("  -"+longname);
            String shortname = (String)longnametoshortnamemap.get(longname);
            if (shortname != null) {
                result.append(" (-"+shortname+")");
            }
            String possibleValsString = ""; 
            if (mPossibleValues.keySet().contains(longname)) {                
                possibleValsString = " "+mPossibleValues.get(longname);
            }            
            result.append(possibleValsString);
            String defaultVal = (String)mDefaultValues.get(longname);
            if (defaultVal != null) {
                result.append(", default="+defaultVal);    
            }
            if (longnames.hasNext()) {
                result.append("\n");
            }                        
        }
        return result.toString();                        
    }
    
    public void setProgName(String pProgName) {
    	mProgName = pProgName;
    }
    
    public String getProgName() {
        return mProgName;
    }
    
    public String usage() {
    	String progName = mProgName != null ? mProgName : "progname";
    	String optsString1 = (mValidOptionNames != null && mValidOptionNames.size() > 0) ? " <options>" : "";
    	String nonOptArgsString = (mNonOptParamNames != null && mNonOptParamNames.size() > 0) ? " <"+StringUtils.listToString(mNonOptParamNames, "> <") + ">": "";
    	String optsString2 = (mValidOptionNames != null && mValidOptionNames.size() > 0) ?
						    "\nOPTIONS:\n"+
						    optsWithValString()+"\n"+
						    optsWithoutValString() :
						    "";
    	return "Usage: "+progName + optsString1 + nonOptArgsString + optsString2;
		
    }
                    
    public String optsWithValString() {        
        // return "options requiring value:\n"+formatOpts(getNamesOfOptionsRequiringValue());
        return formatOpts(getNamesOfOptionsRequiringValue());        
    }
    
    public String optsWithoutValString() {
        // return "options not requiring value:\n"+formatOpts(getNamesOfOptionsNotRequiringValue());
        return formatOpts(getNamesOfOptionsNotRequiringValue());        
    }
    
    public String optionsString() {
        StringBuffer buf = new StringBuffer("Options:\n");
        buf.append(optsWithValString());
        buf.append("\n");
        buf.append(optsWithoutValString());                        
        return buf.toString();        
    }        
}
