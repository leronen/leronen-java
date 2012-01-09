package util.collections.iterator;

import util.*;
import util.condition.*;
import util.dbg.*;

import java.util.*;


public class ConditionalIterator<T> implements Iterator<T> {

    private Iterator<T> mBaseIterator;
    private Condition<T> mCondition;

    private int mMatchingCount;
    private int mNonMatchingCount;
    
    private T mNextNode;
    
    public ConditionalIterator(Iterator<T> pBaseIterator,
                               Condition<T> pCondition) {
        mBaseIterator = pBaseIterator;
        mCondition = pCondition;
        mMatchingCount = 0;
        mNonMatchingCount = 0;
        findNextNode();
    }        
        
    public boolean hasNext() {
        return mNextNode != null;
    }

    public T next() {
        if (mNextNode == null) {
            throw new NoSuchElementException();
        }
        T result = mNextNode;
        findNextNode();
        mMatchingCount++;
        return result;        
    }

    private void findNextNode() {
        T result = null;        
        while (mBaseIterator.hasNext() && result == null) {
            T candidate = mBaseIterator.next();
            if (mCondition.fulfills(candidate)) {
                result = candidate;
            }
            else {
                mNonMatchingCount++;
            }
        }
        mNextNode = result;
    }                                    

    /** Return the number of returned (matching) nodes so far */
    public int getNumMatching() {
        return mMatchingCount;
    }
    
    /** Return the number of skipped (non-matching) nodes so far */ 
    public int getNumNonMatching() {
        return mNonMatchingCount;
    }
    
    public String getCountsString() {
        return "num matching: "+mMatchingCount+"\n"+
               "num non-matching: "+mNonMatchingCount;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }

    public static void main (String[] args) {
        String[] names = new String[] {"a",  "b", "1","aa" ,"2", "3", "bb", "pitka", "4", "5", "asfwefwerq", "6", "bika", "vika"};
        Condition condition = new IsIntegerCondition();
        ConditionalIterator iter = new ConditionalIterator(Arrays.asList(names).iterator(), condition);
        List matchingObjects = CollectionUtils.makeArrayList(iter);
        Logger.info("Number of objects:\n"+names.length);
        Logger.info("Counts:\n"+iter.getCountsString());
        Logger.info("Matching objects:\n"+StringUtils.collectionToString(matchingObjects));                     
    }
   

}



