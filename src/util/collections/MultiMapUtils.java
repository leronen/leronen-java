package util.collections;

import java.util.*;

import util.StringUtils;

public class MultiMapUtils {
        
    public static <T1,T2> MultiMap<T2,T1> inverse(MultiMap<T1,T2> pMultiMap) {
        MultiMap<T2,T1> result = (MultiMap<T2,T1>)(MultiMap)pMultiMap.createEmptyClone();
        for (T1 key: pMultiMap.keySet()) {
            Set<T2> vals = pMultiMap.get(key);
            for (T2 val: vals) {
                result.put(val, key);
            }
        }
        return result;
    }
        
    
    /** Note that keySet does not tolerate concurrent modifications! */
    public static <K,V> IMultiMapReadOps<K,V> asReadOnlyMultiMap(Map<K,V> pMap) {
        return new MultiMapWrapper(pMap);
    }
    
    /** Presents an ordinary map as a multi map */
    private static class MultiMapWrapper<K,V> implements IMultiMapReadOps<K,V> {
        private Map<K,V> mMap;
        
        public MultiMapWrapper(Map<K, V> pMap) {            
            mMap = pMap;
        }
        
        public boolean containsKey(K pKey) {
            return mMap.containsKey(pKey);
        }
        
        public Set<V> get(K pKey) {
            if (containsKey(pKey)) {
                return Collections.singleton(mMap.get(pKey));
            }
            else {
                return Collections.EMPTY_SET;
            }
        }
        
        public V getSingleton(K pKey) {
            return mMap.get(pKey);
        }
        
        public Set<K> keySet() {
            return mMap.keySet();
        }
        
        public List<K> getMultiValKeys() {
            return Collections.EMPTY_LIST;
        }
        
        public String toString() {
            return StringUtils.multiMapToString(this);
        }
        
        public List<V> values() {
            return Collections.unmodifiableList(new ArrayList(mMap.values()));            
        }
        
               
    }
}
