package util.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class LRUTest {
    
    private static final int LRU_CACHE_SIZE = 3;
    
    Map<String, String> cache;
    
    public static void main(String[] args) {             
        new LRUTest().run();
    }
    
    private void run() {
        
         cache = new LinkedHashMap(10, 0.75f, true) {                
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > LRU_CACHE_SIZE; 
            }
        };
                
        cache.put("A", "A");
        cache.put("B", "B");
        cache.put("C", "C");
        cache.put("D", "D");
        cache.put("E", "E");
        cache.put("F", "F");
        System.out.println("After 1:st set of operations:");
        printCache();
        cache.put("A", "A");        
        cache.put("B", "bar");
        cache.put("C", "baz");
        System.out.println("After 2:nd set of operations:");
        printCache();
        cache.get("A");
        cache.put("X","X");
        System.out.println("After 2:nd set of operations:");
        printCache();
//        cache.containsKey("B");
//        System.out.println("After 3:rd set of operations:");
//        printCache();
                        
                
        
    }
        
    private void printCache() {
        // note we cant iterate the map keys directly, as this will cause 
        // a concurrent modification exception!
        Iterator<String> keys = new ArrayList(cache.keySet()).iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            System.out.print(key);
//            Object val = cache.get(key);
//            System.out.print("=");
//            System.out.print(val);            
            System.out.println();            
        }
    }
    
}
