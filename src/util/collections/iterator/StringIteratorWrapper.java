package util.collections.iterator;

import java.util.*;


/** Just for convenience, so that typecasts need not be written by poor programmer */
public class StringIteratorWrapper extends IteratorWrapper {

    public StringIteratorWrapper(Iterator pBaseIterator) {
        super (pBaseIterator);
    }
    
    public String nextString() {
        return (String)next();
    }
}
