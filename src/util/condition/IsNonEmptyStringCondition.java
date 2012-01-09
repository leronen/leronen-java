package util.condition;

public class IsNonEmptyStringCondition implements Condition {                                
            
    public boolean fulfills(Object pObj) {
        String s = (String)pObj;
        if (s.equals("")) {
            return false;
        }
        else {
            return true;
        }            
    }        
}
