package util.collections.iterator;

import util.*;
import java.util.*;

/**
 * Samples a number of objects WITHOUT replacement. Naturally,
 * the number of objects to be iterated has to be known beforehand,
 * as an iterator can only be iterated once. 
 */
public class SamplingIterator<T> implements Iterator<T> {

    private Iterator<T> mBaseIter;    
    private int mSampleIndex;
    private int mIterIndex; 
    private List<Integer> mSampledIndices;
    
    public SamplingIterator(Iterator<T> pBaseIter,
    					    int pIterSize,
                            int pNumToSample) {
    	if (pIterSize < pNumToSample) {
    		throw new RuntimeException("pIterSize < pNumToSample!");
    	}
    	    	    
        mBaseIter = pBaseIter;
        mSampledIndices = RandUtils.sampleIndices(pIterSize, pNumToSample);
        mSampleIndex = 0;
        mIterIndex = 0;
        
        // Logger.info("State at the beginning:\n"+this);
        // mNumToSample = pNumToSample;
        // findNextNode();
    }        
        
    public boolean hasNext() {
        return mSampleIndex < mSampledIndices.size();
    }

    public String toString() {
    	return "mSampledIndices: "+mSampledIndices+"\n"+
    	       "sampleIndex="+mSampleIndex+"\n"+
    	       "iterIndex="+mIterIndex;
    }
    
    public T next() {
        if (!(hasNext())) {        	
            throw new NoSuchElementException();
        }
        
        // Logger.info("Before while:\n"+this);
        
        while(mIterIndex < mSampledIndices.get(mSampleIndex)) {        	
        	mBaseIter.next();
        	mIterIndex++;
        }
        // Logger.info("After while:\n"+this);
        
        if (mSampledIndices.get(mSampleIndex) != mIterIndex) {
        	throw new RuntimeException("WTF?! sampleIndex="+mSampleIndex+", iterIndex="+mIterIndex);
        }
        
        mSampleIndex++;
        mIterIndex++;
        return mBaseIter.next();        
    }
                                       
    public void remove() {
        throw new UnsupportedOperationException();    
    }

    public static void main (String[] args) {
    	int numLines = Integer.parseInt(args[0]);
    	int numToSample = Integer.parseInt(args[1]);
    	// List<Integer> items = new Range(1, numItems+1).asList();
        
    	// SamplingIterator iter = new SamplingIterator(items.iterator(), items.size(), numToSample);
        SamplingIterator iter = new SamplingIterator(IOUtils.lineIterator(System.in), numLines, numToSample);
        
    	while (iter.hasNext()) {
    		System.out.println(iter.next());
    	}
    	// System.out.println(items);
    	
//        String[] names = new String[] {"a",  "b", "1","aa" ,"2", "3", "bb", "pitka", "4", "5", "asfwefwerq", "6", "bika", "vika"};
//        Condition condition = new IsIntegerCondition();
//        Iterator iter = new ConditionalIterator(Arrays.asList(names).iterator(), condition);
//        List matchingObjects = CollectionUtils.makeList(iter);
//        Logger.info("Matching objects:\n"+StringUtils.collectionToString(matchingObjects));                     
    }
   

}



