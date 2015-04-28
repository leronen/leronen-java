package util;

import java.util.List;

import util.sql.TableDef;

/** 
 * Generic interface for a relation with named columns. Does not contain type or key info. 
 * See also subinterface {@link TableDef} that has detailed information
 */
public interface ColumnsDef {
    public List<String> getColumnNames();
    /** @return null if no such column */
    public Integer getColumnIndex(String columnName);
}

