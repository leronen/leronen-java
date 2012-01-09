package util.converter;

import util.*;

/** 
 * 
 */
public final class DoubleToStringConverter implements Converter<Double, String> {         
   
    int mDecimals;
    
    public DoubleToStringConverter() {
        mDecimals = 3;
    }
    
    public DoubleToStringConverter(int pDecimals) {
        mDecimals = pDecimals;
    }
    
    public String convert(Double p) {
        if (p == null) {
            return null;
        }
        else {               
            return StringUtils.formatFloat(p, mDecimals);
            
        }                   
    }
}
