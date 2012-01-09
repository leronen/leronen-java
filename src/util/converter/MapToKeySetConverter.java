package util.converter;

import java.util.*;
                   
public final class MapToKeySetConverter implements Converter {         
                                                                
    public Object convert(Object p) {
        if (p == null) {
            return new HashSet();
        }
        else {            
            return ((Map)p).keySet();
        }                                                    
    }
    
}
