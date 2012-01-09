package util.math;

import util.*;

public class MinOperation extends AbstractVectorToScalarOperation {
        
    public double calculate(double[] pArr) {
        return MathUtils.min(pArr);
    }

}
