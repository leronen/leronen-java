package util.converter;

import util.*;

import java.util.*;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class ListToStringConverter <T> implements Converter<List<T>, String> {         
    
    private String mDelim;           
    private Converter<T, String> mElemFormatter;
    
    /** With delim " " */
    public ListToStringConverter() {                                            
        this(" ", null);                                                                
    }    
        
    public ListToStringConverter(String pDelim) {                                            
        this(pDelim, null);                                                                
    }

    public ListToStringConverter(String pDelim,                                 
                                 Converter<T, String> pElemFormatter) {
                                  
        if (pDelim == null) {
            throw new RuntimeException("pDelim cannot be null!");
        }
        
        mDelim = pDelim;
        mElemFormatter = pElemFormatter;
    }                               
                                                
    public String convert(List<T> pList) {
        if (mElemFormatter != null) {
            return StringUtils.collectionToString(pList, mDelim, mElemFormatter);            
        }
        else {            
            // no elem formatter
            return StringUtils.collectionToString(pList, mDelim);
        }
    }
}
