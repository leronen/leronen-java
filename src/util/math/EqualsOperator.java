package util.math;

public class EqualsOperator implements BinaryOperator {
                
    public double calculate(double p1, double p2) {        
        if (p1 ==  p2) {
            return 1.0;
        }
        else {
            return 0.d;
        }
    }

}
