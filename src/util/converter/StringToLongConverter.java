package util.converter;

import util.StringUtils;


/** 
 * Converts strings to Longs. null, "null" and "" all get converted to null.
 */
public final class StringToLongConverter implements Converter<String, Long> {         
    
    public Long convert(String p) {
        if (p == null || p.equals("null") || StringUtils.isEmpty(p)) {
            return null;
        }
        else {
            return new Long(p);
        }
    }
}
