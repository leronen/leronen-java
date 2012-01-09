package util.converter;

import util.*;

import java.util.regex.*;

/** 
 * Convert strings (possibly repsenting numbers) to "more beautiful" strings,
 * that is, throw away unneeded decimals... Also accept Number instances,
 * and convert them to strings, throwing away unneedeed decimals.
 * For other objects, just return toString(). 
 * 
 */
public final class NumberBeautifyingConverter implements Converter<Object, String> {         
    
    
    
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");
    private int mNumDecimals;
    
    public NumberBeautifyingConverter() {
        mNumDecimals = 3;
    }
    
    public NumberBeautifyingConverter(int pNumFloatDecimals) {
        mNumDecimals = pNumFloatDecimals;
    }
    
    public String convert(Object pObj) {
        if (pObj == null) {
            return null;
        }        
        else if (pObj instanceof Number) {
            Number n = (Number)pObj;
            if (n instanceof Float) {
                Float f = (Float)n;                
                return StringUtils.formatFloat(f, mNumDecimals);
            }
            else if (n instanceof Double) {
                Double d = (Double)n;
                return StringUtils.formatFloat(d, mNumDecimals);
            }
            else {
                // no formatting...
                return n.toString();
            }
        }
        else if (pObj instanceof String) {
            String s = (String)pObj;
            if (StringUtils.isNumeric(s)) {
                Matcher intMatcher = INT_PATTERN.matcher(s);
                if (intMatcher.matches()) {
                    // an integer; no need for formatting...
                    return s;
                }            
                else {
                    // a float, we presume...
                    double val = Double.parseDouble(s);
                    return StringUtils.formatFloat(val, mNumDecimals);
                }        
            }
            else {
                // non-numeric string, does not overlap our field of business...
                return s;               
            }                               
        }
        else {
            // not a string, not a number...
            return pObj.toString();
        }
            
    }
}
