package util.matrix;

import util.*;

/** 
 * support for converting between Ranges and matrix indices 
 * Idea: row=pattern start, col=pattern len-1
 */
public class RangeIndexMapper implements IndexMapper {    
    
    /** otherwise col is end position -1 */
    boolean mColsLengthMinus1;
    
    public RangeIndexMapper(boolean pXIsLengthMinus1) {
       mColsLengthMinus1 = pXIsLengthMinus1;
    }
    
    public Object matrixIndexToIndexObject(int pRow, int pCol) {
        int rangeStart = pRow;
        int rangeEnd;
        if (mColsLengthMinus1) {            
            int rangeLen = pCol+1;
            rangeEnd = rangeStart+rangeLen;                                        
        }
        else {
            // col is the end position-1
            rangeEnd = pCol+1;
        }
        return new Range(rangeStart, rangeEnd);                        
    }
        
    public Index indexObjectToMatrixIndex(Object p) {
        Range range = (Range)p;        
        int row = range.start;
        int col;
        if (mColsLengthMinus1) {                                                                            
            col = range.length()-1;
        }
        else {
            // col is the end position-1
            col = range.end-1;            
        }                        
        
        return new Index(row, col);
    }       

}
