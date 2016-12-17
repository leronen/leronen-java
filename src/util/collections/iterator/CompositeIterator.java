package util.collections.iterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import util.collections.SymmetricPair;
import util.collections.Triple;

/**
 * Iterates a list of iterators in sequential manner, always proceeding to the 
 * next iterator once the previous iterator is exhausted.
 */
public class CompositeIterator<T> implements Iterator<T> {
    
    private List<Iterator<T>> iterators;
    private int currentIndex;            
    private Iterator<T> currentIterator;
                               
    public CompositeIterator(List<Iterator<T>> pIterators) {
        currentIndex = 0;
        this.iterators = pIterators;
        currentIterator = iterators.size() > 0 ? iterators.get(0) : Collections.EMPTY_LIST.iterator();
        proceedToNextIteratorIfNeeded();
    }        

    public CompositeIterator(Iterator<T> pFirst, Iterator<T> pSecond) {
        this(new SymmetricPair<Iterator<T>>(pFirst, pSecond));
    }

    public CompositeIterator(Iterator pFirst, Iterator pSecond, Iterator pThird) {
        this(new Triple(pFirst, pSecond, pThird));
    }                
                                    
    private void proceedToNextIteratorIfNeeded() {            
        while(!currentIterator.hasNext() && currentIndex < iterators.size()-1) {
            currentIndex++;                                    
            currentIterator = iterators.get(currentIndex);
        }                                    
    }
    
    @Override
    public boolean hasNext() {                         
        boolean result = currentIterator.hasNext();                    
        return result;
    }
    
    @Override
    public T next() {                    
        if (!currentIterator.hasNext()) {                
            throw new NoSuchElementException();                        
        }
        // OK, the iterator should have something to return...
        T objectToReturn = currentIterator.next();
        if (objectToReturn == null) {
            throw new RuntimeException("What the heck, our good iterator claimed to have a next, but returned a null anyway!\n"+
                                       "mCurrentIndex=="+currentIndex+"\n"+
                                       "mCurrentIterator=="+currentIterator);
        }                                                       
        if (!currentIterator.hasNext()) {
            proceedToNextIteratorIfNeeded();
        }            
        // mNumObjectsIterated++;        
        return objectToReturn;                        
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();    
    }        

}
