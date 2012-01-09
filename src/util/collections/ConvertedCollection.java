package util.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import util.collections.iterator.ConverterIterator;
import util.converter.Converter;

public class ConvertedCollection<T1,T2> extends AbstractCollection<T2> {
    
    private Collection<T1> mOriginalCollection;
    private Converter<T1, T2> mConverter;
    
    public ConvertedCollection(Collection<T1> pOriginalCollection,
                               Converter<T1,T2> pConverter) {
        mOriginalCollection = pOriginalCollection;
        mConverter = pConverter;        
    }
    
    public Iterator<T2> iterator() {
        return new ConverterIterator<T1, T2>(mOriginalCollection.iterator(),
                                             mConverter);
    }
    
    public int size() {
        return mOriginalCollection.size();
    }
    
    
    

}
