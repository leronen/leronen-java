package gui.form;


public class FormUtils {        
    
    /** Tired klluuddgge */
    public static void populateWithDefaultValues(FormData pData) {
        String[] keys = pData.getAllKeys();
        for (int i=0; i<keys.length; i++) {
            String key = keys[i];            
            Object val = pData.get(key);                        
            if (val == null) {
                pData.put(key, pData.getDefaultVal(key));    
            }
        }            
    }        
        
    
    
    
}


