package util.collections.iterator;

import java.util.*;

import util.IOUtils;
import util.converter.Converter;
import util.converter.ConverterChain;
import util.converter.ListFieldExtractor;
import util.converter.StringToListConverter;

/** 
 * As ChunkwiseIterator, but instead of fixed chunk size, puts into the
 * same chunk objects with same value of a certain key field, specified by 
 * a Converter.
 */
public class ChunkwiseIterator2<T> implements Iterator<List<T>> {
   	 
	//int mNodeCount;
	PushBackIterator<T> mBaseIterator;
    Converter mKeyExtractor;
	List<T> mCurrentChunk;
   	// int mChunkSize;
   	 
   	public ChunkwiseIterator2(Iterator pBaseIterator,
                              Converter pKeyExtractor) {
   	    mBaseIterator = new PushBackIterator(pBaseIterator);
        mKeyExtractor = pKeyExtractor;
   	    extractNextChunk();   	     
   	}
   	 
   	private void extractNextChunk() {
   		// Logger.info("extractNextChunk");        
        
   		if (mBaseIterator.hasNext()) {
            T keyForThisChunk = (T)mKeyExtractor.convert(mBaseIterator.peek());   			
   			mCurrentChunk = new ArrayList();
   			while (mBaseIterator.hasNext() && mKeyExtractor.convert(mBaseIterator.peek()).equals(keyForThisChunk)) { 
   				T o = mBaseIterator.next();   				
   				mCurrentChunk.add(o);   			
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
   	
   	public static void main(String args[]) { 
   		Iterator<String> lineIter = IOUtils.lineIterator(System.in);
        Converter keyExtractor = new ConverterChain(
                new StringToListConverter(),
                new ListFieldExtractor(0));                
   		ChunkwiseIterator2<String> chunkwiseIter = new ChunkwiseIterator2(lineIter, keyExtractor); 
        
   		while (chunkwiseIter.hasNext()) {
   			List<String> list = chunkwiseIter.next();
   			System.err.println(""+list);
   		}
   		
   	}
   	                
}
    