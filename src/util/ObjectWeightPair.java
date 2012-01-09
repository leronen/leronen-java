package util;


/** 
 * An beautiful combination of Object and weight. 
 */
public class ObjectWeightPair<T> implements ObjectWithWeight {
    
    public T mObject;
    public double mWeight;
        
    public ObjectWeightPair(T pObject,
                            double pWeight) {
        mWeight = pWeight;
        mObject = pObject;
    }
           
    public T getObject() {
        return mObject;
    }      
    
    public double getWeight() {
        return mWeight;
    }                                    
           
}

