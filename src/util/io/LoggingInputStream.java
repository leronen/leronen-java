package util.io;

import java.io.IOException;
import java.io.InputStream;

import util.IOUtils;
import util.dbg.Logger;

public class LoggingInputStream extends InputStream {
    
    private InputStream mStream;

    private long mStartTime = System.nanoTime();
    
    public LoggingInputStream(InputStream pStream) {
        mStream = pStream;
    }
    
    public int available() throws IOException {
        int result = mStream.available();
        dbgMsg("available()=="+result);
        return result;
    }
    
    public void close() throws IOException {
        dbgMsg("close()");
        mStream.close();
    }
           
    public void mark(int readlimit) {
        dbgMsg("mark("+readlimit+")");
        mStream.mark(readlimit);
    }
    
    public boolean markSupported() {
        dbgMsg("markSupported()");
        return mStream.markSupported();
    }
    
    public int read() throws IOException {
        dbgMsg("read()");
        return mStream.read();
    }
 
    public int read(byte[] b) throws IOException {
        int result = mStream.read(b); 
        dbgMsg("read("+b+")=="+result);
        return result; 
    }
 
    public int read(byte[] b, int off, int len) throws IOException {        
        int result = mStream.read(b, off, len);
        dbgMsg("read(["+b.length+"],"+off+","+len+")=="+result+") "+((long)(System.nanoTime()-mStartTime)/10000));
        return result;
    }
 
    public void reset() throws IOException {
        dbgMsg("reset()");
        mStream.reset();
    }

    public long skip(long n) throws IOException {
        dbgMsg("skip ("+n+")");
        return mStream.skip(n);
    }
    
    private void dbgMsg(String pMsg) {
        Logger.info("LoggingInputStream: "+pMsg);
    }
        
    public static void main(String[] args) throws IOException {        
        IOUtils.readLines(new LoggingInputStream(System.in));        
    }
}
