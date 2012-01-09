package util.converter;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class ObjectToClassConverter implements Converter<Object, Class> {         
    
    public Class convert(Object pObj) {
        return pObj.getClass();    
    }
}
