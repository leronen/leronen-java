package util.matrix;

import util.*;

import java.util.*;


/** The default row Factory */
public class DefaultRowFactory implements RowFactory  {
    
    protected RowFormat mRowFormat;
    
    public DefaultRowFactory(RowFormat pFormat) {
        if (pFormat == null) {
            throw new RuntimeException("Format cannot be null!");
        }
        mRowFormat = pFormat;
    }
    
    public List makeRow() {
        ArrayList row = internalCreateInstance();
        for (int i=0; i<mRowFormat.getNumFields(); i++) {
            row.add(null);                
        }   
        return row;
    }
    
    /** With data from pRowToClone */        
    public List makeRow(List pRowToClone) {
        ArrayList row = internalCreateInstance();
        row.addAll(pRowToClone);
        return row;
    }                
    
    /** Altough cannot be ensured by interface, the returned objects should be instances of Row */
    public List makeRow(String pDataString) {
        Row row = internalCreateInstance();        
        // dbgMsg("makeRow("+pDataString+","+mRowFormat+")");
        int[] groups = mRowFormat.getNumFieldsArray();
        // dbgMsg("Groups of the format: "+StringUtils.collectionToString(ConversionUtils.asList(groups), " "));
        // dbgMsg("Row("+pDataString+","+mRowFormat+")");                                        
        String[] tokens = StringUtils.split(pDataString, "\\s+", groups);
        // dbgMsg("Tokens: "+StringUtils.arrayToString(tokens, "\n"));
        // ArrayList fields = new ArrayList(mRowFormat.getNumFields());
        for (int i=0; i<tokens.length; i++) {
            row.add(mRowFormat.makeFieldRep(i, tokens[i]));
        }         
        return row;                                 
    }
    
    protected Row internalCreateInstance() {
        return new Row(mRowFormat, false);    
    }
    
    /** For Converter-compatibility; just calls makeRow */
    public Object convert(Object pObj) {
        return makeRow((String)pObj);   
    }
    
    public String toString() {
        return "DefaultRowFactory";
    }        

              
    
}
