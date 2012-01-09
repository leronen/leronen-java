package util.clone;

import java.util.*;

public class HashMapCloner<K,V> implements Cloner<HashMap<K,V>> {
               
    private Cloner<K> mKeyCloner;
    private Cloner<V> mValCloner;
    
    /** Create a shallow cloner (keys and values are copied by reference) */
    public HashMapCloner() {
        mKeyCloner = new ShallowCloner<K>();
        mValCloner = new ShallowCloner<V>();
    }
    
    public HashMapCloner(Cloner<K> pKeyCloner, Cloner<V> pValCloner) {
        mKeyCloner = pKeyCloner;
        mValCloner = pValCloner;
    }
    
    public HashMap<K,V> createClone(HashMap<K,V> pMap) {
        HashMap<K,V> mapClone = new HashMap<K,V>();
        
        Set<K> keySet = pMap.keySet();
        for (K key: keySet) {
            V val = pMap.get(key);
            K keyClone = mKeyCloner.createClone(key);
            V valClone = mValCloner.createClone(val);
            mapClone.put(keyClone, valClone);                            
        }
        
        return mapClone;
    }        
    
} 

