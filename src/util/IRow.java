package util;


public interface IRow {
    public String get(int col);
    public String get(String colName);
    /** may return null */
    public ColumnsDef getColumnsDef();            
}
