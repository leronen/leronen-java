package util.collections.iterator;

import java.util.*;

/** bridges gap between enumeration and iterator */
public class EnumerationIterator<T> implements Iterator<T> {
    
    private Enumeration<T> mEnum;
    
    public EnumerationIterator(Enumeration<T> pEnum) {
        mEnum = pEnum;            
    }
    
    public boolean hasNext() {
        return mEnum.hasMoreElements();    
    }
            
    public T next() {
        return mEnum.nextElement();    
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

    

}
