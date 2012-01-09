package util.collections;

import java.util.*;

public interface IMultiMapReadOps<K,V> {
                                  
    public boolean containsKey(K pKey);
    public Set<V> get(K pKey);                       
    public V getSingleton(K pKey);
    public Set<K> keySet();
    public List<K> getMultiValKeys();
    /** 
     * Return an UNMODIFIABLE list of all values in the multimap;
     */
    public List<V> values();
            
}
