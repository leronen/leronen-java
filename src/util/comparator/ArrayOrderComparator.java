package util.comparator;

import util.*;
import util.dbg.*;

import java.util.*;

/**
 * Compares objects by the ordering specified by the array given as parameter.
 */
public class ArrayOrderComparator implements Comparator {
    
    private HashMap mPositionByObject;
    
    
    public ArrayOrderComparator(Object[] pArr) {
        mPositionByObject = new HashMap();
        for (int i=0; i<pArr.length; i++) {
            mPositionByObject.put(pArr[i], new Integer(i));    
        }        
    }
    
    public ArrayOrderComparator(List pList) {
        mPositionByObject = new HashMap();
        for (int i=0; i<pList.size(); i++) {
            mPositionByObject.put(pList.get(i), new Integer(i));    
        }        
    }
        
            
    public int compare(Object pObj1, Object pObj2) {
        Integer pos1 = (Integer)mPositionByObject.get(pObj1);
        Integer pos2 = (Integer)mPositionByObject.get(pObj2);
        if (pos1 != null & pos2 != null) {
            // both objects are specified in the list
            return pos1.compareTo(pos2);
        }
        if (pos1 != null) {
            // only obj1 is specified in the list; that means it comes first in the result (~is considered "smaller");
            return -1;
        }
        else if (pos2 != null) {
            // only obj2 is specified in the list; that means it comes first in the result (~is considered "smaller");
            return 1;
        }
        else {
            // neither object is specified in the list => resort to natural ordering
            return ((Comparable)pObj1).compareTo(pObj2);   
        }            
    }    
        
    public static void main (String[] args) {
        String[] testData = {"A", "B", "C", "D"};
        String[] ordering = {"B","D","A","C"};
        ArrayOrderComparator comparator = new ArrayOrderComparator(ordering);        
        dbgMsg("original:\n"+StringUtils.arrayToString(testData));
        dbgMsg("ordering:\n"+StringUtils.arrayToString(ordering));
        Arrays.sort(testData, comparator);
        dbgMsg("sorted:\n"+StringUtils.arrayToString(testData));                        
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("ArrayOrderComparator: "+pMsg);
    }
    
}
