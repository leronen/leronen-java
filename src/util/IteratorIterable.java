package util;

import java.util.Iterator;

/**
 * A terrible workaround for the sad state of affairs that
 * java cannot iterate a iterator with the for(X x: Xcol) syntax.. 
 */
public class IteratorIterable<T> implements Iterable<T> {    
    
    boolean alreadyIterated = false;
    private Iterator<T> mIterator;
                    
    public IteratorIterable(Iterator pIterator)  {            
        mIterator = pIterator;
    }
            
    public Iterator iterator() {
        if (alreadyIterated) {
            throw new RuntimeException("Unfortunately, can only iterate once...");
        }
        else {
            alreadyIterated = true;
            return mIterator;
        }           
    }
}