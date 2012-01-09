package util.collections.iterator;

import java.util.*;

import util.IOUtils;
import util.IteratorIterable;

/**
 * Iterates a iteratiorn of collections in sequential manner, always proceeding to the 
 * next collection once the previous collection is exhausted.
 * 
 * Assumes none of the collections is empty!!!
 */
public class CompositeIterator2<T> implements Iterator<T> {
    
    private Iterator<? extends Iterable<T>> mCollectionIterator;
    // private int mCurrentIndex;            
    private Iterator<T> mCurrentIterator;
        
    // int mNumObjectsIterated = 0;
                        
    public CompositeIterator2(Iterator<? extends Iterable<T>> pCollectionIterator) {
        // mCurrentIndex = 0;
        mCollectionIterator = pCollectionIterator;        
        mCurrentIterator = mCollectionIterator.hasNext() ? mCollectionIterator.next().iterator() : Collections.EMPTY_LIST.iterator();        
    }        
           
    public boolean hasNext() {                         
        return mCurrentIterator.hasNext() ;
    }
    
    public T next() {                    
        if (!mCurrentIterator.hasNext()) {                
            throw new NoSuchElementException();                        
        }
        // OK, the iterator should have something to return...
        T objectToReturn = mCurrentIterator.next();
        if (objectToReturn == null) {
            throw new RuntimeException("What the heck, our good iterator claimed to have a next, but returned a null anyway!\n"+                                       
                                       "mCurrentIterator=="+mCurrentIterator);
        }                                                       
        if (!mCurrentIterator.hasNext()) {
            mCurrentIterator = mCollectionIterator.hasNext() ? mCollectionIterator.next().iterator() : Collections.EMPTY_LIST.iterator();
        }            
        // mNumObjectsIterated++;        
        return objectToReturn;                        
    }
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }        
    
    public static void main(String[] args) {
        List<Iterable<String>> tmp = new ArrayList(); 
        
        for (String file: args) {
            tmp.add(IOUtils.lines(file));
        }
        
        CompositeIterator2<String> iter = new CompositeIterator2<String>(tmp.iterator());
        
        for (String s: new IteratorIterable<String>(iter)) {
            System.out.println(s);
        }
    }

}
