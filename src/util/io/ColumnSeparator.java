package util.io;

import util.StringUtils;
import util.StringUtils.UnexpectedNumColumnsException;


public enum ColumnSeparator {    
    SPACE(" "),
    TAB("\t"),
    COMMA(","),
    WHITE_SPACES(" ");
        
    private String printableValue; 
    
    ColumnSeparator(String printableValue) {
        this.printableValue = printableValue;
    }
    
    public void split(String line, String[] result) throws UnexpectedNumColumnsException {
        if (this == SPACE || this == TAB || this == COMMA) {
            // separator is a single char, we can use StringUtils.fastSplit here!
            char separatorChar;
            switch(this) {
                case SPACE: 
                    separatorChar = ' ';                 
                    break;               
                case TAB:   
                    separatorChar = '\t';
                    break;
                case COMMA:
                    separatorChar = ',';
                    break;
                default:
                    throw new RuntimeException("Unknown Column Separator type: "+this); 
            }
                
            StringUtils.fastSplit(line, separatorChar, result);
        }
        else if (this == WHITE_SPACES) {
            // result buffer is actually a nuisance here, but let's do it so
            // for API compalibity (probably WHITE_SPACES is a rare case in real data?)
            String[] res = line.split("\\s+");
            if (result.length != res.length) {
                throw new StringUtils.UnexpectedNumColumnsException(line, res.length, result.length);
            }
            for (int i=0; i<result.length; i++) {
                result[i] = res[i];
            }
        }
        else {
            throw new RuntimeException("Unknown Column Separator type: "+this);
        }
    }
    
    public String[] split(String line)  {
        if (this == SPACE || this == TAB || this == COMMA) {
            // separator is a single char, we can use StringUtils.fastSplit here!
            char separatorChar;
            switch(this) {
                case SPACE: 
                    separatorChar = ' ';                 
                    break;               
                case TAB:   
                    separatorChar = '\t';
                    break;
                case COMMA:
                    separatorChar = ',';
                    break;
                default:
                    throw new RuntimeException("Unknown Column Separator type: "+this); 
            }
                
            return StringUtils.fastSplit(line, separatorChar);
        }
        else if (this == WHITE_SPACES) {
            // result buffer is actually a nuisance here, but let's do it so
            // for API compalibity (probably WHITE_SPACES is a rare case in real data?)
            return line.split("\\s+");            
        }
        else {
            throw new RuntimeException("Unknown Column Separator type: "+this);
        }
    }
    
    /** Split line; get only columns for which the corresponding buffer is not null */
    public void split(String line, StringBuffer[] result) throws UnexpectedNumColumnsException {
        if (this == SPACE || this == TAB || this == COMMA) {
            // separator is a single char, we can use StringUtils.fastSplit here!
            char separatorChar;
            switch(this) {
                case SPACE: 
                    separatorChar = ' ';                 
                    break;               
                case TAB:   
                    separatorChar = '\t';
                    break;
                case COMMA:
                    separatorChar = ',';
                    break;
                default:
                    throw new RuntimeException("Unknown Column Separator type: "+this); 
            }
                
            StringUtils.fastSplit(line, separatorChar, result);
        }
        else if (this == WHITE_SPACES) {
            // result buffer is actually a nuisance here, but let's do it so
            // for API compalibity (probably WHITE_SPACES is a rare case in real data?)
            String[] res = line.split("\\s+");            
            if (result.length != res.length) {
                throw new StringUtils.UnexpectedNumColumnsException(line, res.length, result.length);
            }
//             System.err.println("Both result and res have len: "+result.length);
            for (int i=0; i<result.length; i++) {            	
            	if (result[i] != null) {
            		// only get columns of interest!
            		result[i].setLength(0);
            		result[i].append(res[i]);
            	}
            }
        }
        else {
            throw new RuntimeException("Unknown Column Separator type: "+this);
        }
    }
    
    public String getPrintableValue() {
        return printableValue;
    }
        
}

