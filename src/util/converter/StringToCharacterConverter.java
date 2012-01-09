package util.converter;


/** 
 * Converts Strings to Chars; but beware: if string is not of length 1, an RuntimeException shell be thrown!  
 */
public final class StringToCharacterConverter implements Converter {         
    
    public Object convert(Object pObj) {
        String s = (String)pObj;
        if (s.length() != 1) {
            throw new RuntimeException("String length is not 1("+s.length()+")");
        }
        return new Character(s.charAt(0));    
    }
}
