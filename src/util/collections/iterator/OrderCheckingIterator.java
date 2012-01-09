package util.collections.iterator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import util.CollectionUtils;
import util.IOUtils;
import util.IteratorIterable;
import util.StringUtils;
import util.comparator.ByFieldComparator;
import util.comparator.NaturalOrderComparator;
// import util.dbg.Logger;
import util.converter.StringFieldExtractor;


/**
 * Ensures elements of the iterated collection are ordered; 
 * throws RuntimeException otherwise (we might also allow for some 
 * error-handler callback...)
 *  
 */
public class OrderCheckingIterator<T> extends IteratorWrapper<T> {    
    T mPrev;
    Comparator<T> mComparator;
    
    public OrderCheckingIterator(Iterator<T> pBaseIterator,
                                 Comparator<T> pComparator) {
        super(pBaseIterator);
        mComparator = pComparator;
    } 
    
    public OrderCheckingIterator(Iterator<T> pBaseIterator) {
        this(pBaseIterator, new NaturalOrderComparator());
    }
    
    public T next() {
        T cur = super.next();
        
        if (mPrev != null) {             
            if (mComparator.compare(cur, mPrev) < 0) {
                throw new RuntimeException("The following itarated elements are not ordered: ("+mPrev+","+cur+")");
            }
            else {        
                // Logger.info("Ordered: ("+mPrev+","+cur+")");              
            }
        }
        
        mPrev = cur;
        
        return cur;
    }
    
    public static void main(String[] args) {
        OrderCheckingIterator<String> iter = 
            new OrderCheckingIterator<String>(IOUtils.lineIterator(System.in),
                                              new ByFieldComparator(new StringFieldExtractor(0)));
            
        for (String elem: new IteratorIterable<String>(iter)) {
            System.out.println(elem);
        }
        // test1(CollectionUtils.makeList(args));
        // test2(CollectionUtils.makeList(args));
    }
    
    public static void test1(Collection<String> pData) {
    
        OrderCheckingIterator<String> iter = 
            new OrderCheckingIterator<String>(pData.iterator(), new NaturalOrderComparator());
            
        for (String elem: new IteratorIterable<String>(iter)) {
            System.out.println(elem);
        }
    }
    
    public static void test2(Collection<String> pData) {
        List<String> unorderedElements = 
            CollectionUtils.extractUnorderedElements(pData.iterator(), new NaturalOrderComparator());
        System.out.println(StringUtils.collectionToString(unorderedElements));
    }
}
