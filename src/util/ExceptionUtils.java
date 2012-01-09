package util;


public class ExceptionUtils {

    private static final String EMPTY_STRING = "";
    
    public static String stackTraceAsString(Throwable pThrowable) {    
        StackTraceElement[] traceElems = pThrowable.getStackTrace();
        return StringUtils.arrayToString(traceElems, "\n");
    }              
    
    public static String msgString(Throwable e) {
        String msg = e.getMessage();
        return msg != null ? ": "+msg : "";        
    }
        
    /** Return "<class>: <message>; <causes>" */
    public static String format(Exception e) {
        String result = e.getClass().getName()+msgString(e);
        String causeString = causeString(e, "; ");
        if (causeString != EMPTY_STRING) {
            return result+"; "+causeString;
        }
        else {
            return result;
        }
    }
    
    /** Return "<class>: <message>; <causes>" */
    public static String format(Exception e, String pSeparator) {
        String result = e.getClass().getName()+msgString(e);
        String causeString = causeString(e, pSeparator);
        if (causeString != EMPTY_STRING) {
            return result+pSeparator+causeString;
        }
        else {
            return result;
        }
    }
    
    /** Return {@link #EMPTY_STRING}, if no causes */
    public static String causeString(Exception pException, String pSeparator) {
        StringBuffer buf = null;
        Throwable cause = pException.getCause();
        String prefix = "cause: ";
        while (cause != null) {                
            String tmp = prefix+cause.getClass().getName()+msgString(cause);
            if (buf == null) {
                buf = new StringBuffer(tmp);
            }
            else {
                buf.append(pSeparator);
                buf.append(tmp);
            }
            cause = cause.getCause();
            prefix = "cause of "+prefix; 
        }
        
        if (buf != null) {
            return buf.toString();
        }
        else {
            return EMPTY_STRING;
        }
        
    }
    
    public static void main (String[] args) {
        test1(args);
    }
    
    private static void test1(String[] args) {
        try {
            aMethod();
        }
        catch (TestException e) {
            System.err.println(format(e, "\n\t"));
        }
    }
    
    private static void aMethod() throws TestException {
        throw new TestException("Outer", 
                new TestException("Middle",
                    new TestException("Inner")));
    }

    private static class TestException extends Exception {
        
        public TestException(String pMsg) {
            super(pMsg);
        }
        
        public TestException(String pMsg, Throwable pCause) {
            super(pMsg, pCause);
        }
    }

}


