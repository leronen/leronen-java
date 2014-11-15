package util;

import gui.application.*;

import util.matrix.*;
import util.dbg.*;
import util.collections.*;
import util.condition.*;
import util.converter.*;
import util.comparator.*;
import util.math.*;

import gui.plot.*;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

/** 
 * A dense, in-memory, matrix of untyped values 
 *
 * Note that the table can be used as a java.util.List, as well as javax.swing.table.TableModel,
 * by using the corresponding wrapper methods, asList() and asTableModel()
 * 
 * Todo: new subclass "Table" that is a just matrix with header! Functionality in genetiikka.data.Table is to be moved
 *       there, and a new class, GenedataTable is to be subclassed from that!
 *
 * Todo: Make "pretty printing" optional. - by the way done, I guess.
 *
 * Todo: allow clearer control over formatting different data types.  
 *
 * 
 */
public final class Matrix  {
              
    /**      
     * A matrix is implemented by an ArrayList of ArrayLists.
     * Would a linked-list implementation sometimes be more preferable?
     *
     * Invariants: 
     *   the list must only contain ArrayList objects 
     *   all contained lists must have size() equal to mNumCols.     
     */
    private ArrayList<List> mRows;  

    /** -1 indicates we dont know yet */          
    private int mNumCols = -1;            
            
    private RowFormat mHeader;            
            
    /** optional, just for debugging */
    private String mName;                 

    private boolean mIncludeHeader;

    // wrappers to make the matrix more salon-compatible
    private TableModelWrapper mTableModelWrapper;
    private ListWrapper mListWrapper;

    // ugly kludge:
    public boolean mDoNotWriteHeader = false;

    public static boolean OUTPUT_CRAPPY_DEBUG_MESSAGES = false;                                                    
              
    private RowFormatFactory mRowFormatFactory;              
              
    private boolean mPrettyPrinting = false;              

    /** column separator used for output */
    private String mOutputColumnSeparator = " ";
    
    /** column separator used for input (only when no row format?) */
    private String mInputColumnSeparator = "\\s+";
    
    public void setPrettyPrinting(boolean pVal) {
        mPrettyPrinting = pVal;
    }              
              
    /** Only takes effect for "non-pretty" printing... */
    public void setOutputColumnSeparator(String p) {
    	mOutputColumnSeparator = p;
    }
    
    /** Only takes effect for "non-pretty" printing... */
    public void setInputColumnSeparator(String p) {
    	mInputColumnSeparator = p;
    }
    
    /** Create empty matrix with no rows or columns, and specify that no header shall be included */
    public Matrix() {
        this(false);                
    }              
        
    public void setRowFormatFactory(RowFormatFactory pRowFormatFactory) {
        mRowFormatFactory = pRowFormatFactory;
    }
                 
    /**
     * Create empty matrix with no rows and columns. Note that creating the 
     * row factory is deferred, as this method is meant
     * for initializing matrixes which are to be read from a file.
     */
    public Matrix(boolean pIncludeHeader) {
        mRows = new ArrayList();                                
        mIncludeHeader = pIncludeHeader;
    }
    
    /** Create empty matrix with no rows and columns, possibly with a header... */
    public Matrix(RowFormat pHeader) {
        if (pHeader == null) {
            mRows = new ArrayList();                                    
            mIncludeHeader = false;
        }
        else {
            mHeader = pHeader;
            mRowFormatFactory = pHeader.getFactory();                    
            mRows = new ArrayList();                
            mIncludeHeader = true;            
            mNumCols = pHeader.getNumFields();            
        }
    }   
    
   /**
    * @param pConsiderNullAsObject if true, do not consider a column with a 
    *  single object and null as a singleton column.
    */
    public List<Integer> getNonSingletonCols(boolean pConsiderNullAsObject) {
        Set<Integer> allCols = new HashSet(getAllColIndices());
        List<Integer> result = 
            new ArrayList(CollectionUtils.minus(allCols, 
                                                getSingletonCols(pConsiderNullAsObject)));                
        Collections.sort(result);
        return result;
    }
    
    
    /** 
     * @param pConsiderNullAsObject if true, do not consider a column with a 
     * single object and null as a singleton column. 
     *  
     */
    public List<Integer> getSingletonCols(boolean pConsiderNullAsObject) {
        List<Integer> singletonCols = new ArrayList();
        for (int i=0; i<getNumCols(); i++) {
            if (isSingletonCol(i, pConsiderNullAsObject)) {
                singletonCols.add(i);
            }
        }
        return singletonCols;
    }
    
    /**
     * @param pConsiderNullAsObject if true, do not consider a column with a 
     * single object and null as a singleton column.
     * 
     * keywords: is variable, isvariable, ismultival, is multi-val.
     */
    public boolean isSingletonCol(int pCol, boolean pConsiderNullAsObject) {
        
        if (getNumRows() == 0) {
//            Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): returning true, as num rows=0");
            return true;
        }
        
        if (pConsiderNullAsObject) {        
            // consider null as a first-class object
            Object prototypeVal = get_row_col(0, pCol);
//            Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): prototype=: "+prototypeVal);
            
            for (int i=1; i<getNumRows(); i++) {
                Object val = get_row_col(i, pCol);
//                Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): val=: "+val);
                
                if (prototypeVal == null && val == null) {
//                    Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): both null, no action)");
                    // no action
                }            
                else if (prototypeVal == null && val != null) {
//                    Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): returning false (protytype is null, val is not)");
                    return false;
                }            
                else if (prototypeVal != null && val == null) {
//                    Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): returning false (val is null, prototype is not)");
                    return false;
                }
                else {
                    // both non-null
                    if (!(prototypeVal.equals(val))) {
//                        Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): returning false (both non-null)");
                        return false;
                    }
                }            
            }
//            Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): returning true)");
            return true;
        }
        else {
            // require 2 different non-null objects
            Object prototype = null;
            
            for (int i=0; i<getNumRows(); i++) {
                
                Object o = get_row_col(i, pCol);
//                Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): o=: "+o);
                                
                if (o != null) {
                    if (prototype == null) {
                        // first non-null object found
//                        Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): setting prototype to: "+o);
                        prototype = o;
                    }
                    else if (!(prototype.equals(o))) {
                        // two different objects found
//                        Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): false (two different objects found)");
                        return false;                        
                    }
                }
            }
        
            Logger.info("Is singleton col ("+pCol+","+pConsiderNullAsObject+"): true (end of last else)");
            return true;
            
        }
        
        
    }
    
        
    
    
    
    
    
    
    
    
    
    public Matrix(RowFormat pHeader, List pRows) {
        if (pHeader == null) {
            mRows = new ArrayList(pRows);                                    
            mIncludeHeader = false;
            if (pRows.size()>0) {
                // deduce num cols from first row
                List firstRow = (List)pRows.get(0);
                mNumCols = firstRow.size();                
            }
            else {
                // leave number of cols ambiguous    
            }
        }
        else {
            mHeader = pHeader;             
            mRowFormatFactory = pHeader.getFactory();
            mRows = new ArrayList(pRows);                
            mIncludeHeader = true;            
            mNumCols = pHeader.getNumFields();
        }
    }    
    
    // public double[] colAverages() {
           
    // }
    
    /** Create matrix with specified dimensions. The contained values are initially null */
    public Matrix(int pNumRows, int pNumCols, boolean pIncludeHeader) {
        mIncludeHeader = pIncludeHeader;        
        mNumCols = pNumCols;
        mRows = new ArrayList(pNumRows);
        for (int i=0; i<pNumRows; i++) {
            // System.err.println("Making "+i+":th row...");
            List row = getRowFactory().makeRow();                        
            mRows.add(row); 
            // dbgMsg("length of row "+i+": "+row.size());
        }        
    }
    
    public Matrix(int pNumRows, int pNumCols) {
        this(pNumRows, pNumCols, false);        
    }

    public RowFormatFactory getRowFormatFactory() {
        if (mRowFormatFactory == null) {
            return RowFormat.getDefaultFactory();     
        }
        else {
            return mRowFormatFactory;
        }
    }
    
    /** Create matrix that gets it's data from a table model */
    public Matrix(TableModel pTableModel, RowFormatFactory pRowFormatFactory) {
        mRowFormatFactory = pRowFormatFactory;
        mIncludeHeader = true;        
        mNumCols = pTableModel.getColumnCount();
        int numRows = pTableModel.getRowCount();
        mRows = new ArrayList(numRows);
        String[] fieldNames = new String[mNumCols];
        for (int j=0; j<mNumCols; j++) {
            fieldNames[j] = pTableModel.getColumnName(j);
        }
        mHeader = getRowFormatFactory().makeFromFieldNameArray(fieldNames);
        for (int i=0; i<numRows; i++) {
            ArrayList vals = new ArrayList();
            for (int j=0; j<mNumCols; j++) {
                vals.add(pTableModel.getValueAt(i,j));
            }
            List row = getRowFactory().makeRow(vals);                        
            mRows.add(row);             
        }        
    }
          
    
    /** returns -1 if no such column. Indexing starts from 0. */
    public int getColInd(String pColName) {
        return mHeader.getFieldIndex(pColName);
    }
    
    public String getFieldName(int pColInd) {
        return mHeader.getFieldName(pColInd);
    }
    
    public int fieldNameToFieldIndex(String pColInd) {
        return mHeader.getFieldIndex(pColInd);
    }
        
    public String fieldIndexToFieldName(int pColInd) {
        return mHeader.getFieldName(pColInd);
    }                
    
    public boolean doesColumnFulfill(Condition pCondition, String pColName) {
        int colInd = getColInd(pColName);
        return doesColumnFulfill(pCondition, colInd);
    }        
    
    /** Monomaniously fill the matrix with a single object */
    public void fill(Object pObj) {
        fill(pObj, allIndices());                                    
    }
    
    /** Monomaniously fill part of the matrix with a single object */
    public void fill(Object pObj, IndexSet pIndexSet) {                        
        Iterator indexIter = pIndexSet.iterator();
        while(indexIter.hasNext()){
            Index index = (Index)indexIter.next();
            set_row_col(index.getRow(), index.getCol(), pObj);                         
        }                      
    }    
    
    public int[] findColsFulfillingCondition(Condition pCondition) {        
        ArrayList result = new ArrayList();
        for (int i=0; i<mNumCols; i++) {
            if (doesColumnFulfill(pCondition, i)) {
                result.add(new Integer(i));
            }
        }
        return ConversionUtils.integerCollectionToIntArray(result);
    }
    
    public boolean doesColumnFulfill(Condition pCondition, int pColInd) {
        int numRows = mRows.size();        
        for (int i=0; i<numRows; i++) {
            Object val = get_row_col(i, pColInd);
            if (!(pCondition.fulfills(val))) {
                return false;
            }
        }
        // passed the demanding trials
        return true;
    }        
        
    public boolean isIntegerColumn(int pColInd) {        
        IsIntegerCondition condition = new IsIntegerCondition();
        return doesColumnFulfill(condition, pColInd);                    
    }        
        
        
    public void setHeader(Collection pFieldList) {
        // mHeader = new ArrayList(pHeader);
        mHeader = getRowFormatFactory().makeFromFieldNameList(ConversionUtils.convert(pFieldList, new ObjectToStringConverter()));        
        mIncludeHeader = true;        
    }
    
    public void setRowFormat(RowFormat pFormat) {        
        if (pFormat == null) {                                    
            mIncludeHeader = false;
            mHeader = null;
        }
        else {
            mHeader = pFormat;        
            mIncludeHeader = true;
        }            
    }
    
    private RowFactory getRowFactory() {
        if (mHeader != null) {
            return mHeader.getRowFactory();  
        }
        else {
            ArrayListRowFactory rowFactory = new ArrayListRowFactory(mNumCols);
            rowFactory.setSeparator(mInputColumnSeparator);
            return rowFactory;
        }
    }
    
    public void insertCol(int pColIndex, List pCol) {
        if (pCol.size()!=getNumRows()) {
            throw new RuntimeException("Col has wrong number of rows!");
        }      
        if (mHeader != null) {
            throw new RuntimeException("Cannot insert col, as we have a header!");
        }
        // assertions ok
        for (int i=0; i<mRows.size(); i++) {
            ArrayList row = (ArrayList)mRows.get(i);
            row.add(pColIndex, pCol.get(i));
        }
        // remember to maintain this precious variable:
        mNumCols++;
    }
            
                                
    /**
     * Make a MultiMap such that one colums will be the key in the map, and the values will be 
     * Lists(or Rows, depending on the row factory) containing values of all the other columns 
     * (now: possibly including the value col itself, as governed by the parameter pIncludeColInResult).
     *    
     */         
    public MultiMap toMultiMap(int pCol, boolean pIncludeColInResult) {        
        MultiMap result = new MultiMap();
        
        String[] resultFields = null;
                         
        if (mHeader != null) {
            List resultFieldsList = null;
            resultFieldsList = new ArrayList(Arrays.asList(mHeader.getFieldNames()));
            if (pIncludeColInResult) {
                resultFieldsList.remove(pCol);                        
            }
            resultFields = ConversionUtils.stringCollectionToArray(resultFieldsList);            
        }                         
        
        for (int i=0; i<mRows.size(); i++) {                        
            ArrayList row = getRow(i);
            Object key = row.get(pCol);
            List vals;
            if (pIncludeColInResult) {
                vals  = getRowFactory().makeRow(row);                
            }
            else {
                // di not include the key column in the result rows
                if (mHeader != null) {
                    // we have a header
                    vals = ((Row)row).select(resultFields);
                }
                else {
                    // no header                
                    vals = new ArrayList(row.size()-1);            
                    for (int j=0; j<mNumCols; j++) {
                        if (j != pCol) {                                        
                            vals.add(row.get(j));
                        }
                    }                
                }
            }
            result.put(key, vals);
        }        
        return result;            
    }    
        
    public MultiSet columnHistogram(String pColName) {
        int colInd = mHeader.getFieldIndex(pColName);
        return columnHistogram(colInd);                
    }    
    
    /**
     * Calculate a WeightedSet, where values are the values of the column, and the weights are
     * (absolute) frequencies of the values. 
     */
    public MultiSet columnHistogram(int pCol) {
        List col = getCol(pCol);
        return new HashMultiSet(col);        
    }    
    
    public void appendRow(List pRow) {
        if (pRow.size() != mNumCols) {
            throw new RuntimeException("Trying to append row with wrong number of columns: num_required="+mNumCols+", num attempted="+pRow.size()+"\n"+
                                       "The row itself: "+pRow);
        }
        mRows.add(getRowFactory().makeRow(pRow));
    }
    
    public void appendRows(List pRows) {
        Iterator i = pRows.iterator();
        while(i.hasNext()) {
             appendRow((List)i.next());
        }                
    }
        
    public void removeRowsMatchingCondition(ListCondition pCondition) {
        CollectionUtils.removeMatchingObjects(mRows, pCondition);      
    }        
    
    public void removeRowsWithZeroValueInColumn(int pCol) {
        Condition isZeroCondition = new Condition() {
            public boolean fulfills(Object pObj) {
                double val;
                if (pObj instanceof String) {
                    val = Double.parseDouble((String)pObj);
                }
                else if (pObj instanceof Number) {
                    val = ((Number)pObj).doubleValue();
                }
                else {
                    throw new RuntimeException("Cannot remove rows with zero value: non-numeric value encountered: "+pObj);
                }
                return val == 0;                
            }
        };
        removeRowsMatchingCondition(new ColumnCondition(pCol, isZeroCondition));                    
    }
        
    public void setCol(int pCol, List pValues) {
        if (pCol < 0 || pCol >= mNumCols) {
            throw new RuntimeException("Invalid col: "+pCol);
        }
        int numRows = getNumRows();
        if (numRows!=pValues.size()) {
            throw new RuntimeException("Invalid number of rows: "+pValues.size()+"; matrix has "+numRows+" rows.");
        }
        else {            
            for (int i=0; i<numRows; i++) {
                set_row_col(i, pCol, pValues.get(i));
            }                
        }            
    }
        
    public void append(Matrix pMatrixToAppend) {
        int numColsToAppend = pMatrixToAppend.mNumCols;        
        for (int i=0; i<numColsToAppend; i++) {
            if (pMatrixToAppend.mHeader != null) {
                appendCol(pMatrixToAppend.getCol(i), pMatrixToAppend.mHeader.getFieldId(i));
            }
            else {
                appendCol(pMatrixToAppend.getCol(i));
            }
        }
    }         
        
    public void appendCol(List pCol) {
        appendCol(pCol, (String)null);    
    }
    
    /** Return indices of our columns (this will, of course, always be {0,1,2, ... , mNumCols} */
    public Set colIndexSet() {
        HashSet result = new HashSet();
        for (int i =0; i<mNumCols; i++) {
            result.add(new Integer(i));                
        }
        return result;
    }
    
    public double[] calculate(String pColName1, String pColName2, BinaryOperator pOperator) {
        int colInd1 = getColInd(pColName1);
        int colInd2 = getColInd(pColName2);
        return calculate(colInd1, colInd2, pOperator);
    }        
    
    public double[] calculate(int pColInd1, int pColInd2, BinaryOperator pOperator) {
        List vals1 = colAsDoubleList(pColInd1);
        List vals2 = colAsDoubleList(pColInd2);
        return MathUtils.calculate(vals1, vals2, pOperator);        
    }
    
    public double[] calculate(String pColName, UnaryOperator pOperator) {
        int colInd = getColInd(pColName);        
        return calculate(colInd, pOperator);
    }        
        
    public double[] calculate(int pColInd, UnaryOperator pOperator) {
        List vals = colAsDoubleList(pColInd);        
        return MathUtils.calculate(vals, pOperator);        
    }
    
    public void rearrangeCols_by_name(List pColNamesInNewOrder) {
        ArrayList colIndicesInNewOrder = new ArrayList();
        for (int i=0; i<pColNamesInNewOrder.size(); i++) {
            colIndicesInNewOrder.add(new Integer(fieldNameToFieldIndex((String)pColNamesInNewOrder.get(i))));                        
        }
        rearrangeCols_by_index(colIndicesInNewOrder);    
    }
    
    
    /** @pNewOrder must contain Integer objects and must contain exactly the column indices that we have.
     * 
     * The semantics is such that the given integers present the new order in the terms of the old columns
     * So for instance, if we have: A B C, then after rearrangeCols({2,0,1}) we have: C A B.
     */
    public void rearrangeCols_by_index(List newOrder) {
        // must have same number of cols
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
            dbgMsg("rearrangeCols, we have "+mNumCols+" cols, newOrder = "+StringUtils.collectionToString(newOrder, " "));
        }            
        if (newOrder.size() != mNumCols) {
            throw new RuntimeException("Invalid number of columns: "+StringUtils.collectionToString(newOrder));
        }
        Set oldColIndexSet = colIndexSet();
        Set newColIndexSet = new HashSet(newOrder);
        // must have same cols
        if (!(oldColIndexSet.equals(newColIndexSet))) {
            // Logger.info("Old columns:\n"+StringUtils.collectionToString(oldColIndexSet));
            // Logger.info("New columns:\n"+StringUtils.collectionToString(newColIndexSet));
            Set problematicColumns = CollectionUtils.minus(CollectionUtils.union(oldColIndexSet, newColIndexSet),
                                                           CollectionUtils.intersection(oldColIndexSet, newColIndexSet));
            // Logger.info("Problematic columns:\n"+StringUtils.collectionToString(problematicColumns));
            if (mHeader != null) {
                String[] problematicColumnNames = mHeader.getFieldNames(ConversionUtils.integerCollectionToIntArray(problematicColumns));
                Logger.info("Problematic column names:\n"+StringUtils.arrayToString(problematicColumnNames));
            }                                                          
            throw new RuntimeException("Old and new column index sets do not match! ");
        }
                                    
        if (mHeader != null) {
            // reorder header as well
            setRowFormat(mHeader.getRearrangedVersion(newOrder));                    
        }        
        // assertions OK, proceed to rearrange        
        for (int i=0; i<mRows.size(); i++) {            
            ArrayList oldRow = (ArrayList)mRows.get(i);
            List newRow = getRowFactory().makeRow();            
            for (int j=0; j<mNumCols; j++) {
                int col = ((Integer)newOrder.get(j)).intValue();
                newRow.set(j, oldRow.get(col));
            }
            mRows.set(i, newRow);
        }                                
    }
                
    
    /** Argh, almost duplicate code with appendCol(List, String) (todo: break down!) */ 
    public void appendCol(List pCol, FieldId pHeaderEntry) {
        if (mIncludeHeader == true && pHeaderEntry == null) {
            throw new RuntimeException("pHeaderEntry cannot be null");
        }        
        if (mNumCols == 0 || mNumCols == -1) {
            mNumCols = 1;
            if (mIncludeHeader && pHeaderEntry != null) {
                // no header yet, so create it on the fly                                
                mHeader = getRowFormatFactory().makeSimple(pHeaderEntry);
            }
            // create singleton col
            mRows = new ArrayList(pCol.size());
            Iterator i = pCol.iterator();                                                                                                                                
            while (i.hasNext()) {                
                Object val = i.next();                
                List row;                                                                                                                                                                                                      
                row = getRowFactory().makeRow();
                row.set(0, val);                
                mRows.add(row);
            }            
        }
        else {
            if (mHeader != null) {
                mHeader = mHeader.addFieldToEnd(pHeaderEntry);
            }
            
            // append to existing cols
            if (pCol.size()!=getNumRows()) {
                throw new RuntimeException("Col has wrong number of rows!");
            }                           
            for (int i=0; i<mRows.size(); i++) {
                List row = (List)mRows.get(i);
                Object val = pCol.get(i);
                if (pHeaderEntry != null && row instanceof Row) {                     
                    ((Row)row).appendField(pHeaderEntry.getName(), val);                                                                                                                                      
                }
                else {
                    row.add(val);
                }                    
            }
            // remember to maintain this precious variable:
            mNumCols++;
        }        

    }        
        
    /**
     * @pHeaderEntry may be null only if mIncludeHeader == false
     *
     * Note the horrendous double-maintain effort: appendCol(List, FieldId) is almost exact copy(todo: get rid of!)       
     */ 
    public void appendCol(List pCol, String pHeaderEntry) {
        if (mIncludeHeader == true && pHeaderEntry == null) {
            throw new RuntimeException("pHeaderEntry cannot be null");
        }        
        if (mNumCols == 0 || mNumCols == -1) {
            mNumCols = 1;
            if (mIncludeHeader && pHeaderEntry != null) {
                // no header yet, so create it on the fly
                mHeader = getRowFormatFactory().makeSimple(pHeaderEntry);
            }
            // create singleton col
            mRows = new ArrayList(pCol.size());
            Iterator i = pCol.iterator();                                                                                                                                
            while (i.hasNext()) {                
                Object val = i.next();                
                List row;                                                                                                                                                                                                      
                row = getRowFactory().makeRow();
                row.set(0, val);                
                mRows.add(row);
            }            
        }
        else {
            if (mHeader != null) {
                mHeader = mHeader.addFieldToEnd(pHeaderEntry);
            }
            
            // append to existing cols
            if (pCol.size()!=getNumRows()) {
                throw new RuntimeException("Col has wrong number of rows!");
            }                           
            for (int i=0; i<mRows.size(); i++) {
                List row = (List)mRows.get(i);
                Object val = pCol.get(i);
                if (pHeaderEntry != null && row instanceof Row) {                     
                    ((Row)row).appendField(pHeaderEntry, val);                                                                                                                                      
                }
                else {
                    row.add(val);
                }                    
            }
            // remember to maintain this precious variable:
            mNumCols++;
        }        
    }    
       
    public Matrix createClone() {
        Matrix clone = new Matrix(mHeader);
        clone.mRowFormatFactory = mRowFormatFactory;
        
        for (int i=0; i<mRows.size(); i++) {
            clone.mRows.add(clone.getRowFactory().makeRow((List)mRows.get(i)));
        }            
        clone.mNumCols = mNumCols;                                            
        clone.mName = mName;
        clone.mIncludeHeader = mIncludeHeader;                         
        return clone;            
    }
       
    public boolean contains(Object val) {
        List rowList = asList();
        List valueList = CollectionUtils.flatten(rowList);
        return valueList.contains(val);        
    }
           
    public void removeCol(String pColId) {
        int colInd = mHeader.getFieldIndex(pColId);
        removeCol(colInd);
    }                  
                  
    public void removeCol(int pColInd) {
        removeCols(Collections.singleton(new Integer(pColInd)));
        /*
        for (int i=0; i<mRows.size(); i++) {
            ArrayList row = (ArrayList)mRows.get(i);
            row.remove(pColInd);
        }
        if (mHeader != null) {
            mHeader = mHeader.removeField(pColInd);    
        }    
        mNumCols--;
        */
    }
            
    public void removeNullCols() {
        Logger.dbg("Matrix.removeNullCols");
        LinkedHashSet<Integer> colsToRemove = new LinkedHashSet<Integer>();
        
        for (int j=0; j<getNumCols(); j++) {
            Logger.dbg("Col: "+j);                    
            HashSet valSet = new HashSet(getCol(j));
            valSet.remove(null);
            valSet.remove("null");
            Logger.dbg("valSet:\n"+StringUtils.collectionToString(valSet));
            // if (valSet.equals(Collections.singleton(null))) {
            if (valSet.size() == 0) {
                Logger.dbg("This is a null column!");
                colsToRemove.add(j);    
            }
            else {
                Logger.dbg("Not a null column!");   
            }
        }
        removeCols(colsToRemove);
    }
    
    public void removeNullRows() {
        // Logger.info("Matrix.removeNullRows");
        LinkedHashSet<Integer> rowsToRemove = new LinkedHashSet<Integer>();
        
        for (int i=0; i<getNumRows(); i++) {
            // Logger.info("Row: "+i);                    
            HashSet valSet = new HashSet(getRow(i));
            valSet.remove(null);
            valSet.remove("null");
            // Logger.dbg("valSet:\n"+StringUtils.collectionToString(valSet));
            // if (valSet.equals(Collections.singleton(null))) {
            if (valSet.size() == 0) {
                // Logger.dbg("This is a null column!");
                rowsToRemove.add(i);    
            }
            else {
                // Logger.dbg("Not a null column!");   
            }
        }
        removeRows(rowsToRemove);
    }
           
    
    public void removeColsWithNoNumericValues() {
        Logger.dbg("Matrix.removeColsWithNoNumericValues");
        LinkedHashSet<Integer> colsToRemove = new LinkedHashSet<Integer>();
        
        for (int j=0; j<getNumCols(); j++) {
            Logger.dbg("Col: "+j);
            // Logger.dbg("Values: "+StringUtils.collectionToString(getCol(j), ", "));
            List objects = getCol(j);
            for (Object o: objects) {
               Logger.dbg("o: "+o+", class: "+o.getClass());  
            }
            List numericObjects = (List)CollectionUtils.extractMatchingObjects(objects, new IsNumericCondition());
                                
            // HashSet valSet = new HashSet(numericObjects);             
            // Logger.dbg("valSet:\n"+StringUtils.collectionToString(valSet));
            // if (valSet.equals(Collections.singleton(null))) {
            if (numericObjects.size() == 0) {
                Logger.dbg("This column does not contain numeric objects, removing column...");
                colsToRemove.add(j);    
            }
            else {
                Logger.dbg("This column does contain numeric objects.");   
            }
        }
        removeCols(colsToRemove);
    }

    public void removeColsWithNoMatchingValues(Condition pCondition) {
        Logger.dbg("Matrix.removeColsWithNoMatchingValues");
        LinkedHashSet<Integer> colsToRemove = new LinkedHashSet<Integer>();
        
        for (int j=0; j<getNumCols(); j++) {
            Logger.dbg("Col: "+j);
            
            List objects = getCol(j);
            // Logger.dbg("Values: "+StringUtils.collectionToString(objects, ", "));
            // for (Object o: objects) {
               // Logger.dbg("o: "+o+", class: "+o.getClass());  
            // }
            List matchingObjects = (List)CollectionUtils.extractMatchingObjects(objects, pCondition);
                                
            // HashSet valSet = new HashSet(matchingObjects);             
            // Logger.dbg("valSet:\n"+StringUtils.collectionToString(valSet));
            // if (valSet.equals(Collections.singleton(null))) {
            if (matchingObjects.size() == 0) {
                Logger.dbg("This column does not contain matching objects, removing column...");
                colsToRemove.add(j);    
            }
            else {
                Logger.dbg("This column does contain matching objects.");   
            }
        }
        removeCols(colsToRemove);
    }            
            
    public List<Integer> getAllColIndices() {
        return new Range(0, getNumCols()).asList();
    }
    
    /** Note that indexing starts from 0. */            
    public void removeCols(Set<Integer> pColInds) {        
        // dbgMsg("removeCols: "+pColInds);
        // Logger.dbgInSubSection("original matrix", this.toString());        
        int[] indsToRemove = ConversionUtils.integerCollectionToIntArray(pColInds);
        Arrays.sort(indsToRemove);
        int minInd = MathUtils.minInt(indsToRemove);
        int maxInd = MathUtils.max(indsToRemove);
        if (minInd < 0 || maxInd >= mNumCols) {
            throw new RuntimeException("Cannot remove cols: indices are invalid!");
        }   
        mNumCols-=pColInds.size();        
        if (mHeader != null) {
            mHeader = mHeader.removeFields(pColInds);            
        }                                                
        for (int i=0; i<mRows.size(); i++) {
            List oldRow = (ArrayList)mRows.get(i);
            ArrayList newValues = new ArrayList(mNumCols);            
            for (int j=0; j<oldRow.size(); j++) { 
                if (!(pColInds.contains(new Integer(j)))) {
                    newValues.add(oldRow.get(j));
                }
            }
            List newRow = getRowFactory().makeRow(newValues);                                                       
            mRows.set(i, newRow); 
        }                 
    }            
                   
    public void removeNamedCols(Collection<String> pColNames) {
    	Set indsToRemove = new LinkedHashSet<Integer>();
    	for (String colName: pColNames) {
    		int colInd = getColInd(colName);
    		indsToRemove.add(colInd);
    	}
    	removeCols(indsToRemove);    	
    }
    
    
    /** Note that indexing starts from 0. */            
    public void removeRows(Set<Integer> pRowIndices) {                
        int[] indsToRemove = ConversionUtils.integerCollectionToIntArray(pRowIndices);        
        int minInd = MathUtils.minInt(indsToRemove);
        int maxInd = MathUtils.max(indsToRemove);
        if (minInd < 0 || maxInd >= mRows.size()) {
            throw new RuntimeException("Cannot remove rows: indices are invalid; minind="+minInd+", maxind="+maxInd+", numRows="+mRows.size());
        }                        
        ArrayList newRows = new ArrayList(new ArrayList(mRows.size()-pRowIndices.size()));        
        for (int i=0; i<mRows.size(); i++) {            
            if (!(pRowIndices.contains(new Integer(i)))) {
                newRows.add(mRows.get(i));                
            }
        }                
        mRows = newRows;
    }
               
    public void removeDuplicateRows() {                
        LinkedHashSet rowSet = new LinkedHashSet(mRows);                        
        mRows = new ArrayList(rowSet);
    }                            
                
    /** creates an identical matrix with columns as rows and rows as columns */ 
    public Matrix reverse() {
        Matrix reverse = new Matrix(getNumCols(), getNumRows(), mIncludeHeader);
        reverse.setRowFormatFactory(mRowFormatFactory);
        reverse.setRowFormat(mHeader);
        for (int i=0; i<getNumRows(); i++) {
            for (int j=0; j<getNumCols(); j++) {
                reverse.set_row_col(j, i, get_row_col(i, j));        
            }
        }
        return reverse;                    
    }
    
    public Matrix(List[] pRows) throws InvalidMatrixFormatException {                
        mRows = new ArrayList(pRows.length);
        for (int i=0; i<pRows.length; i++) {
            mRows.add(getRowFactory().makeRow(pRows[i]));    
        }
        // crucial assertion. In case this fails, we throw hatchet into the lake. 
        ensureRowsAreOfEqualLength();
    }
    
    public Matrix(List<List> pRows) throws InvalidMatrixFormatException {                
        mRows = new ArrayList(pRows.size());
        for (int i=0; i<pRows.size(); i++) {
            mRows.add(getRowFactory().makeRow(pRows.get(i)));    
        }
        // crucial assertion. In case this fails, we throw hatchet into the lake. 
        ensureRowsAreOfEqualLength();
    }
               
    public int getNumRows() {
        return mRows.size();
    }
    
    public int getNumCols() {
        return mNumCols;    
    }
                                   
    public Object get_row_col(int pRow, int pCol) {        
        return ((List)mRows.get(pRow)).get(pCol);
    }
    
    public Object get_row_col(int pRow, String pColName) {        
        return get_row_col(pRow, getColInd(pColName));
    }    
    
    public void set_row_col(int pRow, int pCol, Object pVal) {        
        ((List)mRows.get(pRow)).set(pCol, pVal);
    }

    public Object get_x_y(int pX, int pY) {
        return get_row_col(pY, pX);
    }

    public void set_x_y(int pX, int pY, Object pVal) {
        set_row_col(pY, pX, pVal);
    }                                        
        
    public void readFromFile(String pFileName) throws IOException, InvalidMatrixFormatException {        
        readFromFile(new File(pFileName));      
    }
    
    public void readFromFile(File pFile) throws IOException, InvalidMatrixFormatException {
        mName = pFile.getPath();
        FileInputStream istream = new FileInputStream(pFile);
        readFromStream(istream);
        istream.close();
    }
    
    public void setName(String pName) {
        mName = pName;    
    }
    
    public String getName() {
        return mName;
    }
    
    public RowFormat getHeader() {
        return mHeader;
    }            
        
    /**
     * Append a new column "pNewColumnName" to the matrix. The values of the new column will be the values of the map,
     * joined by the values of pJoinColumn in this Matrix and keys in the map.
     *
     * Note: this is only for matrixes with a header!
     */
    public void join(Map pMap, String pJoinColumn, String pNewColumnName) {        
        if (mHeader == null) {
            throw new RuntimeException("Cannot join, no header!");
        }
        List joinCol = getCol(mHeader.getFieldIndex(pJoinColumn));                
        List newCol = ConversionUtils.convert(joinCol, new MapConverter(pMap, MapConverter.NotFoundBehauvior.RETURN_NULL));        
        appendCol(newCol, pNewColumnName);        
    }
    
    public Matrix[] splitColumnwise(int pNumColumnsInPart) {                   
        List fieldNames = mHeader.getFieldNameList();
        List[] fieldNamesForEachPart = CollectionUtils.splitToSegments_segmentlen(fieldNames, pNumColumnsInPart, false);        
        dbgMsg("Splitting matrix: "+StringUtils.arrayToString(fieldNamesForEachPart, "\n"));                    
        int numParts = fieldNamesForEachPart.length;
        Matrix[] result = new Matrix[numParts]; 
        for (int i=0; i<numParts; i++) {
            String[] fieldNamesForThisPart = ConversionUtils.stringCollectionToArray(fieldNamesForEachPart[i]);
            result[i] = select(fieldNamesForThisPart);
        }
        return result;                    
    }
    
    public Matrix[] splitRowwise(int[] pSliceSizes) {
        
        int sumSizes = MathUtils.sum(pSliceSizes);
        if (sumSizes != mRows.size()) {
            throw new RuntimeException("pSliceSizes must sum to the size of the matrix");
        }
        
        // ok, so the line numbers seem to match
        Matrix[] result = new Matrix[pSliceSizes.length];
        int startPtr = 0;
        for (int i=0; i<pSliceSizes.length; i++) {
            int curSize = pSliceSizes[i];
            int endPtr = startPtr+curSize;
            List rows = mRows.subList(startPtr, endPtr);
            result[i]=new Matrix(mHeader, rows);  
            startPtr=endPtr;
        }
        return result;                    
    }
    
    
    /**
     * Select a subset of columns. This will also rearrange the columns. This matrix will be unchanged, the 
     * selected columns will be returned in a new Matrix.
     *
     * Note: this is only for matrixes with a header!
     */
    public Matrix select(String[] pFieldNames) {
        if (mHeader == null) {
            throw new RuntimeException("Cannot select, no header!");
        }        
        RowFormat newFormat = mHeader.select(pFieldNames);
        ArrayList newRows = new ArrayList();
        for (int i=0; i<mRows.size(); i++) {
            Row oldRow = (Row)mRows.get(i);            
            newRows.add(oldRow.select(pFieldNames));
        }
        return new Matrix(newFormat, newRows);                
    }
    
    
    /** fails with runtimeException, if the "assertion" fails. 
     *
     * As a side effect, sets the field mNumCols, if all lines indeed have
     * some equal width.
     */
    private void ensureRowsAreOfEqualLength() throws InvalidMatrixFormatException {
        if (mRows.size()==0) {
            // no rows, so all obviously is well
            return;
        }
        
        int godGivenLen = ((List)mRows.get(0)).size();
        for (int i=1; i<mRows.size(); i++) {
            if (((List)mRows.get(i)).size() != godGivenLen) {
                throw new InvalidMatrixFormatException("Rows 0 and "+i+" cannot agree on the number of cols, abandon ship.\n"+
                                                       "Row 0 (len: "+mRows.get(0).size()+"): "+mRows.get(0)+"\n"+
                                                       "Row "+i+"(len: "+mRows.get(i).size()+"): "+mRows.get(i));
            }
        }     
        mNumCols = godGivenLen;
        // dbgMsg("Number of cols: "+mNumCols);
    }
    
    public void writeToStream(OutputStream pStream) {
        if (mPrettyPrinting) {
            Logger.dbg("Writing to stream...");
            PrintStream printStream = null;
            if (!(pStream instanceof PrintStream)) {
                printStream = new PrintStream(pStream);
            }
            else {
                printStream = (PrintStream)pStream;
            }            
            
            // TODO: here we very questionably first transform the whole
            // matrix into a String!
            String stringRep = toString(); 
            printStream.print(stringRep);
            
            if (!(pStream instanceof PrintStream)) {
                // close, as the stream was created by us
                printStream.close();
            }
            
                
                
            Logger.dbg("Done writing to stream...");
        }
        else {
            // ugly printing!
            // we have a dedicated impl for this; no need for 
            // intermediate string here!
            writeToStream_ugly(pStream);
        }           
        
    }        
    
    public void writeToFile(String pPath) throws IOException {
        writeToFile(new File(pPath));    
    }
    
    public void writeToFile(File pFile) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(pFile);
        PrintStream printStream = new PrintStream(fileStream);
        writeToStream(printStream);
        printStream.close();
        fileStream.close();                
    }
    
                      
    public void readFromStream(InputStream pStream) throws IOException, InvalidMatrixFormatException {        
        // read lines
        String[] lineArray = IOUtils.readLineArray(pStream);
        List<String> lineList = Arrays.asList(lineArray);
        // remove empty lines
        lineList = (List)CollectionUtils.extractMatchingObjects(lineList, new IsNonEmptyStringCondition());
        if (lineList.size()==0) {
            throw new InvalidMatrixFormatException("No lines found in the input stream!");     
        }
        List<String> dataList;
        if (mIncludeHeader) {
            // the first line is the header
            String headerLine = lineArray[0];            
            /** todo: do not use default factory */
            mHeader = getRowFormatFactory().makeFromHeaderString(headerLine);
            mNumCols = mHeader.getNumFields();
            dataList = CollectionUtils.tailList(lineList, 1);                        
        }
        else {
            // no header
            dataList = lineList;             
        }                             
                
        RowFactory rowFactory = getRowFactory();
        String separator = rowFactory.getSeparator();
        Logger.dbg("separator: <"+separator+">");
        Logger.dbg("row format factory: "+getRowFormatFactory());
        Logger.dbg("row factory: "+getRowFactory());
        
        mRows = new ArrayList(dataList.size());
        for (int i=0; i<dataList.size(); i++) {
        	try {                        
        		List row = getRowFactory().convert(dataList.get(i));
        		mRows.add(row);
        	}
        	catch (RuntimeParseException e) {
        		throw new InvalidMatrixFormatException(
        				"Failed parsing row "+i+": "+e.getMessage()+"\n"+
        	            "row text: "+dataList.get(i));	
        	}        	
        }
                                                               
        // dbgMsg("read "+mRows.size()+" rows");
        
        // crucial assertion. In case this fails, we throw axe into the well
        try {        
            ensureRowsAreOfEqualLength();
        }
        catch (InvalidMatrixFormatException e) {
            throw e;                
        }
    }            
    
    public List getCol(int pColInd) {        
        return get(getColIndices(pColInd));
        
        // the following, more efficient implementation disabled for uniformity:
        // ArrayList col = new ArrayList(mRows.size());
        // for (int i=0; col<mRows.size(); i++) {
        //     col.set(i, mRows.get(pColInd); i++);    
        // }
    }

    public List getCol(String pColName) {
        if (mHeader == null) {
            throw new RuntimeException("No header, cannot get column by name!");
        }
        int colInd = mHeader.getFieldIndex(pColName);         
        return getCol(colInd);                
    }            
    
    /** Hack: append a dymmy col to make rows unique */
    public void appendDummyCol() {
        int numRows = mRows.size();
        Range tmp = new Range(0, numRows);
        int[] dummyVals = tmp.asIntArr();
        List dummyValList = ConversionUtils.asList(dummyVals);
        appendCol(dummyValList, "Dummy");               
    }
    
    public void removeDummyCol() {
        removeCol("Dummy");    
    }
    
    
    
    
        
        
         
    
    /** Return rows, which, among all rows with same id, have the largest value. Break ties by random */
    public Matrix selectMaxRows(int idCol, int pValCol, boolean pTwoRowsAtOnce) {
                
        Matrix copy = createClone();
        // append dummy col to make rows unique
        copy.appendDummyCol();
        Matrix result = new Matrix(copy.mHeader);        
        
        MultiMap byIdMultiMap = copy.toMultiMap(idCol, true);                 
        Iterator ids = byIdMultiMap.keySet().iterator();
        while (ids.hasNext()) {            
            Object id = ids.next();
            // dbgMsg("Id: "+id);
            List maxValRow = null;
            double maxVal = -Double.MAX_VALUE;
            Set rowsWithSameId = byIdMultiMap.get(id);
            Iterator rowsWithSameIdIter = rowsWithSameId.iterator();
            List friendOfMaxValRow = null;
            // int dbg = 0; 
            while (rowsWithSameIdIter.hasNext()) {
                // dbgMsg("getting next row, dbg="+(dbg++));                
                List row = (List)rowsWithSameIdIter.next();
                Object valObj = row.get(pValCol);
                double val = ConversionUtils.anyToDouble(valObj);
                if (val > maxVal) {
                    // dbgMsg("new max val="+val);
                    maxVal = val;
                    maxValRow = row;
                    if (pTwoRowsAtOnce) {
                        // dbgMsg("getting next row, dbg="+(dbg++));
                        // dbgMsg("Number of rows with same id: "+rowsWithSameId.size());
                        friendOfMaxValRow = (List)rowsWithSameIdIter.next();
                    }
                }
            }
            result.appendRow(maxValRow);
            if (pTwoRowsAtOnce) {
                result.appendRow(friendOfMaxValRow);
            }
        }       
        
        // remove the dummy col
        result.removeDummyCol();
        return result;              
    }
        
           
    public Matrix selectMaxRows(String pIdCol, String pValCol, boolean pTwoRowsAtOnce) {
        int idCol = mHeader.getFieldIndex(pIdCol);
        int valCol = mHeader.getFieldIndex(pValCol);
        return selectMaxRows(idCol, valCol, pTwoRowsAtOnce);        
    }            
            
    
    /** get index set containing all cells of a single column */
    public IndexSet getColIndices(int pCol) {
        return new ColumnIndexSet(pCol, mRows.size());
    }
    
    /** get index set containing all cells of a single row */
    public IndexSet getRowIndices(int pRow) {
        return new RowIndexSet(pRow, mNumCols);
    }
    
    public IndexSet getAllIndices() {
        return new RowIndexSet(mRows.size(), mNumCols);
    }
    
    /** 
     * Get all the objects specified by the given index set
     * Note the objects contained can be anything, not 
     * only strings.
     */
    public List get(IndexSet pIndexSet) {
        ArrayList result = new ArrayList();
        Iterator indices = pIndexSet.iterator();
        while(indices.hasNext()){
            Index index = (Index)indices.next();
            result.add(get_x_y(index.getX(), index.getY()));            
        }
        return result;
    }
    
    public String[] getAsStrings(IndexSet pIndexSet) {
        List objs = get(pIndexSet);
        List objsAsStrings = ConversionUtils.convert(objs, new ObjectToStringConverter());
        return (String[])objsAsStrings.toArray(new String[objsAsStrings.size()]);             
    }
    
    public double[] getAsDoubles(IndexSet pIndexSet) {
        List objectList = get(pIndexSet);
        List stringList = ConversionUtils.objectListToStringList(objectList);
        String[] stringArray = (String[])stringList.toArray(new String[stringList.size()]);
        double[] doubleArray = ConversionUtils.stringArrToDoubleArr(stringArray);         
        return doubleArray;             
    }
    
    private IndexSet allIndices() {
        return new TotalIndexSet(this);
    }
    
    public double[][] asDoubles() {
        int numRows = getNumRows();
        int numCols = getNumCols();
        double[][] result = new double[numRows][];                        
        for (int i=0; i<numRows; i++) {            
            result[i] = new double[numRows];
            for (int j=0; j<numCols; j++) {
                result[i][j]=Double.parseDouble((String)get_row_col(i,j));
            }
        }
        return result;
    }
                            
    
    public double sum(IndexSet pIndexSet) {                        
        double[] vals = getAsDoubles(pIndexSet);
        return MathUtils.sum(vals);
    }
    
    public double max(IndexSet pIndexSet) {                        
        double[] vals = getAsDoubles(pIndexSet);
        return MathUtils.max(vals);
    }
    
    public double min(IndexSet pIndexSet) {                        
        double[] vals = getAsDoubles(pIndexSet);
        return MathUtils.min(vals);
    }
    
    public double avg(IndexSet pIndexSet) {                        
        double[] vals = getAsDoubles(pIndexSet);
        return MathUtils.avg(vals);
    }
    
    public double sum() {                        
        double[] vals = getAsDoubles(allIndices());
        return MathUtils.sum(vals);
    }
    
    public double max() {                        
        double[] vals = getAsDoubles(allIndices());
        return MathUtils.max(vals);
    }
    
    public double min() {                        
        double[] vals = getAsDoubles(allIndices());
        return MathUtils.min(vals);
    }
    
    public double avg() {                        
        double[] vals = getAsDoubles(allIndices());
        return MathUtils.avg(vals);
    }
    
    public double colSum(int pCol) {                        
        double[] vals = getAsDoubles(getColIndices(pCol));
        return MathUtils.sum(vals);
    }
    
    /** TODO: make own class for indexing fields by name; reserve class 
     *  matrix to pure number-based indexing... the same holds for 
     * the concept of Row and RowFormat */
    public double colSum(String pCol) {
        int colInd = fieldNameToFieldIndex(pCol);                        
        return colSum(colInd);
    }
    
    public double[] colSums() {
        double result[] = new double[mNumCols];
        for (int j=0; j< mNumCols; j++) {
            result[j] = colSum(j);            
        }
        return result;            
    }
    
    public double[] colAvgs() {        
        double result[] = new double[mNumCols];
        for (int j=0; j<mNumCols; j++) {
            result[j] = colAvg(j);
        }
        return result;            
    }
    
    public double colMax(int pCol) {                        
        double[] vals = getAsDoubles(getColIndices(pCol));
        return MathUtils.max(vals);
    }
    
    public double colAvg(int pCol) {                        
        double[] vals = getAsDoubles(getColIndices(pCol));        
        return MathUtils.avg(vals, true);
    }
    
    public double rowSum(int pRow) {                        
        double[] vals = getAsDoubles(getRowIndices(pRow));
        return MathUtils.sum(vals);
    }
    
    public double rowMax(int pRow) {                        
        double[] vals = getAsDoubles(getRowIndices(pRow));
        return MathUtils.max(vals);
    }
    
    public double rowAvg(int pRow) {                        
        double[] vals = getAsDoubles(getRowIndices(pRow));
        return MathUtils.avg(vals);
    }                    
    
    /** 
     * Perform a mathematical vector->scalar mapping for each column 
     * Thus length of result is equal to the number of columns.
     */
    public double[] performColumnWiseOperation(VectorToScalarOperation pOperation) {
        double[] result = new double[mNumCols];
        for (int j=0; j<mNumCols; j++) {
            double[] col = getAsDoubles(getColIndices(j));
            result[j] = pOperation.calculate(col);
        }             
        return result;                   
    }
    
    /** 
     * Perform a mathematical vector->scalar mapping for each column 
     * Thus length of result is equal to the number of columns.
     */
    public double[] performRowwiseOperation(VectorToScalarOperation pOperation) {
        double[] result = new double[mRows.size()];
        for (int i=0; i<mRows.size(); i++) {
            double[] row = getAsDoubles(getRowIndices(i));
            result[i] = pOperation.calculate(row);
        }       
        return result;                         
    }
                
    /** Perform in-place converting of column */
    public void convertCol(int pColIndex, 
                           Converter pConverter,
                           boolean pSuppressClassCastExceptions) {
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
            Logger.startSubSection("Matrix.ConvertCol");
        }            
        for (int i=0; i<mRows.size(); i++) {            
            List row = getRow(i);
            Object oldVal = row.get(pColIndex);
            Object newVal = null;
            if (pSuppressClassCastExceptions) {
                try {
                    newVal = pConverter.convert(oldVal);
                }
                catch (ClassCastException e) {
                    // silently leave the unconvertible value remain infesting our matrix...
                    newVal = oldVal;    
                }
            }
            else {
                newVal = pConverter.convert(oldVal);
            }
                            
            if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
                dbgMsg("old val: "+oldVal);
                dbgMsg("new val: "+newVal);
            }                
            row.set(pColIndex, newVal);                                    
        }
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
            Logger.endSubSection("Matrix.ConvertCol");
        }
    }
    
    public void convertCol(String pColName, Converter pConverter) {
        convertCol(pColName, pConverter, false);
    }
    
    public void convertCol(int pCol, Converter pConverter) {
        convertCol(pCol, pConverter, false);
    }
    
    
    /** Perform in-place converting of column */
    public void convertCol(String pColName, Converter pConverter, boolean pSuppressClassCastExceptions) {
        int colInd = mHeader.getFieldIndex(pColName);
        convertCol(colInd, pConverter, pSuppressClassCastExceptions);
    }
    
    public List colAsDoubleList(int pColInd) {
        return ConversionUtils.convert(getCol(pColInd), new AnyToDoubleConverter());
   }
    
    /** Make a converted copy of the matrix, and do not suppress any exceptions in the conversion */
    public Matrix convert(Converter pConverter) {
        return convert(pConverter, false, false);
    }
    
    /** Make a converted copy of the matrix */
    public Matrix convert(Converter pConverter, boolean pSuppressClassCastExceptions, boolean pSuppressNullPointerExceptions) {
        // dbgMsg("Starting to convert with converter: "+pConverter);
        Matrix converted = new Matrix(mHeader);
        converted.mNumCols = mNumCols;
        // dbgMsg("number of rows: "+mRows.size());
        for (int i=0; i<mRows.size(); i++) {
            // dbgMsg("i="+i);            
            ArrayList newVals = new ArrayList(mNumCols);
            for (int j=0; j<mNumCols; j++) {
                // dbgMsg("j="+j);
                // dbgMsg("Converting object ("+i+","+j+")="+get_row_col(i, j)+" ,with converter: "+pConverter+": ");
                Object oldVal = get_row_col(i, j);
                Object newVal = null;                                                                                
                try {                        
                    newVal = pConverter.convert(oldVal);
                }
                catch (ClassCastException ccEx) {                    
                    if (pSuppressClassCastExceptions) {
                        newVal = oldVal;
                        // silently leave the unconvertible value remain infesting our matrix...
                    }
                    else {
                        throw ccEx;
                    }
                }
                catch (NullPointerException nullEx) {                    
                    if (pSuppressNullPointerExceptions) {
                        // silently leave the unconvertible value remain infesting our matrix...
                        newVal = oldVal;
                    }
                    else {
                        throw nullEx;
                    }    
                }                                                                                                                    
                newVals.add(newVal);
            }
            converted.mRows.add(getRowFactory().makeRow(newVals));        
        }
        // dbgMsg("Done converting");        
        return converted;
    }
    
    /** Make a converted copy of the matrix */
    public Matrix convert(IndexBasedConverter pConverter, IndexMapper pIndexMapper) {
        // dbgMsg("Starting to convert with converter: "+pConverter);
        Matrix converted = new Matrix(mHeader);
        converted.mNumCols = mNumCols;
        // dbgMsg("number of rows: "+mRows.size());
        for (int i=0; i<mRows.size(); i++) {
            // dbgMsg("i="+i);            
            ArrayList newVals = new ArrayList(mNumCols);
            for (int j=0; j<mNumCols; j++) {
                // dbgMsg("j="+j);
                // dbgMsg("Converting object ("+i+","+j+")="+get_row_col(i, j)+" ,with converter: "+pConverter+": ");
                Object indexObject = pIndexMapper.matrixIndexToIndexObject(i, j);
                newVals.add(pConverter.convert(indexObject, get_row_col(i, j)));
            }
            converted.mRows.add(getRowFactory().makeRow(newVals));        
        }
        // dbgMsg("Done converting");        
        return converted;
    }
    
    public Matrix convertToStringMatrix() {
        Converter objectToStringConverter = new ObjectToStringConverter();
        NumberBeautifyingConverter numberBeautifyerConverter = new NumberBeautifyingConverter();        
        ConverterChain converter = new ConverterChain(objectToStringConverter, numberBeautifyerConverter);
        return convertToStringMatrix(converter);
    }
    
    public Matrix convertToStringMatrix(Converter<Object, String> pConverter) {
        
                                                   
        Matrix converted = new Matrix(mHeader);
        converted.mNumCols = mNumCols;        
        for (int i=0; i<mRows.size(); i++) {            
            List oldRow = getRow(i);
            List newRow;             
            if (oldRow instanceof Row) {
                newRow = ((Row)oldRow).formatFields();                                                                        
            }
            else {
                newRow = ConversionUtils.convert(oldRow, pConverter);                
            }                                                            
            converted.mRows.add(getRowFactory().makeRow(newRow));        
        }                
        return converted;       
                                
     //   old impl: 
     //    return convert(new ObjectToStringConverter());           
    }            
    
    public ArrayList getRow(int pInd) {
        return (ArrayList)mRows.get(pInd);
    }
                    
    public String dummyToString() {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<mRows.size(); i++) {
            buf.append(StringUtils.collectionToString(getRow(i), " "));
            buf.append("\n");
        }
        return buf.toString();
    }                    
                    
    public String toString_ugly() {    	
        StringBuffer buf = new StringBuffer();
        
        if (mHeader != null && !mDoNotWriteHeader) {            
            // write header
            // dbgMsg("colWidths_int: "+StringUtils.arrayToString(colWidths_int));
            // dbgMsg("Header as list "+mHeader.asList());            
            buf.append(StringUtils.collectionToString(mHeader.getFieldIdList(), mOutputColumnSeparator));            
            buf.append("\n");
        }           
        
        for (int i=0; i<mRows.size(); i++) {            
            List row = (List)mRows.get(i);
                                    
            for (int j=0; j<row.size(); j++) {
                if (j>0) {
                    buf.append(mOutputColumnSeparator);
                }
                if (row instanceof Row) {
                    buf.append(((Row)row).formatField(j));                    
                }   
                else {
                    Object val = row.get(j);                    
                    buf.append(val != null ? val.toString() : "null");
                }                                                     
            }
            buf.append("\n");            
        }   
        
        return buf.toString();
    }
    
    public void writeToStream_ugly(OutputStream pStream) {
        
        // Logger.info("Writing to stream (no pretty printing)...");
        PrintStream ps = null;
        if (!(pStream instanceof PrintStream)) {
            ps= new PrintStream(pStream);
        }
        else {
            ps = (PrintStream)pStream;
        }
                
        // StringBuffer buf = new StringBuffer();
        
        if (mHeader != null && !mDoNotWriteHeader) {            
            // write header
            // dbgMsg("colWidths_int: "+StringUtils.arrayToString(colWidths_int));
            // dbgMsg("Header as list "+mHeader.asList());                        // 
            ps.append(StringUtils.collectionToString(mHeader.getFieldIdList(), mOutputColumnSeparator));//            
            ps.append("\n");
        }           
        
        for (int i=0; i<mRows.size(); i++) {            
            List row = (List)mRows.get(i);
                                    
            for (int j=0; j<row.size(); j++) {
                if (j>0) {
                    ps.append(mOutputColumnSeparator);
                }
                if (row instanceof Row) {
                    ps.append(((Row)row).formatField(j));                    
                }   
                else {
                    Object val = row.get(j);                    
                    ps.append(val != null ? val.toString() : "null");
                }                                                     
            }
            ps.append("\n");            
        }
                
        if (!(pStream instanceof PrintStream)) {
            // close, as the stream was created by us
            ps.close();
        }
    }
    
    public String toString() {
        if (mPrettyPrinting) {
            return toString_pretty();
        }
        else {            
            return toString_ugly();       
        }
    }        
                                    
    public String toString_pretty() {
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
            Logger.startSubSection("Matrix.toString()");
        }
                                        
        Matrix asStrings = convertToStringMatrix();                
        Matrix stringLengths = asStrings.convert(new StringToStringLenConverter());
        
        if (stringLengths.contains(null)) {
            throw new RuntimeException("Urh, string length matrix contains a null...");
        }           
                    
        double[] maxStringLengths = stringLengths.performColumnWiseOperation(new MaxOperation());
                
        if (mHeader != null && !mDoNotWriteHeader) {
            List fieldNames = ConversionUtils.convert(mHeader.getFieldIdList(), new ObjectToStringConverter());
            dbgMsg("list of header field names = "+fieldNames);
            List headerFieldLengths = ConversionUtils.convert(fieldNames, new StringToStringLenConverter());
            for (int i=0; i<maxStringLengths.length; i++) {
                int headerFieldLength = ((Integer)headerFieldLengths.get(i)).intValue();
                if (headerFieldLength > maxStringLengths[i]) {
                    maxStringLengths[i] = headerFieldLength;
                }
            }
        }            
        
        // dbgMsg("maxStringLengths: "+StringUtils.arrayToString(maxStringLengths));        
        // we want the fields to have spaces in between... 
        double[] colWidths = MathUtils.sum(maxStringLengths, MathUtils.ones(mNumCols));
        int[] colWidths_int = ConversionUtils.doubleArrToIntArr(colWidths);        
        // dbgMsg("5");
        int totalWidth = MathUtils.sum(colWidths_int);
        // dbgMsg("6");
        StringBuffer buf = new StringBuffer((totalWidth+1)*mRows.size());
        // dbgMsg("7");
        // write header
        if (mHeader != null && !mDoNotWriteHeader) {            
            // write header
            // dbgMsg("colWidths_int: "+StringUtils.arrayToString(colWidths_int));
            // dbgMsg("Header as list "+mHeader.asList());            
            buf.append(StringUtils.formatList(mHeader.getFieldIdList(), colWidths_int));
            buf.append("\n");
        }                
        for (int i=0; i<mRows.size(); i++) {
            // dbgMsg("formatting row: "+i);
            List row = asStrings.getRow(i);
            String stringToAppend = row instanceof Row ?                
                ((Row)row).toString(colWidths_int) :
                StringUtils.formatList(row, colWidths_int);                                                         
            buf.append(stringToAppend);
            
            buf.append("\n");
        }
                
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {                
            Logger.endSubSection("Matrix.toString()");
        }            
        return buf.toString();                                
    }
    
            
    public TableModelWrapper asTableModel() {
        if (mTableModelWrapper == null) {
            mTableModelWrapper = new TableModelWrapper();
        }
        return mTableModelWrapper;
    }
    
    /** As table model that converts instances of pClass by pFieldExtractor, before returning them */ 
    public TableModelWrapper  asTableModel(Class pClass, Converter pFieldExtractor) {                  
        return new TableModelWrapper(pClass, pFieldExtractor);                
    }            
    
    public class TableModelWrapper extends AbstractTableModel {    
        
        /**
		 * 
		 */
		private static final long serialVersionUID = 1956554341820123258L;
		private HashMap mFieldExtractorsByClass;
        
        private TableModelWrapper() {
            mFieldExtractorsByClass = new HashMap();
        }
        
        private TableModelWrapper(Class pClass, Converter pFieldExtractor) {
            mFieldExtractorsByClass = new HashMap();
            mFieldExtractorsByClass.put(pClass, pFieldExtractor);                                 
        }                                                
                    
        public int getRowCount() {
            return getNumRows();
        }
        
        public int getColumnCount() {
            return getNumCols();
        }
        
        public boolean isSingletonRow(int pRow, boolean pConsiderNullAsObject, boolean pIgnoreFirstCol) {            
                
            if (getNumCols() == 0 || pIgnoreFirstCol && getNumCols() == 1) {
                return true;
            }
            
            
            int initialIndex = pIgnoreFirstCol ? 1 : 0;
            
            if (pConsiderNullAsObject) {        
                // consider null as a first-class object
                Object prototypeVal = get_row_col(pRow, initialIndex);
               
                for (int i=initialIndex+1; i<getNumCols(); i++) {
                    Object val = getValueAt(pRow, i);
                    
                    if (prototypeVal == null && val == null) {
                        // no action
                    }            
                    else if (prototypeVal == null && val != null) {
                        return false;
                    }            
                    else if (prototypeVal != null && val == null) {
                        return false;
                    }
                    else {
                        // both non-null
                        if (!(prototypeVal.equals(val))) {
                            return false;
                        }
                    }            
                }
                return true;
            }
            else {
                // require 2 different non-null objects
                Object prototype = null;
                
                for (int i=initialIndex; i<getNumCols(); i++) {
                                                            
                    Object o = getValueAt(pRow, i);                    
                                 
                    if (o != null) {
                        if (prototype == null) {
                            // first non-null object found                            
                            prototype = o;
                        }
                        else if (!(prototype.equals(o))) {
                            // two different objects found                            
                            return false;                        
                        }
                    }
                }
                            
                return true;
                
            }
                                           
        }
        
        public Object getValueAt(int row, int column) {
            Object val = get_row_col(row, column);
            if (val != null) {
                Class c = val.getClass();
                Class[] classAndSuperClasses = ReflectionUtils.getSuperClasses(c);                 
                List convenientExtractors = CollectionUtils.select(mFieldExtractorsByClass, Arrays.asList(classAndSuperClasses));
                if (convenientExtractors.size() >= 1) {                                        
                    Converter fiedExtractor = (Converter)convenientExtractors.get(0);                    
                    val = fiedExtractor.convert(val);
                    if (convenientExtractors.size() > 1) {
                        Logger.warning("There were multiple convenient field exractors for val:"+val);
                    }
                }
            }
            return val;    
        }
        
        public String getColumnName(int column) {
            if (mHeader == null) {
                // let's hope superclass provides something sensible
                return super.getColumnName(column);
            }
            else {
                // header has been set
                return mHeader.getFieldName(column).toString();
            }
        }
    } // end of inner class TableModelWrapper
    
    
    
    public List asList() {
        if (mListWrapper == null) {
            mListWrapper = new ListWrapper();
        }
        return mListWrapper;
    }
    
    public SymmetricPair[] toRowPairArray() {
        return CollectionUtils.splitToPairArray(mRows);
    }            
    
    public Matrix condenseAdjacentRowsToPairs() {
        Matrix condensed = new Matrix(mHeader);
        if (getNumRows()%2 != 0) {
            throw new RuntimeException("Cannot condense rows: uneven number of rows!");
        }
        for (int i=0; i<mRows.size(); i+=2) {                
            List row1 = getRow(i);
            List row2 = getRow(i+1);
            ArrayList condensedRow = new ArrayList();
            for (int j=0; j<getNumCols(); j++) {
                Pair pair = new Pair(row1.get(j), row2.get(j));
                condensedRow.add(pair);
            }
            condensed.appendRow(condensedRow);
        }   
        return condensed;
    }
    
    public Matrix uncondensePairsToAdjacentRows() {
        Matrix uncondensed = new Matrix(mHeader);        
        for (int i=0; i<mRows.size(); i++) {                
            List uncondensedrow1 = new ArrayList();
            List uncondensedrow2 = new ArrayList();            
            List condensedRow = getRow(i);
            for (int j=0; j<getNumCols(); j++) {
                Pair pair = (Pair)condensedRow.get(j);
                uncondensedrow1.add(pair.getObj1());
                uncondensedrow2.add(pair.getObj2());
            }
            uncondensed.appendRow(uncondensedrow1);
            uncondensed.appendRow(uncondensedrow2);
        }   
        return uncondensed;
    }
    
    public void shuffleCol(int pCol) {
        List vals = getCol(pCol);
        Collections.shuffle(vals);
        setCol(pCol, vals);
        /*
        int[] newOrder = MathUtils.intRange(0, getNumRows());
        List tmp = ConversionUtils.asList(newOrder);
        Collections.shuffle(tmp);
        newOrder = ConversionUtils.integerCollectionToIntArray(tmp);
        for (int i=0; i<getNumRows(); i++) {
                    
        }
        */        
    }
    
    /**
     * Wraps the Matrix as an (unmodifiable through this interface!) List (elements of the list are the
     * rows of the matrix, which are always ArrayList instances.
     */
    private class ListWrapper extends AbstractList<List> {
        // by default the matrix cannot be modified through this wrapper!
        // this flag is meant to be set only when sorting the matrix
        private boolean mAllowSModifications = false;
        
        public List get(int index) {
            return mRows.get(index);
        }
        public int size() {
            return mRows.size();
        }            
                
        public List set(int index, List element) {
            if (!mAllowSModifications) {
                throw new UnsupportedOperationException("List wrapper for Matrix does not support modifications!");
            }
            else {
                // the element must be an ArrayList!
            	assert(element instanceof ArrayList);
                return mRows.set(index, element);                 
            }
        }
        
    } // end of inner class ListWrapper
    
    
        
    public void sortByCol(int pCol) {        
        sortByCol(pCol, null);
    }
    
    /** If pComparator == null, try to use natural ordering */
    public void sortByCol(int pCol, Comparator pComparator) {
        // wrap the matrix as a java.util.List
        ListWrapper listWrapper = new ListWrapper();
        // only on this special occasion do we allow modifications through the list wrapper
        listWrapper.mAllowSModifications = true;
        // create a comparator that compares by the given field, using pComparator as the base comparator         
        // note that ByFieldComparator accepts null basecomparator, in which case natural ordering is used
                                
        ByFieldComparator byFieldComparator = new ByFieldComparator(new ListFieldExtractor(pCol), pComparator);                                
        Collections.sort(listWrapper, byFieldComparator);
    }           
    
    public List elements() {
        ArrayList result = new ArrayList();
        for (Object o: asList()) {
            result.addAll((List)o); 
        }
        return result;
    }
    
    public static void main (String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Usage: java util.Matrix <filename> (just for debugging; prints the matrix to stdout)");
        }
        String fileName = args[0];
        
        Matrix m = new Matrix(true);
        
        try {
            dbgMsg("starting to load...");
            m.readFromFile(fileName);
            // dbgMsg("loaded:\n"+m.toString());
            JTable table = new JTable(m.asTableModel());
            DummyApplication.showComponent(table, fileName, true);            
        }
        catch (Exception e) {
            e.printStackTrace();
        }                
    }            

    public Data2DWrapper asData2D() {
        return new Data2DWrapper();        
    }
            
    private class Data2DWrapper implements Data2D {
        
        private double mMax;
        private double mMin;
        
        private Data2DWrapper() {            
            mMax = max();
            mMin = min();
        }
                                                                                     
        public int getValueAt(int pX, int pY) {
            double val = ConversionUtils.anyToDouble(get_x_y(pX, pY));
            double normalized = MathUtils.normalize(val, mMin, mMax);
            return (int)(255.d*normalized);
        }
            
        public int getMaxVal() {
            return 256;
        }
      
        public int getMinVal() {
            return 0;
        }
        
        public int getNumXIndices() {
            return getNumCols();
        }   
        
        public String getXIndexAt(int pInd) {
            return ""+pInd;
        }
    
        public int getNumYIndices() {
            return getNumRows();
        }   
        
        public String getYIndexAt(int pInd) {
            return ""+pInd;
        }                        
    }
    
    private static void dbgMsg(String pMsg) {
        if (OUTPUT_CRAPPY_DEBUG_MESSAGES) {
            Logger.dbg("Matrix: "+pMsg);
        }            
    }                               
                                      
}

