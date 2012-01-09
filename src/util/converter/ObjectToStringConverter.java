package util.converter;

import util.*;


/** 
 * Converts any object to string, even a null, which is converted to "null" (sic)
 */
public final class ObjectToStringConverter implements Converter {         
    
    public String convert(Object pObj) {        
        return StringUtils.possiblyNullObjectToString(pObj);    
    }
}
