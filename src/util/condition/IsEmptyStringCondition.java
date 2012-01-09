package util.condition;

public class IsEmptyStringCondition implements Condition {                                
            
    public boolean fulfills(Object pObj) {
        String s = (String)pObj;
        if (s.equals("")) {
            return true;
        }
        else {
            return false;
        }            
    }        
}
