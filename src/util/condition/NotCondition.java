package util.condition;

public class NotCondition<T> implements Condition<T> {
    private Condition<T> mCondition;
    
    public NotCondition(Condition<T> pCondition) {
        mCondition = pCondition;
    }
    
    public boolean fulfills(T pObj) {
        return !mCondition.fulfills(pObj);    
    }        
}
