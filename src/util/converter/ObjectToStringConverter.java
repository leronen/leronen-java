package util.converter;

import util.*;

/** 
 * Converts any object to string, even a null, which is converted to "null" (sic)
 */
public final class ObjectToStringConverter<T> implements Converter<T,String> {         
    
    public String convert(T pObj) {        
        return StringUtils.possiblyNullObjectToString(pObj);    
    }
}
