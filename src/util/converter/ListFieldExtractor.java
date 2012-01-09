package util.converter;

import java.util.*;

/**
 * Extracts a "field" of intrest from object, in this case the field is 
 * the element at a certain index of a List 
 * 
 * Keywords: listelementextractor 
 */
public class ListFieldExtractor implements Converter {

    private int mIndex;
    
    public ListFieldExtractor(int pIndex) {
        mIndex = pIndex;
    }
    
    public Object convert(Object pObj) {
        List list = (List)pObj;
        return list.get(mIndex);
    }
    
}
