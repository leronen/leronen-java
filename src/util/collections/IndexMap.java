package util.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import util.ConversionUtils;

/**
 * A birectional mapping of object <=> index, based on indexing in an existing 
 * array or list providing mapping index=>object, and an additional HashMap 
 * providing mapping object=>index. The hashmap is provided by the IndexMap.
 * Note that naturally the IndexMap cannot cope with changes in the underlying
 * list, so the list provided as parameter SHOULD NOT BE MODIFIED after
 * passing it to the IndexMap. Keys can be appended using {@link #append()}.
 */
public class IndexMap<T> {
    
	private List<T> list;
	private Map<T, Integer> map;  
           
    public static IndexMap<Integer> makeIndexMap(int[] arr) {
        return new IndexMap<Integer>(ConversionUtils.asList(arr));
    }
    
    /** Copy constructor */
    public IndexMap(IndexMap<T> src) {    	        
        list = new ArrayList<T>(src.list);
        map = new HashMap<T,Integer>(src.map);
    }
    
    public IndexMap(List<T> list) {
        this.list = list;

        int n = list.size();
        map = new HashMap<T, Integer>(n);        
        for (int i=0; i<n; i++) {
            T o = list.get(i);
            if (map.containsKey(o)) {
                throw new RuntimeException("List contains a duplicate value: "+o);
            }
            else {
                map.put(o, i);
            }
        }                 
    }
    
    public List<T> asList() {
    	return Collections.unmodifiableList(list);    		
    }
    
    public boolean contains(T value) {
    	return map.containsKey(value);
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
    
    public int getIndex(T obj) {
        return map.get(obj);
    }
    
    
}
