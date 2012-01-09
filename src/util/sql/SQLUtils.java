package util.sql;

import java.sql.*;
import java.util.*;

import util.StringUtils;
import util.collections.iterator.ConverterIterator;
import util.converter.Converter;

public class SQLUtils {

    public static void clearTable(Connection pConnection, String pTableName) throws SQLException {
        Statement stmt = pConnection.createStatement();
        stmt.executeUpdate("DELETE FROM "+pTableName);
        stmt.close();       
    }
    
    public static void delete(Connection pConnection, String pTableName, String pWhereCondition) throws SQLException {
        Statement stmt = pConnection.createStatement();
        stmt.executeUpdate("DELETE FROM "+pTableName+" WHERE "+pWhereCondition);
        stmt.close();       
    }
   
    public static int getRowCount(Connection pConnection, String pTableName) throws SQLException {
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM "+pTableName);
        rs.next();
        int result = rs.getInt(1);
        rs.close();
        stmt.close();
        return result;
    }
    
    /**
     *  Assume query only returns a single value (may be null, or no rows,
     *  both cases in which a null is returned)     
     */
    public static Integer selectInt(Connection pConnection, String pQuery) throws SQLException {
        Statement stmt = pConnection.createStatement();            
        ResultSet rs = stmt.executeQuery(pQuery);                             
        Integer result;
        if (rs.next()) {
            int value = rs.getInt(1);
            if (rs.wasNull()) {
                result = null;
            }
            else {
                result = value;
            }
        }
        else {
            // no rows at all 
            result = null;
        }
        rs.close();
        stmt.close();
        
        return result;                                      
    }

    /**
     * Select all rows from a table.
     */
    public static List<Row> selectAll(Connection pConnection, 
                                      TableDef pTableDef) throws SQLException {
        return select(pConnection, pTableDef, null);
    }
    
    /**
     * if pKeys==null, select all rows; else select rows where key (
     * as defined by TableDef.getKey()) is in pKeys.
     * Allow pKeys to be null also. 
     */
    public static List<Row> select(Connection pConnection, 
                                   TableDef pTableDef,
                                   Collection pKeys) throws SQLException {               
        
        if (pKeys != null && pKeys.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        ArrayList<Row> result = new ArrayList();
        
        // OK, this kludging should prevent us from crashing to out of memory
        // May only work with connector-j mysql drivers?! 
        Statement stmt = pConnection.createStatement();
        // sadly enough, we cannot use the following settings to make the 
        // select "streaming"; instead, we get all data in one blast, which may be bad
        // this is because of the sad limitation that there can only be one
        // "streaming" result set per connection (probably we will crash
        // to the fact that in addition, there can not be any "ordinary"
        // result sets either while a "streaming" result set is active...
        // java.sql.ResultSet.TYPE_FORWARD_ONLY,
        // java.sql.ResultSet.CONCUR_READ_ONLY);
        // stmt.setFetchSize(Integer.MIN_VALUE);
        String query = "SELECT "+makeColumnString(pTableDef.getColumns())+" "+
                       "FROM "+pTableDef.getName();
                       
        if (pKeys != null) {
            query = query+" "+
                    makeWhereStringForSelect(pTableDef, pKeys);
        }
        
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Row row = extractRow(pTableDef, rs);           
            result.add(row);
      }   
      
      rs.close();
      stmt.close();
      
      return result;
    }
    
    public static Row extractRow(TableDef pTableDef, ResultSet rs) throws SQLException {
        HashMap<String, Object> rowData = new HashMap(pTableDef.getColumns().size());
                
        for (ColumnDef col: pTableDef.getColumns()) {
            if (col.getType() == ColumnDef.Type.STRING) {                    
                String val = rs.getString(col.getName());
                rowData.put(col.getName(), val);
            }
            else if (col.getType() == ColumnDef.Type.INT) {                   
                int val = rs.getInt(col.getName());
                if (rs.wasNull()) {
                    // let's decide to put a null there
                    // (then we will have a consistent keyset in the map)
                    rowData.put(col.getName(), null);
                }
                else {
                    // not null
                    // rely on automatic conversion int=>Integer
                    rowData.put(col.getName(), val);                        
                }
            }
            else if (col.getType() == ColumnDef.Type.DECIMAL) {                    
                double val = rs.getDouble(col.getName());
                if (rs.wasNull()) {
                    // let's decide to put a null there
                    // (then we will have a consistent keyset in the map)
                    rowData.put(col.getName(), null);
                }
                else {
                    // not null
                    // rely on automatic conversion double=>Double
                    rowData.put(col.getName(), val);                        
                }
            }
            else {
                throw new RuntimeException("Unknown column type: "+col.getType());
            }                   
        }                      
        
        Row row = new Row(pTableDef, rowData);        
        return row;
    }
    
    public static void insert(Connection pConnection, Row pRow) throws SQLException {
        insert(pConnection, Collections.singleton(pRow));
    }
    
    private static String makeIntoString(TableDef pTableDef) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        Iterator<ColumnDef> colDefIter = pTableDef.getColumns().iterator();        
        buf.append(colDefIter.next().getName());
        while(colDefIter.hasNext()) {
            buf.append(",");
            buf.append(colDefIter.next().getName());
        }
        buf.append(")");
        
        return buf.toString();
    }
    
    private static String makeValuesString(Row pRow) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        boolean first = true;
        for (ColumnDef colDef: pRow.getTableDef().getColumns()) {        
            if (first) {
                first = false;
            }
            else {
                buf.append(",");
            }
            
            ColumnDef.Type colType = colDef.getType();
            String colName = colDef.getName();
            
            if (colType == ColumnDef.Type.STRING) {                 
                String val = pRow.getString(colName);
                if (val != null) {
                    buf.append("'");                    
                    buf.append(val);
                    buf.append("'");
                }
                else {
                    // null}
                    buf.append("null");
                }
                                
            }            
            else if (colType == ColumnDef.Type.INT) { 
                Integer val = pRow.getInt(colName);                
                if (val != null) {
                    buf.append(val);
                }
                else {
                    // null}
                    buf.append("null");
                }
            }
            else if (colType == ColumnDef.Type.DECIMAL) { 
                Double val = pRow.getDecimal(colName);                
                if (val != null) {
                    buf.append(val);
                }
                else {
                    // null}
                    buf.append("null");
                }
            }
            else {
                throw new RuntimeException("Unknown column type: "+colType);
            }
            
        }
        buf.append(")");
        
        return buf.toString();
    }
    
    private static String makeWhereStringForSelect(TableDef pTableDef,
                                                   Collection pKeys) {
        String colName = pTableDef.getKey();
        if (colName == null) {
            throw new RuntimeException("No key!");
        }                
        ColumnDef colDef = pTableDef.getColumn(colName);                           
        ColumnDef.Type colType = colDef.getType();        
                             
        String tmp;
        
        if (colType == ColumnDef.Type.STRING) {                 
            tmp = "'"+StringUtils.collectionToString(pKeys, "','")+"'";                           
        }            
        if (colType == ColumnDef.Type.INT) { 
            tmp = StringUtils.collectionToString(pKeys, ",");
        }
        else if (colType == ColumnDef.Type.DECIMAL) { 
            tmp = StringUtils.collectionToString(pKeys, ",");
        }
        else {
            throw new RuntimeException("Unknown column type: "+colType);
        }
        return "WHERE "+colName +" IN ("+tmp+")";
    }
    
    private static String makeWhereStringForUpdate(Row pRow) {
        String colName = pRow.getTableDef().getUpdateKey();
        if (colName == null) {
            throw new RuntimeException("No update key!");
        }                
        ColumnDef colDef = pRow.getTableDef().getColumn(colName);                           
        ColumnDef.Type colType = colDef.getType();        
        
        String valString;    
            
        if (colType == ColumnDef.Type.STRING) {                 
            String val = pRow.getString(colName);
            if (val != null) {
                valString = "'"+val+"'";                
            }
            else {                
                valString = "null";
            }                           
        }            
        if (colType == ColumnDef.Type.INT) { 
            Integer val = pRow.getInt(colName);                
            if (val != null) {
                valString = ""+val;
            }
            else {                // 
                valString = "null";
            }
        }
        else if (colType == ColumnDef.Type.DECIMAL) { 
            Double val = pRow.getDecimal(colName);                
            if (val != null) {
                valString = ""+val;
            }
            else {
                // null}
                valString = "null";
            }
        }
        else {
            throw new RuntimeException("Unknown column type: "+colType);
        }
        
        return colName+"="+valString;
                      
    }
    
    private static String makeUpdateString(Row pRow) {
        StringBuffer buf = new StringBuffer();        
        boolean first = true;
        for (ColumnDef colDef: pRow.getTableDef().getColumns()) {        
            if (first) {
                first = false;
            }
            else {
                buf.append(",");
            }
            
            ColumnDef.Type colType = colDef.getType();
            String colName = colDef.getName();
        
            buf.append(colName+"=");
            
            if (colType == ColumnDef.Type.STRING) {                 
                String val = pRow.getString(colName);
                if (val != null) {
                    buf.append("'");                    
                    buf.append(val);
                    buf.append("'");
                }
                else {
                    // null}
                    buf.append("null");
                }
                                
            }            
            else if (colType == ColumnDef.Type.INT) { 
                Integer val = pRow.getInt(colName);                
                if (val != null) {
                    buf.append(val);
                }
                else {
                    // null}
                    buf.append("null");
                }
            }
            else if (colType == ColumnDef.Type.DECIMAL) { 
                Double val = pRow.getDecimal(colName);                
                if (val != null) {
                    buf.append(val);
                }
                else {
                    // null}
                    buf.append("null");
                }
            }
            else {
                throw new RuntimeException("Unknown column type: "+colType);
            }
            
        }        
        
        return buf.toString();
    }
    
    /** 
     * 
     * Let's not consider empty Collection an error; instead, do nothing.
     */
    public static void insert(Connection pConnection, 
                              Collection pObjects,
                              Converter<? extends Object, Row> pToRowConverter) throws SQLException {
        insert(pConnection, new ConverterIterator(pObjects.iterator(), pToRowConverter));
    }
    
    /** 
     * 
     * Let's not consider empty Collection an error; instead, do nothing.
     */
    public static void insert(Connection pConnection, Collection<Row> pRows) throws SQLException {
        insert(pConnection, pRows.iterator());
    }        
    
    public static void insert(Connection pConnection, Iterator<Row> pRowIterator) throws SQLException {    
        if (!pRowIterator.hasNext()) {
            return;
        }
        
        // OK, at least one row
        // assume all rows have the same table def (an opposite situation 
        // is unthinkable, to utter the least)
        // As always, this kind of "use first in list as model"-solution is an 
        // annoying hazard to the beauty of code
        // Iterator<Row> rowIterator = pRows.iterator();
        // Iterator<Row> rowIterator = pRowIterator;
        Row firstRow = pRowIterator.next();
        TableDef tableDef = firstRow.getTableDef();
        StringBuffer insertCmd = new StringBuffer();
        String intoString = makeIntoString(tableDef);
        insertCmd.append("INSERT INTO "+tableDef.getName()+" "+intoString+" ");
        insertCmd.append("VALUES ");
        String firstRowValuesString = makeValuesString(firstRow);
        insertCmd.append(firstRowValuesString);        
        while(pRowIterator.hasNext()) {            
            Row row = pRowIterator.next();                            
            insertCmd.append(",");
            String valuesString = makeValuesString(row);
            insertCmd.append(valuesString);
        }
        
        // Logger.info("Executing insert command:");
        // Logger.info(insertCmd.toString());
        
        Statement stmt = pConnection.createStatement();                                           
        stmt.executeUpdate(insertCmd.toString());            
        stmt.close();
    }
        
    public static void update(Connection pConnection, 
                              Row pRow) throws SQLException {        
        TableDef tableDef = pRow.getTableDef();        
        String updateString = makeUpdateString(pRow);
        String whereString = makeWhereStringForUpdate(pRow);
        String insertCmd = "UPDATE "+tableDef.getName()+" "+
                           "SET "+updateString+" "+
                           "WHERE "+whereString;         
        // Logger.info("Executing update statement: "+insertCmd);
        Statement stmt = pConnection.createStatement();                                           
        stmt.executeUpdate(insertCmd);            
        stmt.close();
    }
    
    public static String makeColumnString(Collection<ColumnDef> pColumnDefs) {
        StringBuffer buf = new StringBuffer();
        Iterator<ColumnDef> defIter = pColumnDefs.iterator();
        ColumnDef firstCol = defIter.next();
        buf.append(firstCol.getName());
        while(defIter.hasNext()) {            
            buf.append(", "+defIter.next().getName());
        }
        return buf.toString();
    }
}


