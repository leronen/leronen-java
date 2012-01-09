package util.condition;

import java.util.*;

public class AreAllObjectsEqualCondition implements Condition {                                
                
    public boolean fulfills(Object pObj) {
        Collection col = (Collection)pObj;       
        if (col.size() == 0) {
            // OK, let's decide that in this special case the condition is true...
            return true;
        }
        else {
            Iterator i = col.iterator();
            Object first = i.next();
            while (i.hasNext()) {
                if (!(first.equals(i.next()))) {
                    return false;
                }
            }
            // all objects passed the test of equality...
            return true;
        }        
    }        
}
