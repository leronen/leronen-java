package util.converter;

import util.*;

import java.util.*;


/** 
 * Converts a Collction to an ArrayList, converting each object using 
 * the given converter...
 */
public final class ListConverter <T1,T2> implements Converter<Collection<T1>, ArrayList<T2>> {         
                  
    private Converter<T1, T2> mElemConverter;
            
    public ListConverter(Converter<T1, T2> pElemConverter) {                                  
        mElemConverter = pElemConverter;
    }                               
                                                
    public ArrayList<T2> convert(Collection<T1> pList) {        
        return ConversionUtils.convert(pList, mElemConverter);       
    }
}
