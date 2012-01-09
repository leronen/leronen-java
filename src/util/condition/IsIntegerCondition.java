package util.condition;

import java.util.regex.*;

/** Condition that tests whether string represents an integer */
public class IsIntegerCondition implements Condition {

    private static final Pattern PATTERN = Pattern.compile("^\\d+$");    
    
    public boolean fulfills(Object pObj) {
        if (pObj instanceof Integer) {
            return true;
        }
        else if (pObj instanceof String) {
            Matcher m = PATTERN.matcher((String)pObj); 
            return m.matches();    
        }
        else {
            return false;
        }            
    }        
    
}
