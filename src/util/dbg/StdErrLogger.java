package util.dbg;

/* a naive stderr-based implementation */
public class StdErrLogger implements ILogger {    
    
    @Override
    public void dbg(String msg) {
        System.err.println(msg);        
    }
    
    @Override
    public void info(String msg) {
        System.err.println(msg);        
    }
    
    @Override
    public void warning(String warning) {
        System.err.println("WARNING: "+warning);        
    }
    
    /** Report error and exit with exit code 1. TODO: proper logging */
    @Override
    public void error(String msg) {
        System.err.println("ERROR: "+msg);        
    }
    
    @Override
    public void error(String msg, Exception e) {
        System.err.println("ERROR: "+msg);
        e.printStackTrace();        
    }

    @Override
    public void closeStreams() {
        // no action needed
        
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
        
    }   
}
