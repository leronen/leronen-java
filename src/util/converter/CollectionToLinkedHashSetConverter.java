package util.converter;

import java.util.*;

public final class CollectionToLinkedHashSetConverter implements Converter {    
       
    public Object convert(Object pObj) {
        Collection col = (Collection) pObj;        
        return new LinkedHashSet(col);    
    }
}
