package util.matrix;

import util.*; 
import util.converter.*;

import java.util.*;

/** format specification for Row objects. Note that instances of RowFormat are immutable! */
public final class RowFormat {    
                              
    /////////////////////////////////////////////////////////////////
    // static schaisse...
    /////////////////////////////////////////////////////////////////
    
    public static RowFormatFactory sDefaultFactory = new RowFormatFactory("DEFAULT_ROW_FORMAT_FACTORY");      
    private static final FieldIdToFieldNameConverter FIELD_ID_TO_FIELD_NAME_CONVERTER = new FieldIdToFieldNameConverter();                                          
                   
    /////////////////////////////////////////////////////////////////
    // static schaisse ends
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // actual data
    /////////////////////////////////////////////////////////////////
        
    /** the actual data; all other stuff is created on-demand */
    private FieldId[] mFields;
    
    /** Our honored maker; must NEVER be null */
    private RowFormatFactory mFactory;
    
    /////////////////////////////////////////////////////////////////
    // actual data ends
    /////////////////////////////////////////////////////////////////
    
    
    /////////////////////////////////////////////////////////////////
    // dependent fields that could as well be created on demand
    /////////////////////////////////////////////////////////////////
    
    /**
     * Mapping: field name -> fieldIndex (keys are Strings, values are field indices) 
     * Note that keys are indeed strings, and not FieldIds!
     * Note also that this is only instantiated on demand; always use method getFieldIndexByFieldNameMap() to get the instance,
     * and never refer to it directly!
     */
    private HashMap mFieldIndexByFieldId;
    
    /**
     * Mapping: field name -> fieldId (keys are Strings, values are FieldIds)      
     * Note also that this is only instantiated on demand; always use method getFieldIdByFieldNameMap() to get the instance,
     * and never refer to it directly!
     */
    private HashMap mFieldIdByFieldName;
    
    
    /**
     * Array specifying the lengths of the fields in terms of atomic fields they contain. 
     * This is created on demand, by the method getNumFieldsArray().
     */
    private int[] mNumFieldsArray;        
    
    /** Field name array is created on demand, by the method getFieldNames() */
    private String[] mFieldNames;
        

    /////////////////////////////////////////////////////////////////
    // dependent fields end
    /////////////////////////////////////////////////////////////////

    /** The sole constructor, do not violate this beauty! This is only to be called by RowFormatFactory */
    RowFormat(FieldId[] pFields, RowFormatFactory pFactory) {
        mFields = pFields;
        mFactory = pFactory;        
    }
    

    /////////////////////////////////////////////////////////////////
    // start of methods
    /////////////////////////////////////////////////////////////////
    public FieldId[] getFieldsIds() {
        return mFields;        
    }
        
    
    public String getFieldName(int pInd) {
        return mFields[pInd].getName();
    }
    
    public String[] getFieldNames() {
        if (mFieldNames == null) {            
            mFieldNames = (String[])ConversionUtils.convert(mFields, FIELD_ID_TO_FIELD_NAME_CONVERTER, String.class);            
        }
        return mFieldNames;
    }
    
    public String[] getFieldNames(int[] pFieldIndices) {
        String[] allFieldNames = getFieldNames();            
        List selectedFieldNames = CollectionUtils.selectObjects(Arrays.asList(allFieldNames), pFieldIndices);
        return ConversionUtils.stringCollectionToArray(selectedFieldNames);
    }    
    
    public static RowFormatFactory getDefaultFactory() {
        return sDefaultFactory;                
    }
    
    public static void setDefaultFactory(RowFormatFactory pFactory) {
        sDefaultFactory = pFactory;
    }            
        
    
    private Map getFieldIndexByFieldNameMap() {
        if (mFieldIndexByFieldId == null) {
            mFieldIndexByFieldId = new HashMap();
            for (int i=0; i<mFields.length; i++) {
                mFieldIndexByFieldId.put(mFields[i].mData, new Integer(i));    
            }
        }
        return mFieldIndexByFieldId;        
    }
    
    private Map getFieldIdByFieldNameMap() {
        if (mFieldIdByFieldName == null) {
            mFieldIdByFieldName = new HashMap();
            for (int i=0; i<mFields.length; i++) {
                mFieldIdByFieldName.put(mFields[i].mData, mFields[i]);    
            }
        }
        return mFieldIdByFieldName;        
    }
    
    
    
    
    public Object makeFieldRep(String pFieldName, String pObjectString) {
        return mFactory.makeFieldRep(pFieldName, pObjectString);    
    }

    public Object makeFieldRep(int pFieldIndex, String pObjectString) {
        return mFactory.makeFieldRep(mFields[pFieldIndex].getName(), pObjectString);    
    }        
    
    public String formatField(String pFieldName, Object pValue) {
        // dbgMsg("FormatField("+pFieldName+","+pValue+")");
        FieldId fieldId = getFieldId(pFieldName);
        // return mFactory.formatField(pFieldName, pValue);
        return mFactory.formatField(fieldId, pValue);
    }
    
    public String formatField(int pFieldIndex, Object pValue) {
        // dbgMsg("FormatField("+pFieldIndex+","+pValue+")");
        if (pFieldIndex <0 || pFieldIndex >= mFields.length) {
            throw new RuntimeException("No such field: "+pFieldIndex);
        }            
        // return mFactory.formatField(mFields[pFieldIndex].getName(), pValue);
        return formatField(mFields[pFieldIndex].getName(), pValue);
    }    
    
    public RowFormatFactory getFactory() {
        return mFactory;    
    }
              
    public RowFactory getRowFactory() {
        return mFactory.makeRowFactory(this);        
    }              
              
            
    /** This is because a single entry in the row format may account for multiple fields in a data file */
    public int getNumFields(int pHeaderFieldInd) {
        return mFields[pHeaderFieldInd].getNumAtomicFields();            
    }
        
    public int getNumFields() {
        return mFields.length;            
    }
    
    /*** This is cryptic, no time to explain */
    public int[] getNumFieldsArray() {
        if (mNumFieldsArray == null) {
            mNumFieldsArray = new int[mFields.length];
            for (int i=0; i<mNumFieldsArray.length; i++) {
                mNumFieldsArray[i] = getNumFields(i);
            }
        }
        return mNumFieldsArray;
    }
            
    public FieldId[] getFieldIds() {
        return  mFields;    
    }
    
    /** @return a list of FieldId instances. */ 
    public List getFieldIdList() {
        return Arrays.asList(mFields);
    }
    
    /** @return a list of String instances. */ 
    public List getFieldNameList() {
        return Arrays.asList(getFieldNames());
    }    
            
    public FieldId getFieldId(int pInd) {
        return mFields[pInd];
    }
    
    public FieldId getFieldId(String pFieldName) {
        int fieldIndex = getFieldIndex(pFieldName);
        if (fieldIndex == -1) {
            return null;
        }
        else {
            return getFieldId(fieldIndex);
        }
    }
                        
    
    public String[] getNumericFieldNames() {
        int[] fieldIndices = getNumericFieldIndices();
        List numericFieldsIndicesList = ConversionUtils.asList(fieldIndices);
        List allFieldNamesList = Arrays.asList(getFieldNames());
        List numericFieldNamesList = ConversionUtils.convert(numericFieldsIndicesList, new IndexToListElementConverter(allFieldNamesList));
        return (String[])ConversionUtils.collectionToArray(numericFieldNamesList, String.class);                            
    }
    
    public int[] getNumericFieldIndices() {
        String[] fieldNames = getFieldNames();
        return CollectionUtils.getIndicesOfContainedObjects(Arrays.asList(fieldNames), mFactory.getNumericFieldNames());                    
    }

    public boolean isNumericField(String pFieldName) {
        return mFactory.isNumericField(pFieldName);
    }
    
    /** Get index of field by name. Return -1 when there is no such field */
    public int getFieldIndex(String pFieldName) {
        // dbgMsg("GetFieldIndex("+pFieldName+")");
        // dbgMsg("Fields: "+StringUtils.arrayToString(mFields));
        Integer fieldIndex = (Integer)getFieldIndexByFieldNameMap().get(pFieldName);
        if (fieldIndex == null) {
            return -1;
        }
        else {
            return fieldIndex.intValue();
        }                
    }
        

    public boolean containsField(String pFieldId) {
        return getFieldIndexByFieldNameMap().containsKey(pFieldId);    
    }
                
    // static boolean firstjoin = true;    
    public RowFormat select(String[] pFieldNames) {
        Set ourFieldNames = new HashSet(Arrays.asList(getFieldNames()));                
        if (!(ourFieldNames.containsAll(Arrays.asList(pFieldNames)))) {
            Set desiredFields = new HashSet(Arrays.asList(pFieldNames));
            Set missingFields = new HashSet(desiredFields);
            missingFields.removeAll(ourFieldNames);
            throw new RuntimeException("Cannot select; we do not have the following fields:\n"+
                                        StringUtils.collectionToString(missingFields, ", "));
        }
        // OK, we have all the fields that are asked for...
        ArrayList selectedFieldsList = new ArrayList();        
        Map fieldsByName = getFieldIdByFieldNameMap();
        for (int i=0; i<pFieldNames.length; i++) {            
            selectedFieldsList.add(fieldsByName.get(pFieldNames[i]));
        }
        FieldId[] selectedFields = (FieldId[])ConversionUtils.collectionToArray(selectedFieldsList, FieldId.class);
        RowFormat newFormat = new RowFormat(selectedFields, mFactory);
        return newFormat;                                     
    }
    
    
    /** 
     * Join two RowFormats; fields of pFormat will be appended to ours'
     * 
     * @todo Currently cannot handle case when fields names would conflict! 
     */
    public RowFormat join(RowFormat pFormat, String pFieldName) {        
        FieldId joinField = (FieldId)getFieldIdByFieldNameMap().get(pFieldName);
        LinkedHashSet ourFields = new LinkedHashSet(Arrays.asList(mFields));
        LinkedHashSet theirFields = new LinkedHashSet(Arrays.asList(pFormat.mFields));
        if ( !(getFieldIdByFieldNameMap().containsKey(pFieldName)) || !(pFormat.getFieldIdByFieldNameMap().containsKey(pFieldName))) {
            throw new RuntimeException("Cannot join formats: at least one of them does not contain join field +\""+pFieldName+"\"");
        }
        // let the join field remain in it's original place in our field set
        // ourFields.remove(joinField);
        theirFields.remove(joinField);
        // common fields = all common fields, execpt pFieldName
        LinkedHashSet commonFields = new LinkedHashSet(ourFields);
        commonFields.remove(joinField);        
        commonFields.retainAll(theirFields);
        ourFields.removeAll(commonFields);
        theirFields.removeAll(commonFields);
        
        /*
        if (commonFields.size()!=0) {
            throw new RuntimeException("Currently cannot handle conflicting field names; following field names conflict:\n"+
                                       StringUtils.collectionToString(commonFields,", "));
        } 
        */
        // common fields implementation temporalily disabled
        // commonFields.remove(joinField);
        
        /*
        if (firstjoin) {
            // Logger.dbg("joining by field: "+pField);
            // Logger.dbg("our fields: "+Utils.collectionToString(ourFields, ""));
            // Logger.dbg("their fields: "+Utils.collectionToString(theirFields, ""));
            //Logger.dbg("common fields: "+Utils.collectionToString(commonFields, ""));
            firstjoin = false;
        }
        */
        
        ArrayList newFieldsList = new ArrayList(ourFields.size()+theirFields.size()+commonFields.size()+1);
        // copy our fields (including join field)
        newFieldsList.addAll(ourFields);
        // newFieldsList.add(joinField);
        newFieldsList.addAll(theirFields);
                                        
        // copy common fields other that the join field (renaming fields becomes necessary!)                
        Iterator commonFieldsIter = commonFields.iterator();
        while (commonFieldsIter.hasNext()) {
            FieldId fieldId = (FieldId)commonFieldsIter.next();
            if (!(fieldId instanceof BasicFieldId)) {
                throw new RuntimeException("Cannot rename non-basic field: "+fieldId);
            }
            // assertion OK, proceed...
            newFieldsList.add(makeFieldId(fieldId.getName()+"1"));
            newFieldsList.add(makeFieldId(fieldId.getName()+"2"));            
        }          
              
        FieldId[] newFields = (FieldId[])ConversionUtils.collectionToArray(newFieldsList, FieldId.class);
        RowFormat newFormat = new RowFormat(newFields, mFactory);
        return newFormat;
    }
            
    
    public String toString() {
        return StringUtils.arrayToString(mFields, " ");        
        /*
        StringBuffer buf = new StringBuffer();
        buf.append(mFields[0]);
        for (int i=1; i<mFields.length; i++) {
            buf.append(" ");            
            buf.append(formatField(mFields[i]));   
        }
        return buf.toString();
        */
    }
    
    
    /** 
     * The kludge is as follows:
     * We insist that there is exactly one multi-part field(otherwise abandon all hope)
     * We format that field with the help of the Factory, which has a list of default field components...
     * (This has really gotten out of hand.)
     */
    public String toString(int pNumMarkers) {
        // dbgMsg("toString, len="+mFields.length);        
        StringBuffer buf = new StringBuffer();                                        
        for (int i=0; i<mFields.length; i++) {
            if (i!=0) {
                buf.append(" ");
            }
            FieldId field = mFields[i];
            if (field instanceof BasicFieldId) {
                // dbgMsg("Basic field: "+field.toString());
                buf.append(field.toString());
            }
            else if(field instanceof MultiPartFieldId) {
                // dbgMsg("Multi-part field: "+field.toString());
                MultiPartFieldId multiField = (MultiPartFieldId)field;
                if (multiField.mAtomicParts == null) {
                    // dbgMsg("Multi-part field does not know it's components! "+field.toString());
                    // the multi field does not know it's components
                    // System.err.println("Atomic parts: "+mFactory.getDefaultMultiPartFieldComponents());
                    List atomicParts = mFactory.getDefaultMultiPartFieldComponents().subList(0, pNumMarkers);                    
                    buf.append(StringUtils.collectionToString(atomicParts, " "));
                }
                else {
                    // Logger.warning("ZZzzt, we knows how to mind our own business!");
                    // the multi field knows how to mind it's own business                    
                    buf.append(field.toString());
                }                            
            }
            else {
                throw new RuntimeException("rätinää!");
            }                                                 
        }
        return buf.toString();
    }    

    public boolean equals(Object pObj) {
        RowFormat pOther = (RowFormat)pObj;
        return CollectionUtils.areArraysEqual(mFields, pOther.mFields);
    }            
    
    public int hashCode() {
        return Arrays.asList(mFields).hashCode();
    }                               

    private FieldId makeFieldId(String pFieldName) {
        if (mFactory.isBasicField(pFieldName)) {
            return new BasicFieldId(pFieldName);
        }
        else {
            return new MultiPartFieldId(pFieldName);
        }        
    }            
    
    public RowFormat addFieldToBeginning(String pFieldName) {
        return addFieldToBeginning(makeFieldId(pFieldName));                           
    }
    
    public RowFormat addFieldToEnd(String pFieldName) {
        return addFieldToEnd(makeFieldId(pFieldName));                
    }
    
    public RowFormat addFieldToEnd(FieldId pFieldId) {        
        ArrayList fieldIdList = new ArrayList(getFieldIdList());
        fieldIdList.add(pFieldId);
        return new RowFormat((FieldId[])ConversionUtils.collectionToArray(fieldIdList, FieldId.class), mFactory);        
    }
    
    public RowFormat addFieldToBeginning(FieldId pFieldId) {
        ArrayList fieldIdList = new ArrayList(getFieldIdList());
        fieldIdList.add(0, pFieldId);
        return new RowFormat((FieldId[])ConversionUtils.collectionToArray(fieldIdList, FieldId.class), mFactory);                           
    }
    

    public RowFormat insertField(int pWhereToInsert, String pFieldName) {        
        return insertField(pWhereToInsert, makeFieldId(pFieldName));                
    }

    public RowFormat insertField(int pWhereToInsert, FieldId pFieldId) {        
        ArrayList fieldIdList = new ArrayList(getFieldIdList());
        fieldIdList.add(pWhereToInsert, pFieldId);
        return new RowFormat((FieldId[])ConversionUtils.collectionToArray(fieldIdList, FieldId.class), mFactory);        
    }                                          
                        
    
    public RowFormat removeField(int pFieldInd) {
        ArrayList fieldList = new ArrayList(Arrays.asList(mFields));
        fieldList.remove(pFieldInd);
        return new RowFormat((FieldId[])ConversionUtils.collectionToArray(fieldList, FieldId.class), mFactory);
    }
    
    public RowFormat getRearrangedVersion(List newOrder) {
        FieldId[] newFields = new FieldId[mFields.length];
        for (int i=0; i<newFields.length; i++) {
            int col = ((Integer)newOrder.get(i)).intValue();
            newFields[i] = mFields[col];
        }
        return new RowFormat(newFields, mFactory);
    }
    
    public RowFormat removeFields(Set pFieldInds) {
        int[] indsToRemove = ConversionUtils.integerCollectionToIntArray(pFieldInds);
        int minInd = MathUtils.minInt(indsToRemove);
        int maxInd = MathUtils.max(indsToRemove);
        if (minInd < 0 || maxInd >= mFields.length) {
            throw new RuntimeException("Cannot remove fields from format: index set is  invalid!");
        }                                
        List oldFieldsList = Arrays.asList(mFields);
        ArrayList newFieldsList = new ArrayList(mFields.length-pFieldInds.size());                            
        for (int i=0; i<oldFieldsList.size(); i++) { 
            if (!(pFieldInds.contains(new Integer(i)))) {
                newFieldsList.add(oldFieldsList.get(i));                
            }
        }
        return new RowFormat((FieldId[])ConversionUtils.collectionToArray(newFieldsList, FieldId.class), mFactory);
    }        
    

    public RowFormat createClone() {    
        /** the actual data; all other stuff is created on-demand */
        FieldId[] fieldsClone = new FieldId[mFields.length]; 
        for (int i=0; i<mFields.length; i++) {
            fieldsClone[i] = mFields[i].createClone();
        }            
        return new RowFormat(fieldsClone, mFactory);
    }        
                 
    private static class FieldIdToFieldNameConverter implements Converter {
        public Object convert(Object pObj) {
            FieldId fieldId = (FieldId)pObj;
            return fieldId.mData;
        }
            
   }
    
 }
