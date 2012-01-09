package util.io;

import util.*;
import util.dbg.*;

import java.util.*;
import java.io.*;

/** 
 * Class that does sorting without reading the whole file into memory at once
 * Only does numeric sorting at the moment.
 * Only sorts according to first col at the moment.
 *
 * Motivation: I cannot make the unix "sort" utility to sort by first col only; it insists on 
 * taking next columns into account as well. Petteri agrees on this problem!
 * 
 * Todo: menee perseelleen, kun filessä on headeri; file positionit menee sekasin, nääs. Ei jaksa säätää.
 */
public class FileSorter {
    
    /** 
     * Sorts file according to first column(columns separated by white space).
     * Only does numeric sorting at the moment.
     * @param pReverse if true, rows having largest values come first instead of rows with smallest values.
     */
    public static void sortFile(String fileName, PrintStream pOstream, boolean pReverse, int pCol, boolean pIncludesHeader) throws IOException {
        ArrayList lineLengthsList = new ArrayList();    
        ArrayList ordering = new ArrayList();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));         
        if (pIncludesHeader) {
            String header = reader.readLine();
            pOstream.println(header);            
        }                                       
        String line = reader.readLine();
        int lineNum = 0;
        while (line!=null) {
            // dbgMsg("read line: "+line);
            lineLengthsList.add(new Integer(line.length()));
            String[] cols = StringUtils.split(line);                                                                                        
            double d = Double.parseDouble(cols[pCol]);
            IntDoublePair orderingObject = new IntDoublePair(lineNum, d);
            ordering.add(orderingObject);
            
            // proceed to next line
            line = reader.readLine();
            lineNum++;
        }
        reader.close();
        Collections.sort(ordering);
        if (pReverse) {
            Collections.reverse(ordering);
        }                
        
        int[] lineLengths = ConversionUtils.integerCollectionToIntArray(lineLengthsList);
        int[] linePositions = new int[lineLengths.length];
        int numLines = lineLengths.length;
        linePositions[0]=0;
        for(int i=1; i<numLines; i++) {             
            linePositions[i]=linePositions[i-1]+lineLengths[i-1]+1;
        }
        // pOstream.println("line lengths: "+StringUtils.arrayToString(lineLengths, " "));
        // pOstream.println("line positions: "+StringUtils.arrayToString(linePositions, " "));        
        RandomAccessFile file = new RandomAccessFile(fileName, "r");        
        for(int i=0; i<numLines; i++) {
            IntDoublePair orderingObject = (IntDoublePair)ordering.get(i);
            lineNum = orderingObject.mInt;
            file.seek(linePositions[lineNum]);  
            byte[] buf = new byte[lineLengths[lineNum]];
            file.read(buf, 0, lineLengths[lineNum]);            
            // String s = new String(buf);
            pOstream.write(buf, 0, buf.length);
            pOstream.println();             
        }
    }
         
                                   
    public static void main (String[] args) {
        Logger.setProgramName("java.util.io.FileSorter");
        CmdLineArgs argParser = new CmdLineArgs(args);
        String fileName = argParser.shift("file name"); // first non-opt arg
        if (fileName == null) {
            dbgMsg("Usage: java.util.fileSorter filename (writes to STDOUT)");
        }                                
        String colString = argParser.getOpt("col");
        int col = Integer.parseInt(colString);
        boolean reverse = argParser.isDefined("reverse");
        boolean includesheader = argParser.isDefined("includesheader");                    
        String mode = argParser.getOpt("mode");
        if (mode!=null) {
            throw new RuntimeException("option mode not implemented; nowadays just considers the sort col to be numeric data");
        }
        
        try {
            sortFile(fileName, System.out, reverse, col, includesheader);                            
        }
        catch (Exception e) {
            e.printStackTrace();
        }            
    }
        
    private static void dbgMsg(String pMsg) {
        Logger.dbg("FileSorter: "+pMsg);
    }
            
}
