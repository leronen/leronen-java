package util.io;

import util.*;
import util.Timer;
import java.util.*;
import java.io.*;

/** 
 * Unixin sort on jyrätty!
 */
public class FileSorter2 {
    
	public static void main(String[] args) {
		try {
			Timer.startTiming("reading");
			String[] lines = IOUtils.readLineArray(System.in);
			Timer.endTiming("reading");
			
			Timer.startTiming("sorting");
			Arrays.sort(lines);
			Timer.endTiming("sorting");
			
			Timer.startTiming("writing");
			BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(System.out));
			for (String line: lines) {
				bufWriter.write(line);
				bufWriter.write("\n");
				// System.out.println(line);
			}
			bufWriter.flush();			
			Timer.endTiming("writing");
			
			System.err.println(Timer.stringRep());
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
	}
            
}
