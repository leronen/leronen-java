package util;


import java.util.List;
import java.util.Map;

/**
 * In vast majority (all?) cases, implementations should extend {@link AbstractRowFactory}.
 * 
 * See for example {@link WebformsRow.Factory}.
 */
public interface RowFactory<T extends Row> {
    /** Must not return null */
    public ColumnsDef getColumns();
    
   /** 
    * Create row with given data values. Needless to say, values in the list must correspond
    * to the columns definition returned by {@link getColumns}
    */
    public T create(List<String> data);

    /**
    * Create row with given data values. Not all values need to be present. 
    * Needless to say, keys of map must be existing column names, as specified by the ColumnsDef.
    */
    public T create(Map<String,String> data);
    
    /** 
     * Create row with null data values.
     */
    public T create();
}

