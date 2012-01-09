package util.converter;

import util.StringUtils;


/** 
 * Converts strings to Integers. null, "null" and "" all get converted to null.
 */
public final class StringToIntegerConverter implements Converter<String, Integer> {         
    
    public Integer convert(String p) {
        if (p == null || p.equals("null") || StringUtils.isEmpty(p)) {
            return null;
        }
        else {
            return new Integer(p);
        }
    }
}
