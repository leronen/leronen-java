package util.math;

import util.*;

public class AvgOperation extends AbstractVectorToScalarOperation {
        
    public double calculate(double[] pArr) {
        return MathUtils.avg(pArr);
    }

}
