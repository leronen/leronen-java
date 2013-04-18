package util.io;

import java.io.IOException;
import java.util.*;


import util.IOUtils;
import util.IOUtils.LineIterator;
import util.StringUtils;

import static util.StringUtils.UnexpectedNumColumnsException;

/**
 * Reader for table-like data files with a header row.
 * tab-,white-space- and comma-delimited files are supported.  
 * 
 * Simple example usage to extract marker name and position from 
 * a file (exception handling omitted for clarity):   
 *    BCFileReader reader = new BCFileReader(file, "MARKER", "DIST");
 *    while (reader.hasNextLine()) {
 *        reader.readLine();
 *        String name = reader.get("MARKER");
 *        String bp = reader.get("DIST");
 *        System.out.println(name+"\t"+bp);
 *    }
 *  
 * @see FileReader
 * @see FiletypeBasedFileReader
 */  
public class BCFileReader {
         
    BCFileMetadata meta;
	
    /** Field values from the last readline */
    private StringBuffer[] curColumns;
    
    /** Always read one line ahead into this buffer. */
    private StringBuffer[] nextColumns;
    
    /** iterator for actual, unprocessed lines of input. */
    private LineIterator lineIter;
    
    /** only these columns are read. */ 
    private int[] columnsOfInterest;

    private String curLine;
    private String nextLine;    
    private int lineNr = 0;
        
    
    public BCFileReader(String pFileName) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {
        this(pFileName, (List<String>)null);
    }    
               
    public BCFileReader(String pFileName, String... pColumnsOfInterest) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {
        this(pFileName, Arrays.asList(pColumnsOfInterest));
    }
    
        
    public BCFileReader(String pFileName, List<String> pColumnsOfInterest) throws IOException, StringUtils.UnexpectedNumColumnsException, NoSuchColumnException {    	
    	meta = new BCFileMetadata(pFileName);
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
    
    public String getFileName() {
    	return meta.getFilename();
    }
    
    /** Read all rows into a map indexed by the given field */
    public Map<String, Map<String,String>> readAsMapMap(String keyColumn) throws UnexpectedNumColumnsException, NoSuchElementException, IOException {
    	Map<String, Map<String,String>> result = new HashMap<String, Map<String,String>>();    	
                
        while (hasNextLine()) {
            readLine();
            Map<String,String> rowAsMap = getRowAsMap(); 
            String key = rowAsMap.get(keyColumn);
            result.put(key, rowAsMap);
        }        
        return result;            
    }
    
    /** Read all rows into a map indexed by the given field */
    public Map<List<String>, Map<String,String>> readAsMapMap(List<String> keyColumns) throws UnexpectedNumColumnsException, NoSuchElementException, IOException {
    	Map<List<String>, Map<String,String>> result = new HashMap<List<String>, Map<String,String>>();    	
                
        while (hasNextLine()) {
            readLine();
            Map<String,String> rowAsMap = getRowAsMap();
            List<String> key = new ArrayList<String>();
            for (String keyColumn: keyColumns) {
            	key.add(rowAsMap.get(keyColumn));
            }
            result.put(key, rowAsMap);
        }        
        return result;            
    }
        
    
    /** Read all rows into a map indexed by the given field */
    public Map<String, List<String>> readAsListMap(String keyColumn) throws UnexpectedNumColumnsException, NoSuchElementException, IOException {
    	Map<String, List<String>> result = new HashMap<String, List<String>>();    	
            
    	int keyInd = getMetadata().getColumnInd(keyColumn);
        while (hasNextLine()) {
            readLine();
            List<String> rowAsList = getRowAsList();            
            String key = rowAsList.get(keyInd);
            result.put(key, rowAsList);
        }        
        return result;            
    }
    
    public List<String> getRowAsList() {
    	List<String> row = new ArrayList<String>(columnsOfInterest.length);
    	for (int i: columnsOfInterest) {
    		int ind = columnsOfInterest[i];    		
    		String val;
    		try {
    			val = get(ind);
    		}
    		catch (NoSuchColumnException e) {
				// should not occur
    			throw new RuntimeException(e);
			}
    		row.add(val);
    	}
    	return row;
    }
    
    /**
     * Return the current row as map (iteration order of fields will be as in original file.
     * A new map is created for each call, so the caller is free to modify the returned map.
     */
    public Map<String,String> getRowAsMap() {    	
    	LinkedHashMap<String,String> row = new LinkedHashMap<String,String>(columnsOfInterest.length);
    	for (int i: columnsOfInterest) {
    		int ind = columnsOfInterest[i];
    		String key = meta.getColumnName(ind);
    		String val;
    		try {
    			val = get(ind);
    		}
    		catch (NoSuchColumnException e) {
				// should not occur
    			throw new RuntimeException(e);
			}
    		row.put(key, val);
    	}
    	return row;
    }
    
    public BCFileMetadata getMetadata() {
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
    
    /** Get header of original file */ 
    public String getHeader() {
    	return meta.getHeader();
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
    
    // set curline to nextline, read data from next line of input into nextLine and nextColumns
    private void internalReadLine() throws UnexpectedNumColumnsException {
    	curLine = nextLine;
    	
        if (!(lineIter.hasNext())) {
            // no more lines in input
            nextColumns = null;
            nextLine = null;
            return;
        }
        
        nextLine = lineIter.next();                   
        meta.getColumnSeparator().split(nextLine, nextColumns);        
        
        lineNr++;
    }    
    
    /**
     * Read next line. The line read last is to be accessed using methods such
     * as {@link #getString(String)} 
     * @throws UnexpectedNumColumnsException
     * @throws NoSuchElementException
     */
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
    public String get(int ind) throws NoSuchColumnException {
    	StringBuffer buf = curColumns[ind];
    	if (buf == null) {
    		throw new NoSuchColumnException("No such column in file "+meta.getFilename()+": "+ind);	
    	}
    	else {
    		return buf.toString();
    	}
    }                 
    
    /** get value of column by name */
    public String getString(String columnName) throws NoSuchColumnException {
        int ind = meta.getColumnInd(columnName);
        if (ind == -1) {
            throw new NoSuchColumnException("No such column in file "+meta.getFilename()+": "+columnName);
        }
        
        StringBuffer buf = curColumns[ind];
        if (buf == null) {
    		throw new NoSuchColumnException("No such column in file "+meta.getFilename()+": "+columnName);	
    	}        
        
        return buf.toString();        
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
            return StringUtils.arrToStr(curColumns, separator);
        }
    }
    
    /**
     * Read and output all (remaining) lines to stdout. Only columns of interest
     * are outputted. No header is outputted by this method.
     */ 
    public void readAndOutputDataLines() throws UnexpectedNumColumnsException {    
        IOUtils.setFastStdout();
        while (hasNextLine()) {
            readLine();
            String line = project();
            System.out.println(line);
        }
        System.out.flush();
    }

    /** read all (remaining) lines and write them to stdout. */
    public void cat() throws UnexpectedNumColumnsException {
    	while (hasNextLine()) {
    	    readLine();    		
    		System.out.println(getCurrentLine());
        }
    }
    
    /**
     * Unit test by performing a simple projection on a file. If no columns given, output all columns.
     */ 
    public static void main(String[] args) throws Exception {
    	BCFileReader reader;
    	if (args.length == 0) {
    		System.err.println("No input file!");
    		System.exit(1);
    		return;
    	}
    	else if (args.length == 1) {
    		reader = new BCFileReader(args[0]);
    	}
    	else {
    		// a projection
    		List<String> argsList = Arrays.asList(args);
    		String file = argsList.get(0);
    		List<String> columns = argsList.subList(1, argsList.size());
    		reader = new BCFileReader(file, columns);    		
    	}            
    	
        System.out.println(reader.makeHeaderRow());
        reader.readAndOutputDataLines();
        System.out.flush();    	
    }
        
}
