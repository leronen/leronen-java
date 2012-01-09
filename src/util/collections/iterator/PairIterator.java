package util.collections.iterator;

import java.util.Iterator;

import util.collections.SymmetricPair;
import util.converter.ListToPairConverter;


/** Iterates a given iterator in pairs */
public class PairIterator <T> extends ConverterIterator<T, SymmetricPair<T>> { // mplements Iterator<SymmetricPair<T>> {
      
    public PairIterator(Iterator<T> pBaseIterator) {
        super(pBaseIterator, new ListToPairConverter());
    }

}
