package util.dbg;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import util.io.OutputStreamManager;

/** Logger where the base name (added as prefix to all output) can be changed on the fly... */ 
public class MutableLogger implements ILogger {
    
    @SuppressWarnings("unused")
    private String baseName;
    
    private String infoFile;
    private String warningFile;
    private PrintStream infoStream;
    private PrintStream warningStream;
    
    public MutableLogger(String baseName) throws FileNotFoundException {
        setBaseName(baseName);
    }
    
    public void setBaseName(String baseName) throws FileNotFoundException {
        closeActiveStreams();
        
        this.baseName = baseName;
        infoFile = baseName+".log";
        warningFile = baseName+".warnings";
        this.infoStream = OutputStreamManager.getPrintStream(infoFile);
        this.warningStream = OutputStreamManager.getPrintStream(warningFile);
    }
    
    private void closeActiveStreams() {
        if (infoFile != null) {
            OutputStreamManager.closeAndUnregister(infoFile);
        }
        if (warningFile != null) {
            OutputStreamManager.closeAndUnregister(warningFile);
        }
    }
    
    
    public void finalize() {
        closeStreams();
    }
    
    /** TODO: proper impl */
    @Override
    public void info(String msg) {
        System.err.println(msg);
        infoStream.println(msg);
    }
    
    /** TODO: proper impl */
    @Override
    public void warning(String warning) {
        System.err.println("WARNING: "+warning);
        warningStream.println(warning);
        infoStream.println("WARNING: "+warning);
    }
    
    /** Report error and exit with exit code 1. TODO: proper logging */
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
    public void closeStreams() {
        closeActiveStreams();
        
    }

    @Override
    public void error(Exception e) {
        warningStream.println("ERROR:");
        e.printStackTrace(warningStream);
        infoStream.println("ERROR:");
        e.printStackTrace(infoStream);
        e.printStackTrace();
        
    }
    
    
}
