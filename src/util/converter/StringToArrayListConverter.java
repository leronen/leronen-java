package util.converter;

import java.util.*;
import java.util.regex.*;

/** 
 * Converts strings to lists, given a delimeter pattern 
 * The returned lists are immutable.
 */
public final class StringToArrayListConverter implements Converter {
    Pattern mDelimPattern; 
    
    public StringToArrayListConverter(String pDelim) {
        mDelimPattern = Pattern.compile(pDelim);
    }
    
    public StringToArrayListConverter() {
        mDelimPattern = Pattern.compile("\\s+");
    }
    
    public Object convert(Object pObj) {
        return new ArrayList(Arrays.asList(mDelimPattern.split((String)pObj)));    
    }        
}
