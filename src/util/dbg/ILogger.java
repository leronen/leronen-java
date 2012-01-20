package util.dbg;

public interface ILogger {
    public void dbg(String msg);
    public void info(String msg);        
    public void warning(String warning);                
    public void error(String msg);
    public void error(Exception e);
    public void error(String msg, Exception e);
    /** might not be needed for all loggers */
    public void closeStreams();
}
