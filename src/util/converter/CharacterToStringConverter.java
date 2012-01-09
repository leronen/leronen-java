package util.converter;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class CharacterToStringConverter implements Converter {         
    
    public Object convert(Object pObj) {
        return ((Character)pObj).toString();            
    }
}
