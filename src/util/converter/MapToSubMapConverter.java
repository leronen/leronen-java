package util.converter;

import util.*;

import java.util.*;

/**
 * Extracts fields of intrest from a map. If map does not contain
 * */
public class MapToSubMapConverter<K,V> implements Converter<Map<K,V>, Map<K,V>> {

    private Set<K> mKeyset;
    private boolean mIncludeNulls;
    
    public MapToSubMapConverter(Set<K> pKeyset, boolean pIncludeNulls) {
        mKeyset = pKeyset;
        mIncludeNulls = pIncludeNulls;
    }
    
    public Map<K,V> convert(Map<K,V> p) {
        // Map<K,V> map = (Map)p;
        return CollectionUtils.subMap(p, mKeyset, mIncludeNulls);        
    }
    
}
