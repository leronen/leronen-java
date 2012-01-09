package util.math;


public class GreaterThanOrEqualOperator implements BinaryOperator {
                
    public double calculate(double p1, double p2) {
        // dbgMsg("Calculating: "+p1+">="+p2+"?");
        if (p1 >= p2) {            
            return 1;
        }
        else {
            return 0;
        }
    }
    
    

}
