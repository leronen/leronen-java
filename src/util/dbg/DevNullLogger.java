package util.dbg;

/** Logger which does nothing */
public class DevNullLogger implements ILogger {    
    
    public static final DevNullLogger SINGLETON = new DevNullLogger();
    
    @Override
    public void info(String msg) {
        //         
    }
    
    @Override
    public void warning(String warning) {
        //        
    }
    
    /** Report error and exit with exit code 1. TODO: proper logging */
    @Override
    public void error(String msg) {
        //        
    }
    
    @Override
    public void error(String msg, Exception e) {
        //               
    }

    @Override
    public void closeStreams() {
        //        
    }

    @Override
    public void error(Exception e) {
        //        
    }   
}

