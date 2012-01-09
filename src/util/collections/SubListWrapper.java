package util.collections;

import util.*;

import java.util.*;

public class SubListWrapper extends AbstractList {
    
    private List mSrcList;
    private int mStart;
    private int mEnd;    
    
    public SubListWrapper(List pSrcList, int pStart, int pEnd) {
        mSrcList = pSrcList;
        mStart = pStart;
        mEnd = pEnd;
    }

    public SubListWrapper(List pSrcList, Range pRange) {
        mSrcList = pSrcList;
        mStart = pRange.start;
        mEnd = pRange.end;
    }                
    
    public int size() {
        return mEnd-mStart;        
    }
        
    public Object get(int pInd) {
        return mSrcList.get(mStart+pInd);                    
    }        
    
    public Object set(int pInd, Object pObj) {
        return mSrcList.set(mStart+pInd, pObj);                         
    }                                        

    public Iterator iterator () {
        return new OurIterator();
    }
    
    private class OurIterator implements Iterator {
        int mInd;
        
        private OurIterator() {
            mInd = mStart;    
        }
        
        public boolean hasNext() {
            return mInd < mEnd;    
        }    
            
        public Object next() {
            Object obj = get(mInd);
            mInd++;
            return obj;              
        }
    
        public void remove() {
            throw new UnsupportedOperationException();
        }                
    }
    
}
