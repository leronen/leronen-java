package util.io;

@SuppressWarnings("serial")
public class NoSuchColumnException extends RuntimeException {
    
    public NoSuchColumnException (String message) {
        super(message);
    }       
    
}