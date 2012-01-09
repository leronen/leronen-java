package util.condition;

public class OrCondition<T> implements Condition<T> {
    private Condition<T> mCondition1;
    private Condition<T> mCondition2;
    
    public OrCondition(Condition<T> pCondition1,
                       Condition<T> pCondition2) {
        mCondition1 = pCondition1;
        mCondition2 = pCondition2;
    }
    
    public boolean fulfills(T pObj) {
        return mCondition1.fulfills(pObj) || mCondition2.fulfills(pObj);    
    }        
}
