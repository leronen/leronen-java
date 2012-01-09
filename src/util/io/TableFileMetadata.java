package util.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.IOUtils;
import util.StringUtils.UnexpectedNumColumnsException;
import util.collections.Pair;

public class TableFileMetadata {
	protected String fileName;
    protected String originalHeader;
    protected int numCols;
    protected ColumnSeparator columnSeparator;
    protected List<String> columnNames;
    protected Map<String, Integer> columnByName;
    protected String chrFromFileName;    
    protected boolean deducedChrFromFileName;
    
    /** No action, subclasses are responsible for initializing all fields */
    protected TableFileMetadata() {
    	// no action 
    }
        
    public TableFileMetadata(String pFilename) throws IOException, UnexpectedNumColumnsException {
        fileName = pFilename;
        String header = IOUtils.readFirstLine(new File(pFilename));
        Pair<ColumnSeparator, Integer> tmp = deduceSeparatorAndNumCols(header);
        columnSeparator = tmp.getObj1();
        numCols = tmp.getObj2();
        String[] colNamesArr= new String[numCols];
        columnSeparator.split(header, colNamesArr);
        columnNames = Arrays.asList(colNamesArr);            
        columnByName = makeColumnByNameMap(columnNames);
    }
    
    protected Map<String, Integer> makeColumnByNameMap(List<String> pColNames) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (int i=0; i<pColNames.size(); i++) {
            String cn = pColNames.get(i);
            result.put(cn, i);
        }
        return result;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
       
    /** return -1 if no such column */
    public int getColumnInd(String columnName) {
    	Integer result = columnByName.get(columnName);
    	if (result != null) {
    		return result;
    	}
    	else {
    		return -1;
    	}
    }
    
    public String getColumnName(int index) {
    	return columnNames.get(index);
    }
    
    /** Get the (original) header line of the file */
    public String getHeaderLine() {
        return originalHeader;
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

