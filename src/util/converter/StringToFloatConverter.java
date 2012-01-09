package util.converter;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class StringToFloatConverter implements Converter<String, Float> {         
    
    public Float convert(String p) {
        return new Float(p);    
    }
}
