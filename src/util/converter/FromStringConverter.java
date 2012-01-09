package util.converter;

import util.Initializable;

/**
 * Convert string to instance of an class implementing Initializable;
 * (Initializable objects can be initialized using data given as a String) 
 *  
 * Resorts, of course, to reflection.
 */
public class FromStringConverter implements Converter<String, Initializable> {
    
    private Class mDestClass;
    
    public FromStringConverter(Class pDestClass) {
        mDestClass = pDestClass;
    }
    
    public Initializable convert(String p ) {
        try {
            Initializable result = (Initializable)mDestClass.newInstance();
            result.init(p);
            return result;        
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unrecoverable exception in FromStringConverter (dest_class="+mDestClass+", string="+p);
            
        }
       // Initiali result = 
        
    }
    
}
