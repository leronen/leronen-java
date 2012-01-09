package util.converter;

import java.util.*;

/** 
 * Converts List to Pairs. Note that the lists to be converted must have exactly 2 elements, or else!
 */
public final class CollectionToSizeConverter implements Converter {    
       
    public Object convert(Object pObj) {
        Collection col = (Collection)pObj;
        return new Integer(col.size());                
    }
}
