package util.converter;

import java.util.*;

/** 
 * Converts Strings to Chars; but beware: if string is not of length 1, an RuntimeException shell be thrown!  
 */
public final class ArrayToListConverter implements Converter {         
    
    public Object convert(Object pObj) {
        Object[] arr = (Object[]) pObj;
        return Arrays.asList(arr);            
    }
}
