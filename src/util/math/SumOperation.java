package util.math;

import util.*;

public class SumOperation extends AbstractVectorToScalarOperation {
        
    public double calculate(double[] pArr) {
        return MathUtils.sum(pArr);
    }

}
