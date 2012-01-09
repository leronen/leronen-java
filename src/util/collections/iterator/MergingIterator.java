package util.collections.iterator;

import java.util.*;

import util.IOUtils;
import util.IteratorIterable;
import util.comparator.NaturalOrderComparator;


/**
 * Iterates elements from two ordered iterators, merging them such 
 * that the result is ordered. In case of ties, always retuturns
 * elements from the first iterator first.
 * 
 * Crashes with a RuntimeException, if iterators are not ordered.
 */
public class MergingIterator<T> implements Iterator<T> {
    
    private PushBackIterator<T> mIterator1;
    private PushBackIterator<T> mIterator2;
    private Comparator<T> mComparator;
    
    public MergingIterator(Iterator<T> pFirst, 
                           Iterator<T> pSecond) {
        this(pFirst, pSecond, new NaturalOrderComparator());
    }
    
    public MergingIterator(Iterator<T> pFirst, 
                           Iterator<T> pSecond,
                           Comparator<T> pComparator) {
        mIterator1 = new PushBackIterator<T>(new OrderCheckingIterator<T>(pFirst, pComparator));
        mIterator2 = new PushBackIterator<T>(new OrderCheckingIterator<T>(pSecond, pComparator));        
        mComparator = pComparator;        
    }

    
    public boolean hasNext() {
        return mIterator1.hasNext() || mIterator2.hasNext();
    }
    
    public T next() {                      
        
        boolean hasNext1 = mIterator1.hasNext();
        boolean hasNext2 = mIterator2.hasNext();
        
        if (hasNext1 && hasNext2) {
            T o1 = mIterator1.peek();
            T o2 = mIterator2.peek();        
        
            if (mComparator.compare(o1,o2) <= 0) {
                return mIterator1.next();                       
            }
            else {
                return mIterator2.next();
            }
        }
        else if (hasNext1 && !hasNext2) {
            return mIterator1.next(); 
        }
        else if (!hasNext1 && hasNext2) {
            return mIterator2.next();
        }
        else {
            throw new NoSuchElementException();
        }
    }
                        
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }    
    
    public static void main(String[] args) throws Exception {
        Iterator<String> iter1 = IOUtils.lineIterator(args[0]);
        Iterator<String> iter2 = IOUtils.lineIterator(args[1]);
        MergingIterator<String> iter = new MergingIterator<String>(iter1, iter2);
        for (String elem: new IteratorIterable<String>(iter)) {
            System.out.println(elem);
        }
            
        
    }

}
