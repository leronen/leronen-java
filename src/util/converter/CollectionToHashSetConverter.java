package util.converter;

import java.util.*;

public final class CollectionToHashSetConverter implements Converter {    
       
    public Object convert(Object pObj) {
        Collection col = (Collection) pObj;        
        return new HashSet(col);    
    }
}
