package util.collections.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import util.IOUtils;
import util.StringUtils;
import util.comparator.ByFieldComparator;
import util.comparator.NaturalOrderComparator;
import util.converter.CollectionToLinkedHashSetConverter;
import util.converter.Converter;
import util.converter.StringFieldExtractor;

public class IteratorUtils {
    
    
   /** 
    * Removes duplicates from a base iterator. Assumes elements are sorted by 
    * (natural order of) some key, and we only have to keep elements of one key in memory
    * at once. Actually, the implementation is quite involved.
    * 
    * Note that the orderedness of elements is checked and asserted, 
    * so watch out for stray RuntimeExceptions...
    */
    public static <T> Iterator<T> duplicateRemovingIterator(Iterator<T> pIter,
                                                            Converter<T, ? extends Object> pKeyExtractor) {
        Iterator<T> orderCheckingIter = new OrderCheckingIterator<T>(pIter, new ByFieldComparator<T>(pKeyExtractor, new NaturalOrderComparator()));
        Iterator<List<T>> listIter = new ChunkwiseIterator2<T>(orderCheckingIter, pKeyExtractor);
        Iterator<Set<T>> setIter = new ConverterIterator(listIter, new CollectionToLinkedHashSetConverter());
        Iterator<T> result = new CompositeIterator2<T>(setIter);        
        return result;
    }
    
    public static void main(String[] args) {
        // just testing; use first char as key
        Converter<String, String> keyExtractor = new StringFieldExtractor(0);
        Iterator<String> drIter = duplicateRemovingIterator(IOUtils.lineIterator(System.in), keyExtractor);
        System.out.println(StringUtils.iteratorToString(drIter));
        
    }
    
}
