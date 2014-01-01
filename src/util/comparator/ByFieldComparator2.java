package util.comparator;

import java.util.*;

import util.StringUtils;
import util.collections.Pair;
import util.converter.Converter;

/**
 * New, supposedly better reincarnation of ByFieldComparator.
 * 
 * Comparator, which orders objects by comparing one specific field of the objects, instead of the objects 
 * themselves. The field is specified by an helper object that extracts the ordering field from the actual objects,
 * or by implementing a subclass that overrides method extractField().
 * 
 * Note that an default implementation is provided for extractField(), so that it does not need to be implemented if a comparator is provided.
 * This implementation, however, throws a {@link RuntimeException}.
 * 
 * Type parameters: T - type of compared objects; F - type of compared field.
 * 
 * Keywords: ConverterComparator, ConvertedObjectComparator
 */
public class ByFieldComparator2<T, F extends Comparable<F>> implements Comparator<T> {
                           
    /** may be null, in which case uses the natural ordering of the field */
    private Comparator<F> mBaseComparator;
    
    /** extract the field of intrest, on which the ordering is based on. If this is null,
     * uses the method extractField to get the field of intrest. Note that in this 
     * case the extractField-method must be implemented in subclass!    
     */
    private Converter<T,F> mFieldExtractor;

    /** The ordering imposed by pBaseComparator provided onto the domain mapped by pFieldExtractor */
    public ByFieldComparator2(Converter<T,F> pFieldExtractor, Comparator<F> pBaseComparator) {
        mFieldExtractor = pFieldExtractor;
        mBaseComparator = pBaseComparator;
    }    
    
    /** The natural ordering provided  by field specified by pFieldExtractor */
    public ByFieldComparator2(Converter<T,F> pFieldExtractor) {
        this(pFieldExtractor, null);
    }

    /**
     * The ordering imposed by pBaseComparator on extracted by method extractField.
     * (ExtractField must be implemeted in subclass!) 
     */
    protected ByFieldComparator2(Comparator<F> pBaseComparator) {
        this(null, pBaseComparator);
    }

   /**
    *The natural ordering of the field extracted by method extractField.
     * (ExtractField must be implemeted in subclass!) 
     */
    protected ByFieldComparator2() {
        this(null, null);
    }        
                        
    protected F extractField(T pObj) {
        throw new RuntimeException("Operation not implemented!");
    }                    
                    
        
    public int compare(T pObj1, T pObj2) {
        F field1;
        F field2;
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
            // try natural ordering
            return ((Comparable<F>)field1).compareTo(field2);
        }        
    }
    
    
    public static final List<Pair<String,Integer>> TESTDATA = 
            new ArrayList<Pair<String,Integer>>();
    
    static {
        TESTDATA.add(new Pair<String,Integer>("foo", new Integer(1))); 
        TESTDATA.add(new Pair<String,Integer>("bar", new Integer(3)));
        TESTDATA.add(new Pair<String,Integer>("reduction", new Integer(2)));
        TESTDATA.add(new Pair<String,Integer>("asap", new Integer(5)));
        TESTDATA.add(new Pair<String,Integer>("mcmchchc", new Integer(4)));
    }
   
           
    public static void main (String[] args) {       
        Converter<Pair<String,Integer>, Integer> secondElementOfPairFieldExtractor = 
            new Converter<Pair<String,Integer>, Integer>() {
            public Integer convert(Pair<String,Integer> pair) {
                return pair.getObj2();                    
            }
        };
        ByFieldComparator2<Pair<String,Integer>,Integer> naturalOrdercomparator = 
            new ByFieldComparator2<Pair<String,Integer>,Integer>(secondElementOfPairFieldExtractor);               
        dbgMsg("original:\n"+StringUtils.colToStr(TESTDATA, "\n"));
        Collections.sort(TESTDATA, naturalOrdercomparator);        
        dbgMsg("ordered:\n"+StringUtils.colToStr(TESTDATA, "\n"));        
    }
    
    private static void dbgMsg(String pMsg) {
        System.err.println("ByFieldComparator2: "+pMsg);
    }

    
    
    
    
    
}

