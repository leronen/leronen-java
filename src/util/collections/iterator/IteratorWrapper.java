package util.collections.iterator;

import java.util.*;

public class IteratorWrapper<T> implements Iterator<T>{

    protected Iterator<T> mBaseIterator;
    
    public IteratorWrapper(Iterator<T> pBaseIterator) {
        mBaseIterator = pBaseIterator;
    }
    
    public T next() {
        return mBaseIterator.next();
    }
    
    public boolean hasNext() {
        return mBaseIterator.hasNext();
    }

    public void remove() {
        mBaseIterator.remove();    
    }
}
