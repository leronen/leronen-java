package util.converter;

import java.math.*;

/** 
 * Throws away undesired decimals (mainly intended for supporting more beautiful outputting of Doubles).
 */
public final class DoubleScaleConverter implements Converter {    
       
    private int mScale;       
       
    public DoubleScaleConverter(int pScale) {       
        mScale = pScale;
    }
        
    public Object convert(Object p) {
        Double oldVal = (Double) p;
        BigDecimal bigDecimal = new BigDecimal(oldVal.doubleValue());
        bigDecimal = bigDecimal.setScale(mScale, BigDecimal.ROUND_DOWN);
        return new Double(bigDecimal.doubleValue());          
    }
}
