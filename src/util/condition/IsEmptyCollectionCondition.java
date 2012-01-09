package util.condition;

import java.util.*;

public class IsEmptyCollectionCondition implements Condition {                                
            
    public boolean fulfills(Object pObj) {
        Collection c = (Collection)pObj;
        return c.size() == 0;                        
    }        
}
