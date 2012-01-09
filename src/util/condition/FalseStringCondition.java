package util.condition;

public class FalseStringCondition implements Condition<String> {
    private Condition mCondition;
        
    public boolean fulfills(String pObj) {
        return !mCondition.fulfills(pObj);    
    }        
}
