package util.collections;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ConversionUtils;
import util.DuplicateKeyException;

/**
 * A birectional mapping of object <=> index, based on indexing in an existing
 * array or list providing mapping index=>object, and an additional HashMap
 * providing mapping object=>index. The hashmap is provided by the IndexMap.
 * Note that naturally the IndexMap cannot cope with changes in the underlying
 * list, so the list provided as parameter SHOULD NOT BE MODIFIED after
 * passing it to the IndexMap. Keys can be appended using {@link #append()}.
 */
public class IndexMap<T> {
       
    private final List<T> list;
    private final Map<T, Integer> map;

    public IndexMap() {
        list = new ArrayList<T>();
        map = new HashMap<T, Integer>();
    }

    /**
     * Creates a copy of the list as an Integer array!
     * Note that the caller must declare the type of the intex map as integer!
     */
    public static IndexMap<Integer> makeIndexMap(int[] arr) {
        return new IndexMap<Integer>(ConversionUtils.toList(arr));
    }

    /** Copy constructor */
    public IndexMap(IndexMap<T> src) {
        list = new ArrayList<T>(src.list);
        map = new HashMap<T,Integer>(src.map);
    }
    
    public Map<T,Integer> getMap() {
        return map;
    }

    /**
     * Create an index map by storing an reference to existing array.
     * The array is not to be modified after this outside of the IndexMap.
     * @throws DuplicateKeyException if duplicate keys.
     */
    public IndexMap(List<T> list) {
        this.list = list;

        int n = list.size();
        map = new HashMap<T, Integer>(n);
        for (int i=0; i<n; i++) {
            T o = list.get(i);
            if (map.containsKey(o)) {
                throw new DuplicateKeyException("List contains a duplicate value: "+o);
            }
            else {
                map.put(o, i);
            }
        }
    }

    /** @throws RuntimeException if key already exists */
    public void append(T key) {
        if (map.containsKey(key)) {
            throw new RuntimeException("Already contains key: "+key);
        }
        int index = list.size();
        map.put(key, index);
        list.add(key);
    }

    public int size() {
        return list.size();
    }

    public T get(int index) {
        return list.get(index);
    }

    public List<T> asList() {
        return Collections.unmodifiableList(list);

    }

    public boolean contains(T value) {
        return map.containsKey(value);
    }


    /** Return null if no such column */
    public Integer getIndex(T obj) {
        return map.get(obj);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IndexMap)) {
            return false;
        }
        IndexMap<?> other = (IndexMap<?>)o;
        return list.equals(other.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }


}

