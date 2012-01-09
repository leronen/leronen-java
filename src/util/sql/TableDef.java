package util.sql;

import java.util.*;

import util.CollectionUtils;
import util.ConversionUtils;

public class TableDef {

    private String mName;
    private List<ColumnDef> mColumns;
    private String mUpdateKey;
    private String mKey;
    
    // derived data:
    private Set<String> mColumnNames;
    
    protected TableDef(String pName) {
        mName = pName;
        mColumns = new ArrayList<ColumnDef>();
        mColumnNames = new HashSet();
    }
        
    
    public TableDef(String pName, ColumnDef... pColumnDefs) {
        mName = pName;
        mColumns = CollectionUtils.makeList(pColumnDefs);
        mColumnNames = new HashSet(ConversionUtils.convert(mColumns, new ColumnDef.NameExtractor()));
    }
    
    protected void setUpdateKey(String pColumnName) {
        mUpdateKey = pColumnName;
    }
    
    protected void setKey(String pColumnName) {
        mKey = pColumnName;
    }
    
    protected void addColumn(ColumnDef pColDef) {
        mColumns.add(pColDef);
        mColumnNames.add(pColDef.getName());
    }     
    
    public String getName() {
        return mName;
    }
    
    public ColumnDef getColumn(String pColName) {
        // just find from the list
        for (ColumnDef colDef: mColumns) {
            if (colDef.getName().equals(pColName)) {
                return colDef;
            }
        }
        throw new RuntimeException("No such column: "+pColName);
    }
    
    public List<ColumnDef> getColumns() {
        return Collections.unmodifiableList(mColumns);
    }        
    
    public int getNumColumns() {
        return mColumns.size();
    }
    
    public boolean containsColumn(String pColName) {
        return mColumnNames.contains(pColName);
    }
    
    public List<String> getColumnNames() {
        
        return ConversionUtils.convert(mColumns, new ColumnDef.NameExtractor());
    }


    public String getUpdateKey() {
        return mUpdateKey;
    }
    
    public String getKey() {
        return mKey;
    }
    
    
        
}
 