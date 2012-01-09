package util.matrix;

import util.*; 

import java.util.*;


/* An Row is just a ArrayList, with additional informationt(RowFormat) about its "fields */ 
public class Row extends ArrayList {
          
    protected RowFormat mFormat;                                                        
    
    /** 
     * Note that altough this is a public constructor, Rows should always be created by a RowFactory. 
     * 
     */
    public Row(RowFormat pFormat) {
        this(pFormat, true);
    }
                        
    /** 
     * Note that altough this is a public constructor, Rows should always be created by a RowFactory. 
     * @param pAddNulls will the list be filled with nulls?
     */
    public Row(RowFormat pFormat, boolean pAddNulls) {
        super(pFormat.getNumFields());                
        mFormat = pFormat;                
        if (pAddNulls) {
            int numFields = pFormat.getNumFields();            
            for (int i=0; i<numFields; i++) {
                add(null);
            }
        }                        
    }
    
    /** 
     * Append a field to the Row. The field needs also be added to the format.
     * Note that this is a bit inefficient, as a new RowFormat needs to be created,
     * as RowFormat objects are immutable(if that were not the case, we would just run into another kinds of problems!).
     * As a work-around for this inefficiency, a method should be devised to add the field and set the new format at 
     * the same time, so that format objects might be shared.     
     */
    public void appendField(String pFieldName, Object pData) {
        mFormat = mFormat.addFieldToEnd(pFieldName);
        add(pData);                
    }
    
    public void setOrAppendField(String pFieldName, Object pData) {
        if (mFormat.containsField(pFieldName)) {
            set(pFieldName, pData);
        }
        else {
            appendField(pFieldName, pData);    
        }
    }
    
    public void setOrInsertField(String pFieldName, Object pData, int pInd) {
        if (mFormat.containsField(pFieldName)) {
            set(pFieldName, pData);
        }
        else {
            insertField(pInd, pFieldName, pData);    
        }
    }
    
    /** 
     * @see #appendField(String, Object) ,as this is conceptually a similar operation.     
     */
    public void insertField(int pIndex, String pFieldName, Object pData) {
        mFormat = mFormat.insertField(pIndex, pFieldName);
        add(pIndex, pData);                
    }           
    
    public void setFormat(RowFormat pFormat) {
        mFormat = pFormat;    
    }
                                        
    public RowFormat getFormat() {
        return mFormat;    
    }
                                          
    public double getAsDouble(String pColId) throws NumberFormatException {                  
        Object valObj = get(pColId);        
        if (valObj == null) {
            return Double.NaN;
        }
        else if (valObj instanceof String){                
            return Double.parseDouble((String)valObj);
        }                         
        else if (valObj instanceof Number) {
            return ((Number)valObj).doubleValue();
        }
        else {
            throw new RuntimeException("Cannot represent as double: "+valObj);
        }
    }
        
    public String toString() {
        // @todo: improve?
        List fields = formatFields();
        return StringUtils.listToString(fields, " ");             
    }        
    
    public String formatField(int pInd) {
        return mFormat.formatField(pInd, get(pInd));
    }
    
    public List formatFields() {
        ArrayList result = new ArrayList(size());
        for (int i=0; i<size(); i++) {                        
            result.add(mFormat.formatField(i, get(i)));
        }    
        return result;
    }
    
    public String toString(int[] pFieldWidhts) {
        List fields = formatFields();
        return StringUtils.formatList(fields, pFieldWidhts);    
    }
        
    public void set(String pFieldName, Object pVal) {
        if (!(mFormat.containsField(pFieldName))) {
            throw new RuntimeException("No such field: "+pFieldName);
        }
        int fieldInd = mFormat.getFieldIndex(pFieldName);
        set(fieldInd, pVal);                
    }
    
    public void set(String pFieldName, double pVal) {        
        set(pFieldName, ""+pVal);                
    }    
    
    public void removeField(String pFieldName) {
        int index = mFormat.getFieldIndex(pFieldName);
        mFormat = mFormat.removeField(index);
        remove(index);        
    }
            
    public Object get(String pFieldName) {
        if (!(mFormat.containsField(pFieldName))) {
            throw new RuntimeException("No such field: "+pFieldName);
        }
        int fieldInd = mFormat.getFieldIndex(pFieldName);
        return get(fieldInd);    
    }        
    
    public Row join(Row pRow, String pField) {
        // dbgMsg("Join!");                      
        RowFormat newFormat = mFormat.join(pRow.mFormat, pField);  
        Row newRow = (Row)newFormat.getRowFactory().makeRow(); 
        // String[] ourFieldNames = mFormat.getFieldNames();
        // String[] theirFieldNames =pRow.mFormat.getFieldNames();
        // dbgMsg("ourFieldNames="+StringUtils.arrayToString(ourFieldNames, ", "));
        // dbgMsg("theirFieldNames="+StringUtils.arrayToString(theirFieldNames, ", "));
        
        HashSet ourFields = new HashSet(Arrays.asList(mFormat.getFieldNames()));
        HashSet theirFields = new HashSet(Arrays.asList(pRow.mFormat.getFieldNames()));
        ourFields.remove(pField);
        theirFields.remove(pField);
        HashSet commonFields = new HashSet(ourFields);        
        commonFields.retainAll(theirFields);
        ourFields.removeAll(commonFields);
        theirFields.removeAll(commonFields);
        
        // dbgMsg("join field "+pField);
        // dbgMsg("our fields: "+StringUtils.collectionToString(ourFields, " "));
        // dbgMsg("their fields: "+StringUtils.collectionToString(theirFields, " "));
        // dbgMsg("common fields: "+StringUtils.collectionToString(commonFields, " "));
        

        // copy join field 
        newRow.set(pField, get(pField)); // (copied arbitrarily from...us, as should be same!)
                
        // copy our fields 
        Iterator ourFieldsIter = ourFields.iterator();
        while (ourFieldsIter.hasNext()) {
            String field = (String)ourFieldsIter.next();
            newRow.set(field, (get(field)));            
        }
        
        // copy their fields        
        Iterator theirFieldsIter = theirFields.iterator();
        while (theirFieldsIter.hasNext()) {
            String field = (String)theirFieldsIter.next();
            newRow.set(field, (pRow.get(field)));            
        }

        // copy common fields (renaming fields becomes necessary)
        Iterator commonFieldsIter = commonFields.iterator();
        while (commonFieldsIter.hasNext()) {
            String field = (String)commonFieldsIter.next();
            newRow.set(field+"1", (get(field)));
            newRow.set(field+"2", (pRow.get(field)));            
        }                                 
          
        return newRow;         
    }
    
    public Row select(String[] pFieldNames) {                              
        RowFormat newFormat = mFormat.select(pFieldNames);                  
        Row newRow = (Row)newFormat.getRowFactory().makeRow();                
        for (int i=0; i<pFieldNames.length; i++) {
            String fieldName = pFieldNames[i]; 
            newRow.set(fieldName, get(fieldName));
        }                  
        return newRow;        
    }
            
    public boolean equals(Object pObj) {
        if (!(pObj instanceof Row)) {
            return false;
        }
        Row other = (Row)pObj;
        return mFormat.equals(other.mFormat) && super.equals(other); 
    }             
    
    // rely to superclass for hash code
            
    public Row createClone() {                                
        Row clone = new Row(mFormat, false);
        clone.addAll(this);
        return clone;
    }            
    
    public boolean containsField(String pFieldId) {
        return mFormat.containsField(pFieldId);    
    }    

    public static class ByFieldComparator implements Comparator {
        private String mFieldName;
        
        public ByFieldComparator(String pFieldName) {
            mFieldName = pFieldName;
        }
        
        public int compare(Object p1, Object p2) {
            Row r1 = (Row)p1;
            Row r2 = (Row)p2;
            Comparable val1 = (Comparable)r1.get(mFieldName);
            Comparable val2 = (Comparable)r2.get(mFieldName);
            return val1.compareTo(val2);
        }
    }        
    
}

