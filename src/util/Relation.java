package util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.collections.IndexMap;
import util.condition.Condition;
import util.converter.Converter;
import util.converter.MapConverter;
import util.dbg.ILogger;
import util.io.NoSuchColumnException;



/**
 * Simple relation with named columns, using Strings to store values. Stored into main memory.
 *
 * Note that often rows are implemented as {@link Row} instances (Row extends List<String>).
 * 
 * <h2>TODO</h2>
 * <ul>
 * <li> Add possibility to use a row factory (now wrapping row data (List<String>) as Row or some subclass has to be done by clients.
 * <li> clarify copy / reference semantics when adding rows
 * <li> clarify policy of Lists used as row implementation regarding whether new columns can be added to them or not
 * <li> enable enforcing uniqueness of keys
 * <li> enable indexing by multiple columns
 * <li> extend implementation to handle columns with arbitrary type * 
 * <li> cache keys as List<String> instances
 * </ul>
 */
public class Relation {
    private List<List<String>> rows;
    private IndexMap<String> columnIndex;
    private IndexMap<String> rowIndex = null; // Only exists if indexing method (TODO: method name) has been called
    private List<String> keyColumns;          // may be null
    private String name;

    /** may be null. TODO: switch to more generic ColumnsDef? */
    private ColumnsDef columnsDef;

    public List<List<String>> rows() {
        return rows;
    }

    /** 
     * Return rows of the relation as instances of a given Row subclass. The rows must actually already be known to be 
     * instances of that subclass; this method is needed just because that information cannot (at least currently) be 
     * stored into the Relation itself.
     * 
     * Usage example:
     * <br>
     * <br>
     * <code> 
     * List&lt;MyRow&gt; myRows = relation.&lt;MyRow&gt;castRows();
     * </code>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Row> List<T> castRows() {                            
        return (List)rows();
    }
    
    /** 
     * Return single row of the relation as instances of a given Row subclass. 
     * The rows must actually already be known to be  instances of that subclass; 
     * this method is needed just because that information cannot (at least currently) be 
     * stored into the Relation itself; parametrizing class Relation based on Row implementation could
     * be considered (or parametrizing some subclass).
     * 
     * Usage example:
     * <br>
     * <br>
     * <code> 
     * MyRow myRow = relation.castRow(0);
     * </code>
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Row> T castRow(int row) {                            
        return (T)rows.get(row);
    }
    
    private static ILogger logger;

   /**
    * Enable logging using zero-dependencies logger interface that can be used also in GWT code.
    * Implementations provided at least for stderr logging and firebug logging.
    */
    public static void setLogger(ILogger logger) {
        Relation.logger = logger;
    }

    private void debug(String msg) {
        if (logger != null) {
            logger.dbg(msg);
        }
    }
    
    @SuppressWarnings("unused")
    private void info(String msg) {
        if (logger != null) {
            logger.info(msg);
        }
    }
    
    public Relation(String... columnNames) {
        this(Arrays.asList(columnNames));
    }

    /** 
     * Check if the data in the relation corresponds to the given TableDef. Following conditions are checked:
     * <ul>
     * <li> Relation needs to contain exactly the columns defined in the table def (they do not need to be in the same order) 
     * <li> Text values must not exceed maximum length or corresponding columns.
     * <li> Values of numeric columns must indeed be numeric
     * <li> File must not contain duplicate keys.
     * </ul>
     *
     * TODO: check that NOT NULL columns do not get null values; what about empty strings...?
     * TODO: what about precision / limits of numeric values?
     * TODO: replace FileFormatException with something more suitable
     * 
     * @throws FileFormatException on any validation errors.
     */
    public void validate(TableDef tableDef) throws FileFormatException {
        int nrows = getNumRows();
        int ncols = getNumColumns();
    
        Set<String> relationColumnNames = new HashSet<>(columnIndex.asList());
        Set<String> tableColumnNames = new HashSet<>(tableDef.getColumnNames());
        if (!relationColumnNames.equals(tableColumnNames)) {
            throw new FileFormatException("Relation column names differ from table column names"); // TODO: explain difference in message
        }
        
        // check uniqueness of primary keys        
        Set<String> primaryKeyColumns = new LinkedHashSet<>(tableDef.getPrimaryKeyColumnNames());
        Set<List<String>> primaryKeys = new HashSet<>(getNumRows());
        for (int row=0; row<nrows; row++) {
            List<String> rowKey = projectRow(row, primaryKeyColumns);
            if (primaryKeys.contains(rowKey)) {
                throw new FileFormatException("Duplicate occurence of primary key on row " + (row+1) +
                                              "; key: " + StringUtils.colToStr(rowKey, ", ")); 
            }
            primaryKeys.add(rowKey);
        }                
                
        // proceed to check data, column by column
        for (int col=0; col<ncols; col++) {
            String columnName = columnIndex.get(col);
            ColumnDef columnDef = tableDef.getColumn(columnName); 
            ColumnType columnType = columnDef.getType();
            boolean partOfPrimaryKey = primaryKeyColumns.contains(columnName);
            Integer maxLength = (columnType == ColumnType.STRING) ? columnDef.getMaxLength() : null;
            for (int row=0; row<nrows; row++) {                
                String value = get(row, col);
                boolean emptyOrNull = (value == null || value.length() == 0) ? true : false;
                if (partOfPrimaryKey) {
                    if (emptyOrNull) {
                        throw new FileFormatException("Empty value for primary key constituent column " + columnName + " on row " + row);
                    }                                        
                }
                else {
                    // not part of primary key
                    if (emptyOrNull) {
                        // no further checks for empty or null values
                        continue;
                    }
                }
                
                if (!columnType.checkValue(value)) {
                    throw new RuntimeException("Invalid value of column " + columnName + " on row " + row + "; not a " + columnType);
                }
                
                if (columnType == ColumnType.STRING && maxLength != null && value.length() > maxLength) {
                    throw new RuntimeException("Too long value for column " + columnName + " on row " + row + "; must not exceed " + maxLength);
                }               
            }
        }
    }
    
    public Relation(List<String> columnNames) {
        try {
            init(columnNames, new ArrayList<List<String>>());
        }
        catch (FileFormatException e) {
            throw new RuntimeException("not possible");         
        }
    }

    public Relation(ColumnsDef def) {
        try {
            init(def.getColumnNames(), new ArrayList<List<String>>());
            this.columnsDef = def;
        }
        catch (FileFormatException e) {
            throw new RuntimeException("not possible");
        }
    }

    private void init(List<String> columnNames, List<List<String>> dataRows) throws FileFormatException { 
        this.columnIndex = new IndexMap<>(new ArrayList<>(columnNames));
        this.rows = dataRows;
        for (int i=0; i<rows.size(); i++) {
            if (rows.get(i).size() != columnIndex.size()) {
                throw new FileFormatException("Invalid number of columns on line " + (i+1) + ":" + rows.get(i).size() + 
                                              "; expecting " + columnIndex.size());
            }
        }
    }    
     
    private Relation() {
        // must be populated by calling init()  
    }
    
    public static Relation create(List<String> columnNames, List<List<String>> dataRows) throws FileFormatException {
        Relation relation = new Relation();
        relation.init(columnNames, dataRows);
        return relation;
    }
        
    /** 
     * Replace values in a single column of the relation.
     * @throws RuntimeException if parameter data does not equal number of rows in the relation, or if no such column.  
     */
    public void replaceColumn(String colName, List<String> data) {
        if (!columnIndex.contains(colName)) {
            throw new RuntimeException("No such column: " + colName);
        }
        
        if (getNumRows() != data.size()) {
            throw new RuntimeException("Invalid number of elements in parameter data: " + data.size() + "; expecting " + getNumRows());                                        
        }
        
        int column = this.columnIndex.getIndex(colName);
        for (int row=0; row<data.size(); row++) {
            set(row, column, data.get(row));
        } 
    }

    public void setPrimaryKeyColumns(String... keyColumns) {
        setPrimaryKeyColumns(Arrays.asList(keyColumns));
    }

    public void setPrimaryKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    public Set<List<String>> getKeySet() {
        return project(keyColumns, new HashSet<List<String>>(getNumRows()));
    }

    public List<List<String>> getKeyList() {
        if (keyColumns == null) {
            throw new RuntimeException("No key columns defined");
        }

        return this.project(keyColumns, new ArrayList<List<String>>());
    }


   /** 
    * If param keyColumns == null, do nothing. It is possible that a relation does not have any key columns.
    */
    public void setKeyColumns(List<String> keyColumns) {
        if (keyColumns == null) {
            return;
        }
        this.keyColumns = new ArrayList<>(keyColumns);
    }

    public void setName(String name) {
        this.name = name;
    }

    private class ColumnsDefImpl implements ColumnsDef {

        @Override
        public List<String> getColumnNames() {
            return columnIndex.asList();
        }

        @Override
        public Integer getColumnIndex(String columnName) {
            return columnIndex.getIndex(columnName);
        }
        
    }
    
    public ColumnsDef getColumnsDef() {
        if (columnsDef == null) {
            columnsDef = new ColumnsDefImpl();
        }
        
        return columnsDef;
    }

    /**
     * Constructs the list of indices from scratch, so do not use for repeated calls.
     * @return list (0, 1, 2, ... , numrows - 1)
     */
    public List<Integer> getRowIndices() {
        return new Range(0, getNumRows()).asIntegerList();
    }

    /** Extract one column from the relation */
    public List<String> project(String colname) {
        int numrows = getNumRows();
        List<String> result = new ArrayList<>(numrows);
        for (int row=0; row<numrows; row++) {
            result.add(get(row, colname));
        }
        return result;
    }
    
    /**
     * Extract the one and only column from the relation.
     * 
     * @throws RuntimeException
     *             should there be more than one column, in contrast to expectations.
     */
    public List<String> projectToSingletonColumn() {
        if (getNumColumns() != 1) {
            throw new RuntimeException("Cannot project relation to singleton column; there are more than one columns");
        }

        int numrows = getNumRows();
        List<String> result = new ArrayList<>(numrows);
        for (int row = 0; row < numrows; row++) {
            result.add(get(row, 0));
        }

        return result;
    }

    /** Project relation to given columns. Return nulls for columns not found in the original relation */
    public <T extends Collection<List<String>>> T project(List<String> colnames, T result) {
        int numrows = getNumRows();
        for (int i=0; i<numrows; i++) {
            List<String> row = new ArrayList<>(colnames.size());
            for (String column: colnames) {
                row.add(get(i, column));
            }
            result.add(row);
        }

        return result;
    }
    
    /**
     * TODO: This implementation requires constructing new instances of ArrayList, which may be costly.
     * Should manage row keys by some other means, possibly by always using. Also, should make it possible 
     * to use a more efficient implementation for single-key (also two, three, ...-key) relations.  
     */
    private List<String> projectRow(int row, Collection<String> colnames) {
        List<String> result = new ArrayList<>(colnames.size());
        for (String column: colnames) {
            result.add(get(row, column));
        } 
        return result;
    }
    
    /** Project relation to given columns. Return nulls for columns not found in the original relation */
    public Relation project(List<String> columnNames) {
        try {
            return Relation.create(columnNames, projectToRowList(columnNames));
        }
        catch (FileFormatException e) {
            // should not be possible when creating relation from an existing one
            throw new RuntimeException();
        }
    }
    
    /** Extract columns from the relation */
    private List<List<String>> projectToRowList(List<String> colnames) {
        int numrows = getNumRows();
        List<List<String>> result = new ArrayList<>(numrows);
        return project(colnames, result);
    }

    /** Convenience method for selecting rows where given column has given value */
    public Relation simpleSelect(String column, String val) {
        Relation result = new Relation(getColumns());
        int nrows = getNumRows();
        for (int row=0; row<nrows; row++) {
            if (get(row, column).equals(val)) {
                result.addRow(getRow(row));
            }
        }

        return result;
    }

   /**
    * Create new relation with identical columns and only rows that meet given Condition.
    * Condition does the filtering based on row numbers. Naturally the Condition needs
    * to know about the relation in order to perform filtering based on the row number.
    */
    public Relation filter(Condition<Integer> condition) {
        Relation filtered = new Relation(getColumns());
        filtered.setKeyColumns(keyColumns);
        int nrows = getNumRows();
        for (int i=0; i<nrows; i++) {
            if (condition.fulfills(i)) {
                filtered.addRow(getRow(i));
            }
        }
        return filtered;
    }

    /** Return new relation containing only rows in the given key set */
    public Relation select(Set<List<String>> keyset) {
        return filter(new KeysetCondition(keyset));
    }

    public Map<List<String>, List<String>> getRowsByKeyColumnsMap() {
        HashMap<List<String>, List<String>> result = new HashMap<>();
        int nrows = getNumRows();
        List<List<String>> keyList = project(keyColumns, new ArrayList<List<String>>(nrows));
        for (int row = 0; row<nrows; row++) {
            result.put(keyList.get(row), getRow(row));
        }
        return result;
    }

    /**
     * Return numbers of rows that meet given Condition.
     *
     * Condition does the filtering based on row numbers. Naturally the Condition needs
     * to know about the relation in order to perform filtering based on the row number.
     *
     * @return set of row numbers, ordered in ascending numeric order.
     */
     public Set<Integer> filterRows(Condition<Integer> condition) {
         LinkedHashSet<Integer> result = new LinkedHashSet<Integer>();
         int nrows = getNumRows();
         for (int row=0; row<nrows; row++) {
             if (condition.fulfills(row)) {
                 result.add(row);
             }
         }
         return result;
     }

    public String getName() {
        if (name != null) {
            return name;
        }
        else {
            return "(" + StringUtils.colToStr(getColumns(), ",") + ")";
        }
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        else {
            return "(" + StringUtils.colToStr(getColumns(), ",") + ")";
        }
    }

    /**
     * Build row index according to a given column. Only single-key indexing is currently supported.
     * 
     * Index is later used in method {@link #get(String rowKey, String columnName)}
     * 
     * @throws RuntimeException if values in given column are not unique.
     */
    public void indexByColumn(String indexColumn) {
        this.rowIndex = buildIndex(indexColumn);
    }

    /** @throws RuntimeException if number of columns is not correct */
    public void addRow(String... row) {
        addRow(Arrays.asList(row));
    }

    /** @throws RuntimeException if number of columns is not correct */
    public void addRow(List<String> row) {
        if (row.size() != getNumColumns()) {
            throw new RuntimeException("Invalid number of columns in row to be added");
        }

        this.rows.add(row);
    }

   /**
    * @throws DuplicateKeyException if values in given column are not unique.
    */
    private IndexMap<String> buildIndex(String keyColumn) {
        int keyCol = columnIndex.getIndex(keyColumn);
        int ncols = getNumColumns();
        List<String> keys = new ArrayList<>(rows.size());
        for (int i=0; i<rows.size(); i++) {
            // TODO: remove this validation once data insertion methods ensure correct number of columns
            if (rows.get(i).size() != ncols) {
                throw new RuntimeException("Invalid number of columns on row " + (i+1) +": " + rows.get(i).size() +
                                          " (expected " + ncols + ")");
            }
            String key = rows.get(i).get(keyCol);            
            keys.add(key);
        }     
        try {
            return new IndexMap<>(new ArrayList<>(keys));
        }
        catch (DuplicateKeyException e) {
            Object duplicateKey = e.getKey();
            throw new DuplicateKeyException("Duplicate key for column to be indexed: " + duplicateKey + 
                                            " (column: " + keyColumn + ")");
        }
    }

    public List<String> getColumns() {
        return columnIndex.asList();
    }

    public int getNumColumns() {
        return columnIndex.size();
    }

   /**
    * Replace all values with converted versions. Note that converter must generally saying
    * support null values, unless of course one knows that there will not be null values in the relation.
    */
    public void convertValues(Converter<String,String> converter) {
        int nrows = getNumRows();
        int ncols = getNumColumns();
        for (int row=0; row<nrows; row++) {
            for (int col=0; col<ncols; col++) {
                String original = get(row, col);
                String converted = converter.convert(original);
                set(row, col, converted);
            }
        }
    }

    /**
     * Replace all values with converted versions. Note that converter must generally saying
     * support null values, unless of course one knows that there will not be null values in the relation.
     */
     public void convertValues(Converter<String,String> converter, Set<Integer> rows) {
         int ncols = getNumColumns();
         for (int row: rows) {
             for (int col=0; col<ncols; col++) {
                 String original = get(row, col);
                 String converted = converter.convert(original);
                 set(row, col, converted);
             }
         }
     }

    /**
     * Temporary hack to convert integers represented with decimal part to just integers.
     * Wont be needed when DB types are used while reading data.
     */
    public void beautifyNumericValues(TableDef tableDef) {
        for (String colname: getColumns()) {
            ColumnDef colDef = tableDef.getColumn(colname);
            if (colDef == null) {
                throw new RuntimeException("No definition for column: " + colname);
            }
            ColumnType colType = colDef.getType();
            if (colType == ColumnType.INTEGER) {
                int nrows = getNumRows();
                for (int row=0; row<nrows; row++) {
                    String original = get(row, colname);
                    if (original != null) {
                        Long val = Long.parseLong(original);
                        set(row, colname, ""+val);
                    }
                }
            }
        }
    }


    /**
     * Temporary hack to convert floats with integral value by dropping the unneeded decimal part.
     * Fails for empty strings.
     */
    public void beautifyIntegralFloats(TableDef tableDef) {
        for (String colname: getColumns()) {
            ColumnDef colDef = tableDef.getColumn(colname);
            ColumnType colType = colDef.getType();
            if (colType == ColumnType.FLOAT) {
                int nrows = getNumRows();
                for (int row=0; row<nrows; row++) {
                    String original = get(row, colname);
                    if (original != null) {
                        double d = Double.parseDouble(original);
                        if (MathUtils.isInteger(d)) {
                            long l = (long)d;
                            String newval = ""+l;
                            set(row, colname, newval);
                        }
                    }
                }
            }
        }
    }

    /**
     * Replace all values in given column with converted versions.
     */
    public void convertValues(String colname, Converter<String,String> converter) {
        int nrows = getNumRows();
        for (int row=0; row<nrows; row++) {
            String original = get(row, colname);
            String converted = converter.convert(original);
            set(row, colname, converted);
        }
    }

    public void convertValues(String colname, Converter<String,String> converter, Set<Integer> rows) {
        for (int row: rows) {
            String original = get(row, colname);
            String converted = converter.convert(original);
            set(row, colname, converted);
        }
    }

    /** Append column with null values */
    public void appendColumn(String name) {
        int nrows = getNumRows();
        ArrayList<String> column = new ArrayList<>(nrows);
        for (int i=0; i<nrows; i++) {
            column.add(null);
        }
        appendColumn(name, column);
    }

    public void appendColumn(String name, List<String> data) {
        if (data.size() != getNumRows()) {
            throw new RuntimeException("Column to be added has wrong number of rows. "+
                                       "Expected:" + getNumRows() + ", got: " + data.size());
        }
        columnIndex.append(name);
        for (int row=0; row<data.size(); row++) {
            rows.get(row).add(data.get(row));
        }
    }

    /** Add new column containing encoding values for given column. New column named like <column>_<encoding> */
    public void addEncodedColumn(String encoding, String column) throws UnsupportedEncodingException {

        List<String> newColumnData = new ArrayList<>(getNumRows());
        for (int row=0; row<rows.size(); row++) {
            String original = get(row, column);
            byte[] bytes = original.getBytes("ISO-8859-15");
            List<Integer> intVals = new ArrayList<>();
            for (int i=0; i<bytes.length; i++) {
                int asInt = bytes[i] & 0xff;
                intVals.add(asInt);
            }
            String rep = StringUtils.colToStr(intVals, " ");
            newColumnData.add(rep);
        }
        String newColumnName = column +  "_" + encoding;
        appendColumn(newColumnName, newColumnData);
    }
    
    public List<String> getRow(int row) {
        return rows.get(row);
    }

    /** Check if a row with given key (in previously indexed column) exists */
    public boolean hasKey(String key) {
        if (rowIndex == null) {
            throw new RuntimeException("Rows not indexed");
        }
        return rowIndex.contains(key);
    }
    
    /**
     * Get a the value of a single column for a single row.
     * @param rowKey value of a key column that uniquely identifies a row. Said column must be indexed using
     * method {@link #buildIndex(String)} prior to calling this method. Note that only one can be indexed, using 
     * currrent implementation (this could be easily changed).
     * @param columnName obvious
     * @return null if no such column.
     * @throws RuntimeException if no such rowKey exists in the set of indexed keys, or if rows have not been indexed.
     */
    public String get(String rowKey, String columnName) {
        if (rowIndex == null) {
            throw new RuntimeException("Rows not indexed");
        }
            
        Integer row = rowIndex.getIndex(rowKey);
        if (row == null) {
            throw new RuntimeException("No such row key in set of indexed rows: " + rowKey);
        }
        
        Integer column = columnIndex.getIndex(columnName);
        if (column != null) {
            return get(row, column);
        }
        else {
            return null;
        }
    }
    

    /**
     * Get the column values of a single row. 
     * @param rowKey value of a key column that uniquely identifies a row. Said column must be indexed using
     * method {@link #buildIndex(String)} prior to calling this method. Note that currently only one column can be indexed
     * (this could be easily changed).
     * @return null if no such column.
     * @throws RuntimeException if no such rowKey exists in the set of indexed keys, or if rows have not been indexed.
     */
    public List<String> getRow(String rowKey) {
        if (rowIndex == null) {
            throw new RuntimeException("Rows not indexed");
        }
            
        Integer row = rowIndex.getIndex(rowKey);
        if (rowIndex == null) {
            throw new RuntimeException("No such row key in set of indexed rows: " + rowKey);
        }
        
        return rows.get(row);
    }
           
    
    /**
     * Set the value of a single column of a single row.
     * @param rowKey value of a key column that uniquely identifies a row. Said column must be indexed using
     * method {@link #buildIndex(String)} prior to calling this method. Note that currently only one column can be indexed
     * (this could be easily changed).
     * @param columnName obvious
     * @return null if no such column.
     * @throws RuntimeException if no such rowKey exists in the set of indexed keys, or if rows have not been indexed,
     *         or no such column exists.
     */
    public void set(String rowKey, String columnName, String value) {
        if (rowIndex == null) {
            throw new RuntimeException("Rows not indexed");
        }
            
        Integer row = rowIndex.getIndex(rowKey);
        if (row == null) {
            throw new NoSuchRowException("No such row key in set of indexed rows: " + rowKey);
        }
        
        Integer column = columnIndex.getIndex(columnName);
        if (column == null) {
            throw new NoSuchColumnException("No such column: " + columnName);            
        }
        
        set(row, column, value);
    }

    /**
     * Perform in-place sorting of rows according to numeric values in a given column.
     * The column must have non-null numeric values for all rows. Sorting order 
     * is undefined if values of given column are not unique.
     */
    public void sortNumeric(String columnName) {
        debug("Sorting by column: " + columnName);
        NumericComparator comparator = new NumericComparator(columnName);
        Collections.sort(rows, comparator);
        rowIndex = null;
    }

    /** @throws RuntimeException if more than one or no rows */
    public String getSingletonValue(String column) {
        if (getNumRows() != 1) {
            throw new RuntimeException("Illegal number of rows: " + getNumRows());
        }

        return get(0, column);
    }

    
    
    private class NumericComparator implements Comparator<List<String>> {
        int col;

        private NumericComparator(String colname) {
            this.col = columnIndex.getIndex(colname);

        }

        @Override
        public int compare(List<String> o1, List<String> o2) {
            Double val1 = Double.parseDouble(o1.get(col));
            Double val2 = Double.parseDouble(o2.get(col));
            return val1.compareTo(val2);
        }
    }


    /** @return null if no such column. See {@link get(int,int)} */
    public String get(int row, String columnName) {
        Integer column = columnIndex.getIndex(columnName);
        if (column != null) {
            return get(row, column);
        }
        else {
            return null;
        }
    }

    /** May return null if no keys defined */
    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public int getNumRows() {
        return rows.size();
    }

    public String get(int row, int col) {
        String val = rows.get(row).get(col);

        return val;
    }

    /** @throws RuntimeException if no such row or column */
    public void set(int row, int col, String val) {

        if (row < 0 || row >= getNumRows()) {
            throw new RuntimeException("No such row: " + row);
        }

        if (col < 0 || col >= getNumColumns()) {
            throw new RuntimeException("No such column: " + col);
        }

        rows.get(row).set(col, val);
    }

    /** @throws RuntimeException if no such row or column */
    public void set(int row, String colName, String val) {
        Integer col = columnIndex.getIndex(colName);
        if (col == null) {
            throw new RuntimeException("No such column: " + colName);
        }
        set(row, col, val);
    }

    /** @throws RuntimeException if no such row or column */
    public String get(List<String> row, String colName) {
        Integer col = columnIndex.getIndex(colName);
        if (col == null) {
            throw new RuntimeException("No such column: " + colName);
        }

        return row.get(col);
    }

    public boolean hasColumn(String columnName) {
        return columnIndex.contains(columnName);
    }

    /** Write to stream, representing null values with empty string */
    public void writeToStream(IWriter writer) {
        writeToStream(writer, "");
    }
    
    /** Write to stream, representing null values with empty string */
    public void writeToStream(PrintStream ps) {
        writeToStream(ps, "");
    }
    
    public void writeToStream(IWriter writer, String nullRep) {        
        List<String> columns = columnIndex.asList();
        
        MapConverter<String,String> nullConverter = 
            new MapConverter<String,String>(Collections.singletonMap((String)null, nullRep), 
                                            MapConverter.NotFoundBehauvior.RETURN_ORIGINAL); 
        
        writer.println(StringUtils.colToStr(columns, "\t"));
        for (List<String> row: rows) {
            writer.println(StringUtils.colToStr(row, "\t", nullConverter));
        }
    }
    
    public interface IWriter {
        void println(String line);
        void print(String string);
        void print(int id);
        void flush();
    }
    
    public void writeToStream(PrintStream ps, String nullRep) {        
        List<String> columns = columnIndex.asList();
        
        MapConverter<String,String> nullConverter = 
            new MapConverter<String,String>(Collections.singletonMap((String)null, nullRep), 
                                            MapConverter.NotFoundBehauvior.RETURN_ORIGINAL); 
        
        ps.println(StringUtils.colToStr(columns, "\t"));
        for (List<String> row: rows) {
            ps.println(StringUtils.colToStr(row, "\t", nullConverter));
        }
    }
    
    public void infoToLogger() {
        if (logger == null) {
            return;
        }
    
        logger.info(StringUtils.colToStr(columnIndex.asList(), "\t"));
        for (List<String> row: rows) {
            logger.info(StringUtils.colToStr(row, "\t"));
        }
    }
    
    public void debugToLogger() {
        if (logger == null) {
            return;
        }
    
        logger.dbg(StringUtils.colToStr(columnIndex.asList(), "\t"));
        for (List<String> row: rows) {
            logger.dbg(StringUtils.colToStr(row, "\t"));
        }
    }
    
    

    /** TODO: validate consistency with actual columns */
    public void setTableDef(TableDef tableDef) {
        this.columnsDef = tableDef;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Relation)) {
            return false;
        }
        Relation other = (Relation)o;

        if (this.getNumColumns() != other.getNumColumns()) {
            debug("Differing number of columns: " + this.getNumColumns() + ", "+ other.getNumColumns());
            return false;
        }

        if (this.getNumRows() != other.getNumRows()) {
            debug("Differing number of rows: " + this.getNumRows() + ", "+ other.getNumRows());
            return false;
        }

        if (!(this.columnIndex.equals(other.columnIndex))) {
            return false;
        }

        if (!(this.getKeyColumns().equals(other.keyColumns))) {
            return false;
        }

        if (!(this.rows.equals(other.rows))) {
            return false;
        }

        return true;
    }

    public class KeysetCondition implements Condition<Integer> {
        Set<List<String>> keyset;
        List<String> keyRow = new ArrayList<>(keyColumns.size()); // recycle

        public KeysetCondition(Set<List<String>> keyset) {
            this.keyset = keyset;
        }

        @Override
        public boolean fulfills(Integer row) {
            keyRow.clear();
            for (String keyColumn: keyColumns) {
                keyRow.add(get(row, keyColumn));
            }
            return keyset.contains(keyRow);
        }

    }

    
    
    /** 
     * Convert names of columns according to given mapping. Column names that are not present in the keyset 
     * of the given map are left as is.
     * 
     * @throws RuntimeException if map is null, of if the resulting column names would not be unique. 
     */
    public void convertColumnNames(Map<String, String> map) {        
        Converter<String,String> converter = new MapConverter<>(map, MapConverter.NotFoundBehauvior.RETURN_ORIGINAL);
        List<String> convertedColumnNames = ConversionUtils.convert(columnIndex.asList(), converter);
        if (convertedColumnNames.size() > new HashSet<>(convertedColumnNames).size()) {
            throw new RuntimeException("Conversion of column names would produce non-unique column names: " +  
                                       StringUtils.colToStr(convertedColumnNames, ", "));
        }
        columnIndex = new IndexMap<String>(convertedColumnNames);
        if (columnsDef != null) {
            if (columnsDef instanceof TableDef) {
                throw new RuntimeException("converting column names would invalidate table def");
            }
            else {
                columnsDef = null;
                // will be refreshed automatically if requested by clients
            }
        }
        
        if (keyColumns != null) {
            // also convert key columns
            keyColumns = ConversionUtils.convert(keyColumns, converter);
        }
    }

    /**
     * Caution: causes ColumnsDef object to replaced by a SimpleColumnsDef implementation, which may cause loss of data!
     * This is because interface ColumnsDef does not support renaming of columns, so we have to construct a new one. 
     */
    public void renameColumn(String oldName, String newName) {
        Integer i = columnIndex.getIndex(oldName);
        if (i == null) {
            throw new RuntimeException("No such column: " + oldName);
        }
        
        List<String> newColumns = new ArrayList<>(columnIndex.asList());
        newColumns.set(i, newName);
        
        if (columnsDef != null) {
            columnsDef = new DefaultColumnsDef(newColumns);
        }
    }

    public List<String> getIndexedKeys() {
        return rowIndex.asList();
    }
    
    public Map<String, Integer> getRowIndexMap() {
        return rowIndex.getMap();
    }
}

