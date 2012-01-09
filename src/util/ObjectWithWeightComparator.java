package util;

import java.util.*;


/** 
 * TODO: As it is bad practice to force clients to implement arbitrary
 * interfaces like ObjectWithWeight,  * this should not require that the 
 * object implement ObjectWithWeight. Instead, this should be an abstract 
 * class (parametrized with type of compared objects), and the subclass should 
 * provide the access to the weight.
 * 
 */
public class ObjectWithWeightComparator implements Comparator {
    private int mFactor;     
    
    public ObjectWithWeightComparator() {
        this(false);        
    }
    
    public ObjectWithWeightComparator(boolean pReverse) {
        mFactor = pReverse ? -1 : 1;
    }
                                    
    public int compare(Object p1, Object p2) {
        ObjectWithWeight obj1 = (ObjectWithWeight)p1;
        ObjectWithWeight obj2 = (ObjectWithWeight)p2;            
        double diff = obj1.getWeight()-obj2.getWeight();                                 
        if (diff < 0) return -1*mFactor;
        else if (diff == 0) return 0;
        else return mFactor;
    }             
}
