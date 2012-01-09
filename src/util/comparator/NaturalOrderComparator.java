package util.comparator;

import java.util.*;
 
public class NaturalOrderComparator implements Comparator {
    
    public int compare(Object pObj1, Object pObj2) {        
        return ((Comparable)pObj1).compareTo(pObj2);                
    }                       
    
}
