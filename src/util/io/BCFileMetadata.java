package util.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import util.IOUtils;
import util.collections.IndexMap;
import util.collections.Pair;
import util.StringUtils.UnexpectedNumColumnsException;

public class BCFileMetadata {
	protected String fileName;
	/** The original header in the file as is */
    protected String header;
    protected int numCols;
    protected ColumnSeparator columnSeparator;
    protected String chrFromFileName;    
    protected boolean deducedChrFromFileName;
    /** 
     * Mapping from column indices to column names.
     * Currently duplicates data in columnNames and columnByName */ 
    protected IndexMap<String> columnMap;
    
    /** No action, subclasses are responsible for initializing all fields */
    protected BCFileMetadata() {
    	// no action 
    }
            
    
    public BCFileMetadata(String pFilename) throws IOException, UnexpectedNumColumnsException {
        fileName = pFilename;
        header = IOUtils.readFirstLine(new File(pFilename));
        Pair<ColumnSeparator, Integer> tmp = deduceSeparatorAndNumCols(header);
        columnSeparator = tmp.getObj1();
        numCols = tmp.getObj2();
        String[] colNamesArr= new String[numCols];
        columnSeparator.split(header, colNamesArr);
//        columnNames = Arrays.asList(colNamesArr);            
//        columnByName = makeColumnByNameMap(columnNames);
        columnMap = new IndexMap<String>(Arrays.asList(colNamesArr)); 
    }
    
    public String getFilename() {
    	return fileName;
    }
    
//    protected Map<String, Integer> makeColumnByNameMap(List<String> pColNames) {
//        Map<String, Integer> result = new HashMap<String, Integer>();
//        for (int i=0; i<pColNames.size(); i++) {
//            String cn = pColNames.get(i);
//            result.put(cn, i);
//        }
//        return result;
//    }
    
    
    /** list of column names in file */ 
    public List<String> getColumnNames() {
        return columnMap.asList();
    }
       
    /** return -1 if no such column */
    public int getColumnInd(String columnName) {
    	Integer result = columnMap.getIndex(columnName);
    	if (result != null) {
    		return result;
    	}
    	else {
    		return -1;
    	}
    }
    
    public String getColumnName(int index) {
    	return columnMap.asList().get(index);
    }
    
    /** Get the (original) header line of the file */
    public String getHeader() {
        return header;
    }    
    
    public int getNumCols() {
        return numCols;
    }
    
    public ColumnSeparator getColumnSeparator() {
        return columnSeparator;
    }
    
    /**
     * Return TAB as the separator for single-column files. Also returns numcols.
     */
    public static Pair<ColumnSeparator, Integer> deduceSeparatorAndNumCols(String header) {
        int numColsWithWhiteSpaces = header.split("\\s+").length;
        int numColsWithSpace = header.split(" ").length;
        int numColsWithTab = header.split("\\t").length;
        int numColsWithComma = header.split(",").length;
        if (numColsWithWhiteSpaces == 1) {
            if (numColsWithComma > 1) {
                // white space did not split, comma did. Must be comma-separated                
                return new Pair<ColumnSeparator, Integer>(ColumnSeparator.COMMA, numColsWithComma);
            }
            else {
                // must be single column, lets just return TAB, as it will not matter anyway
            	return new Pair<ColumnSeparator, Integer>(ColumnSeparator.TAB, 1);                
            }
        }
        else {
            // multiple columns
            if (numColsWithSpace == numColsWithWhiteSpaces) {
                // spaces suffice to split
            	return new Pair<ColumnSeparator, Integer>(ColumnSeparator.SPACE, numColsWithSpace);
            }
            else if (numColsWithTab == numColsWithWhiteSpaces) {
                // tabs suffice to split
            	return new Pair<ColumnSeparator, Integer>(ColumnSeparator.TAB, numColsWithTab);               
            }
            else {
                // must use general white space parsing
            	return new Pair<ColumnSeparator, Integer>(ColumnSeparator.WHITE_SPACES, numColsWithWhiteSpaces);                
            }
        }               
    }
    
    
}
