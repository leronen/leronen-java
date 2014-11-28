package util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import util.collections.IndexMap;

/** Simple relation with named columns, using Strings to store values. */
public class Relation {
    private final List<List<String>> rows;
    private final IndexMap<String> columnIndex;
    /** Only exists if a key column is defined. Currently, only single-key data is supported */
    private IndexMap<String> rowIndex = null;
    private final EmptyValuePolicy emptyValuePolicy;
    /** may be null */
    private List<String> keyColumns;
    private String name;

    public List<List<String>> rows() {
        return rows;
    }

    public Relation(List<String> titleRow, EmptyValuePolicy emptyValuePolicy) {
        this.rows = new ArrayList<>();
        this.columnIndex = new IndexMap<>(titleRow);
        this.emptyValuePolicy = emptyValuePolicy;
    }

    public Relation(List<String> titleRow, List<List<String>> dataRows, EmptyValuePolicy emptyValuePolicy) {
        this.rows = dataRows;
        this.columnIndex = new IndexMap<>(titleRow);
        this.emptyValuePolicy = emptyValuePolicy;
    }

    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = new ArrayList<>(keyColumns);
    }

    public void setName(String name) {
        this.name = name;
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

    /** Build row index according to a given key column. Only single-key indexing is currently supported. */
    public void indexByColumn(String keyColumn) {
        this.rowIndex = buildIndex(keyColumn);
    }

    public void addRow(String... row) {
        addRow(Arrays.asList(row));
    }

    /** TODO: number of columns should be validated */
    public void addRow(List<String> row) {
        this.rows.add(row);
    }

    private IndexMap<String> buildIndex(String keyColumn) {
        int keyCol = columnIndex.getIndex(keyColumn);
        List<String> keys = new ArrayList<>(rows.size());
        for (int i=0; i<rows.size(); i++) {
            if (rows.get(i).size() != columnIndex.size()) {
                throw new RuntimeException("Invalid number of columns on row " + (i+1) +": " + rows.get(i).size() +
                                          " (expected " + columnIndex.size() + ")");
            }
            keys.add(rows.get(i).get(keyCol));
        }
        return new IndexMap<>(keys);
    }

    public List<String> getColumns() {
        return columnIndex.asList();
    }

    public int getNumColumns() {
        return columnIndex.size();
    }

    /** replace all values with encoded versions DEBUG ONLY ! */
    public void encodeAll(String encoding) throws UnsupportedEncodingException {
        for (int row=0; row<rows.size(); row++) {
            for (int col=0; col<columnIndex.size(); col++) {
                String original = get(row, col);
                byte[] bytes = original.getBytes("ISO-8859-15");
                List<Byte> byteList = new ArrayList<>();
                for (int i=0; i<bytes.length; i++) {
                    byteList.add(bytes[i]);
                }
                String rep = StringUtils.colToStr(byteList, " ");
                set(row, col, rep);
            }
        }
    }

    public void appendColumn(String name, List<String> data) {
        if (data.size() != getNumRows()) {
            throw new RuntimeException("Column to be added has wrong number of rows");
        }
        columnIndex.append(name);
        for (int row=0; row<data.size(); row++) {
            rows.get(row).add(data.get(row));
        }
    }

    /** Add new column containing encoding values for given column. New column named like <column>_<encoding> */
    public void encodeColumn(String encoding, String column) throws UnsupportedEncodingException {

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

    /** DO NOT MODIFY RETURNED ROW! */
    public List<String> getRow(int row) {
        return Collections.unmodifiableList(rows.get(row));
    }

    /**
     * Get a the value of a single column for a single row.
     * @param rowKey value of a key column that uniquely identifies a row. Said column must be indexed using
     * method {@link #buildIndex(String)} prior to calling this method.
     * @param columnName obvious
     * @return null if no such column. For interpretation of empty values in the file, see {@link EmptyValuePolicy}.
     * @throws RuntimeException if no such rowKey exists in the set of indexed keys.
     */
    public String get(String rowKey, String columnName) {
        int row = rowIndex.getIndex(rowKey);
        Integer column = columnIndex.getIndex(columnName);
        if (column != null) {
            return get(row, column);
        }
        else {
            return null;
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

    /**
     * For interpretation of empty values in the file, see {@EmptyValuePolicy}
     */
    private String get(int row, int col) {
        String val = rows.get(row).get(col);
        if (val != null && val.length() == 0 && emptyValuePolicy == EmptyValuePolicy.NULL) {
            val = null;
        }
        return val;
    }

    /** row must pre-exist */
    private void set(int row, int col, String val) {
        rows.get(row).set(col, val);
    }

    @SuppressWarnings("unused")
    private void set(int row, String colName, String val) {
        int col = columnIndex.getIndex(colName);
        set(row, col, val);
    }

    public String get(List<String> row, String colName) {
        int col = columnIndex.getIndex(colName);
        return row.get(col);
    }

    public boolean hasColumn(String columnName) {
        return columnIndex.contains(columnName);
    }

    public void writeToStream(PrintStream ps) {
        ps.println(StringUtils.colToStr(columnIndex.asList(), "\t"));
        for (List<String> row: rows) {
            ps.println(StringUtils.colToStr(row, "\t"));
        }
    }

    /**
     * As data is represented in textual form as a BCOS formatted file, it is unclear how
     * empty values should be interpreted. Clearly, either:
     * <ul>
     *   <li>NULL: empty strings are allowed as valid values and it is not possible to specify null value for a string</li>
     *   <li>EMPTY_STRING: empty strings are interpreted as a null value, and it is not possible to have an empty string as a value.</li>
     * </ul>
     */
    public enum EmptyValuePolicy {
        NULL,
        EMPTY_STRING
    }
}

