package util.io;

import java.io.IOException;
import java.util.*;

import util.IOUtils;
import util.StringUtils;
import util.IOUtils.LineIterator;
import util.StringUtils.UnexpectedNumColumnsException;


/**
 * Reader for tab-,white-space- and comma-delimited data files with a header row.
 * 
 * Main use case is reading bc-formatted files.
 * 
 * Simple example usage to read marker name and position from a file (exception handling omitted for clarity):   
 *   
 *    TableFileReader reader = new TableFileReader(file, "MARKER", "DIST");
 *    while (reader.hasNextLine()) {
 *        reader.readLine();
 *        String name = reader.get("MARKER");
 *        String bp = reader.get("DIST");
 *        System.out.println(name+"\t"+bp);
 *    }
 *       
 */  
public class TableFileReader {
         
    TableFileMetadata meta;
	
    /** Always columns resulting from the last readline */
    private StringBuffer[] curColumns;
    
    /** Always read one line ahead into this buffer */
    private StringBuffer[] nextColumns;
    
    /** iterator for actual, unprocessed lines of input */
    private LineIterator lineIter;
    
    /** Actually, a projection into these columns. */ 
    private int[] columnsOfInterest;

    private String curLine;
    private String nextLine;
    
    private int lineNr = 0;
    
    public TableFileReader(String pFileName) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {
        this(pFileName, (List<String>)null);
    }
           
    
    public TableFileReader(String pFileName, String... pColumnsOfInterest) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {
        this(pFileName, Arrays.asList(pColumnsOfInterest));
    }
           
        
    public TableFileReader(String pFileName, List<String> pColumnsOfInterest) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {    	
    	meta = new TableFileMetadata(pFileName);
        // dbgMsg("Metadata: "+meta);
        // System.err.println("Creating curColumns and nextColumns with "+meta.getNumCols()+" columns");
        curColumns = new StringBuffer[meta.getNumCols()];
        nextColumns = new StringBuffer[meta.getNumCols()];
        if (pColumnsOfInterest != null) {
            // only read columns of interest        	
            List<Integer> columnsOfInterestList = new ArrayList<Integer>();
            for (String colName: pColumnsOfInterest) {
                // dbg("Handling coltype: "+colType);
                int col = meta.getColumnInd(colName);
                if (col == -1) {
                	throw new NoSuchColumnException("No such column: "+colName+" in file: "+pFileName);
                }
                			
                // reserve buffers for parsing the column of interest 
                curColumns[col] = new StringBuffer();
                nextColumns[col] = new StringBuffer();
                columnsOfInterestList.add(col);                                    
            }
            columnsOfInterest = new int[columnsOfInterestList.size()];
            for (int i=0; i<columnsOfInterestList.size(); i++) {
                columnsOfInterest[i] = columnsOfInterestList.get(i);
            }
        }
        else {
            // read all columns
            columnsOfInterest = new int[meta.getNumCols()];
            for (int i=0; i<columnsOfInterest.length; i++) {
                columnsOfInterest[i]=i;
            }
            
            // reserve buffers for parsing the columns
            for (int i=0; i<curColumns.length; i++) {            	
                curColumns[i] = new StringBuffer();
                nextColumns[i] = new StringBuffer();
            }
            
        }
        
        lineIter = new LineIterator(pFileName);         
     
        // skip header row        
        lineIter.next();
        
        
        // read first line into nextColumns
        internalReadLine();        
    }       
        
    public TableFileMetadata getMetadata() {
        return meta;
    }

    public int getLineNr() {
        return lineNr;
    }
    
    
    /**
     * Make a header row that only contains columns of interest, if ones are specified.
     * Use the same separator as in the original file (multiple white spaces are converted into one space)
     */
    public String makeHeaderRow() {
    	return makeHeaderRow(meta.columnSeparator);
    }
    
    /** Make a header row that only contains columns of interest, if ones are specified */
    public String makeHeaderRow(ColumnSeparator separator) {
                                               
        StringBuffer buf = null;
        for (int i=0; i<columnsOfInterest.length; i++) {
        	int ind = columnsOfInterest[i];
            String columnName = meta.getColumnName(ind);            
            
            if (buf == null) {
                buf = new StringBuffer(""+columnName);
            }
            else {
                buf.append(separator.getPrintableValue());
                buf.append(columnName);
            }
        }
        
        return buf.toString();       
    }
    
    // read data from next line of input into nextColumns
    private void internalReadLine() throws UnexpectedNumColumnsException {
        if (!(lineIter.hasNext())) {
            // no more lines in input
            nextColumns = null;
            return;
        }

        curLine = nextLine;
        nextLine = lineIter.next();                   
        meta.getColumnSeparator().split(nextLine, nextColumns);        
        
        lineNr++;
    }    
    
    public void readLine() throws UnexpectedNumColumnsException, NoSuchElementException {
        
        if (nextColumns == null) {
            throw new NoSuchElementException();
        }
        
        // swap role or curColumns and NextColumns
        StringBuffer[] tmp = curColumns;
        curColumns = nextColumns;
        nextColumns = tmp;
        
        // read next line of input into nextColumns, or set it to null, if no more lines in input        
        internalReadLine();               
    }
    
    public String getCurrentLine() {
        return curLine;
    }
    
    public boolean hasNextLine() {
        return nextColumns != null;
    }          
    
    /** get value of column by index */
    public String getValue(int col) {
        return curColumns[col].toString();
    }                 
      
    /** Project current line to columns of interest */
    public String project() {
    	String separator = meta.columnSeparator.getPrintableValue();
    	if (columnsOfInterest != null) {
    		StringBuffer buf = new StringBuffer();
            for (int i=0; i<columnsOfInterest.length; i++) {
            	buf.append(curColumns[columnsOfInterest[i]].toString());
                if (i < columnsOfInterest.length-1) {
                    buf.append(separator);
                }
            }
            return buf.toString();
        }
        else {
            return StringUtils.arrayToString(curColumns, separator);
        }
    }
    
    /** Output the read file in BCOS format */ 
    public void outputDataLines() throws UnexpectedNumColumnsException {    
        IOUtils.setFastStdout();
        while (hasNextLine()) {
            readLine();
            String line = project();
            System.out.println(line);
        }
        System.out.flush();
    }
    
    /**
     * Unit test by performing a simple projection on a file. If no columns given, output all columns.
     * Use the notorious ISO-8859-1 encoding.
     */ 
    public static void main(String[] args) throws Exception {
    	TableFileReader reader;
    	if (args.length == 0) {
    		System.err.println("No input file!");
    		System.exit(1);
    		return;
    	}
    	else if (args.length == 1) {
    		reader = new TableFileReader(args[0]);
    	}
    	else {
    		// a projection
    		List<String> argsList = Arrays.asList(args);
    		String file = argsList.get(0);
    		List<String> columns = argsList.subList(1, argsList.size());
    		reader = new TableFileReader(file, columns);    		
    	}            
    	
        System.out.println(reader.makeHeaderRow());
        reader.outputDataLines();
        System.out.flush();    	
    }

    @SuppressWarnings("serial")
    public class NoSuchColumnException extends Exception {
        
        public NoSuchColumnException (String message) {
            super(message);
        }       
        
    }
}

