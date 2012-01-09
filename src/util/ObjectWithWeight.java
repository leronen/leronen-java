package util;

/** 
 * The most general imaginable superconcept for objects that have some kind of weight. 
 *
 * Hopefully this will be used e.g. in objects in weighted sets, distributions, etc,
 * as well as in frequency counting... you name it(sic). 
 * 
 */

public interface ObjectWithWeight {

    public double getWeight();
        
}
