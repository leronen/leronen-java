package util.collections.iterator;

import util.*;
import util.dbg.*;

import java.util.*;

public class ArrayIterator implements Iterator {

    private Object[] mArray;        
    private int mIndex; 
    
    public ArrayIterator(Object[] pArray) {        
        mArray = pArray;
        mIndex = 0;        
    }        
        
    public boolean hasNext() {
        return mIndex < mArray.length;
    }

    public Object next() {
        if (mIndex >= mArray.length) {
            throw new NoSuchElementException();
        }
        
        return mArray[mIndex++];        
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public static void main (String[] args) {
        String[] names = new String[] {"a",  "b", "c"};        
        Iterator iter = new ArrayIterator(names);
        List list = CollectionUtils.makeArrayList(iter);
        Logger.info("List:\n"+StringUtils.collectionToString(list));                     
    }
   

}



