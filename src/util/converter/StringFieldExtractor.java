package util.converter;

import java.util.regex.*;

/** extracts a "field" of intrest from object, in this case the field is the element at a certain index of a List 
 * represented as a pattern-delimited string. */
public class StringFieldExtractor implements Converter<String, String> {

    private int mIndex;
    private Pattern mDelimeterPattern = Pattern.compile("\\s+");    
    
    public StringFieldExtractor(int pIndex) {
        mDelimeterPattern = Pattern.compile("\\s+");
        mIndex = pIndex;
    }
    
    public String convert(String pObj) {         
        String[] components = mDelimeterPattern.split((String)pObj);        
        return components[mIndex];         
    }
    
}
