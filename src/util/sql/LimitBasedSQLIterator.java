package util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  SQL iterator which uses (my?)sql LIMIT to avoid putting large result sets to 
 *  RAM at once. Does not seem to work very nicely when number of rows is 
 *  very large (millions); using limit starts to have its toll there.
 *  On the other hand, avoids the problem (encountered in class
 *  StreamingSQLIterator) of reserving the connection completely! 
 * 
 *  The current policy shall be: use this for nodes and other entities
 *  with at least a semi-reasonable number of instances. Use StreamingSQLIterator
 *  for links (and potentially other "very large" tables).
 *   
 */
public abstract class LimitBasedSQLIterator<T> implements Iterator<T> {       

    private static final int SELECT_SIZE = 100000;
    
    // god-given data:
    private Connection mConnection;    
    
    // transient data
    protected ResultSet mResultSet;    
    private Statement mStatement;           
    private int mCurrentLimitStart;
    private T mCurrentObject;
    private boolean mInitialized;
     
    protected LimitBasedSQLIterator(Connection pConnection) {        
        mConnection = pConnection;
        mInitialized = false;
        mCurrentObject = null;
        mResultSet = null;
        mStatement = null;
        mCurrentLimitStart = 0;                                                                    
    }    
     
    private void init() {
        try {
            performQuery();
            fetchOne();
        }
        catch (SQLException e) {
            throw new RuntimeException("SQLException while fetching object: "+e);               
        }
    }
          
    protected abstract String makeQuery();      
    protected abstract T makeObject(ResultSet pResultSet) throws SQLException;
     
    private void performQuery() throws SQLException {        
        mStatement = mConnection.createStatement();        
        String query = makeQuery()+" "+
                       "LIMIT "+mCurrentLimitStart+", "+SELECT_SIZE;
       // Logger.info("Executing query:\n"+query);
        mResultSet = mStatement.executeQuery(query);
        // we need this to prevent memory leaks!            
        // Logger.info("Fetch size: "+mResultSet.getFetchSize());
        mCurrentLimitStart+=SELECT_SIZE;
    }
    
    private void fetchOne() throws SQLException {
        // Logger.info("fetchOne");
        if (mResultSet.next()) {
           // Logger.info("We have a next row");
            mCurrentObject = makeObject(mResultSet);
        }
        else {              
            mResultSet.close();
            mStatement.close();                 
            
            // let's perform a new query                        
            performQuery();
        
            if (mResultSet.next()) {
               // not yet out of rows; advanced to the first row of new result set                  
                mCurrentObject = makeObject(mResultSet);
            }
            else {                 
                // out of rows
                mResultSet.close();
                mStatement.close();
                mCurrentObject= null;
            }
        }                              
    }
    
    public T next() {
        if (!mInitialized) {
            init();
            mInitialized = true;
        }
        
        if (mCurrentObject!= null) {
            T result = mCurrentObject;
            try {
                fetchOne();
                return result;
            }
            catch (SQLException e) {
            throw new RuntimeException("SQLException while fetching synonym: "+e);              
        }
            
        }           
        else {
            throw new NoSuchElementException();
        }
    }
    
    public boolean hasNext() {
        if (!mInitialized) {
            init();
            mInitialized = true;
        }
        
        return mCurrentObject != null;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
                  
}
