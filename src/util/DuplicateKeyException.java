package util;

/**
 *
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends RuntimeException {   

    private Object key;
    
    public DuplicateKeyException(Object key) {
        super("Duplicate key: " + key);
        this.key = key;
    }
    
    public DuplicateKeyException(Object key, String message) {
        super(message);
        this.key = key;
    }
    
    public Object getKey() {
        return key;
    }
    
    
}

