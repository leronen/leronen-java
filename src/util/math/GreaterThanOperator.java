package util.math;

public class GreaterThanOperator implements BinaryOperator {
                
    public double calculate(double p1, double p2) {
        if (p1 > p2) {
            return 1;
        }
        else {
            return 0;
        }
    }

}
