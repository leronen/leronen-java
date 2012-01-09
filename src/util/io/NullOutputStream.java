package util.io;

import java.io.IOException;

public class NullOutputStream extends java.io.OutputStream {
    
    @Override
    public void write(byte[] b) {
        // no-op
    }

    @Override
    public void write(int arg0) throws IOException {
        // no-op
        
    }

}
