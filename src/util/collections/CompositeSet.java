package util.collections;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.collections.iterator.CompositeIterator;

/**
 * 
 * Presents a list of sets as an "union" of them. 
 * 
 * Not a very "proper" implementation, use with caution...
 *  
 */
public class CompositeSet<T> extends AbstractSet<T> {
    
    private ArrayList<Set<T>> mSets;
    
    public CompositeSet() {
        mSets = new ArrayList();
    }
    
    public CompositeSet(Collection<Set<T>> pSets) {
        mSets = new ArrayList<Set<T>>(pSets);
    }
    
    public void addSet(Set<T> pSet) {
        mSets.add(pSet);        
    }
    
    /**
     * Shall return object multiple times if it occurs in more than
     * one set...
     */
    public Iterator<T> iterator() {
        List<Iterator<T>> iterators = new ArrayList();
        for (Set<T> s: mSets) {
            iterators.add(s.iterator());
        }
        return new CompositeIterator(iterators);
    }
    
    public boolean contains(Object o) {
        for (Set<T> set: mSets) {
            if (set.contains(o)) {
                return true;
            }
        }
        
        // not found in any set
        return false;
        
    }
    
    public int size() {
        int size = 0;
        for (Set<T> s: mSets) {
            size+=s.size();
        }
        return size;
    }

}
