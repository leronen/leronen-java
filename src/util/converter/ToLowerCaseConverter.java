package util.converter;


/** 
 * Converts ArbitRaryCase Strings to lowercase strings.  
 */
public final class ToLowerCaseConverter implements Converter {         
    
    @Override
    public Object convert(Object pObj) {
        String s = (String)pObj;
        return s.toLowerCase();            
    }
}
