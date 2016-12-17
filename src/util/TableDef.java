package util;

import java.util.List;
import java.util.Map;

/** Generic interface for a database table or other such relation with named and typed columns */
public interface TableDef extends ColumnsDef {

    public String getName();
    /** column name => column def map */
    public Map<String, ? extends ColumnDef> getColumnMap();
    public List<? extends ColumnDef> getColumns();    
    public ColumnDef getColumn(String columnName);    
    public List<String> getPrimaryKeyColumnNames();
}

