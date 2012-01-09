package util.converter;

import util.*;

import java.util.*;

/** 
 * Converts Collection of WeightedObject instances into their total to their weight (Double) (horribly enough!)
 */
public final class CollectionToTotalWeightConverter implements Converter {         
    
    private Converter mObjectToWeightConverter = new ObjectToWeightConverter();
    
    public Object convert(Object pObj) {
        Collection col = (Collection)pObj;
        List weightList = ConversionUtils.convert(col, mObjectToWeightConverter);
        double[] weights = ConversionUtils.DoubleCollectionTodoubleArray(weightList);
        double sum = MathUtils.sum(weights); 
        return new Double(sum);
    }
}
