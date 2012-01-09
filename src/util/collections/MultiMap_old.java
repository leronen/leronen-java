package util.collections;

import util.*;
import util.dbg.*;

import java.util.*;

import java.io.*;

/**
 * A map that may have multiple items with the same key
 * implemented with a HashMap containing LinkedHashSet instances.
 *  
 * As the top-level data structure is a HashMap That means that no ordering is maintained for for the keys;
 * the sets, on the contrary, are linked hash sets, so their ordering is the insertiong order.  
 *
 * Todo: permit using this with different kind of Set and Map Implementations. A factory for those?
 */
public class MultiMap_old implements Serializable {
    private LinkedHashMap mMap;
    
    // argh, no time to implement sensible serialization... so use fast-and-horrible default serialization
    static final long serialVersionUID = 7077091808887542726L;
    
    public MultiMap_old() {
        mMap = new LinkedHashMap();
    }
        
    public void put(Object pKey, Object pVal) {
        LinkedHashSet set = (LinkedHashSet)mMap.get(pKey);
        if (set==null) {
            set = new LinkedHashSet();
            mMap.put(pKey, set);
        }
        set.add(pVal);    
    }

    public void putAll(Map pMap) {
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            Object val = pMap.get(key);
            put(key, val);
        }            
    }
    
    public void putAll(MultiMap_old pMultiMap) {
        Iterator keys = pMultiMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            Set vals = pMultiMap.get(key);
            putMultiple(key, vals);
        }            
    }
                
    public void putMultiple(Object pKey, Collection pVals) {        
        Iterator vals = pVals.iterator();
        while(vals.hasNext()) {            
            Object val = vals.next();
            put(pKey, val);
        }            
    }
    
    public boolean containsKey(String pKey) {
        return mMap.containsKey(pKey);
    }
        
    public Set get(Object pKey) {
        LinkedHashSet set = (LinkedHashSet)mMap.get(pKey);
        if (set==null) {
            set = new LinkedHashSet();
            mMap.put(pKey, set);
        }
        return Collections.unmodifiableSet(set);
    }        

    public Object getSingleton(Object pKey) {
        Set vals = get(pKey);
        if (vals.size() == 0) {
            return null;
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
    
    public Set keySet() {
        return mMap.keySet();    
    }

    public List getMultiValKeys() {
        ArrayList result = new ArrayList();
        Iterator keys = mMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            LinkedHashSet vals = (LinkedHashSet)mMap.get(key);
            if (vals.size()>1) {
                result.add(key);
            }
        }
        return result;        
    }
    
    
    
    /** Sorts each invidual contained set according to pComparator */
    public void sortKeys(Comparator pComparator) {
        LinkedHashMap orderedMap = new LinkedHashMap();
        ArrayList orderedKeys = new ArrayList(mMap.keySet());
        Collections.sort(orderedKeys, pComparator);        
        Iterator orderedKeysIterator = orderedKeys.iterator();
        while(orderedKeysIterator.hasNext()) {
            Object key = orderedKeysIterator.next();
            LinkedHashSet set = (LinkedHashSet)mMap.get(key);
            orderedMap.put(key, set);                                    
        }
        mMap = orderedMap;
    }       
    
    
    /** Sorts each invidual contained set according to pComparator */
    public void sortSets(Comparator pComparator) {
        Iterator keys = mMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            LinkedHashSet set = (LinkedHashSet)mMap.get(key);
            List asList = new ArrayList();
            asList.addAll(set);            
            Collections.sort(asList, pComparator);                        
            mMap.put(key, new LinkedHashSet(asList));            
        }            
    }        
    
    /** Prunes the set map such that only one entry (hopefully the one that was inserted first!) per key remains */
    public void prune() {
        LinkedHashMap tmpMap = new LinkedHashMap();
        Iterator keys = mMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            LinkedHashSet valSet = (LinkedHashSet)mMap.get(key);
            Iterator vals = valSet.iterator();
            if (vals.hasNext()) {
                tmpMap.put(key, vals.next());
            }
        }
        mMap.clear();
        putAll(tmpMap);                 
    }
    
    public void clear(Object pKey) {
        mMap.remove(pKey);    
    }
    
    public void removeKey(Object pKey) {
        mMap.remove(pKey);                
    }
    
    public void remove(Object pKey, Object pVal) {
        LinkedHashSet set = (LinkedHashSet)mMap.get(pKey);
        set.remove(pVal);
    }
    
    public String toString() {
        return StringUtils.multiMapToString(this);    
    }
        
    public Collection getValuesAsCollectionOfSets() {
        return mMap.values();                
    }                
    
    public List values() {
        ArrayList result = new ArrayList();
        Iterator keys = mMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            Set valsForSingleKey = (Set)mMap.get(key);            
            result.addAll(valsForSingleKey);                                                                        
        } 
        return Collections.unmodifiableList(result);    
    }
    
    public static void main (String[] args) {
        MultiMap_old map = new MultiMap_old();
        map.put("A", "neljasA");
        map.put("A", "ekaA");
        map.put("A", "tokeA");
        map.put("A", "kolmasA");        
        map.put("B", "ekaB");
        map.put("B", "tokaB");
        map.put("C", "ainutC");
        dbgMsg("Ennen karsintaa:\n"+map);
        map.prune();        
        dbgMsg("Karsinnan jälkeen:\n"+map);                    
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("MultiMap: "+pMsg);
    }
    
    
}
