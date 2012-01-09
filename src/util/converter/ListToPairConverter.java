package util.converter;

import util.collections.*;
import java.util.*;

/** 
 * Converts List to Pairs. Note that the lists to be converted must have exactly 2 elements, or else!
 */
public final class ListToPairConverter implements Converter {    
       
    public Object convert(Object pObj) {
        List list = (List) pObj;
        if (list.size() != 2) {
            throw new RuntimeException("List size must be 2, as we are converting list to pair.");
        }
        return new Pair(list.get(0), list.get(1));    
    }
}
