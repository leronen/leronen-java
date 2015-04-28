package util.converter;

import util.StringUtils;


/** 
 * Converts strings to Integers. null, "null" and "" all get converted to null.
 */
public final class MinsToHourMinsStringConverter implements Converter<String, String> {         
            
    @Override
    public String convert(String p) {        
        if (p == null || StringUtils.isEmpty(p)) {
            return "";
        }
        else {
            int totalMin = Integer.parseInt(p);
            int min = totalMin % 60;
            int h = (totalMin-min) / 60;            
            return "" + h + ":" + (min < 10 ? "0" + min : min); 
        }
    }
}
