package util.condition;

public class FalseCondition<T> implements Condition<T> {    
        
    public boolean fulfills(T pObj) {
        return false;    
    }        
}
