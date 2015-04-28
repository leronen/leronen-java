package util;

/**
 * An Exception used to notify about wrong or erroneous file format.
 * @author ahienola
 *
 */
@SuppressWarnings("serial")
public class FileFormatException extends Exception {    

    public FileFormatException(){}
    
    public FileFormatException(String msg){
        super(msg);
    }
    
    public FileFormatException(String msg, Exception cause){
        super(msg, cause);
    }
    
    public FileFormatException(Exception cause){
        super(cause);
    }
}

