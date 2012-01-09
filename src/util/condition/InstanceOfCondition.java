package util.condition;

public class InstanceOfCondition implements Condition {
    
    private Class mClass;
    
    public InstanceOfCondition(Class pClass) {
        mClass = pClass;
    }
    
    public boolean fulfills(Object p) {
        return mClass.isAssignableFrom(p.getClass());           
    }
}
