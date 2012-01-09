package util.converter;

import util.*;

/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class AnyToDoubleConverter implements Converter {         
    
    public Object convert(Object pObj) {
        if (pObj instanceof Double) {
            return pObj;
        }
        else if (pObj instanceof Number) {
            Number n = (Number)pObj; 
            return new Double(n.doubleValue());
        }
        else if (pObj instanceof String) {
            return new Double((String)pObj);
        }
        else if (pObj instanceof Boolean) {
            boolean val = ((Boolean)pObj).booleanValue();
            if (val == true) {
                return new Double(1.0);
            }
            else {
                return new Double(0.d);
            }
        }
        else {
            throw new NumberFormatException("Cannot convert object to double: "+StringUtils.toMoreInformativeString(pObj));
        }    
    }
}
