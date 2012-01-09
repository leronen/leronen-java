package util.condition;

import util.*;
import java.util.*;

/** Condition that tests whether an Object represents an number, be it a string or a Number instance... */
public class IsNumericCondition implements Condition {
            
    public boolean fulfills(Object p) {
        return Utils.isNumeric(p);
    }                    
    
    public static void main(String[] args) {
        String[] a = new String[]{"1","2.0","3,0"};
        List l = Arrays.asList(a);
        List numeric = (List)CollectionUtils.extractMatchingObjects(l, new IsNumericCondition());
        System.out.println("Numeric values:\n"+StringUtils.listToString(numeric));
    }
}
