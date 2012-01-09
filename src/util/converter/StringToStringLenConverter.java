package util.converter;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class StringToStringLenConverter implements Converter {         
    
    
    public Object convert(Object pObj) {
        if (pObj == null) {
            return new Integer(-1);
        }
        else {
            return new Integer(((String)pObj).length());
        }
    }
}
