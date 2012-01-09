package gui.form;
import java.util.*;


public abstract class LinkedHashMapFormData extends AbstractFormData implements FormData {
        
    
    protected LinkedHashMap<String, Object> mMap = new LinkedHashMap();
            
    public Object get(String pKey) {        
        return mMap.get(pKey);
    }

    public Object put(String pKey, Object pVal) {
        String key = pKey.intern();
        Object val = pVal instanceof String ? ((String)pVal).intern() : pVal;
        return mMap.put(key, val);
    }        
        
    public void clear() {
        mMap.clear();
    }        
        
                                                                                          
    
    public Map asMap() {
        return mMap;
    }
            
}
