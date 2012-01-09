package util.collections;


import java.util.*;
import util.StringUtils;
import util.IOUtils;

/** LinkedHashMap-based multimap. Store single values as Strings, only use sets if more than 1 values... */
public class StringMultiMap { 
    
    private LinkedHashMap<String,Object> mToplevelMap;
    
    public StringMultiMap() {                
        mToplevelMap = new LinkedHashMap<String,Object>();
    }
    
    public void put(String pKey, String pVal) {
        Object values = mToplevelMap.get(pKey);
        if (values == null) {
            // store as a single string
            mToplevelMap.put(pKey, pVal);
        }
        else if (values instanceof String) {
            // was a single string, change to set with 2 values
            LinkedHashSet<String> set = new LinkedHashSet<String>(2);
            set.add((String)values);
            set.add(pVal);
            mToplevelMap.put(pKey, set);
        }
        else {
            // a set 
            LinkedHashSet<String> set = (LinkedHashSet<String>)values ; 
            set.add(pVal);
        }           
    }

    /**
     * Use putMultiple() (and not this method) to put all values from a set 
     * under a single given key. 
     */
    public void putAll(Map<String,String> pMap) {
        Iterator<String> keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            String val = pMap.get(key);
            put(key, val);
        }            
    }
    
    public MultiSet<String> valCounts() {
        HashMultiSet<String> result = new HashMultiSet<String>();
        for (String key: keySet()) {
            result.add(key, getValCount(key));
        }
        return result;
    }
    
    public void putAll(MultiMap<String,String> pMultiMap) {
        Iterator<String> keys = pMultiMap.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            Set<String> vals = pMultiMap.get(key);
            putMultiple(key, vals);
        }            
    }
                
    public void putMultiple(String pKey, Collection<String> pVals) {        
        for (String val: pVals) {
            put(pKey, val);
        }            
    }
    
    public boolean containsKey(String pKey) {
        return mToplevelMap.containsKey(pKey);
    }
           
    /**
     * Use this instead of get(K).contains(V) for efficiency (get involves
     * creating an Collections.unmodifiableSet wrapper.
     */
//    public boolean containsMapping(K pKey, V pVal) {
//        Set<V> set = mToplevelMap.get(pKey);
//        return (set != null) ? set.contains(pVal) : false;
//    }
    
        
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
//    public Set<V> get(K pKey) {
//        Set<V> set = mToplevelMap.get(pKey);
//        if (set==null) {
//            Set<V> result; 
//            result = Collections.emptySet();
//            return result;
//        }
//        else {
//            return Collections.unmodifiableSet(set);
//        }
//    }      
    
       
    
//    public void updateKey(K pOldKey, K pNewKey) {
//        if (mToplevelMap.containsKey(pNewKey)) {
//            throw new RuntimeException("Already contains key: "+pNewKey);
//        }
//        Set<V> vals = mToplevelMap.get(pOldKey);
//        mToplevelMap.remove(pOldKey);
//        mToplevelMap.put(pNewKey, vals);            
//    }

    
    public int getValCount(String key) {
        Object values = mToplevelMap.get(key);
        if (values == null) {
            return 0;
        }
        else if (values instanceof String) {
            return 1;
        }
        else {
            Set<String> set = (Set<String>)values;
            return set.size();
        }
             
    }
    
    /** Always return a set, even if there is no keys (in which case return Collections.EMPTY_SET */
    public Set<String> getSet(String pKey) {
        Object vals = mToplevelMap.get(pKey);
        
        if (vals == null) {
            // no vals for key; return null (
            return Collections.EMPTY_SET;
        }        
        else if (vals instanceof String) {
            return Collections.singleton((String)vals);
        }
        else if (vals instanceof Set) {
            return (Set<String>)vals;
            
        }
        else {            
            throw new RuntimeException("Should not occur."); 
        }                                                   
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
    public String getSingleton(String pKey) {
         Object vals = mToplevelMap.get(pKey);
        
        if (vals == null) {
            // no vals for key; return null (
            return null;
        }        
        else if (vals instanceof String) {
            return (String)vals;
        }
        else if (vals instanceof Set) {
            Set<String> set = (Set<String>)vals;
            if (set.size() == 0) {        
                return null;
            }
            else if (set.size() == 1) {
                return set.iterator().next();
            }
            else {
                throw new RuntimeException("Cannot get singleton value; there are more than 1 values! "+
                                           "Key: "+pKey+"\n"+
                                           "Values: "+StringUtils.colToStr(set,", "));
            }
        }
        else {            
            throw new RuntimeException("Should not occur."); 
        }                                                    
    }
    
//    public Iterator<Pair<K,V>> entryIterator() {
//        return new MultiMapIterator();
//    }
    
    /** Constructs a new set of pairs from scratch -> no repeated calls, please! */
//    public Set<Pair<K,V>> entrySet() {
//        return CollectionUtils.makeHashSet(entryIterator());
//    }
        
//    public class MultiMapIterator implements Iterator<Pair<K,V>> {
//        
//        private K mCurrentKey;
//        private Iterator<K> mKeyIterator;
//        private Iterator<V> mValIterator;
//        
//        private MultiMapIterator() {
//            mKeyIterator = keySet().iterator();
//            mValIterator = null;
//        }
//        
//        public boolean hasNext() {
//            return mKeyIterator.hasNext() || (mValIterator != null && mValIterator.hasNext());
//        }
//        
//        public Pair<K,V> next() {
//            if (mValIterator == null || !(mValIterator.hasNext())) {
//                // no more vals for this key or iteration not initialized
//                if (mKeyIterator.hasNext()) {
//                    // proceed to next key
//                    mCurrentKey = mKeyIterator.next();
//                    mValIterator = mToplevelMap.get(mCurrentKey).iterator();
//                }
//                else {
//                    // no more elements in key iterator
//                    throw new NoSuchElementException();
//                }
//            }
//            
//            // val iterator should now have next element for us
//            return new Pair<K,V>(mCurrentKey, mValIterator.next());            
//        }
//        
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//        
//    }
    
    
    /** Returms (an unmodifiable!) key set */
    public Set<String> keySet() {
        return Collections.unmodifiableSet(mToplevelMap.keySet());    
    }

    /** Return an UNMODIFIABLE list of multi val keys */
    public List<String> getMultiValKeys() {
        ArrayList<String> result = new ArrayList<String>();
        for (String key: mToplevelMap.keySet()) {                    
            Object vals = mToplevelMap.get(key);
            if (vals instanceof Set && ((Set)vals).size() > 1) {
                result.add(key);
            }            
        }
        return Collections.unmodifiableList(result);        
    }
                
//    public void sortKeys(Comparator<K> pComparator) {
//        LinkedHashMap<K,Set<V>> orderedMap = new LinkedHashMap<K,Set<V>>();
//        ArrayList<K> orderedKeys = new ArrayList<K>(mToplevelMap.keySet());
//        Collections.sort(orderedKeys, pComparator);        
//        Iterator<K> orderedKeysIterator = orderedKeys.iterator();
//        while(orderedKeysIterator.hasNext()) {
//            K key = orderedKeysIterator.next();
//            Set<V> set = mToplevelMap.get(key);
//            orderedMap.put(key, set);                                    
//        }
//        mToplevelMap = orderedMap;
//    }       
//    
//    
//    /** Sorts each invidual contained set according to pComparator */
//    public void sortSets(Comparator<V> pComparator) {
//        Iterator<K> keys = mToplevelMap.keySet().iterator();
//        while(keys.hasNext()) {
//            K key = keys.next();
//            Set<V> set = mToplevelMap.get(key);
//            List<V> asList = new ArrayList<V>();
//            asList.addAll(set);            
//            Collections.sort(asList, pComparator);                        
//            mToplevelMap.put(key, new LinkedHashSet<V>(asList));            
//        }            
//    }        
    
//    /**
//     * Prunes the set map such that only one entry (hopefully the one that 
//     * was inserted first!) per key remains 
//     */
//    public void prune() {
//        LinkedHashMap<K, V> tmpMap = new LinkedHashMap<K,V>();
//        Iterator<K> keys = mToplevelMap.keySet().iterator();
//        while(keys.hasNext()) {
//            K key = keys.next();
//            Set<V> valSet = mToplevelMap.get(key);
//            Iterator<V> vals = valSet.iterator();
//            if (vals.hasNext()) {
//                tmpMap.put(key, vals.next());
//            }
//        }
//        mToplevelMap.clear();
//        putAll(tmpMap);                 
//    }
    
//    /**
//     * Removes the set alltogether from the top-level map (that is, does not
//     * just do mere clearing of a potentially large set, which might lead
//     * to dangling memory!
//     */
//    public void clear(K pKey) {
//        mToplevelMap.remove(pKey);    
//    }
    
//    /** Uh, seems to be exactly the same as {@link #clear(K)}! */
//    public void removeKey(K pKey) {
//        mToplevelMap.remove(pKey);                
//    }
    
//    public void remove(K pKey, V pVal) {
//        Set<V> set = mToplevelMap.get(pKey);
//        if (set != null) {
//            set.remove(pVal);
//            // Maintain invariant!!!
//            if (set.size() == 0) {
//                mToplevelMap.remove(pKey);
//            }
//        }
//    }
    
    /** for debug purposes only (formatting might change abruptly) */
    public String toString() {        
        return StringUtils.toString(this);
    }
    
//        
//    /** TODO: this may enable malicious access to the internal data structures */
//    public Collection<Set<V>> getValuesAsCollectionOfSets() {
//        return mToplevelMap.values();                
//    }                
    
//    /** 
//     * Return an UNMODIFIABLE list of all values in the multimap;
//     * Hmm, this could have been done with less overhead, by just
//     * implementing a dummy collection, with a convenient iterator over the values...
//     * 
//     * Note that we will have multiple instances of a value in the result,
//     * if the same value appears in sets for multiple keys.
//     */
//    public List<V> values() {
//        ArrayList<V> result = new ArrayList<V>();
//        Iterator<K> keys = mToplevelMap.keySet().iterator();
//        while(keys.hasNext()) {
//            K key = keys.next();
//            Set<V> valsForSingleKey = mToplevelMap.get(key);            
//            result.addAll(valsForSingleKey);                                                                        
//        } 
//        return Collections.unmodifiableList(result);    
//    }
        
    
    /** Test by reading a input with 2 colums: key and val */ 
    public static void main (String[] args) throws Exception {
        StringMultiMap map = new StringMultiMap();
        for (String line: IOUtils.readLines()) {
            String[] tok = line.split("\\s+");
            String key = tok[0];
            String val = tok[1];
            map.put(key, val);
        }
        System.out.println(""+map);
        

                          
    }
        
    
}
