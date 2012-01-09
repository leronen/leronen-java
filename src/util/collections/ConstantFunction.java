package util.collections;

public class ConstantFunction<P,V> implements Function<P,V> {
    
    private V mConstant;
    
    public ConstantFunction(V pConstant) {
        mConstant = pConstant;
    }
    
    public String getName() {
        return "a function with an eternally constant value "+mConstant;
    }
    
    @Override
    public V compute(P p) {
        return mConstant;
    }
}
