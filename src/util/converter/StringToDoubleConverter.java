package util.converter;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class StringToDoubleConverter implements Converter {         
    
    private boolean mPermitNulls;
    
    /** By default the converter does not permit nulls */
    public StringToDoubleConverter() {
        this(false);
    }
    
    /**
     * @param pPermitNulls if true, a null String is just "converted" to a null 
     * Double; if false, a RuntimeException is raised on encountering a null.     * 
     */
    public StringToDoubleConverter(boolean pPermitNulls) {
        mPermitNulls = pPermitNulls;        
    }
    
    public Object convert(Object pObj) {
        if (pObj == null) {
            if (mPermitNulls) {
                return null;
            }
            else {
                throw new RuntimeException("We do not permit nulls!");    
            }
        }
        else {
            if (pObj.equals("-")) {
                return new Double(Double.NaN);
            }
            else {
                // we now assume the string to be well formatted...
                return new Double((String)pObj);
            }
        }
    }
}
