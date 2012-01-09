package util.collections.iterator;

import java.util.*;

import util.IOUtils;
import util.IteratorIterable;
import util.StringUtils;
import util.dbg.Logger;

/**
 * Iterator only a given number of first elements from a base iterator.
 * If there are less elements, just iterates all elements. 
 */
public class LoggingIterator<T> implements Iterator<T>{

    private Iterator<T> mBaseIterator;
    private int mLogInterval;
    private int mNumIteratedElements;
    private ILogger mLogger;
    
    public LoggingIterator(int pLogInterval, Iterator<T> pBaseIterator) {
        mBaseIterator = pBaseIterator;
        mLogInterval= pLogInterval;
        mNumIteratedElements = 0;
        mLogger = new DefaultLogger();
    }
    
    public LoggingIterator(int pLogInterval, Iterator<T> pBaseIterator, ILogger pLogger) {
        mBaseIterator = pBaseIterator;
        mLogInterval= pLogInterval;
        mNumIteratedElements = 0;
        mLogger = pLogger;        
    }
    
    public T next() {
        mNumIteratedElements++;
        T result = mBaseIterator.next(); 
        if (mNumIteratedElements == 1 || mNumIteratedElements % mLogInterval == 0) {
            // Logger.info("Iterating "+StringUtils.formatOrdinal(mNumIteratedElements)+" object");
            mLogger.log(mNumIteratedElements);
        }
        return result;
    }
    
    public boolean hasNext() {
        return mBaseIterator.hasNext();
    }

    public void remove() {
        mBaseIterator.remove();    
    }
    
    public static void main(String[] args) {
        Iterator<String> lines = IOUtils.lineIterator(System.in);
        Iterator<String> loggingIter = new LoggingIterator<String>(3, lines);
        for (String line: new IteratorIterable<String>(loggingIter)) {
            System.err.println(line);
        }
    }
    
    private class DefaultLogger implements ILogger {
        public void log(int pIter) {
            Logger.info("Iterating "+StringUtils.formatOrdinal(pIter)+" object");
        }
    }
    
    public interface ILogger {
        public void log(int pIter);
    }
}
