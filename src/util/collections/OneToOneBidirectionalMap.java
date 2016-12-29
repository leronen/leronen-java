package util.collections;

import util.*;
import util.collections.iterator.ConverterIterator;
import util.converter.MapEntryToPairConverter;

import java.util.*;

/**
 * 
 * Maintains an one-to-one mapping between two (potentially overlapping)
 * sets.
 *
 * Search terms: two-way map, two-way mapping, bidir map, bidirectional map, 
 * bi-directional map.
 *
 * Currently implemented with 2 hashmaps, although a more efficient 
 * implementation would perhaps be possible. 
 * 
 * TODO: permit using this with different kind of Set and Map Implementations?
 * 
 * TODO: what to do about null values? They are not at least officially supported!
 * Actually, we should have an invariant forbidding nulls totally.
 * 
 * keywords: bipartite graph, two-way map
 * 
 * @see DirectedGraph
 * @see DefaultGraph
 * @see IGraph
 * @see MultiMap
 * @see NonInjectiveBidirectionalMap
 * 
 *  
 */
public class OneToOneBidirectionalMap<T1,T2> implements Iterable<Pair<T1,T2>> {
    private Map<T1,T2> mMap;
    private Map<T2,T1> mInverseMap;
        
    public OneToOneBidirectionalMap() {
        mMap = new HashMap();
        mInverseMap = new HashMap();
    }
    
    public OneToOneBidirectionalMap(int pInitialCapacity) {
        mMap = new HashMap(pInitialCapacity);
        mInverseMap = new HashMap(pInitialCapacity);
    }
    
    public OneToOneBidirectionalMap(Map<T1,T2> pDirectMap) {
        mMap = pDirectMap;
        mInverseMap = new HashMap(mMap.size());
        for (T1 src: pDirectMap.keySet()) {
            T2 tgt = pDirectMap.get(src);
            mInverseMap.put(tgt,src);
        }
    }
    
    /**
     * Note that neither pSrc of pTgt are allowed to pre-exist: 
     * thus it is not possible to override existing mappings by calling this;
     * instead, any mappings to be overridden must be explicitly removed
     * before adding the new mapping.
     */
    public void put(T1 pSrc, T2 pTgt) {
    	if (mMap.containsKey(pSrc)) {
    		throw new RuntimeException("Re-putting existing src entry: "+pSrc);     		
    	}
        if (mInverseMap.containsKey(pTgt)) {
            throw new RuntimeException("Re-putting existing tgt entry: "+pTgt);           
        }
    	mMap.put(pSrc, pTgt);
    	mInverseMap.put(pTgt, pSrc);
    }
    
    public boolean containsSrcKey(T1 pKey) {
        return mMap.containsKey(pKey);
    }
    
    public boolean containsTgtKey(T2 pKey) {
        return mInverseMap.containsKey(pKey);
    }
    
    /** return the direct map as UNMODIFIABLE */     
    public Map<T1,T2> getDirectMap() {
        return Collections.unmodifiableMap(mMap);
    }
    
    /** return the reverse map as UNMODIFIABLE */     
    public Map<T2,T1> getReverseMap() {
        return Collections.unmodifiableMap(mInverseMap);
    }
    
    public int size() {
    	return mMap.size();
    }
    
    public T2 get(T1 p) {
        return mMap.get(p);
    }
    
    public T1 getInverse(T2 p) {
    	return mInverseMap.get(p);
    }
    
    public void removeSrc(T1 o1) {
        T2 o2 = mMap.get(o1);
        if (o2 == null) {           
            throw new NoSuchElementException(""+o1);
        }
        else {
            mMap.remove(o1);
            mInverseMap.remove(o2);
        }
    }
    
    public void removeTgt(T2 tgt) {
        T1 src = mInverseMap.get(tgt);
        if (src == null) {
            throw new NoSuchElementException(""+tgt);
        }
        else {
            mMap.remove(src);
            mInverseMap.remove(tgt);
        }         
    }
    
    /**
     * Remove the existing mapping between pSrc and some other tgt
     * (assert that such a mapping exists in the first place), 
     * add a new mapping from pSrc to pTgt. 
     */
    public void replaceTgt(T1 pSrc, T2 pTgt) {
        removeSrc(pSrc);
        put(pSrc, pTgt);
    }
    
    /**
     * Remove the existing mapping between some src and pTgt.
     * (assert that such a mapping exists in the first place), 
     * add a new mapping between pSrc to pTgt. 
     */
    public void replaceSrc(T1 pSrc, T2 pTgt) {
        removeTgt(pTgt);
        put(pSrc, pTgt);
    }
    
    public Set<T1> getSrcValues() {
    	return mMap.keySet();    	
    }
    
    public Set<T2> getTgtValues() {
        return mInverseMap.keySet();       
    }
    
    
    public Iterator<Pair<T1,T2>> iterator() {
        return new ConverterIterator(mMap.entrySet().iterator(), new MapEntryToPairConverter());
    }        
        
    public static void main (String[] args) {
    	OneToOneBidirectionalMap map = new OneToOneBidirectionalMap();
    	map.put(1, "A");
    	map.put(2, "B");
    	map.put(3, "C");    	   
    	System.err.println("Initial situation:\n"+map);    	
    	    	                   
    }
    
    public String toString() {
    	return StringUtils.format(mMap, " => ", "\n");
    }
        
    
    
}
