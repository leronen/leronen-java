package util.collections;

import util.factory.*;
import java.util.*;
import java.io.*;

/** 
 * A Tree table-based implementation of the WeightedSet interface. 
 *
 * The current implementation is based on a TreeMap, so it implictly 
 * uses TreeCode and equals for managing objects.
 *  
 */ 
public class TreeWeightedSet<T> extends CustomizableWeightedSet<T> 
                             implements WeightedSet<T>, Serializable {                    
        
    
	public TreeWeightedSet() {
    	super(new TreeMapFactory(), new TreeSetFactory());
    }
    
    public TreeWeightedSet(Collection<T> pCol) {
    	super(new TreeMapFactory(), new TreeSetFactory(), pCol);
    	
    }    

	public TreeWeightedSet(WeightedSet<T> pWeightedSet) {
		super(new TreeMapFactory(), new TreeSetFactory(), pWeightedSet);
	}
    
}
