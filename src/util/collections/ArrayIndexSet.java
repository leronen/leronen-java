package util.collections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import util.CollectionUtils;
import util.StringUtils;

public class ArrayIndexSet extends AbstractSet<Integer> {
    
    private int mLen;
    
    public ArrayIndexSet(int pLen) {
        mLen = pLen;
    }       
    
    public int size() {
        return mLen;
    }
    
    public boolean contains(Integer pVal) {
        return pVal >= 0 && pVal < mLen; 
    }
    
    public Iterator<Integer> iterator() {
        return new IndexIterator();
    }
    
    private class IndexIterator implements Iterator<Integer> {        
        
        int mInd;
        
        private IndexIterator() {
            mInd = 0;    
        }
        
        public boolean hasNext() {
            return mInd < mLen;    
        }    
            
        public Integer next() {
            if (mInd <= mLen) {
                Integer result = mInd;
                mInd++;
                return result;
            }
            else {
                throw new NoSuchElementException();
            }
        }
    
        public void remove() {
            throw new UnsupportedOperationException();
        }                        
        
    }

    public static void main(String[] args) {
        int len = Integer.parseInt(args[0]);
        
        Set<Integer> set = new ArrayIndexSet(len);
        
        System.out.println(StringUtils.collectionToString(set, ", "));
                
        List<Integer> testData = CollectionUtils.makeList(-1,0,1,5,10,100);
        for (int val: testData) {
            boolean contains = set.contains(val);
            System.out.println("Contains "+val+": "+contains);          
        }
    }
    
}
