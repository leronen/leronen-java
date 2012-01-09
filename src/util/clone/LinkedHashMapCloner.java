package util.clone;

import java.util.*;

public class LinkedHashMapCloner<K,V> implements Cloner<LinkedHashMap<K,V>> {
               
    private Cloner<K> mKeyCloner;
    private Cloner<V> mValCloner;
     
    public LinkedHashMapCloner(Cloner<K> pKeyCloner, Cloner<V> pValCloner) {
        mKeyCloner = pKeyCloner;
        mValCloner = pValCloner;
    }
         public LinkedHashMap<K,V> createClone(LinkedHashMap<K,V> pMap) {
        LinkedHashMap<K,V> mapClone = new LinkedHashMap<K,V>();
        
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

