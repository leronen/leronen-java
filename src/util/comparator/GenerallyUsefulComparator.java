package util.comparator;

import util.*;
import java.util.*;
 
/** 
 * As natural order comparator, but compares Strings interpretable as numbers based on the numeric value. 
 * Note that this may(?) work erranously, if there are both numbers and non-numbers in the data.
 * Note also lurking ineffectiveness.
 *
 * Todo: name of class seemt to be arbitrary.
 */ 
public class GenerallyUsefulComparator implements Comparator {
        
    public int compare(Object p1, Object p2) {
        return Utils.possiblyNumericCompare(p1, p2);
    }
    /*
    try {            
            double d1 = ConversionUtils.anyToDouble(p1);
            double d2 = ConversionUtils.anyToDouble(p2);
            // OK, succeeded in converting, so let's compare as numbers
            return MathUtils.sign(d1-d2);
        }
        catch (NumberFormatException e) {
            // resort to natural ordering
            return ((Comparable)p1).compareTo((Comparable)p2);        
        }                        
    }
*/                       
    
}
