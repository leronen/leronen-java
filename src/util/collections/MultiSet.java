package util.collections;

import java.util.List;

/** 
 * As weighted set, with the added constraint that the weights must be integers(this constraint, of course, cannot be 
 * be enforced by this interface, but is left to the responsibility of the developer instead.
 */ 
public interface MultiSet<T> extends WeightedSet<T> {
    
    public int getCount(Object pObj);
    
    public List<T> asMultiOccurenceList();

} 
