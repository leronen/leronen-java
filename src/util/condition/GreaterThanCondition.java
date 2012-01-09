package util.condition;

/** 
 * Checks for tha Number given as parameter to #fulfills, whether it is greater that the 
 * number stored in the condition
 */ 
public class GreaterThanCondition implements Condition {                                
    
    private double mDouble;
    
    public GreaterThanCondition(double pVal) {
        mDouble = pVal;        
    }
        
    public boolean fulfills(Object pObj) {        
        Number number = (Number)pObj;
        double val = number.doubleValue();
        return val > mDouble;            
    }        
}
