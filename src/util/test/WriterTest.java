package util.test;

import java.io.PrintWriter;

import util.Range;
import util.dbg.Logger;

/**
 * Johtopäätös: char[]:ia ei voi järkevästi tulostaa writerillä,
 * ellei siellä oikeasti ole jotain järkellisiä stringejä (ei siis
 * voi vaan lapioida esim. mielivaltaista int[]-taulukkoa sinne...)
 * @author leronen
 *
 */
public class WriterTest {
    
    public static void main(String[] args) {
        int[] intData = new  Range(40,1000).asIntArr();
        char[] charData = new char[intData.length];
        for (int i=0; i<intData.length; i++) {
            charData[i]=(char)charData[i];
        }
        Logger.info("Chardata has "+charData.length+" elements.");
        
        PrintWriter writer = new PrintWriter(System.out);
        writer.println("foo");
        writer.println(charData);
        writer.println("bar");
        writer.close();
        
    }
}
