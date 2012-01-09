package util.matrix;

import util.*;
import java.util.*;

/** Index set representing all cells of a column */ 
public final class ColumnIndexSet implements IndexSet {
    
    private int mCol;
    private int mNumRows;
    
    /** the ranges include range.start and exclude range.end */
    public ColumnIndexSet(int pCol, int pNumRows) {
        mCol = pCol;
        mNumRows = pNumRows;                            
    }
    
   /**
    * Return collection of Index objects.
    *
    * Note the horribly ineffecient implementation... 
    */
    Collection indexCollection() {
        Range xRange = new Range(mCol, mCol+1);
        Range yRange = new Range(0, mNumRows);
        IndexRange range = new IndexRange(xRange, yRange);  
        return range.indexCollection();        
    }
    
    public Iterator iterator() {    
        // call a horribly ineffecient implementation...
        return indexCollection().iterator();        
    }
            
}
