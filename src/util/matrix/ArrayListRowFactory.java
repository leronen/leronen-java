package util.matrix;

import util.*;
import util.dbg.*;

import java.util.*;


/** 
 * A factory for rows that do not know their format. These are just ArrayLists 
 * An ArrayListRowFactory may or may not have a row format, according to which the rows are parsed.
 * In the latter case, the rows are parsed just simply as white-space-delimited String fields.
 */
public class ArrayListRowFactory implements RowFactory  {
     
     /** note: -1 indicates that the number of fields is not fixed */
     private int mNumFields;     
     private RowFormat mFormat;

     private String columnSeparatorRegex = "\\s+";
     
    /** 
     * No enforced row format
     * note: -1 indicates that the number of fields is not fixed
     */                
    public ArrayListRowFactory(int pNumFields) {
        mNumFields = pNumFields;
        mFormat = null;
    }

    /** No enforced row format */                
    public ArrayListRowFactory(RowFormat pFormat) {
        mNumFields = pFormat.getNumFields();
        mFormat = pFormat;
    }                

    public void setSeparator(String separatorRegex) { 
    	columnSeparatorRegex = separatorRegex;
    }
    
    public String getSeparator() {
    	return columnSeparatorRegex;
    }
    
    /** With no data */
    public List makeRow() {
        ArrayList row = new ArrayList(mNumFields);        
        for (int i=0; i<mNumFields; i++) {
            row.add(null);                
        }
        return row;
    }
    
    /** With data from pRowToClone */        
    public List makeRow(List pRowToClone) {
        // note: -1 indicates that the number of fields is not fixed
        if (mNumFields != -1 && pRowToClone.size() != mNumFields) {
            throw new RuntimeException("Cannot make row: numFiedlds = "+mNumFields+" != numTokens = "+pRowToClone.size());
        }
            
        return new ArrayList(pRowToClone);
    }            
            
    public List makeRow(String pDataString) {
        ArrayList row;
        if (mFormat != null) {
            // we have a format
            row = new ArrayList(mFormat.getNumFields());                    
            dbgMsg("makeRow("+pDataString+","+mFormat+")");                                        
            int[] groups = mFormat.getNumFieldsArray();
            dbgMsg("Groups of the format: "+StringUtils.collectionToString(ConversionUtils.asList(groups), " "));
            dbgMsg("Row("+pDataString+","+mFormat+")");                               
            String[] tokens = StringUtils.split(pDataString, columnSeparatorRegex, groups);
            dbgMsg("Tokens: "+StringUtils.arrayToString(tokens, "\n"));            
            for (int i=0; i<tokens.length; i++) {
                row.add(mFormat.makeFieldRep(i, tokens[i]));
            }
        }
        else {
            // we do not have a format
            String[] tokens = pDataString.split(columnSeparatorRegex);
            // note: -1 indicates that the number of fields is not fixed!
            if (mNumFields != -1 && tokens.length != mNumFields) {
                throw new RuntimeException("Cannot make row: numFieds = "+mNumFields+" != numTokens = "+tokens.length);
            }
            else {
                row = new ArrayList(Arrays.asList(tokens));
            }
        }
        return row;                                          
    }

    public String toString() {
    	return "ArrayListRowFactory, separator: <"+getSeparator()+">";
    }
    
    /** For Converter-compatibility; just calls makeRow */
    public Object convert(Object pObj) {
        return makeRow((String)pObj);   
    }
     
    private void dbgMsg(String pMsg){
        Logger.dbg("ArrayListRowFactory"+pMsg);
    }        

              
    
}
