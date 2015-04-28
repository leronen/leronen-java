package util;

@SuppressWarnings("serial")
public class NoSuchRowException extends RuntimeException {

    public NoSuchRowException (String message) {
        super(message);
    }

}
