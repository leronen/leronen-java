package util.condition;

public class TrueCondition<T> implements Condition<T> {    
            
    public boolean fulfills(T pObj) {
        return true;    
    }        
}
