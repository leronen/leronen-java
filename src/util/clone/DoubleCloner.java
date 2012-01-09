package util.clone;

public class DoubleCloner implements Cloner<Double> {
     
    public Double createClone(Double d) {
        // no need to clone, as Doubles are immutable!
        return d;
    }        
    
} 

