package util.converter;

import util.*;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class ObjectToMoreInformativeStringConverter implements Converter {         
    
    public Object convert(Object pObj) {
        return StringUtils.toMoreInformativeString(pObj);    
    }
}
