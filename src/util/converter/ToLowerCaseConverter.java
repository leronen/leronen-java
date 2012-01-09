package util.converter;


/** 
 * Converts Strings to Chars; but beware: if string is not of length 1, an RuntimeException shell be thrown!  
 */
public final class ToLowerCaseConverter implements Converter {         
    
    public Object convert(Object pObj) {
        String s = (String)pObj;
        return s.toLowerCase();            
    }
}
