package util.comparator;


import util.*;
import util.collections.*;
import util.converter.*;
import util.dbg.*;

import java.util.*;

/**
 * Comparator, which orders objects by comparing one specific field of the objects, instead of the objects 
 * themselves. The field is specified through an helper object that extracts the ordering field from the actual objects.
 * 
 * Keywords: ConverterComparator, ConvertedObjectComparator
 */
public class ByFieldComparator<T> implements Comparator<T> {
                           
    /** may be null, in which case uses the natural ordering of the field */
    private Comparator mBaseComparator;
    
    /** extract the field of intrest, on which the ordering is based on. If this is null,
     * uses the method extractField to get the field of intrest. Note that in this 
     * case the extractField-method must be implemented in subclass!    
     */
    private Converter mFieldExtractor;

    /** The ordering imposed by pBaseComparator provided onto the domain mapped by pFieldExtractor */
    public ByFieldComparator(Converter pFieldExtractor, Comparator pBaseComparator) {
        mFieldExtractor = pFieldExtractor;
        mBaseComparator = pBaseComparator;
    }    
    
    /** The natural ordering provided  by field specified by pFieldExtractor */
    public ByFieldComparator(Converter pFieldExtractor) {
        this(pFieldExtractor, null);
    }

    /**
     *The ordering imposed by pBaseComparator on extracted by method extractField.
     * (ExtractField must be implemeted in subclass!) 
     */
    public ByFieldComparator(Comparator pBaseComparator) {
        this(null, pBaseComparator);
    }

   /**
    * The natural ordering of the field extracted by method extractField.
     * (ExtractField must be implemented in subclass!) 
     */
    protected ByFieldComparator() {
        this(null, null);
    }        
                    
    public Object extractField(Object pObj) {
        throw new RuntimeException("Operation not implemented!");
    }                    
                    
    
    public int compare(Object pObj1, Object pObj2) {
        Object field1;
        Object field2;
        if (mFieldExtractor != null) {
            field1 = mFieldExtractor.convert(pObj1);
            field2 = mFieldExtractor.convert(pObj2);
        }
        else {
            // rely on implementation provided by subclass
            field1 = extractField(pObj1);
            field2 = extractField(pObj2);
        }        
        if (mBaseComparator!=null) {
            // ordering provided by mBaseComparator
            return mBaseComparator.compare(field1, field2);
        }
        else {
            // natural ordering
            return ((Comparable)field1).compareTo(field2);
        }        
    }
    
    
    public static final Pair[] TESTDATA = {
        new Pair("foo", new Integer(1)), 
        new Pair("bar", new Integer(3)), 
        new Pair("reduction", new Integer(2)),
        new Pair("asap", new Integer(5)),
        new Pair("mcmchchc", new Integer(4))
    };
    
    public static final Integer[] TESTORDERING = {
        new Integer(5), 
        new Integer(4), 
        new Integer(3),
        new Integer(2),
        new Integer(1)
    };
    
    public static void main (String[] args) {       
        Converter secondElementOfPairFieldExtractor = new Converter() {
            public Object convert(Object pObj) {
                return ((Pair)pObj).getObj2();                    
            }
        };
        ByFieldComparator naturalOrdercomparator = new ByFieldComparator(secondElementOfPairFieldExtractor);        
        ByFieldComparator arrayOrdercomparator = new ByFieldComparator(secondElementOfPairFieldExtractor, new ArrayOrderComparator(TESTORDERING));        
        dbgMsg("original:\n"+StringUtils.arrayToString(TESTDATA, "\n"));
        Arrays.sort(TESTDATA, naturalOrdercomparator);        
        dbgMsg("natural ordering:\n"+StringUtils.arrayToString(TESTDATA, "\n"));
        Arrays.sort(TESTDATA, arrayOrdercomparator);
        dbgMsg("array ordering:\n"+StringUtils.arrayToString(TESTDATA, "\n"));
        
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("ByFieldComparator: "+pMsg);
    }

    
    
    
    
    
}
