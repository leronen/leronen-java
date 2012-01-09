package util.collections;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;


import util.CollectionUtils;
import util.ConversionUtils;
import util.FooAndPals;
import util.HashUtils;
import util.StringUtils;
import util.dbg.Logger;
import util.factory.HashSetFactory;

/**
 * Wraps an array as a index -> object map.
 * This is to enable more efficient storage of integer-key maps
 * for collection-based use.
 * 
 * E.g. one could use this as the top-level map in a MultiMap, as (a bit
 * tediously, as the component set type must be specified "twice") follows:
 *   MultiMap multiMap = new MultiMap(
 *               new ArrayMapFactory(arrayLen, HashSet.class),
 *               new HashSetFactory());
 * 
 * 
 * The arraymap has a constant size (length), and it is always considered to contain
 * mappings for all its elements. Thus, if the array length is 3,
 * the keyset is always (0,1,2).
 * 
 * The problem is now, do we want to explicitly maintain the entries
 * as Entry objects, or just persist a data array?
 * 
 * If entrySet does not get called often, the latter alternative saves
 * some memory. On the other hand, it is quite clumsy.
 * 
 * OK, for the time being we create the entries on the fly.
 * 
 * TODO: should we allow variable-length arrays (that is, ArrayLists)?
 * 
 * Note that, quite unconsistently with common Set practices, keys cannot be 
 * removed as such; there will always be key->null mappings present for 
 * the whole range of array indices! TODO: should configure the possibility
 * of considering the null-key values to NOT be part of the keyset!!!
 * 
 *   
 */
public class ArrayMap<T> extends AbstractMap<Integer, T> {

    private Set<Integer> mKeySet;
    private T[] mData;
    
    /**
     * TODO: the difficulty in calling this may be how to get a array
     * with a given runtime class.
     * 
     * One has to have a Class object for that: 
     *   "java.lang.reflect.Array.newInstance(class, len)"
     */
    ArrayMap(T[] pData) {
        mData = pData;
        mKeySet = new ArrayIndexSet(pData.length);
    }
    
    public T get(Object pKey) {
        return mData[(Integer)pKey];
    }        
    
    public T put(Integer pKey, T pVal) {
        T oldVal = mData[pKey];
        mData[pKey] = pVal;
        return oldVal;
    }
        
    /**
     * Keys cannot be removed as such, so throws an UnsupportedOperationException! 
     * (there will always be key->null mappings present! 
     */    
    public T remove(Integer pKey) {
        throw new UnsupportedOperationException();
    }        
    
    public Set<Integer> keySet() {
        return mKeySet;
    }

    public Collection<T> values() {
        return Collections.unmodifiableList(Arrays.asList(mData));
    }
    
    /** 
     * Constructs a new set of pairs from scratch -> no repeated calls, please! 
     * Could also use a iterator based set-implementation, without explicitly constructing
     * the entries*/
    public Set<Map.Entry<Integer, T>> entrySet() {
        Logger.error("Inefficient implementation of entrySet being called in class: "+this.getClass());
        throw new RuntimeException();
        // return (Set<Map.Entry<Integer, T>>) (Set) CollectionUtils.makeHashSet(new EntryIterator());        
    }
            
    private class Entry implements Map.Entry<Integer, T> {
        
        int mKey;
        
        private Entry(int pKey) {
            mKey = pKey;
        }
                               
        public Integer getKey() {
            return mKey;
        }
        
        public T getValue() {
            return mData[mKey];
        }
        
        public boolean equals(Object o) {
            Entry other = (Entry)o;
            return mKey == other.mKey && getValue().equals(other.getValue());                     
        }
        
        public int hashCode() {
            int result = HashUtils.SEED;         
            result = HashUtils.hash(result, mKey);
            result = HashUtils.hash(result, mData[mKey]);
            return result;           
        }
       
        public T setValue(T pVal) {
            T oldVal = mData[mKey];
            mData[mKey] = pVal;
            return oldVal;
        }
    }
        
    public class EntryIterator implements Iterator<Entry> {
                
        private Iterator<Integer> mKeyIterator;        
        
        private EntryIterator() {
            mKeyIterator = mKeySet.iterator();            
        }
        
        public boolean hasNext() {
            return mKeyIterator.hasNext();
        }
        
        public Entry next() {                        
            // val iterator should now have next element for us
            int key = mKeyIterator.next();
            return new Entry(key);            
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }        
    }       
    
    public static void main(String[] args) {
        // test map
        java.util.List<String> fooList = FooAndPals.asStringList();
        String[] fooArr = ConversionUtils.stringCollectionToArray(fooList);
        ArrayMap map = new ArrayMap(fooArr);
        System.out.println("A map:");
        System.out.println(StringUtils.mapToString(map));
                
        // test multimap
        List<SymmetricPair<String>> fooPairList = CollectionUtils.splitToPairs(
                CollectionUtils.headList(fooList, 10));
        MultiMap multiMap = new MultiMap(
                new ArrayMapFactory(fooList.size()/2, HashSet.class),
                new HashSetFactory(2));
        for (int i=0; i<fooPairList.size(); i++) {
            SymmetricPair<String> fooPair = fooPairList.get(i);
            for (String foo: fooPair) {
                multiMap.put(i, foo);
            }
        }
        System.out.println("A multi-map:");
        System.out.println(StringUtils.multiMapToString(multiMap));                                                   
    }
    
    public static class ArrayMapFactory<T> implements util.factory.ParametrizedFactory<ArrayMap, Integer> {
        
        private int mArrayLength;
        private Class mComponentClass;
        
        public ArrayMapFactory(int pArrayLength,                
                               Class pComponentClass) {
            mArrayLength = pArrayLength;
            mComponentClass = pComponentClass;
        }
        
        public ArrayMap makeObject() {
            T[] arr = (T[]) java.lang.reflect.Array.newInstance(mComponentClass, mArrayLength);
            return new ArrayMap<T>(arr);
        }
        
        public ArrayMap makeObject(Integer pSize) {
            T[] arr = (T[]) java.lang.reflect.Array.newInstance(mComponentClass, pSize);
            return new ArrayMap(arr);
        }
       
        
    }
}
