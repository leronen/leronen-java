package util.converter;

import java.util.Map;

import util.collections.Pair;

public class MapEntryToPairConverter <K,V> implements Converter<Map.Entry<K, V>, Pair<K, V>> {
    public Pair<K,V> convert(Map.Entry<K, V> p) {
        return new Pair(p.getKey(), p.getValue());
    }

}
