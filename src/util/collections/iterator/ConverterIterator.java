package util.collections.iterator;

import util.*;
import util.converter.*;
import util.dbg.*;

import java.util.*;

public class ConverterIterator <T1, T2> implements Iterator<T2> {

    private Iterator<T1> mBaseIterator;
    private Converter<T1, T2> mConverter;        
    
    public ConverterIterator(Iterator<T1> pBaseIterator,
                             Converter<T1, T2> pConverter) {
        mBaseIterator = pBaseIterator;
        mConverter = pConverter;        
    }        
        
    public boolean hasNext() {
        return mBaseIterator.hasNext();
    }

    public T2 next() {
        return mConverter.convert(mBaseIterator.next());                
    }
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }

    public static void main (String[] args) {
        String[] names = new String[] {"AAA",  "b", "1","ABASa" ,"2", "3", "Bb", "pitka", "4", "5", "asfwefwerq", "6", "bika", "vika"};
        Converter converter = new ToLowerCaseConverter();
        Iterator iter = new ConverterIterator(Arrays.asList(names).iterator(), converter);
        List convertedNames = CollectionUtils.makeArrayList(iter);
        Logger.info("Converted objects:\n"+StringUtils.collectionToString(convertedNames));                     
    }
   

}



