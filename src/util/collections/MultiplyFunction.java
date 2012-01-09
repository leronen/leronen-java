package util.collections;

public class MultiplyFunction implements Function<Double, Double> {
    
    private double mMultiplier; 
    
    public MultiplyFunction(double pMultiplier) {
        mMultiplier = pMultiplier;         
    }
    
    public String getName() {
        return "MultiplyFunction with multiplier: "+mMultiplier;
    }
    
    public Double compute(Double p) {
        return mMultiplier * p;
    }
}
