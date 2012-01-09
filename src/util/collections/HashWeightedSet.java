package util.collections;

import util.factory.*;

import java.util.*;
import java.io.*;

/** 
 * A hash table-based implementation of the WeightedSet interface. 
 *
 * The current implementation is based on a HashMap, so it implictly 
 * uses hashCode and equals for managing objects.
 *  
 */ 
public class HashWeightedSet<T> extends CustomizableWeightedSet<T> 
                             implements WeightedSet<T>, Serializable {        
    
	public HashWeightedSet() {
    	super(new HashMapFactory(), new HashSetFactory());
    }
    
	public HashWeightedSet(int initialCapacity) {
        super(new HashMapFactory(initialCapacity), new HashSetFactory());
    }
	
    public HashWeightedSet(Collection<T> pCol) {
    	super(new HashMapFactory(), new HashSetFactory(), pCol);
    	
    }    

	public HashWeightedSet(WeightedSet<T> pWeightedSet) {
		super(new HashMapFactory(), new HashSetFactory(), pWeightedSet);
	}
    
}
