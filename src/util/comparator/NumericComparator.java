package util.comparator;

import util.*;
import java.util.*;
 
public class NumericComparator implements Comparator {
    
    public int compare(Object p1, Object p2) {
        double d1 = ConversionUtils.anyToDouble(p1);
        double d2 = ConversionUtils.anyToDouble(p2);
        double diff = d1 - d2;
        return MathUtils.sign(diff);                                
    }                       
    
}
