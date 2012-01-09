package util.matrix;

import util.converter.*;
import util.condition.*;

import java.util.*;

/** Condition that tests whether the object, as converter by a converter, matches the given condition */
public class ColumnCondition extends ConvertedObjectCondition
                             implements ListCondition {    
    
    public ColumnCondition(int pCol, Condition pBaseCondition) {
        super(new ListFieldExtractor(pCol), pBaseCondition);
    }
    
    public boolean fulfills(List pObj) {
        return fulfills((Object)pObj);    
    }                        
    
}
