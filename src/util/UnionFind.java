package util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import util.collections.HashMultiSet;
import util.collections.MultiMap;
import util.collections.MultiSet;
import util.collections.SymmetricPair;

public class UnionFind<T> {

    private Map<T, Entry> mMap;            
    
    public UnionFind() {
        mMap = new HashMap<T, Entry>();
    }
    
    /**
     * Invariant: set does not exist yet (violating this could have
     * grave consequences!)
     */
    private void internalMakeSet(T pObj) {        
        Entry entry = new Entry(pObj);
        mMap.put(pObj, entry);
        entry.parent = entry;
        entry.rank = 0;
    }
        
    /** 
     * It is an error to call this if a set already exists (a RuntimeException
     * shall boom out.
     */ 
    public void makeSet(T pObj) {
        if (mMap.containsKey(pObj)) {
            throw new RuntimeException("Already contains key: "+pObj);
        }
        else {
            makeSet(pObj);
        }
    }
    
    
    /** 
     * Shorthand for union(pPair.getObj1(), pPair.getObj2()) 
     * */ 
    public void union(SymmetricPair<T> pPair) {
        union(pPair.getObj1(), pPair.getObj2());
    }
    
    /**
     * Combines equivalence classes of pX and pY into the same equivalence 
     * equivalence class. If pX and/or pY are not yet represented 
     * in the "UF", create singleton sets for them first, if needed.
     * 
     * If pX and pY are already in the same equivalence class, nothing
     * shall happen.
     * 
     * Return true, if a mapping was added, false if the mapping already
     * existed.
     */ 
    public boolean union(T pX, T pY) {
        
        if (!(mMap.containsKey(pX))) {
            internalMakeSet(pX);
        }
        
        if (!(mMap.containsKey(pY))) {
            internalMakeSet(pY);
        }
        
        Entry xEntry = mMap.get(pX);
        Entry yEntry = mMap.get(pY);
        Entry xRoot = internalFind(xEntry);
        Entry yRoot = internalFind(yEntry);        
        
        if (xRoot.rank > yRoot.rank) {
             yRoot.parent = xRoot;
             return true;
        }
        else if (xRoot.rank < yRoot.rank) {
            xRoot.parent = yRoot;
            return true;
        }
        else if (xRoot != yRoot) {
            yRoot.parent = xRoot;
            xRoot.rank = xRoot.rank + 1;
            return true;
        }
        else {
            return false;
        }
    }
    
    // Find the representative (root) of the set that pX belongs to */
    public T find(T pX) {
        Entry xEntry = mMap.get(pX);
        return internalFind(xEntry).obj;
    }
    
    private Entry internalFind(Entry pEntry) {
        if (pEntry.parent == pEntry) {
            return pEntry;
        }     
        else {
            pEntry.parent = internalFind(pEntry.parent);
            return pEntry.parent;
        }
    }

    /**
     * Mainly intended to provide possiblity for creating a MultiMap
     * with suitably sized component sets.
     */
    public MultiSet<T> countSetSizes() {
        MultiSet<T> result = new HashMultiSet();
        
        for (T o: mMap.keySet()) {
            result.add(find(o));
        }
         
        return result;
    }
    
    public Collection<Set<T>> reconstructEquivalenceClasses() {
        MultiSet<T> sizes = countSetSizes();
        MultiMap<T,T> result = MultiMap.makeExactSizeHashBasedMultiMap(sizes);
                
        for (T o: mMap.keySet()) {
            result.put(find(o), o);
        }

        return result.getValuesAsCollectionOfSets();
    }
    
    public static void main(String[] args) throws Exception {        
//        stressTest();       
         buizness();               
    }
    
    private static void buizness() throws IOException {
        UnionFind<String> uf = new UnionFind();
        
        for (SymmetricPair<String> pair: IOUtils.readPairs(System.in)) {
            uf.union(pair);
        }
         
        for (Set<String> set: uf.reconstructEquivalenceClasses()) {
            System.out.println(StringUtils.collectionToString(set, ", "));
        }
    }
    
    public Set<T> getAllObjects() {
        return mMap.keySet();
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Set<T> set: reconstructEquivalenceClasses()) {
            buf.append(StringUtils.collectionToString(set, ", "));
            buf.append("\n");
        }
        return buf.toString();
    }
    
    private class Entry {
        T obj;
        Entry parent;
        /** Note that rank is only used for the roots */
        int rank;
        
        Entry(T pObj) {
            obj = pObj;
        }
        
    }
    
}
 