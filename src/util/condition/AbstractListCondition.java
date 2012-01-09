package util.condition;

import java.util.*;

public abstract class AbstractListCondition implements ListCondition {    
    
    public boolean fulfills(Object pObj) {
        return fulfills((List)pObj);
    }        
    
} 

