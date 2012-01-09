package util.collections;

import util.*;
import java.util.*;

import util.converter.*;

import java.io.*;


/** 
 * A Tree-table-based weighted set, with the additional 
 * constraint that the weights must be integers.
 * 
 * Todo: ?implement with integers for effeciency? 
 */ 
public final class TreeMultiSet<T> extends TreeWeightedSet<T> 
                                implements MultiSet<T>, Serializable {
                                    
                                        
    // @todo: own serialization system                                        
    static final long serialVersionUID = 3744236451466425L;
    
    
    public TreeMultiSet() {
        super();        
    }
    
    public TreeMultiSet(Collection<T> pCol) {
        super(pCol);        
    }        
    
    /** Wrap the super class method to prevent non-integer weights */
    public void add(T pObj, double pCount) {
        if (pCount % 1.d != 0.d) {
            throw new RuntimeException("Trying to add object with a non-integer weight into a Multi-set!");
        }
        super.add(pObj, pCount);                         
    }
    
    
    /** Unfortunately duplicated from HashMultiSet */
    public List<T> asMultiOccurenceList() {
    	ArrayList<T> result = new ArrayList<T>();
    	for (T obj: this) {    		
    		int w = ConversionUtils.asInt(getWeight(obj));
    		if (w<1) {
    			throw new RuntimeException("Unexpectedly w is < 1");
    		}
    		for (int i=0; i<w; i++) {
    			result.add(obj);
    		}    		
    	}
    	return result;
    }
    
    public WeightedSet convert(Converter pConverter) {
        TreeMultiSet result = new TreeMultiSet();        
        internalConvert(result, pConverter);
        return result;
    }    
    
    public int getCount(Object pOb) {
        return (int)getWeight(pOb);
    }            
        
    public WeightedSet<T> makeCompatibleWeightedSet() {
    	return new TreeMultiSet();
    }            
    
}
