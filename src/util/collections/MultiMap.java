package util.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import util.CollectionUtils;
import util.SU;
import util.StringUtils;
import util.clone.Cloner;
import util.clone.ShallowCloner;
import util.clone.StringCloner;
import util.collections.graph.DirectedGraph;
import util.collections.graph.IGraph;
import util.collections.graph.defaultimpl.DefaultGraph;
import util.factory.ArrayUnenforcedSetFactory;
import util.factory.HashMapFactory;
import util.factory.HashSetFactory;
import util.factory.LinkedHashMapFactory;
import util.factory.LinkedHashSetFactory;

/**
 * A map that may have multiple items with the same key.
 * Implemented with a Map containing Set instances.
 * The actual Map and Set implementations can be 
 * specified by parametrizing the multimap with appropriate
 * util.factory.Factory objects (done in the constructor). By default,
 * linked hash maps and sets are used, in order to maintain insertion order 
 * in iteration, which is often a desirable property (of course, this results
 * in slightly larger overhead in storage space and efficiency). 
 *
 * Note that the values are sets; thus, multimap cannot have multiple instances 
 * of the same value for a single key. (sometimes we could desire "multimaps"
 * with list-like semantics in the value collections; how to incorporate
 * this need into these libraries is an open problem)
 * 
 * Implementation invariant: no keys with empty sets should ever be contained in 
 * the multimap. This kind of situation should only be imaginable after  
 * a remove (???). There it is now (once again) checked; at earlier times,
 * that check was once again disabled, for unknown reasons!
 *  
 * Note that we currently do not implement equals, hashCode or compareTo,
 * as their semantics could be imagined to vary, according to the actual set
 * and map implementations used (e.g. ordering in a LinkedHashSet).
 * Hmm, maybe this should be done after all? But then again, I imagine it would
 * be quite rare to use MultiSet as a key. If needed, we could implement specialized 
 * comparison methods, not conforming to the Object inteface.
 * Of course a general-purpose implementation is also possible, conforming to 
 * set and map specifications. This could be even implemented by just calling
 * the corresponding methods in mToplevelMap.
 * 
 * TODO: encourage using as efficient set implementations as possible
 * when e.g. hashing is not a strict requisite. Should maybe allow
 * even lists as values, but that would maybe be in too stark a constrast
 * with the current (already heavily deployed) API. 
 *
 * @see DirectedGraph
 * @see DefaultGraph
 * @see IGraph
 * @see MultiMap
 */
public class MultiMap<K,V> implements IMultiMap<K,V>, Serializable {
    
    private Map<K,Set<V>> mToplevelMap;
    
    /** Note: currently read-only access not enforced! */
    public final static MultiMap EMPTY_INSTANCE = makeHashMapBasedMultiMap();
    
    // argh, no time to implement sensible serialization... so use fast-and-horrible default serialization
    static final long serialVersionUID = 7077091808887542726L;
    
    private static final util.factory.ParametrizedFactory<? extends Map, Integer> DEFAULT_TOP_LEVEL_MAP_FACTORY = new LinkedHashMapFactory(); 
    private static final util.factory.ParametrizedFactory<? extends Set, Integer> DEFAULT_SET_FACTORY = new LinkedHashSetFactory();
//    private static final util.factory.Factory<Set> DEFAULT_SET_FACTORY = new LinkedHashSetFactory();
    
    private util.factory.ParametrizedFactory<? extends Map, Integer> mToplevelMapFactory;
    private util.factory.ParametrizedFactory<? extends Set, Integer> mSetFactory;
    
    /**
     * Endow MultiMap with the almost magical power to remember its creator!
     * (may be null)
     * let's not, as the factories are known to us anyway...
     */      
    // private Factory mFactory;
    
    public MultiMap() {
    	mToplevelMapFactory = DEFAULT_TOP_LEVEL_MAP_FACTORY;
    	mSetFactory = DEFAULT_SET_FACTORY;
    	
        mToplevelMap = mToplevelMapFactory.makeObject(); // new LinkedHashMap<K,Set<V>>();
    }
    
    @Override
    public Map toMap() {
    	Map<K,V> result = mToplevelMapFactory.makeObject();
    	
    	for (K key: keySet()) {
    		Set<V> vals = get(key);
    		if (vals.size() == 0) {
    			// nothing here!
                // something must be gravely wrong 
                // (we must maintain the invariant!)
                throw new RuntimeException("Invariant violated!");
                
    		}
    		else if (vals.size() == 1) {
    			result.put(key, vals.iterator().next());
    		}
    		else {
    			throw new RuntimeException("Cannot convert to map: multiple values for key "+key);
    		}
    	}
    	return result;
    }
            
    public MultiMap(util.factory.ParametrizedFactory<? extends Map, Integer> pToplevelMapFactory,
                    util.factory.ParametrizedFactory<? extends Set, Integer> pSetFactory) {
    	mToplevelMapFactory = pToplevelMapFactory;
    	mSetFactory = pSetFactory;
    	
        mToplevelMap = mToplevelMapFactory.makeObject();                
    }
    
    /** The most efficient option currently available (if general functionality is needed); use hash maps and sets */
    public static MultiMap makeHashMapBasedMultiMap() {
        return new MultiMap(new HashMapFactory(), new HashSetFactory());
    }
    
    /**
     * An impl where the sets are stored as simple arrays. 
     * The most efficient option currently available. NOTE: 
     * caller has to ensure that duplicate elements are not added!
     * Also, does not support fast set membership checking.
     * */
    public static MultiMap makeArrayUnenforcedSetBasedMultiMap() {
        return new MultiMap(new HashMapFactory(), new ArrayUnenforcedSetFactory());
    }
    
    public MultiMap(int pDefaultMapCapacity, int pDefaultSetCapacity) {
    	mToplevelMapFactory = new LinkedHashMapFactory(pDefaultMapCapacity);
        mSetFactory = new LinkedHashSetFactory(pDefaultSetCapacity);     
        mToplevelMap = mToplevelMapFactory.makeObject();
    }
     
    public static <K,V> MultiMap<K,V> makeExactSizeHashBasedMultiMap(MultiSet<K> pSizes) {
        MultiMap<K,V> result = makeHashMapBasedMultiMap();
        for (K key: pSizes) {
            int size = pSizes.getCount(key);
            Set<V> set = result.mSetFactory.makeObject(size);
            result.mToplevelMap.put(key, set);
        }
        return result;
    }
    
    /** May not be very efficient... */
    public int countNumVals() {
        return CollectionUtils.countObjects(entryIterator());
    }
    
    public double countAvgSetSize() {
        return ((double)countNumVals()) / keySet().size();
    }
    
    @Override
    public void put(K pKey, V pVal) {
        Set<V> set = mToplevelMap.get(pKey);
        if (set==null) {
            set = mSetFactory.makeObject();
            mToplevelMap.put(pKey, set);
        }
        set.add(pVal);    
    }

    /**
     * Use {@link #putMultiple(Object, Collection)} (and not this method) to put all values from a set 
     * under a single given key 
     */
    @Override
    public void putAll(Map<K,V> pMap) {
        Iterator<K> keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            V val = pMap.get(key);
            put(key, val);
        }            
    }
    
    @Override
    public void putAll(MultiMap<K,V> pMultiMap) {
        Iterator<K> keys = pMultiMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            Set<V> vals = pMultiMap.get(key);
            putMultiple(key, vals);
        }            
    }
                
    @Override
    public void putMultiple(K pKey, Collection<V> pVals) {        
        Iterator<V> vals = pVals.iterator();
        while(vals.hasNext()) {            
            V val = vals.next();
            put(pKey, val);
        }            
    }
    
    @Override
    public boolean containsKey(K pKey) {
        return mToplevelMap.containsKey(pKey);
    }
    
    public Map<K,Set<V>> rawRep() {
        return Collections.unmodifiableMap(mToplevelMap);
    }
    
    /**
     * Use this instead of get(K).contains(V) for efficiency (get involves
     * creating an Collections.unmodifiableSet wrapper.
     */
    public boolean containsMapping(K pKey, V pVal) {
        Set<V> set = mToplevelMap.get(pKey);
        return (set != null) ? set.contains(pVal) : false;
    }
    
        
    /**
     * Return an UNMODIFIABLE set!
     * 
     * Note that we never return null; if there are no values, we just return 
     * an empty set (adding it to the top-level map!!) Undoubtedly, there 
     * admittably probably exists a good reason for this, 
     * but as of now, as I scrabble through these desolated words on the late night of 29.11.2006 AD,
     * that reason, as formidable as it once might have been, has been unreversably
     * lost to the unforgiving annals of time. Of course, it is too late to 
     * change this now. Too late for us... 
     * OK, on the not-so late night of 14.12.2006 AD, let us decide it was not 
     * too late after all! We have come to the reasonable conclusion that 
     * it is turmoilous that a get() should modify a data-structure
     * as a side-effect! OK, as some callers may assume an empty set, let's
     * return a empty set; however, let's not impose an empty set upon our
     * precious data structures. (maybe the motivation for doing this earlier
     * was some real or, more probably, imagined gains in efficiency).
     * 
     * 
     */
    @Override
    public Set<V> get(K pKey) {
        Set<V> set = mToplevelMap.get(pKey);
        if (set==null) {
        	return Collections.emptySet();
        }
        else {
            return Collections.unmodifiableSet(set);
        }
    }      
    
    public Set<V> getMultiple(Collection<K> pKeys) {
        Set<V> result = mSetFactory.makeObject();
        for (K k: pKeys) {
            result.addAll(get(k));
        }
        return result;
    }
    
    
    @Override
    public void updateKey(K pOldKey, K pNewKey) {
    	if (mToplevelMap.containsKey(pNewKey)) {
    		throw new RuntimeException("Already contains key: "+pNewKey);
    	}
    	Set<V> vals = mToplevelMap.get(pOldKey);
    	mToplevelMap.remove(pOldKey);
    	mToplevelMap.put(pNewKey, vals);    	    
    }

    
    /**  
     * Assume that this key only has one value, and get it.
     * If no values for this key, return null;
     * If multiple values for this key, throw a RuntimeException.
     * Note that this has been changed as of 29.11.2006;
     * at earlier times we first called get(K), and in so doing 
     * probably threw a runtimeexception, as get generates an empty set 
     * for a non-existent key!
     */
    @Override
    public V getSingleton(K pKey) {
         Set<V> vals = mToplevelMap.get(pKey);
        
        if (vals == null) {
            // no vals for key; return null (
            return null;
        }        
        else if (vals.size() == 0) {
            // Hmm, returning null was enabled; but in the class javadoc
            // we state that there is an implementation invariant stating
            // that there are no empty sets as values of the top-level map!
            // return null;
            throw new RuntimeException("Should not contain sets with no values!");
            // Hmm, returning null was for some reason commented out at earlier times:
            // return null;
        }
        else if (vals.size() == 1) {
            return vals.iterator().next();
        }
        else {            
            throw new RuntimeException("Cannot get singleton value; there are more than 1 values! "+
                                       "Key: "+pKey+"\n"+
                                       "Values: "+SU.toString(vals,", "));
        }                                                    
    }
    
    public Iterator<Pair<K,V>> entryIterator() {
        return new MultiMapIterator();
    }
    
    /** Constructs a new set of pairs from scratch =&lt; no repeated calls, please! */
    public Set<Pair<K,V>> entrySet() {
        return CollectionUtils.makeHashSet(entryIterator());
    }
        
    public class MultiMapIterator implements Iterator<Pair<K,V>> {
        
        private K mCurrentKey;
        private Iterator<K> mKeyIterator;
        private Iterator<V> mValIterator;
        
        private MultiMapIterator() {
            mKeyIterator = keySet().iterator();
            mValIterator = null;
        }
        
        @Override
        public boolean hasNext() {
            return mKeyIterator.hasNext() || (mValIterator != null && mValIterator.hasNext());
        }
        
        @Override
        public Pair<K,V> next() {
            if (mValIterator == null || !(mValIterator.hasNext())) {
                // no more vals for this key or iteration not initialized
                if (mKeyIterator.hasNext()) {
                    // proceed to next key
                    mCurrentKey = mKeyIterator.next();
                    mValIterator = mToplevelMap.get(mCurrentKey).iterator();
                }
                else {
                    // no more elements in key iterator
                    throw new NoSuchElementException();
                }
            }
            
            // val iterator should now have next element for us
            return new Pair(mCurrentKey, mValIterator.next());            
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    
    /** Returms (an unmodifiable!) key set */
    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(mToplevelMap.keySet());    
    }

    /** Return an UNMODIFIABLE list of multi val keys */
    @Override
    public List<K> getMultiValKeys() {
        ArrayList<K> result = new ArrayList<K>();
        Iterator<K> keys = mToplevelMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            Set<V> vals = mToplevelMap.get(key);
            if (vals.size()>1) {
                result.add(key);
            }
        }
        return Collections.unmodifiableList(result);        
    }
            
    /** Sorts each invidual contained set according to pComparator */
    @Override
    public void sortKeys(Comparator pComparator) {
        LinkedHashMap<K,Set<V>> orderedMap = new LinkedHashMap<K,Set<V>>();
        ArrayList<K> orderedKeys = new ArrayList<K>(mToplevelMap.keySet());
        Collections.sort(orderedKeys, pComparator);        
        Iterator<K> orderedKeysIterator = orderedKeys.iterator();
        while(orderedKeysIterator.hasNext()) {
            K key = orderedKeysIterator.next();
            Set<V> set = mToplevelMap.get(key);
            orderedMap.put(key, set);                                    
        }
        mToplevelMap = orderedMap;
    }       
    
    
    /** Sorts each invidual contained set according to pComparator */
    @Override
    public void sortSets(Comparator pComparator) {
        Iterator<K> keys = mToplevelMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            Set<V> set = mToplevelMap.get(key);
            List<V> asList = new ArrayList<V>();
            asList.addAll(set);            
            Collections.sort(asList, pComparator);                        
            mToplevelMap.put(key, new LinkedHashSet<V>(asList));            
        }            
    }        
    
    /**
     * Prunes the set map such that only one entry (hopefully the one that 
     * was inserted first!) per key remains 
     */
    @Override
    public void prune() {
        LinkedHashMap<K, V> tmpMap = new LinkedHashMap<K,V>();
        Iterator<K> keys = mToplevelMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            Set<V> valSet = mToplevelMap.get(key);
            Iterator<V> vals = valSet.iterator();
            if (vals.hasNext()) {
                tmpMap.put(key, vals.next());
            }
        }
        mToplevelMap.clear();
        putAll(tmpMap);                 
    }
    
    /**
     * Removes the set alltogether from the top-level map (that is, does not
     * just do mere clearing of a potentially large set, which might lead
     * to dangling memory!
     */
    @Override
    public void clear(K pKey) {
        mToplevelMap.remove(pKey);    
    }
    
    /** Uh, seems to be exactly the same as {@link #clear(K)}! */
    @Override
    public void removeKey(K pKey) {
        mToplevelMap.remove(pKey);                
    }
    
    @Override
    public void remove(K pKey, V pVal) {
        Set<V> set = mToplevelMap.get(pKey);
        if (set != null) {
            set.remove(pVal);
            // Maintain invariant!!!
            if (set.size() == 0) {
                mToplevelMap.remove(pKey);
            }
        }
    }
    
    @Override
    public String toString() {        
        return StringUtils.multiMapToString(this);    
    }
        
    /** TODO: this may enable malicious access to the internal data structures */
    @Override
    public Collection<Set<V>> getValuesAsCollectionOfSets() {
        return mToplevelMap.values();                
    }                
    
    /** 
     * Return an UNMODIFIABLE list of all values in the multimap;
     * Hmm, this could have been done with less overhead, by just
     * implementing a dummy collection, with a convenient iterator over the values...
     * 
     * Note that we will have multiple instances of a value in the result,
     * if the same value appears in sets for multiple keys.
     */
    @Override
    public List<V> values() {
        ArrayList<V> result = new ArrayList<V>();
        Iterator<K> keys = mToplevelMap.keySet().iterator();
        while(keys.hasNext()) {
            K key = keys.next();
            Set<V> valsForSingleKey = mToplevelMap.get(key);            
            result.addAll(valsForSingleKey);                                                                        
        } 
        return Collections.unmodifiableList(result);    
    }
    
    public MultiMap<K,V> createEmptyClone() {
        return new MultiMap<K,V>(mToplevelMapFactory, mSetFactory);
    }
    
    /** Creates a deep clone */
    @Override
    public MultiMap<K,V> createClone(Cloner<K> pKeyCloner, Cloner<V> pValCloner) {
        MultiMap<K,V> clone = new MultiMap<K,V>(mToplevelMapFactory, mSetFactory);                
        
        Set<K> keySet = keySet();
        for (K key: keySet) {
            K keyClone = pKeyCloner.createClone(key);
            Set<V> vals = get(key);
            for (V val: vals) {
                V valClone = pValCloner.createClone(val);
                clone.put(keyClone, valClone);
            }                
        }
        
        return clone;                
    }
    
    /** Creates a shallow clone where keys and attributes are copied by reference*/
    @Override
    public MultiMap<K,V> createClone() {
        return createClone(new ShallowCloner<K>(), new ShallowCloner<V>());                
    }
    
    public static class Factory<KEYTYPE,VALTYPE> {
        
        private util.factory.ParametrizedFactory<? extends Map, Integer> mToplevelMapFactory;
        private util.factory.ParametrizedFactory<? extends Set, Integer> mSetFactory;        
                               
        public Factory(util.factory.ParametrizedFactory<? extends Map, Integer> pMapFactory, 
                       util.factory.ParametrizedFactory<? extends Set, Integer> pSetFactory) { 
            mToplevelMapFactory = pMapFactory;
            mSetFactory = pSetFactory;            
        }

        public MultiMap<KEYTYPE,VALTYPE> makeMultiMap() {
            return new MultiMap<KEYTYPE,VALTYPE>(mToplevelMapFactory, mSetFactory);
        }
    }
    
    public static void main (String[] args) {
        MultiMap map = new MultiMap();
        map.put("A", "neljasA");
        map.put("A", "ekaA");
        map.put("A", "tokeA");
        map.put("A", "kolmasA");        
        map.put("B", "ekaB");
        map.put("B", "tokaB");
        map.put("C", "ainutC");
        System.err.println("Ennen karsintaa:\n"+map);
        map.prune();        
        System.err.println("Karsinnan jälkeen:\n"+map);

        MultiMap clone = map.createClone(new StringCloner(), new StringCloner());
        System.err.println("Klooni:\n"+clone);                    
    }
    
    /** Note that this can only be used when the key and value types are same */
    public class GraphWrapper implements IGraph<K> {
        
        @Override
        public Set<K> followers(K p) {
            return (Set<K>)get(p);
        }
        
        @Override
        public Set<K> nodes() {
            return mToplevelMap.keySet();
        }
        
        @Override
        public Set<IPair<K,K>> edges() {
            throw new UnsupportedOperationException();
        }
    }
    
    public IGraph<K> asDirectedGraph() {
        return new GraphWrapper();
    }
    
    
}
