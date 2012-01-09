package util.comparator;

import java.util.*;

 
/** 
 * Compares first by list.get(0), then by list.get(1), etc...
 * 
 * If one list is shorter than other, and the common prefix is equal,
 * then define that the shorter list is the smaller one.
 */ 
public class ListComparator<T extends Comparable<T>> implements Comparator<List<T>> {
    
    public int compare(List<T> list1, List<T> list2) {                
        int numElems = Math.min(list1.size(), list2.size()); 
        
        for (int i=0; i<numElems; i++) {
            T o1 = list1.get(i);
            T o2 = list2.get(i);
            int cmp = o1.compareTo(o2);
            if (cmp != 0) {
                // OK, we have an ordering
                return cmp;
            }
            // else let's proceed to next element in both lists
        }
        
        // elements at all common indices were equal =>  resort to size comparison
        // (of course, if sizes are equal, all elements were equal, and we end
        // up returning 0, as is to be expected from a sensible implementation...
        return  list1.size() - list2.size();                                                                    
    }                           
}
