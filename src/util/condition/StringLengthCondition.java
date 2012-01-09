package util.condition;

public class StringLengthCondition implements Condition {                                
               
    Condition mNumericCondition;           
    
    public StringLengthCondition(int pLength) {
        mNumericCondition = new EqualsCondition(new Integer(pLength));
    }
       
    public StringLengthCondition(Condition pNumericCondition) {
        mNumericCondition = pNumericCondition;                
    }            
            
    public boolean fulfills(Object pObj) {
        String s = (String)pObj;
        return mNumericCondition.fulfills(new Integer(s.length()));                    
    }        
}
