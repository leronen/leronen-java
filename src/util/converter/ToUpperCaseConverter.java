package util.converter;



/** 
 * Converts ArbitRaryCase Strings to UPPERCASE STRINGS.  
 */
public final class ToUpperCaseConverter implements Converter {         
    
    @Override
    public Object convert(Object pObj) {
        String s = (String)pObj;
        return s.toUpperCase();            
    }
}
