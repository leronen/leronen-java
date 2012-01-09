package util.converter;

import util.*;

/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class AnyToIntegerConverter implements Converter {         

    public Object convert(Object pObj) {
        return ConversionUtils.anyToInteger(pObj);
    }
    
    /*    
    public Object convert(Object pObj) {        
        if (pObj instanceof Integer) {
            return (Integer)pObj;             
        }
        else if (pObj instanceof String) {
            return new Integer((String)pObj);
        }
        else if (pObj instanceof Double) {
            double d = ((Double)pObj).doubleValue();;                                    
            return new Integer((int)d);
        }
        else if (pObj instanceof Boolean) {
            boolean val = ((Boolean)pObj).booleanValue();
            if (val == true) {
                return new Integer(1);
            }
            else {
                return new Integer(0);
            }
        }
        else {
            throw new NumberFormatException("Cannot convert object to double: "+StringUtils.toMoreInformativeString(pObj));
        }    
    }
    */
}
