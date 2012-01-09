package util.condition;

/** A boolean-valued function */
public interface Condition<T> {
    public boolean fulfills(T pObj);
} 

