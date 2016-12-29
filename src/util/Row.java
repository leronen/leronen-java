package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import util.converter.Converter;
import util.io.NoSuchColumnException;

/**
 * Simple row row of a database, relational file or other relational data.
 * Represents all values as Strings, so conversions have to be performed when interpreting data
 * or writing it to database.
 */
public class Row implements IRow, List<String> {
    
    protected final ColumnsDef columnsDef;
    private final List<String> data;

    /**
     * @throws RuntimeDataIntegrityException if number of columns in def and data do not match.
     */
    public Row(ColumnsDef columnsDef, List<String> data) {
        if (columnsDef.getColumnNames().size() != data.size()) {
            throw new RuntimeDataIntegrityException("Mismatching number of columns in def and data");
        }
        
        this.columnsDef = columnsDef;
        this.data = data;
    }

    /** Init with null values for all columns */
    public Row(ColumnsDef columnsDef) {
        this.columnsDef = columnsDef;
        this.data = new ArrayList<>(columnsDef.getColumnNames().size());
        for (int i=0; i<columnsDef.getColumnNames().size(); i++) {
            data.add(null);
        }
    }

    public List<String> getData() {
        return data;
    }
    
    /** @throws NoSuchColumnException */ 
    @Override
    public String get(int column) {
        if (column < 0 || column >= data.size()) {
            throw new NoSuchColumnException("No such column: " + column);
        }
        
        return data.get(column);
    }
    
    /** @throws NoSuchColumnException if no such column */
    public void set(String columnName, String value) {
        Integer column = columnsDef.getColumnIndex(columnName);
        if (column == null) {
            throw new NoSuchColumnException("No such column: " + columnName);
        }
        data.set(column, value);
    }
    
    @Override
    public int size() {
        return data.size();
    }

    /**
     * @throws NoSuchColumnException
     */
    @Override
    public String get(String columnName) {
        Integer column = columnsDef.getColumnIndex(columnName);
        if (column != null) {
            return get(column);            
        }
        else {
            throw new NoSuchColumnException("No such column: " + columnName);            
        }
    }
    
    /** Get value of an optional column. In no such column, return null. */ 
    public String getOpt(String columnName) {
        if (columnsDef.getColumnIndex(columnName) != null) {
            return get(columnName);
        }
        else {
            return null;
        }
    }

    
    /**
     * @return the Integer value, or null if value of column is null or empty string
     * @throws NumberFormatException if non-empty non-numeric string
     * @throws NoSuchColumnException if no such column
     */
    public Integer getInt(String columnName) {
        String s = get(columnName);
        
        if (s == null || s.equals("")) {
            return null;
        }
        
        return Integer.parseInt(s);
    }
    
    /**
     * @return the Integer value, or null if value of column is null or empty string, or if no such column.
     * @throws NumberFormatException if non-empty non-numeric string
     * @throws NoSuchColumnException if no such column
     */
    public Integer getOptInt(String columnName) {
        String s = getOpt(columnName);
        
        if (s == null || s.equals("")) {
            return null;
        }
        
        return Integer.parseInt(s);
    }

    /**
     * @return the Long value, or null if value of column is null or empty string
     * @throws NumberFormatException
     *             if non-empty non-numeric string
     * @throws NoSuchColumnException
     *             if no such column
     */
    public Long getLong(String columnName) {
        String s = get(columnName);

        if (s == null || s.equals("")) {
            return null;
        }       
        
        return Long.parseLong(s);
    }

    /**
     * Convert all values of a row with ones provided by the given converter.
     * 
     * @param converter
     *            must convert null to null, as a Row is allowed to have null values.
     */
    public void convertValues(Converter<String, String> converter) {
        for (int i = 0; i < data.size(); i++) {
            String original = data.get(i);
            String converted = converter.convert(original);
            data.set(i, converted);
        }
    }
    
    @Override
    public ColumnsDef getColumnsDef() {
        return columnsDef;
    }

    /** Perform conversion to map. Note that map representation is not cached, so avoid repeated calls. */
    public Map<String,String> toMap() {
        Map<String,String> result = new LinkedHashMap<>();
        for (String column: columnsDef.getColumnNames()) {
            String val = get(column);
            result.put(column,  val);
        }
        return result;
    }
    
    public static class FieldExtractor implements Converter<Row, String> {

        String columnName;
        
        public FieldExtractor(String columnName) {
            this.columnName = columnName;           
        }
        
        @Override
        public String convert(Row row) {
            return row.get(columnName);
        }
        
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return data.iterator();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return data.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return data.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return data.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        return data.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return data.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return data.retainAll(c);
    }

    @Override
    public void clear() {
        data.clear();
        
    }

    @Override
    public String set(int index, String element) {
        return data.set(index, element);
    }

    @Override
    public void add(int index, String element) {
        data.add(index, element);        
    }

    @Override
    public String remove(int index) {
        return data.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    @Override
    public ListIterator<String> listIterator() {
        return data.listIterator();
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        return data.listIterator(index);
    }

    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex);
    }
    
    public class FieldExractor implements Converter<Row, String> {

        private final String column;
        
        FieldExractor(String column) {
            this.column = column;            
        }
        
        @Override
        public String convert(Row row) {
            return row.get(column);
        }
        
    }       
    
    @Override
    public String toString() {
        return StringUtils.format(toMap(), "=", ", ");
    }

    /**
     * Convert row instances to another kind of row instances, based on a mapping of column names.
     * 
     * In current implementation, behavior for target columns missing from either the conversion 
     * map or the source data is just to set null as the target column value. 
     */
    public static class RowConverter <T1 extends Row, T2 extends Row> implements Converter<T1, T2> {
            
        private final util.RowFactory<T2> factory;
        private Map<String, String> columnNameMappings;
        
        /**
         * @param factory Factory for creating converted instances
         * @param columnNameMappings map of TARGET column names to SOURCE column names.
         */
        public RowConverter(RowFactory<T2> factory,
                     Map<String, String> columnNameMappings) {
            this.factory = factory;
            this.columnNameMappings = columnNameMappings;           
        }
             
        @Override
        public T2 convert(T1 source) {
            List<String> targetValues = new ArrayList<>(factory.getColumns().getColumnNames().size());
            for (String targetColumn: factory.getColumns().getColumnNames()) {
                String sourceColumn = columnNameMappings.get(targetColumn);                
                if (sourceColumn != null) {                    
                    targetValues.add(source.get(sourceColumn));
                }
                else {
                    targetValues.add(null);
                }
            }
            return factory.create(targetValues);            
        }        
    }
        
    
    @Override
    public int hashCode() {
        int hash = HashUtils.SEED;
        hash = HashUtils.hash(hash, columnsDef.hashCode());
        hash = HashUtils.hash(hash, data.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object param) {
        if (!(param instanceof Row)) {
            return false;
        }
        Row other = (Row)param;
        
        return columnsDef.equals(other.columnsDef) && data.equals(other.data); 
    }
    
}

