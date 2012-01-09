package util.collections.iterator;

import java.util.Iterator;

public class SingletonIterator<T> implements Iterator<T> {

    private T mObj;
    private boolean mHasNext;
    
    public SingletonIterator(T p) {        
        mObj = p;
        mHasNext = true;
    }
    
    public boolean hasNext() {
        return mHasNext;  
    }
            
    public T next() {
        mHasNext = false;
        T result = mObj;
        mObj = null;
        return result;    
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
