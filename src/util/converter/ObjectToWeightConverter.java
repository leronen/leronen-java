package util.converter;

import util.*;


/** 
 * Converts WeightedObject instances to their weights (fuzzy, eh)
 */
public final class ObjectToWeightConverter implements Converter {         
    
    public Object convert(Object pObj) {
        ObjectWithWeight weightedObject = (ObjectWithWeight)pObj;        
        return new Double(weightedObject.getWeight());
    }
}
