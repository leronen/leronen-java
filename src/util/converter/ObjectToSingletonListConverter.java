package util.converter;

import java.util.*;

/** 
 * Converts strings to lists, given a delimeter pattern 
 * The returned lists are immutable.
 */
public final class ObjectToSingletonListConverter implements Converter {     
            
    public Object convert(Object pObj) {
        return Collections.singletonList(pObj);    
    }
}
