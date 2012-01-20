package util.dbg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import util.io.OutputStreamManager;

/** Logs to stderr and 2 files: "<basename>.log" and "<basename>.warnings".  */   
public class DefaultLogger implements ILogger {
    
    @SuppressWarnings("unused")
    private String baseName;
    
    private String infoFile;
    private String warningFile;
    private PrintStream infoStream;
    private PrintStream warningStream;
    
    public DefaultLogger(String baseName) throws FileNotFoundException {
        this.baseName = baseName;
        this.infoFile = baseName+".log";
        this.warningFile = baseName+".warnings";
        this.infoStream = OutputStreamManager.getPrintStream(infoFile);
        this.warningStream = OutputStreamManager.getPrintStream(warningFile);
    }
            
    @Override
    public void closeStreams() {
        if (infoFile != null) {
            OutputStreamManager.closeAndUnregister(infoFile);
            removeEmptyFileIfNeeded(infoFile);
            infoFile = null;
        }
        if (warningFile != null) {
            OutputStreamManager.closeAndUnregister(warningFile);
            removeEmptyFileIfNeeded(warningFile);
            warningFile = null;
        }
    }
    
    private void removeEmptyFileIfNeeded(String filename) {
    	File file = new File(filename);
    	if (file.exists() && file.length() == 0) {
    		file.delete();
    	}
    }
    
    public void finalize() {
        closeStreams();
    }
    
    @Override
    public void dbg(String msg) {
        System.err.println(msg);
        infoStream.println(msg);
    }
    
    @Override
    public void info(String msg) {
        System.err.println(msg);
        infoStream.println(msg);
    }

    @Override
    public void warning(String warning) {
        System.err.println("WARNING: "+warning);
        warningStream.println(warning);
        infoStream.println("WARNING: "+warning);
    }
    
    /** Report error and exit with exit code 1. */
    @Override
    public void error(String msg) {
        System.err.println("ERROR: "+msg);
        warningStream.println("ERROR: "+msg);
        infoStream.println("ERROR: "+msg);
        System.exit(1);
    }
    
    @Override
    public void error(String msg, Exception e) {
        System.err.println("ERROR: "+msg);
        e.printStackTrace();
        warningStream.println("ERROR: "+msg);
        infoStream.println("ERROR: "+msg);
        System.exit(1);
    }

    @Override
    public void error(Exception e) {
        String msg = "ERROR: "+e.getClass().getName()+": "+e.getMessage();
                
        warningStream.println(msg);
        e.printStackTrace(warningStream);
        
        infoStream.println(msg);
        e.printStackTrace(infoStream);
        
        System.err.println(msg);
        e.printStackTrace();
        
    }
}
