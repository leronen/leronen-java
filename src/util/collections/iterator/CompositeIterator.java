package util.collections.iterator;

import java.util.*;

import util.collections.SymmetricPair;
import util.collections.Triple;

/**
 * Iterates a list of iterators in sequential manner, always proceeding to the 
 * next iterator once the previous iterator is exhausted.
 */
public class CompositeIterator<T> implements Iterator<T> {
    
    private List<Iterator<T>> mIterators;
    private int mCurrentIndex;            
    private Iterator<T> mCurrentIterator;
        
    // int mNumObjectsIterated = 0;
                        
    public CompositeIterator(List<Iterator<T>> pIterators) {
        mCurrentIndex = 0;
        mIterators = pIterators;
        mCurrentIterator = pIterators.size() > 0 ? (Iterator)pIterators.get(0) : Collections.EMPTY_LIST.iterator();
        proceedToNextIteratorIfNeeded();
    }        

    public CompositeIterator(Iterator<T> pFirst, Iterator<T> pSecond) {
        this(new SymmetricPair<Iterator<T>>(pFirst, pSecond));
    }

    public CompositeIterator(Iterator pFirst, Iterator pSecond, Iterator pThird) {
        this(new Triple(pFirst, pSecond, pThird));
    }                
                                    
    private void proceedToNextIteratorIfNeeded() {            
        while(!mCurrentIterator.hasNext() && mCurrentIndex < mIterators.size()-1) {
            mCurrentIndex++;                                    
            mCurrentIterator = mIterators.get(mCurrentIndex);
        }                                    
    }
    
    public boolean hasNext() {                         
        boolean result = mCurrentIterator.hasNext();                    
        return result;
    }
    
    public T next() {                    
        if (!mCurrentIterator.hasNext()) {                
            throw new NoSuchElementException();                        
        }
        // OK, the iterator should have something to return...
        T objectToReturn = mCurrentIterator.next();
        if (objectToReturn == null) {
            throw new RuntimeException("What the heck, our good iterator claimed to have a next, but returned a null anyway!\n"+
                                       "mCurrentIndex=="+mCurrentIndex+"\n"+
                                       "mCurrentIterator=="+mCurrentIterator);
        }                                                       
        if (!mCurrentIterator.hasNext()) {
            proceedToNextIteratorIfNeeded();
        }            
        // mNumObjectsIterated++;        
        return objectToReturn;                        
    }
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }        

}
