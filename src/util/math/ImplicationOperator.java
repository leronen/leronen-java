package util.math;

import util.*;

public class ImplicationOperator implements BinaryOperator {
                
    public double calculate(double p1, double p2) {
        boolean b1 = ConversionUtils.toBoolean(p1);
        boolean b2 = ConversionUtils.toBoolean(p2);
        if (!b1 || b2) {
            return 1.0;
        }
        else {
            return 0.d;
        }
    }

}
