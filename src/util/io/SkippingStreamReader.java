package util.io;

import java.io.IOException;
import java.io.InputStream;

import util.dbg.Logger;

/**
 * Thread for reading a stream in an disinterested way, in other words stream is redirected to some
 * equivalent of /dev/null within java.
 * 
 * Listener is notified once everything has been read.
 * 
 * Stops on first exception, notifying the listener appropriately. Caller is responsible for closing the 
 * input stream once done (one of handle(XXXexception) methods called, or noMoreObjects() called.
 */
public class SkippingStreamReader implements Runnable {
    
    private InputStream is;
    private Listener listener;    
    
    public SkippingStreamReader(InputStream is, Listener listener) {
        this.is = is;
        this.listener = listener;        
    }
            
    public void run() {
        
        log("Starting run()");
        
        try {
            log("Reading...");
            @SuppressWarnings("unused")
            long n;
            while ((n = is.skip(2048)) > 0) {
                // no action            
//                log("Skipped "+n+" bytes");
            }        
                                   
            listener.noMoreBytesInStream();
        }
        catch (IOException e) {
            listener.handle(e);
        }                
        catch (RuntimeException e) {
            listener.handle(e);
        }
        
        log("Finished run()");
    }       
    
    private void log(String msg) {
        Logger.info(this.getClass().getSimpleName()+": "+msg);
    }
    
    public interface Listener {    
        /** Called when nothing more to read from stream */
        public void noMoreBytesInStream();        
        public void handle(IOException e);               
        public void handle(RuntimeException e);
    }
}
