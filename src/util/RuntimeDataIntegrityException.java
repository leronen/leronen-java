package util;


/**
 * Arbitrary violation of data integrity that is not expected to occur in the abscence of programming errors.  
 */
public class RuntimeDataIntegrityException extends RuntimeException {   
           
    private static final long serialVersionUID = 1L;

    public RuntimeDataIntegrityException(String msg){
        super(msg);
    }
    
    public RuntimeDataIntegrityException(String msg, Exception cause){
        super(msg, cause);
    }
    
    public RuntimeDataIntegrityException(Exception cause){
        super(cause);
    }
}

