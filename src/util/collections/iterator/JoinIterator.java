package util.collections.iterator;

import java.util.*;

import util.IOUtils;
import util.IteratorIterable;
import util.collections.Pair;
import util.comparator.ByFieldComparator;
import util.comparator.NaturalOrderComparator;
import util.converter.Converter;
import util.converter.StringFieldExtractor;


/**
 * "Joins" two iterators, much like in relational db joins.
 * 
 * Always consider the first iterator as "primary", that is, each element of
 * the first iterator shall be iterated, and will map to 0-n elements of
 * the second iterator.
 * 
 * Crashes with a RuntimeException, if iterators are not ordered.
 */
public class JoinIterator<T1,T2> implements Iterator<Pair<T1, List<T2>>> {
    
    private Converter mConverter1;
    private Converter mConverter2;
    private Iterator<T1> mIterator1;
    private PushBackIterator<List<T2>>  mIterator2;
    private Comparator mComparator;
    
    public JoinIterator(Iterator<T1> pIter1, 
                        Iterator<T2> pIter2,
                        Converter pConverter1,
                        Converter pConverter2,
                        Comparator pComparator) {        
        mIterator1 = new OrderCheckingIterator(pIter1, new ByFieldComparator(pConverter1, pComparator));
        mIterator2 =
                new PushBackIterator(
                    new ChunkwiseIterator2(
                        new OrderCheckingIterator(pIter2, new ByFieldComparator(pConverter2, pComparator)),
                                                  pConverter2));
        mConverter1 = pConverter1;
        mConverter2 = pConverter2;
        
        mComparator = pComparator;        
    }

    
    public boolean hasNext() {
        return mIterator1.hasNext();
    }
    
    public Pair<T1, List<T2>> next() {                      
                
        List<T2> list2 = Collections.EMPTY_LIST; // no attributes (yet?)
                
        T1 o1 = mIterator1.next();
        Object key1 = mConverter1.convert(o1);
        
        if (mIterator2.hasNext()) {
            List<T2> list2Candidate = mIterator2.peek();
            Object key2 = mConverter2.convert(list2Candidate.get(0));
            
            while (mComparator.compare(key1, key2) > 0) {
                // pass on by unused values of key2
                mIterator2.next();
                list2Candidate = mIterator2.peek();
                key2 = mConverter2.convert(list2Candidate.get(0));
            }
                                   
            if (key1.equals(key2)) {
                list2 = list2Candidate;
                mIterator2.next();
            }
            else {
                // no match found for key1; let's use the empty list initialized earlier
            }
        }
        
        return new Pair(o1, list2);                        
    }
                        
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }    
    
    public static void main(String[] args) throws Exception {
        Iterator<String> iter1 = IOUtils.lineIterator(args[0]);
        Iterator<String> iter2 = IOUtils.lineIterator(args[1]);
        JoinIterator<String, String> iter = 
            new JoinIterator(iter1, 
                             iter2, 
                             new StringFieldExtractor(0),
                             new StringFieldExtractor(0),
                             new NaturalOrderComparator());
        for (Pair<String, List<String>> elem: new IteratorIterable<Pair<String, List<String>>>(iter)) {
            String o1 = elem.getObj1();        
            List<String> list2 = elem.getObj2(); 
            if (list2.size() == 0) {
                System.out.println(o1+" null");
            }
            else {
                for (String o2: list2) {                
                    System.out.println(o1+" "+o2);
                }
            }
        }
            
        
    }

}
