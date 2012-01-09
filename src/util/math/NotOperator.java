package util.math;

import util.*;

public class NotOperator implements UnaryOperator {
                
    public double calculate(double pVal) {
        boolean b = ConversionUtils.toBoolean(pVal);        
        if (b) {
            return 0.d;
        }
        else {
            return 1.d;
        }
    }

}
