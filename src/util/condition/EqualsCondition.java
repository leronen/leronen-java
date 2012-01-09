package util.condition;

public class EqualsCondition implements Condition {                                
    
    private Object mObj;
    
    public EqualsCondition(Object pObj) {
        mObj = pObj;
    }
        
    public boolean fulfills(Object pObj) {        
        return (mObj.equals(pObj));            
    }        
}
