package util.converter;


/** 
 * Converts Integers to different Integers by addition.
 */
public final class IntAdderConverter implements Converter {

    private int mTerm;

    public IntAdderConverter(int pTerm) {
        mTerm = pTerm;
    }         
    
    public Object convert(Object pObj) {
        Integer original = (Integer)pObj;
        return new Integer(original.intValue()+mTerm);            
    }
}
