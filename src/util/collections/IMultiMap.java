package util.collections;

import util.clone.*;
import java.util.*;

public interface IMultiMap<K,V> extends IMultiMapReadOps<K,V> {
             
    public Map toMap();                      
    public void put(K pKey, V pVal);            
    public void putAll(Map<K,V> pMap);              
    public void putAll(MultiMap<K,V> pMultiMap);                            
    public void putMultiple(K pKey, Collection<V> pVals);                        
    // public boolean containsKey(K pKey);                    
    // public Set<V> get(K pKey);                   
    public void updateKey(K pOldKey, K pNewKey);
    public V getSingleton(K pKey);
    public Set<K> keySet();            
    public List<K> getMultiValKeys();            
    /** Sorts each invidual contained set according to pComparator */
    public void sortKeys(Comparator pComparator);                        
    /** Sorts each invidual contained set according to pComparator */
    public void sortSets(Comparator pComparator);               
    /** Prunes the set map such that only one entry (hopefully the one that was inserted first!) per key remains */
    public void prune();                
    public void clear(K pKey);                
    public void removeKey(K pKey);                   
    public void remove(K pKey, V pVal);                                                     
    public Collection<Set<V>> getValuesAsCollectionOfSets();                               
    // public List<V> values();                
    /** Creates a deep clone */
    public MultiMap<K,V> createClone(Cloner<K> pKeyCloner, Cloner<V> pValCloner);                
    /** Creates a shallow clone where keys and attributes are copied by reference*/
    public MultiMap<K,V> createClone();
        
                                     
    
}
