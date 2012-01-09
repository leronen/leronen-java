package util.collections.iterator;

import util.dbg.*;

import java.util.*;



/** 
 * Iterator with amazing gift to remember the last iterated entry and simulate pushing it back!
 * 
 * Sad note: currently only supports one push-back.  
 * Sad note2: does not have ability to handle removals.
 */
public class PushBackIterator<T> implements Iterator<T> {
                        
    private Iterator<T> mBaseIterator;
    
    private T mLast;
    private T mPushedBack;
    
    private int mIndexOfNext = 0;
    
    public PushBackIterator(Iterator<T> pBaseIterator) {
        mBaseIterator = pBaseIterator;            
    }
    
    public boolean hasNext() {
        return mPushedBack != null || mBaseIterator.hasNext();    
    }
            
    /** Can only call peek, if there are elements left! */
    public T peek() {
        T obj = next();
        pushBack();
        return obj;
    }
    
    public T next() {
        mIndexOfNext++;
        if (mPushedBack != null) {
            // we have one object pushed back; return it!
            T retVal = mPushedBack;
            mPushedBack = null;
            // memorize the object again, in case it is pushed back again
            mLast = retVal;
            
            return retVal;
        }
        else {
            // no object pushed back; return and remember the next entry from mBaseIterator
            mLast = mBaseIterator.next();
            return mLast;
        }                        
    }            
    
    public int getIndexOfLast() {
        return mIndexOfNext-1;
    }
    
    public int getIndexOfNext() {
        return mIndexOfNext;
    }
    
    public void pushBack() {
        if (mLast == null) {
            throw new RuntimeException("Cannot push back more that one object!");
        }
        // Push back the last returned object; it will be returned again in the next call to next()
        // note that mLast is set to null -> no further pushbacks are possible, as the "last" buffer only holds one element
        mPushedBack = mLast;
        mLast = null;
        mIndexOfNext--;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

    
    public static void main (String[] args) {
        String[] objs = {"A", "B", "C"};
        Iterator<String> baseIter = Arrays.asList(objs).iterator();
        PushBackIterator<String> pushBackIter = new PushBackIterator<String>(baseIter);
        
        HashSet<String> objsToPushBack = new HashSet<String>(Arrays.asList(new String[]{"B", "C"}));
        while (pushBackIter.hasNext()) {
            String obj = pushBackIter.next();
            if (objsToPushBack.contains(obj)) {
                objsToPushBack.remove(obj);
                pushBackIter.pushBack();
            }
            dbgMsg(""+obj);
        }
    }
    
    private static void dbgMsg(String pMsg){
        Logger.dbg("PushBackIterator"+pMsg);
    }
    
    
    
}    


