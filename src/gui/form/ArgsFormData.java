package gui.form;

import util.*;

import java.util.*;

/** 
 */
public class ArgsFormData extends LinkedHashMapFormData implements IArgs {
                                                                                                                                                                                                                        
    private String mName;
    private ArgsDef mArgsDef;                                                
                        
    @SuppressWarnings("unused")
    private ArgsFormData() {
        throw new RuntimeException("Soh, soh!");            
    }        
    
    public ArgsFormData(String pName, ArgsDef pArgsDef) {
        mName = pName;                       
        mArgsDef = pArgsDef;                    
            
        // setDefaultVals();        
    }
                                
    public Object get(String pKey) {
        Object result = super.get(pKey);
        if (result == null || result.equals("") || result.equals("-")) {
            result = getDefaultVal((String)pKey);
        }
        return result;
    }        
                       
    public Object getDefaultVal(String pKey) {
        
       // try to get default value from test def (may return null, when not defined!)
       Object defaultVal = mArgsDef.getDefaultValue(pKey);
       if (defaultVal == null) {
           // OK, let's get the default value from the super class LinkedHashMapFormData
           defaultVal = super.getDefaultVal(pKey);
       }
       return defaultVal;
    }
                   
    public String getFormName() {
        return mName;
    }
            
    public String getType(String pKey) {
        if (mArgsDef.isEnum(pKey)) {
            return FormData.TYPE_ENUM;     
        }
        else {
            return FormData.TYPE_STRING;
        }                 
    }
    
    public Object[] getOptions(String pKey) {         
        return ConversionUtils.collectionToArray(mArgsDef.getOptions(pKey));
    }
        
    
    public String[] getAllKeys() {        
        return ConversionUtils.stringCollectionToArray(mArgsDef.getLongOptionNames());   
    }                      
                                                                                                                        
    public String getTooltip(String pKey) {
        return "TODO: implement!";
        // return mArgsDef.outputValidOptions
    }
    
    //////////////////////////////////////
    // interface IArgs........
    //////////////////////////////////////
    public String[] getDefinedOptions() {
        ArrayList definedOptions = new ArrayList(); 
        
        String[] allKeys = getAllKeys();
        for (int i=0; i<allKeys.length; i++) {
            String key = allKeys[i];
            if (isDefined(key)) {
                definedOptions.add(key);
            }                       
        }      
                   
        return ConversionUtils.stringCollectionToArray(definedOptions); 
    }
    
    public String[] getNonOptArgs() {
        throw new RuntimeException("Uhh!");   
    }
    
    public String getOpt(String pName) {
        String val = (String)get(pName);
        if (val.equals("")) {
            // OK, let's interpret this as "option not given"
            val = null;
        }
        return val;
    }
                                                            
    public boolean isDefined(String pName) {        
        Object val = getOpt(pName);
        return val != null && val != "";                
    }
        
    public ArgsDef getDef() {
        return mArgsDef;        
    }
    //////////////////////////////////////

}


