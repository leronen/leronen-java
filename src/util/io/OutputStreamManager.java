package util.io;

import java.io.*;
import java.util.*;


/**
 * awk-style management of output streams (mostly files)
 * Note that created streams are not auto-flushed!
 */
public class OutputStreamManager {    
    
    private static OutputStreamManager sInstance;
            
    private static OutputStreamManager getInstance() {
        if (sInstance == null) {
            sInstance = new OutputStreamManager();
        }
        return sInstance;
    }
    
    private Map<String,PrintStream> mOutputStreamByFileName = new HashMap<String, PrintStream>();
    
    private PrintStream internalGetPrintStream(String fileName) throws FileNotFoundException {
        return internalGetPrintStream(fileName, null);
    }
    
    /** Create a stream if needed first, and print a header to the stream if not null */
    private PrintStream internalGetPrintStream(String fileName, String header) throws FileNotFoundException {        
        PrintStream ps = mOutputStreamByFileName.get(fileName);
        if (ps == null) {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);
            ps = new PrintStream(bos, false); // the last parameter is "autoflush"
            if (header != null) {
                ps.println(header);
            }
            mOutputStreamByFileName.put(fileName, ps);
        }
        
        return ps;        
    }
    
    public static PrintStream getPrintStream(String fileName) throws FileNotFoundException {
        return getInstance().internalGetPrintStream(fileName);
    }
    
    /** Get a stream; if it does not exists, create it and write a header to it first */ 
    public static PrintStream getPrintStream(String fileName, String header) throws FileNotFoundException {
        return getInstance().internalGetPrintStream(fileName, header);
    }
    
    public static void println(String pFileName, String pString) throws FileNotFoundException {
        getInstance().internalGetPrintStream(pFileName).println(pString);
    }
    
    public static void printf(String pFileName, String pFormat, Object... pData) throws FileNotFoundException {
        getInstance().internalGetPrintStream(pFileName).printf(pFormat, pData);
    }
    
    /** also unregister all streams */
    public static void closeStreams() {
        getInstance().internalCloseStreams();
    }
    
    public static void closeAndUnregister(String pFileName) {
        getInstance().internalCloseAndUnregister(pFileName);        
    }
    
    public void internalCloseAndUnregister(String pFileName) {        
        PrintStream ps = mOutputStreamByFileName.get(pFileName);
        if (ps != null) {           
            ps.close();
            mOutputStreamByFileName.remove(pFileName);
        }
        
        
    }
    
    /** also unregister all streams */
    private void internalCloseStreams() {               
        
        for (PrintStream ps: mOutputStreamByFileName.values()) {
            ps.flush();
            ps.close();
        }
        
        mOutputStreamByFileName.clear();
    }
    
    /** just in case caller forgets to call closeOutputStreams... */
    protected void finalize() throws Throwable {
        try {
            closeStreams();            
        } 
        finally {
            super.finalize();
        }
    }
    
}
