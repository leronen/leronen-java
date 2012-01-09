package util.sql;
 
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import util.dbg.Logger;


/** 
 * A more EFFICIENT (at least in the case of the POOR current mysql implementation,
 * which does inserts ROW BY ROW) and more RELIABLE (column names are used 
 * instead of indices, which allows for some sanity checks and maybe even 
 * schema changes; this is based on the use of a TableDef) replacement for 
 * java.sql.PreparedStatement.
 * 
 * Usage: 
 *   (1) Create, specifying a connection and a table def
 *   (2) populate current row (created on demand) with data, using setXXX methods
 *   (3) call rowFinished()
 *   (4) goto (2) if more data
 *   (5) call execute() to put data to db; this clears data structures,
 *       allowing for re-use of the same InsertStatement
 *   (6) goto (2) if more data 
 */
public class InsertStatement {
    
    private Connection mConnection;
    private TableDef mTableDef;
    private ArrayList mRows;
    private HashMap<String, Object> mCurrentData;
    
    public InsertStatement(Connection pConnection,
                           TableDef pTableDef) {
        // core data
        mConnection = pConnection;
        mTableDef = pTableDef;
        
        // transient data
        mRows = new ArrayList();
        mCurrentData = null;
    }
    
    private void internalSet(String pColName, Object pVal) {
        if (!mTableDef.containsColumn(pColName)) {
            throw new RuntimeException("No such column in table "+mTableDef.getName()+": "+pVal);
        }
        
        if (mCurrentData == null) {
            mCurrentData = new HashMap(mTableDef.getNumColumns());
        }
        
        mCurrentData.put(pColName, pVal);
    }
    
    public void rowFinished() {
        mRows.add(new Row(mTableDef, mCurrentData));
        mCurrentData = null;
    }    
    
    public void setString(String pColName, String pVal) {
        internalSet(pColName, pVal);               
    }
    
    public void setInt(String pColName, Integer pVal) {
        internalSet(pColName, pVal);                
    }
    
    public void setInt(String pColName, Boolean pVal) {
        if (pVal == null) {
            internalSet(pColName, null);
        }
        else {
            internalSet(pColName, pVal ? 1 : 0);
        }
    }
    
    public void setDecimal(String pColName, Double pVal) {
        internalSet(pColName, pVal);                
    }
    
    public List<Row> getRows() {
        return mRows;
    }
    
    public int size() {
       return mRows.size();
    }
    
    public void execute() throws SQLException {
        if (mCurrentData != null) {
            Logger.warning("Executing insert statement but current row has not been added!");
        }
        
        if (mRows.size() > 0) {
            SQLUtils.insert(mConnection, mRows);
        }
        
        mRows.clear();
        mCurrentData = null;
    }
}


