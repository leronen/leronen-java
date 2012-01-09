package util.matrix;

import util.*;
import java.util.*;

public final class IndexRange implements IndexSet {

    private Range mXRange;
    private Range mYRange;

    /** the ranges include range.start and exclude range.end */
    public IndexRange(Range pXRange, Range pYRange) {
        mXRange = pXRange;
        mYRange = pYRange;            
    }
    
    /** not private, as ColumnIndexSet and RowIndexSet use this as helper method */
    Collection indexCollection() {
        ArrayList indices = new ArrayList(mXRange.length() * mYRange.length());
        for (int i=mYRange.start; i<mYRange.end; i++) {
            for (int j = mXRange.start; j<mXRange.end; j++) {
                indices.add(new Index(i, j));
            }
        }
        return indices;
    }
    
    public Iterator iterator() {
        // call a horribly ineffecient implementation...
        return indexCollection().iterator();        
    }
            
}
