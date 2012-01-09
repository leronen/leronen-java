package util.converter;

import util.*;

import java.util.*;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class MapToStringConverter implements Converter {         
    
    private String mKeyAndValDelim;
    private String mKeyValPairDelim;
    
    private Converter mKeyConverter;
    private Converter mValConverter;
    
        
    public MapToStringConverter(String pKeyAndValDelim,
                                String pKeyValPairDelim) {                                            
        this(pKeyAndValDelim, pKeyValPairDelim, null, null);                                                                
    }

    public MapToStringConverter(String pKeyAndValDelim,
                                String pKeyValPairDelim,
                                Converter pKeyConverter,
                                Converter pValConverter) {
        if (pKeyAndValDelim == null || pKeyValPairDelim == null) {
            throw new RuntimeException("Delims cannot be null!");
        }                                    
        mKeyAndValDelim = pKeyAndValDelim;
        mKeyValPairDelim = pKeyValPairDelim;           
        mKeyConverter = pKeyConverter;
        mValConverter = pValConverter;                    
    }                               
                                    
            
    public Object convert(Object pObj) {
        if (mKeyConverter != null || mValConverter != null) {
            return StringUtils.mapToString((Map)pObj, mKeyAndValDelim, mKeyValPairDelim, mKeyConverter, mValConverter);            
        }
        else {            
            return StringUtils.mapToString((Map)pObj, mKeyAndValDelim, mKeyValPairDelim);
        }
    }
}
