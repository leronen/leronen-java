package util.math;

import util.*;

public class MaxOperation extends AbstractVectorToScalarOperation {
        
    public double calculate(double[] pArr) {
        return MathUtils.max(pArr);
    }

}
