package util.matrix;

import util.*;
import java.util.*;

/** Index set representing all cells of a matrix */ 
public final class TotalIndexSet implements IndexSet {
    
    private int mNumCols;
    private int mNumRows;
    
    /** the ranges include range.start and exclude range.end */
    public TotalIndexSet(int pNumRows, int pNumCols ) {
        mNumRows = pNumRows;                       
        mNumCols = pNumCols;             
    }
    
    public TotalIndexSet(Matrix pMatrix) {
        mNumRows = pMatrix.getNumRows();                       
        mNumCols = pMatrix.getNumCols();             
    }
    
   /**
    * Return collection of Index objects.
    *
    * Note the horribly ineffecient implementation... 
    */
    Collection indexCollection() {
        Range xRange = new Range(0, mNumCols);
        Range yRange = new Range(0, mNumRows);
        IndexRange range = new IndexRange(xRange, yRange);  
        return range.indexCollection();        
    }
    
    public Iterator iterator() {    
        // call a horribly ineffecient implementation...
        return indexCollection().iterator();        
    }
            
}
