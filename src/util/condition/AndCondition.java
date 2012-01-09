package util.condition;

public class AndCondition<T> implements Condition<T> {
    
    /*private Condition mCondition1;
    private Condition mCondition2;
    */
    private Condition<T>[] mConditions;
    
    public AndCondition(Condition<T>[] pConditions) {
        mConditions = pConditions;                                    
    }
    
    public AndCondition(Condition<T> pCondition1,
                        Condition<T> pCondition2) {
        mConditions = new Condition[2];                            
        mConditions[0] = pCondition1;
        mConditions[1] = pCondition2;
    }
    
    public AndCondition(Condition<T> pCondition1,
                        Condition<T> pCondition2,
                        Condition<T> pCondition3) {
        mConditions = new Condition[3];                            
        mConditions[0] = pCondition1;
        mConditions[1] = pCondition2;
        mConditions[2] = pCondition3;
    }
    
    
    public boolean fulfills(T pObj) {
        for (int i=0; i<mConditions.length; i++) {
            if (!(mConditions[i].fulfills(pObj))) {
                return false;
            }
        }
        // fulfilled all conditions
        return true; 
    }        
}
