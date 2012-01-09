package util.collections.iterator;

import java.util.*;


import util.IOUtils;

/** Iterates the the base iterator, returning its elements in chunks of suitable size */
public class ChunkwiseIterator<T> implements Iterator<List<T>> {
   	 
	//int mNodeCount;
	Iterator<T> mBaseIterator;
	List<T> mCurrentChunk;
   	int mChunkSize;
   	 
   	public ChunkwiseIterator(Iterator<T> pBaseIterator,
   		                      int pChunkSize) {
   		mBaseIterator = pBaseIterator;
        mChunkSize = pChunkSize;
   	    extractNextChunk();   	     
   	}
    
    public ChunkwiseIterator(Iterable<T> pIterable,
                             int pChunkSize) {
        this(pIterable.iterator(), pChunkSize);
    }
        
   	 
   	private void extractNextChunk() {
   		// Logger.info("extractNextChunk");
   		if (mBaseIterator.hasNext()) {
   			int count = 0;   			
   			mCurrentChunk = new ArrayList(mChunkSize);
   			while (mBaseIterator.hasNext() && count < mChunkSize) {
   				T o = mBaseIterator.next();
   				// Logger.info("Adding "+o+" to current chunk");
   				mCurrentChunk.add(o);
   				count++;
   				// Logger.info("Count is now: "+count);
   			}   			
   		}
   		else {
   			mCurrentChunk = null;
   		}
   	}
   	
   	public List<T> next() {
   		// Logger.info("next");
   		if (mCurrentChunk!= null) {
   			List<T> result = mCurrentChunk;
   			extractNextChunk();
   			return result;   		 	
   		}    		 
   		else {
   			throw new NoSuchElementException();
   		}
   	}
   	 
   	public boolean hasNext() {
   		return mCurrentChunk != null;
   	}
   	 
   	public void remove() {
   	    throw new UnsupportedOperationException();
   	}

   	public static <T> Iterable<List<T>> iterable(Iterable<T> pIterable,
            int pChunkSize) {
   	    return new util.IteratorIterable(new ChunkwiseIterator(pIterable, pChunkSize));
   	}
   	
   	public static void main(String args[]) {
   		int chunkSize = Integer.parseInt(args[0]); 
   		Iterator<String> lineIter = IOUtils.lineIterator(System.in);
   		ChunkwiseIterator<String> chunkwiseIter = new ChunkwiseIterator(lineIter, chunkSize);
   		while (chunkwiseIter.hasNext()) {
   			List<String> list = chunkwiseIter.next();
   			System.err.println(""+list);
   		}
   		
   	}
   	                
}
    