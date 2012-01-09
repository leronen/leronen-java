package util.math;

import util.*;

import java.util.*;

/** Provides the list-based implementation by delegating to the array-based implementation */
public abstract class AbstractVectorToScalarOperation implements VectorToScalarOperation {
        
    public abstract double calculate(double[] pVector);    
    
    public double calculate(List pVector) {
        double[] vals = ConversionUtils.DoubleCollectionTodoubleArray(pVector);
        return calculate(vals);        
    }

}
