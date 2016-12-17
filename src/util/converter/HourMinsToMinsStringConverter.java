package util.converter;

import util.StringUtils;


/** 
 * Converts strings to Integers. null, "null" and "" all get converted to null.
 */
public final class HourMinsToMinsStringConverter implements Converter<String, String> {         
            
    @Override
    public String convert(String p) {        
        if (p == null || StringUtils.isEmpty(p)) {
            return "";
        }
        else {
            String tok[] = p.split("[:]");
            int h = Integer.parseInt(tok[0]);
            int m = Integer.parseInt(tok[1]);
            return "" + (h * 60 + m); 
        }
    }
}
