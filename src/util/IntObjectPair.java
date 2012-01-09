package util;

/** 
 * An unholy union of int and Object. 
 */
public class IntObjectPair {
    public int mInt;
    public Object mObject;
    
    public IntObjectPair(int pInt, Object pObject) {
        mInt = pInt;
        mObject = pObject;
    }
           
    public int getInt() {
        return mInt;
    }

    public Object getObject() {
        return mObject;
    }                                      
            
}
