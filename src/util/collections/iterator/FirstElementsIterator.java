package util.collections.iterator;

import java.util.*;

/**
 * Iterator only a given number of first elements from a base iterator.
 * If there are less elements, just iterates all elements. 
 */
public class FirstElementsIterator<T> implements Iterator<T>{

    private Iterator<T> mBaseIterator;
    private int mMaxNumElements;
    private int mNumIteratedElements;
    
    public FirstElementsIterator(int pNumElements, Iterator<T> pBaseIterator) {
        mBaseIterator = pBaseIterator;
        mMaxNumElements = pNumElements;
        mNumIteratedElements = 0;
    }
    
    public T next() {
        if (mNumIteratedElements < mMaxNumElements) {
            mNumIteratedElements++;
            return mBaseIterator.next();
        }
        else {
            throw new NoSuchElementException("Already iterated specified number of elements"); 
        }
    }
    
    public boolean hasNext() {
        return mBaseIterator.hasNext() && mNumIteratedElements < mMaxNumElements;
    }

    public void remove() {
        mBaseIterator.remove();    
    }
}
