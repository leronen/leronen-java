package util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import util.factory.Factory;

/**
 * New iterator, based on setting Statement properties such that
 * it does not try to put everything into main memory.
 * 
 * Note that resources are not freed unless the iterator is "exhausted completely".
 * 
 * Establishes a dedicated connection to the db for the sole purpose of
 * performing one query. Initializing the connection to db is deferred; 
 * connection is not made until the first object is iterated.
 * 
 * Checked exceptions cannot be exposed by the iterator methods,
 * as Iterator does not support it. Therefore, any SQLExceptions
 * occuring are wrapperd using the SQLExceptionWrapper, which 
 * extends RuntimeException.
 *
 */
public abstract class StreamingSQLIterator<T> implements Iterator<T> {       

    // god-given data:
    private Factory<Connection> mConnectionFactory;    
    
    // internal, transient data:
    private Connection mConnection;
    protected ResultSet mResultSet;    
    private Statement mStatement;               
    private T mCurrentObject;
    private boolean mInitialized;    
     
    /**
     * Note that we require a own connection for our selects! 
     * Let's also remember to close the connection!
     */
    protected StreamingSQLIterator(Factory<Connection> pConnectionFactory) {        
        mConnectionFactory = pConnectionFactory;               
        
        mInitialized = false;        
        mConnection = null;
        mResultSet = null;
        mStatement = null;
        mCurrentObject = null;              
   }    
     
    /**
     * Note that we cannot call the initialization from the constructor,
     * as subclasses may need to perform initialization of their own,
     * which may have to be done before this is called. This is of course
     * the traditional dilemma that occurs with constructors and inheritance.
     * 
     * Also note that initing happears automatically once the client
     * calls hasNext() of next() for the first time.
     *  
     */
    private void init() {
        try {
            mConnection = mConnectionFactory.makeObject();
            performQuery();
            fetchOne();
        }
        catch (SQLException e) {
//            e.printStackTrace();
            // wrap as a runtime exception...
            throw new SQLExceptionWrapper(e);               
        }
    }
          
    protected abstract String makeQuery();      
    protected abstract T makeObject(ResultSet pResultSet) throws SQLException;
     
    private void performQuery() throws SQLException {
        // OK, the following chants should cause the mysql driver to access
        // the result set in "streaming" mode, thus avoiding problems with
        // "very large" result sets
        // May only work with connector-j mysql drivers?!
        mStatement = mConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                 java.sql.ResultSet.CONCUR_READ_ONLY);
        mStatement.setFetchSize(Integer.MIN_VALUE);
        String query = makeQuery();       
        // Logger.info("Executing query:\n"+query);
        // Logger.info("Concrete class="+getClass());
        mResultSet = mStatement.executeQuery(query);
    }
    
    private void fetchOne() throws SQLException {
        // Logger.info("fetchOne");
        if (mResultSet.next()) {
           // Logger.info("We have a next row");
            mCurrentObject = makeObject(mResultSet);
        }
        else {              
            // out of rows
            closeAll();
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
                throw new SQLExceptionWrapper(e);              
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
                      
    /**
     * Probably violates iterator contract, but this is the only way to 
     * clean up in case we do not want to iterate everything.
     *
     */
    public void closeAll() throws SQLException {
        mResultSet.close();
        mStatement.close();
        // Note: even close the connection, as the guilt of opening it
        // rests heavily upon us
        mConnection.close();
        mCurrentObject = null;
    }
    
    /**
     * Have to wrap potential sql exceptions as RuntimeExceptions, 
     * as the Iterator interface does not allow for checked exceptions...  
     */
    public static class SQLExceptionWrapper extends RuntimeException {

        public SQLExceptionWrapper(SQLException pWrappedException) {
            super (pWrappedException);
        }
    }
    
}
