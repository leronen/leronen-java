package util.condition;

public class IsNullCondition implements Condition {                                
            
    public boolean fulfills(Object pObj) {
        return (pObj == null);                                
    }        
}
