package util.collections;

import util.*;


import java.util.*;

import util.converter.*;

import java.io.*;



/** 
 * A hash-table-based weighted set, with the additional 
 * constraint that the weights must be integers.
 * 
 * TODO: ?implement with integers for effeciency?
 * 
 */ 
public class HashMultiSet<T> extends HashWeightedSet<T> 
                                implements MultiSet<T>, Serializable {
                                                                           
    // @todo: own serialization system                                        
    static final long serialVersionUID = 3744236451466838295L;

    /**
     * Remember to call setAllowZeros() if needed; by default, 
     * objects with zero weight are "ignored".
     *
     */
    public HashMultiSet() {
        super();        
    }
        
    public HashMultiSet(Collection<T> pCol) {
        super(pCol);        
    }        
    
    /** Wrap the super class method to prevent non-integer weights */
    public void add(T pObj, double pCount) {
        if (pCount % 1.d != 0.d) {
            throw new RuntimeException("Trying to add object with a non-integer weight into a Multi-set!");           
        }
        super.add(pObj, pCount);                         
    }

    public WeightedSet<T> makeCompatibleWeightedSet() {
    	return new HashMultiSet();
    }            	       
    
    public HashWeightedSet<T> normalizedVersion(double pDenominator) {
        HashWeightedSet<T> result = new HashWeightedSet();
        
        for (T o: this) {            
            double normalizedWeight = ((double)getWeight(o)) / pDenominator;
            result.add(o, normalizedWeight);          
        }
        
        return result;        
    }
    
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
        HashMultiSet result = new HashMultiSet();        
        internalConvert(result, pConverter);
        return result;
    }    
    
    public int getCount(Object pOb) {
        return (int)getWeight(pOb);
    }            
    
}
