package util.collections.iterator;

import java.util.Iterator;

import util.IOUtils;
import util.Utils;
import util.collections.Pair;

public class NumberingIterator<T> implements Iterator<Pair<T,Integer>>{

    protected Iterator<T> mBaseIterator;
        
    private int mNextInd;
    
    public NumberingIterator(Iterator<T> pBaseIterator,
                             int pStartingIndex) {
        mBaseIterator = pBaseIterator;
        mNextInd = pStartingIndex;
    }

    public NumberingIterator(Iterator<T> pBaseIterator) {                             
        this(pBaseIterator, 0);
    }
    
    public Pair<T,Integer> next() {
        return new Pair(mBaseIterator.next(), mNextInd++);
    }
    
    public boolean hasNext() {
        return mBaseIterator.hasNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public static void main(String[] args) throws Exception {        
        Utils.logIterator(new NumberingIterator(IOUtils.lineIterator()));
        
    }
}
