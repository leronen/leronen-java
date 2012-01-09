package util.test;

import java.io.*;

import util.Timer;

public class BufferedOutputTest {
        
    
    public static void main(String[] args) {
        setFastOutput();
        run();
    }
    
    public static void setFastOutput() {
        FileOutputStream fdout = new FileOutputStream(FileDescriptor.out);
        BufferedOutputStream bos = new BufferedOutputStream(fdout, 16384);
        PrintStream ps = new PrintStream(bos, false);        
        System.setOut(ps);        
    }
    
    public static void run() {
        Timer.startTiming(Timer.KEY_TOTAL);      
                        
        int N = 100000;
        
        for (int i = 1; i <= N; i++)
          System.out.println(i);

        Timer.endTiming(Timer.KEY_TOTAL);        
        Timer.logToStdErr();
        
        System.out.close();        
    }
        
    
}
