package util.matrix;

import util.*;
import java.util.*;

/** Index set representing all cells of a row */ 
public final class RowIndexSet implements IndexSet {
    
    private int mRow;
    private int mNumCols;
    
    /** the ranges include range.start and exclude range.end */
    public RowIndexSet(int pRow, int pNumCols) {
        mRow = pRow;
        mNumCols = pNumCols;                            
    }
    
   /**
    * Return collection of Index objects.
    *
    * Note the horribly ineffecient implementation... 
    */
    Collection indexCollection() {
        Range xRange = new Range(0, mNumCols);
        Range yRange = new Range(mRow, mRow+1);
        IndexRange range = new IndexRange(xRange, yRange);  
        return range.indexCollection();        
    }
    
    public Iterator iterator() {    
        // call a horribly ineffecient implementation...
        return indexCollection().iterator();        
    }
            
}
