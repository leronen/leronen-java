package util.converter;

import java.util.*;
import java.util.regex.*;

/** 
 * Converts strings to lists, given a delimeter pattern 
 * The returned lists are immutable.
 */
public final class StringToListConverter implements Converter {
    Pattern mDelimPattern; 
    
    public StringToListConverter(String pDelim) {
        mDelimPattern = Pattern.compile(pDelim);
    }
    
    public StringToListConverter() {
        mDelimPattern = Pattern.compile("\\s+");
    }
    
    public Object convert(Object pObj) {
        return Arrays.asList(mDelimPattern.split((String)pObj));    
    }        
}
