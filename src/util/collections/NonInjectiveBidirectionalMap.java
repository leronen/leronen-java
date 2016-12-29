package util.collections;

import util.*;
import util.converter.Converter;

import java.util.*;

/**
 * 
 * An many-to-one mapping, in other words a bidir mapping, where the "forward" 
 * mapping maps each src element to a single tgt element, but is not required 
 * to be an injection, that is, the backward mapping may map a tgt 
 * element to several src elements. 
 * 
 * The inverse mapping may be (and indeed is) implemented as a multimap.  
 *
 * search terms: two-way map, bipartite graph, many-to-one, manytone
 *
 * TODO: permit using this with different kind of Set and Map Implementations.
 * 
 * @see DirectedGraph
 * @see DefaultGraph
 * @see IGraph
 * @see MultiMap
 * @see OneToOneBidirectionalMap
 *  
 */
public class NonInjectiveBidirectionalMap<T1,T2> implements Iterable<Map.Entry<T1,T2>> {
    private LinkedHashMap<T1,T2> mMap;
    private MultiMap<T2,T1> mInverseMultiMap;
        
    public NonInjectiveBidirectionalMap() {
        mMap = new LinkedHashMap<T1,T2>();
        mInverseMultiMap = new MultiMap<T2,T1>();
    }
    
    public void put(T1 p1, T2 p2) {
    	if (mMap.containsKey(p1)) {
    		throw new RuntimeException("Re-putting existing key1; this is an " +
    				 				   "unfortunately unexpected situation for "+
    				 				   "which the current code base has no "+
    				 				   "sane response pattern");    		
    	}
    	mMap.put(p1, p2);
    	mInverseMultiMap.put(p2, p1);
    }
    
    public int size() {
    	return mMap.size();
    }

    public T2 get(T1 p) {
        return mMap.get(p);
    }
    
    public Set<T1> getInverse(T2 p) {
    	return mInverseMultiMap.get(p);
    }
    
    public Set<T1> getSrcSet() {
    	return mMap.keySet();    	
    }
    
    public Iterator<Map.Entry<T1,T2>> iterator() {
    	return mMap.entrySet().iterator();
    }
    
    public void updateTgt(T2 pOldVal2, T2 pNewVal2) {
    	Set<T1> vals1 = mInverseMultiMap.get(pOldVal2);
    	// update direct map
    	for (T1 val1: vals1) {
    		mMap.put(val1, pNewVal2);
    	}
    	// update inverse map
    	mInverseMultiMap.removeKey(pOldVal2);
    	mInverseMultiMap.putMultiple(pNewVal2, vals1);    	
    }
    
    public boolean containsSrc(T1 p) {
    	return mMap.containsKey(p);
    }
        
    public boolean containsTgt(T2 p) {
        return mInverseMultiMap.containsKey(p);
    }
    
    public static void main (String[] args) {
    	NonInjectiveBidirectionalMap map = new NonInjectiveBidirectionalMap();
    	map.put(4, 1);
    	map.put(3, 1);
    	map.put(2, 1);
    	map.put(5, 2);
    	map.put(6, 2);    
    	System.err.println("Initial situation:\n"+map);    	
    	
    	map.updateTgt(2, 3);
    	System.err.println("After first update:\n"+map);
    	
    	map.updateTgt(3, 1);
    	System.err.println("After second update:\n"+map);
    	
    	
//        MultiMap map = new MultiMap();
//        map.put("A", "neljasA");
//        map.put("A", "ekaA");
//        map.put("A", "tokeA");
//        map.put("A", "kolmasA");        
//        map.put("B", "ekaB");
//        map.put("B", "tokaB");
//        map.put("C", "ainutC");
//        System.err.println("Ennen karsintaa:\n"+map);
//        map.prune();        
//        System.err.println("Karsinnan jälkeen:\n"+map);
//
//        MultiMap clone = map.createClone(new StringCloner(), new StringCloner());
//        System.err.println("Klooni:\n"+clone);                    
    }
    
    public String toString() {
    	return "direct map:\n"+StringUtils.mapToString(mMap)+"\n"+
    	       "inverse map:\n"+StringUtils.multiMapToString(mInverseMultiMap);
    }
    
    public String toString(Converter<T1,String> formatter1,
                           Converter<T2,String> formatter2) {
        return "direct map:\n"+StringUtils.format(mMap, "=", "\n", formatter1, formatter2)+"\n"+
               "inverse map:\n"+StringUtils.multiMapToString(mInverseMultiMap, formatter2, formatter1);
    }
    
    
}
