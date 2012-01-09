package util.collections;

import java.util.*;

/**
 * A hash map that allows aggregate keys.
 *
 * Currently supports only 2- and 3- tuples as keys. (and, of course, "1-tuples", as this, extends a normal hashmap); 
 *
 * todo: is this an good idea to extend hashmap, instead of delegation?
*/
public class MultiKeyHashMap extends HashMap {
    
    private HashMap mData;
    
    public MultiKeyHashMap() {
        mData = new HashMap();        
    }
    
    ///////////////////////////////////////////////////
    // 2-key methods
    ///////////////////////////////////////////////////
    public Object get(Object pKey1, Object pKey2) {
        return mData.get(makeKey(pKey1, pKey2));
    }
            
    public void put(Object pKey1, Object pKey2, Object pVal) {
        mData.put(makeKey(pKey1, pKey2), pVal);               
    }
    
    public void remove(Object pKey1, Object pKey2) {
        mData.remove(makeKey(pKey1, pKey2));
    }
    
    ///////////////////////////////////////////////////
    // 3-key methods
    ///////////////////////////////////////////////////
    public Object get(Object pKey1, Object pKey2, Object pKey3) {
        return mData.get(makeKey(pKey1, pKey2, pKey3));       
    }
    
    public void put(Object pKey1, Object pKey2, Object pKey3, Object pVal) {
        mData.put(makeKey(pKey1, pKey2, pKey3), pVal);               
    }
    
    public void remove(Object pKey1, Object pKey2, Object pKey3) {
        mData.remove(makeKey(pKey1, pKey2, pKey3));
    }
    
    ///////////////////////////////////////////////////
    // arraylists are used as keys
    ///////////////////////////////////////////////////
    private ArrayList makeKey(Object pKey1, Object pKey2) {
        ArrayList key = new ArrayList(2);
        key.add(pKey1);
        key.add(pKey2);
        return key;
    }
    
    private ArrayList makeKey(Object pKey1, Object pKey2, Object pKey3) {
        ArrayList key = new ArrayList(2);
        key.add(pKey1);
        key.add(pKey2);
        key.add(pKey3);
        return key;
    }
            

}


