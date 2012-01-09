package util.converter;

import java.util.*;

/** extracts a "field" of intrest from object, in this case the field is the element at a certain index of a List */
public class MapFieldExtractor implements Converter {

    private Object mKey;
    
    public MapFieldExtractor(Object pKey) {
        mKey = pKey;
    }
    
    public Object convert(Object p) {
        Map map = (Map)p;
        return map.get(mKey);
    }
    
}
