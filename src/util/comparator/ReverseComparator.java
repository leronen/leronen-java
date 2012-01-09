package util.comparator;

import util.*;
import util.dbg.*;

import java.util.*;

public class ReverseComparator implements Comparator {

    /** may be null, in which case uses the reversed natural ordering */
    private Comparator mOriginalComparator;

    /** Reverse the ordering provided by mOriginalComparator */
    public ReverseComparator(Comparator pOriginalComparator) {
        mOriginalComparator = pOriginalComparator;    
    }
    
    /** Reverse the natural ordering of objects  */
    public ReverseComparator() {
        mOriginalComparator = null;    
    }
    
    public int compare(Object pObj1, Object pObj2) {
        if (mOriginalComparator!=null) {
            // reverse ordering provided by mOriginalComparator
            return -mOriginalComparator.compare(pObj1, pObj2);
        }
        else {
            // reverse natural ordering 
            return -((Comparable)pObj1).compareTo(pObj2);
        }        
    }
    
    public static final String[] TESTDATA = {"foo", "bar", "reduction", "asap", "mcmchchc"};
    
    public static void main (String[] args) {
        Arrays.sort(TESTDATA);
        dbgMsg("sorted:\n"+StringUtils.arrayToString(TESTDATA, "\n"));
        Arrays.sort(TESTDATA, new ReverseComparator());
        dbgMsg("reverse sorted:\n"+StringUtils.arrayToString(TESTDATA, "\n"));
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("ReverseComparator: "+pMsg);
    }
    
    
    
    
    
    
}
