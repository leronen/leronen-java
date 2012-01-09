package util.condition;

import java.util.Set;

/** At least for now, we shall allow modification of the set after the condition has been created. */
public class ContainsCondition<T> implements Condition<T> {                                
    
    private Set<T> mSet;
    
    public ContainsCondition(Set<T> pSet) {
        mSet = pSet;
    }
        
    public boolean fulfills(T pObj) {        
        return mSet.contains(pObj);            
    }        
}
