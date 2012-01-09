package util.comparator;

import java.util.*;

 
/** 
 * Compares collections by size. 
 */ 
public class BySizeCollectionComparator implements Comparator {
    
    boolean smallestFirst;
    
    public BySizeCollectionComparator() {
        smallestFirst = true;
    }
    
    public BySizeCollectionComparator(boolean smallestFirst) {
        this.smallestFirst = smallestFirst;
    }
    
    public int compare(Object p1, Object p2) {
        Collection list1 = (Collection)p1;
        Collection list2 = (Collection)p2;
        
        if (smallestFirst) {
            return list1.size() - list2.size();
        }
        else {
            return list2.size() - list1.size();
        }
                                                                                   
    }                           
}
