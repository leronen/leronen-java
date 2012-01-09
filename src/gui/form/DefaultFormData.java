package gui.form;

import util.*;

import java.util.*;

public class DefaultFormData extends LinkedHashMapFormData  {
    
    private String mFormName;
    private Map mParamDefMap;

    // these constructors are a mess
    
    public DefaultFormData(String pFormName, Map pData, Map pParamTypes) {                        
        mFormName = pFormName;        
        mMap.putAll(pData);
        mParamDefMap = pParamTypes; 
    }
    
    public DefaultFormData(String pFormName, String[] pKeys, Map pParamTypes) {                        
        mFormName = pFormName;        
        for (int i=0; i<pKeys.length; i++) {
            put(pKeys[i], "");
        }
        mParamDefMap = pParamTypes; 
    }
                                        
    
    public DefaultFormData(Map pData, Map pParamTypes) {
        this(null, pData, pParamTypes);         
    }
                     
    public DefaultFormData(String pFormName, String[] pKeys) {
        this(pFormName, pKeys, null);        
    }
    
    public DefaultFormData(String pFormName, Map pData) {
        this(pFormName, pData, null);
    }          
        
    public DefaultFormData(String[] pKeys) {
        this(null, pKeys, null);                                
    }
    
    public DefaultFormData(Map pData) {
        this(null, pData, null);        
    }          
    
    public String getFormName() {
        return mFormName;
    }   

    public String[] getAllKeys() {
        return ConversionUtils.stringCollectionToArray(mMap.keySet());
    }          

    public String getType(String pKey) {
        if (mParamDefMap != null) {
            return (String)mParamDefMap.get(pKey);
        }
        else {
            return super.getType(pKey);
        }
    }           
                                 
    
//    private static void dbgMsg(String pMsg) {
//        Logger.dbg("DefaultFormData: "+pMsg);
//    }
    
    

}
