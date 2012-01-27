package util.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import util.IOUtils;
import util.dbg.DevNullLogger;
import util.dbg.ILogger;

/**
 * Uses a dedicated thread (automatically created at constructor) to send queued byte array messages to a socket passed in at constructing time. Closes output 
 * stream of socket after exiting main loop (caller should not close socket until being notified about this!).
 * Also, output stream of socket should not be used by other threads while the sender thread is running.
 * Does not close the output stream, that is to be done by the caller. 
 */
public class NonBlockingSender {

    private ILogger log;
    
    /** Special "message" that is used to stop execution of sender thread */ 
    private static final byte[] POISON = new byte[0];
    
    private String name;
    private Listener listener;
    private OutputStream os;    
    private Thread senderThread;    
    private BlockingQueue<byte[]> messageQueue;
    private boolean stopped;
    // null if stopped cleanly because of being requested by calling {@link #stop()} 
    private Exception stopCause;
    
    public void setName(String name) {
        this.name = name; 
    }
    
    /** 
     * Listener should only close output stream of socket after receiving a finished notification,
     * so having the listener is mandatory. 
     */
    public NonBlockingSender(Socket socket, Listener listener) throws IOException {
        this(socket,listener,null);
    }
    
    /** 
     * Listener should only close output stream of socket after receiving a finished notification,
     * so having the listener is mandatory. 
     */
    public NonBlockingSender(Socket socket, Listener listener, ILogger log) throws IOException {
        if (listener == null) {
            throw new IOException("Null listener!");
        }
        
        if (log != null) {
            this.log = log;
        }
        else {
            this.log = DevNullLogger.SINGLETON;
        }
        
        this.name = "NonBlockingSender-"+socket.getRemoteSocketAddress();
        this.listener = listener;
        this.stopped = false;        
        this.messageQueue = new LinkedBlockingQueue<byte[]>();
        this.os = socket.getOutputStream();    
                          
        // Create sender and receiver threads responsible for performing the I/O.
        log("Creating sender thread");
        this.senderThread = new Thread(new Runner(), name);        
        log("Starting sender thread");
        this.senderThread.start();
    }
    
    /** 
     * Add a specific POISON message to the send queue, which is interpreted as a
     * request to stop the sender thread (once all previous packets have been sent.
     * Any further attempts to send will raise IOExceptions
     */     
    public void requestStop() {
        if (!stopped) { 
            messageQueue.add(POISON);
        }
    }
    
    /** 
     * Put message to queue of messages to be sent and return immediately
     * (assume queue has unlimited capacity).
     * 
     * @throws IOException in the lack of a suitable exception type. In practice, there
     * are two cases: sender thread has already been stopped for some reason,
     * or sender thread was interrupted. In both cases, the cause is stored
     * to the IOException. 
     */
    public synchronized void send(byte[] msg) throws IOException {
        
        if (stopped) {
            throw new IOException("Sender thread has been stopped", stopCause);
        }
        
        try {
            messageQueue.put(msg);
        }
        catch (InterruptedException e) {
            // should not occur
            error("NonBlockingSender send interrupted, giving up", e);            
            senderThread.interrupt();
            stopCause = e;
            throw new IOException("Sender thread interrupted", e);
        }
    }    
    
    private void mainLoop() {

        while (!stopped) {
            try {
                byte[] msg = messageQueue.take(); // Will block until a message is available.
                if (msg == POISON) {
                    dbg("Received stop request");
                    stopped = true;
                    continue;
                }
                try {
                    dbg("Writing a message of "+msg.length+" bytes");
                    IOUtils.writeBytes(os, msg);
                    os.flush();
                    dbg("Wrote "+msg.length+" bytes");
                } catch (IOException e) {
                    error("Failed writing, giving up", e);                    
                    stopped = true;
                    stopCause = e;
                }                                            
            }
            catch (InterruptedException e) {
                error("NonBlockingSender main loop interrupted, giving up", e);
                // should not occur?!
                stopped = true;
                stopCause = e;
            }
        }
        
        dbg("Ended main loop");
        listener.senderFinished();
    }
        
    public String toString() {
        return name;
    }
    
    public interface Listener {
        /** Called in all of following circumstances:
         *  <pre>
         *   -sender requested stopping (all stuff sent before stop request has been delivered to stream)
         *   -IOException while reading
         *   -InterruptedException while reading
         *  </pre>
         * 
         * Make this interface more fine-grained if needed later.
         */
        public void senderFinished();
    }
    
    private class Runner implements Runnable {
        public void run() {
            mainLoop();
        }
    }
            
    
    @SuppressWarnings("unused")
    private void error(String msg) {
        log.error(name+": "+msg);
    }
    
    private void error(String msg, Exception e) {
        log.error(name+": "+msg, e);
    }
    
    private void log(String msg) {
        log.info(name+": "+msg);
    }
    
    private void dbg(String msg) {
        log.dbg(name+": "+msg);
    }
        
}
