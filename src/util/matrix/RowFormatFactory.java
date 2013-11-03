package util.matrix;

import util.*; 
import util.collections.iterator.PushBackIterator;
import util.converter.*;

import java.util.*;
import java.util.regex.*;


/**
 * As initializing RowFormat objects seems to be a bit difficult, we delegate the task to this separate factory, to 
 * avoid battling with different constructors and static factory methods.
 */
public final class RowFormatFactory {
    
    private String mName;
    
    public static final int DEFAULT_FLOAT_OUTPUT_PRECISION = 6;
    
    private int mFloatPrecision = DEFAULT_FLOAT_OUTPUT_PRECISION; 
    
    /** 
     * This is for multi part fields; each key is a part of a multi-part field(Strings), and the values are names of multi-
     * part fields(Strings).
     */
    private HashMap mFieldNameByAtomicFieldId = new HashMap();
    
    private Map mFieldNameByFieldCharacter = new HashMap();        
    
    /** Contains Strings */
    private HashSet mMultiPartFieldNames = new HashSet();

    /** 
     * Values: Converter instances that convert string to some rep object (which may also be String) 
     * Keys:   Field names(Strings)
     */
    private HashMap mRepFactoryByFieldName = new HashMap();
    
    /** 
     * Values: Converter instances that convert rep objects to string presentations 
     * Keys:   Field names(Strings)
     */
    private HashMap mRepFormatterByFieldName = new HashMap();
    

    /** Sad concept indeed: */
    private RowFactoryFactory mRowFactoryFactory;                              
                    
    private List mDefaultMultiPartFieldComponents = new ArrayList();                        
            
    private Set mNumericFieldNames = new LinkedHashSet();

    private Set mRegisteredFieldNames = new LinkedHashSet();
    
    private String columnSeparatorRegex;
    
	public static RowFormatFactory DEFAULT_FACTORY;
	public static RowFormatFactory DEFAULT_TABBED_FACTORY;
	
	static {
		DEFAULT_FACTORY = new RowFormatFactory("Default rowformat factory");
		DEFAULT_TABBED_FACTORY = new RowFormatFactory("Default rowformat factory, tabbed version");
		DEFAULT_TABBED_FACTORY.setSeparator("\t");
	}
            
    /** 
     * Creates a default factory; it can then be configured by calls to following methods 
     *  - registerMultiPartField
     *  - registerRepFactory
     *  - setFieldNameByFieldCharacterMap
     *  - setSeparator
     */
    public RowFormatFactory(String pName) {
        mName = pName;
        columnSeparatorRegex = "\\s+";
    }            
    
    public void setSeparator(String separatorRegex) {
    	this.columnSeparatorRegex = separatorRegex;
    }
    
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return mName;
    }
    
    /*
    public FieldId makeFieldId(String pFieldName) {
                        
    }
            
    */
    
    /** 
     * Register the names of the component parts of a multi-part field:
     * pFieldName will be a multi-part field, and pAtomicPartNames will be parts of that multi-part field.          
     *
     * Example: registerMultiPartFieldy("Haplotype", "M!", "M2", "M3"))
     */
    public void registerMultiPartField(String pFieldName, List pAtomicPartNames) {
        mMultiPartFieldNames.add(pFieldName);
        mRegisteredFieldNames.addAll(pAtomicPartNames);
        Iterator i=pAtomicPartNames.iterator();
        while(i.hasNext()) {
            String atomic = (String)i.next();
            mFieldNameByAtomicFieldId.put(atomic, pFieldName);
        }
    }
    
    public boolean isNumericField(String pFieldName) {
        return mNumericFieldNames.contains(pFieldName);
    }
    
    public Set getNumericFieldNames() {
        return mNumericFieldNames;
    }        
    
    public Set getRegisteredFieldNames() {
        return mRegisteredFieldNames;
    }
    
    public void registerNumericFieldNames(List pNames) {
        mNumericFieldNames.addAll(pNames);            
    }
    
    public void registerFieldNames(List pNames) {
        mRegisteredFieldNames.addAll(pNames);
        // dbgMsg("Registered official field names: "+mRegisteredFieldNames);            
    }
    
    
    public void registerDefaultMultiPartFieldComponents(List pComponents) {
        mDefaultMultiPartFieldComponents = pComponents;
    }        

    public List getDefaultMultiPartFieldComponents() {
        return mDefaultMultiPartFieldComponents;
    }        
    
    public void registerRepFactory(String pFieldName, Converter pFactory) {
        mRepFactoryByFieldName.put(pFieldName, pFactory);                            
    }
    
    public void registerRepFormatter(String pFieldName, Converter pFormatter) {
        mRepFormatterByFieldName.put(pFieldName, pFormatter);                            
    }
    
    /** uh... */
    public void registerRowFactoryFactory(RowFactoryFactory pRowFactoryFactory) {
        mRowFactoryFactory = pRowFactoryFactory;
    }
    
    /** Quite ridiculous, indeed: */
    public RowFactory makeRowFactory(RowFormat pFormat) {
    	RowFactory result = null;
        if (mRowFactoryFactory == null) {
        	result = new DefaultRowFactory(pFormat);
        }
        else {            
        	result = mRowFactoryFactory.makeRowFactory(pFormat);
        }            
        
        result.setSeparator(columnSeparatorRegex);
        
        return result;
    }
    
    public void setFieldNameByFieldCharacterMap(Map pMap) {
        mFieldNameByFieldCharacter = pMap;
    }
    
    boolean isBasicField(String pFieldName) {
        return !(mMultiPartFieldNames.contains(pFieldName));    
    }
    
    boolean isMultiPartField(String pFieldName) {
        return mMultiPartFieldNames.contains(pFieldName);    
    }
    
    public void setFloatOutputPrecision(int pVal) {
        mFloatPrecision = pVal;
    }
    
    private MultiPartFieldId parseMultiPartFieldId(PushBackIterator pIter) {
        String atomicField = (String)pIter.next();
        String multiPartFieldName = (String)mFieldNameByAtomicFieldId.get(atomicField);
        ArrayList atomicFields = new ArrayList();
        atomicFields.add(atomicField);
        boolean done = false;
        while (pIter.hasNext() && !done) {
            String candidate = (String)pIter.next();
            String fieldName = (String)mFieldNameByAtomicFieldId.get(candidate);
            if (fieldName != null && fieldName.equals(multiPartFieldName)) {             
                atomicFields.add(candidate);
            }
            else {
                // the next field does not belong to the same multi-part field
                pIter.pushBack();
                done = true;
            }
        }
        return new MultiPartFieldId(multiPartFieldName, atomicFields);
    }                                                                                                
    
    public RowFormat makeEmpty() {
        // dbgMsg("makeEmpty");
        return makeFromFieldNameList(Collections.EMPTY_LIST);
    }
    
    public RowFormat makeSimple(String pFieldName) {
        // dbgMsg("makeSimple: "+pFieldName);
        return makeFromFieldNameList(Collections.singletonList(pFieldName));
    }

    public RowFormat makeSimple(FieldId pFieldId) {
        // dbgMsg("makeSimple: "+pFieldId);
        FieldId[] fields = new FieldId[1];
        fields[0] = pFieldId;
        return new RowFormat(fields, this);        
    }                                            
    
    public RowFormat makeFromFormatString(String pFormatString) {
        // dbgMsg("makeFromFormatString: "+pFormatString);
        char[] fieldChars = pFormatString.toCharArray();
        List charList = ConversionUtils.asList(fieldChars);
        MapConverter charToFieldNameConverter = new MapConverter(mFieldNameByFieldCharacter, MapConverter.NotFoundBehauvior.RETURN_NULL);
        List fieldNames = ConversionUtils.convert(charList, charToFieldNameConverter);
        return makeFromFieldNameList(fieldNames);
    }    
    
    public RowFormat makeFromHeaderString(String pHeaderString) {
        // dbgMsg("makeFromHeaderString: "+pHeaderString);
        // dbgMsg("deducing format from header string: "+pHeaderString);            
        Pattern delim = Pattern.compile(columnSeparatorRegex);
        String[] tokens = delim.split(pHeaderString);
        // dbgMsg("Header string has "+tokens.length+" tokens.");
        List tokenList = Arrays.asList(tokens);
        return makeFromFieldNameList(tokenList);
    }
    
    public RowFormat makeFromFieldNameCollection(Collection pFields) {        
        return makeFromFieldNameList(new ArrayList(pFields));    
    }
    
    public Object makeFieldRep(String pFieldName, String pObjectString) {
        Converter repFactory = (Converter)mRepFactoryByFieldName.get(pFieldName);
        if (repFactory != null) {
            // there is a factory object registered for the field -> rep is made by the factory object
            return repFactory.convert(pObjectString);
        }
        else {
            // no factory registered, leave the field be
            return pObjectString;                                
        }        
    }
    
    public String formatField(FieldId pFieldId, Object pObject) {
        Converter fieldFormatter = (Converter)mRepFormatterByFieldName.get(pFieldId.getName());
        if (fieldFormatter == null) {
            if (pObject == null) {
                return null;
            }
            else {
                return internalFormatBasicField(pObject);
            }
            
            /*
            else if (pObject instanceof String) {
                return (String)pObject;
            }
            else {
                return pObject.toString();
            }
            */
        }
        else {
            if (fieldFormatter instanceof ContextAwareFormatter) {
                ((ContextAwareFormatter)fieldFormatter).setFormatterContext(pFieldId);
            }
            return (String)fieldFormatter.convert(pObject);    
        }       
    }
    
    private String internalFormatBasicField(Object pObject) {
        if (pObject instanceof String) {
            return StringUtils.beautifyString_professional((String)pObject, mFloatPrecision);
        }
        else {
            return StringUtils.beautifyString_professional(pObject.toString(), mFloatPrecision);
        }        
    }
        
    
    public String formatField(String pFieldName, Object pObject) {
        Converter fieldFormatter = (Converter)mRepFormatterByFieldName.get(pFieldName);
        if (fieldFormatter == null) {
            if (pObject == null) {
                return null;
            }
            else {
                return internalFormatBasicField(pObject);
            }
            /*
            else if (pObject instanceof String) {
                return StringUtils.beautifyString_professional((String)pObject, 6);
            }
            else {
                return StringUtils.beautifyString_professional(pObject.toString(), 6);
            }
            */
        }
        else {
            return (String)fieldFormatter.convert(pObject);    
        }
    }
    
    public RowFormat makeFromFieldNameArray(String[] pFields) {
        return makeFromFieldNameList(Arrays.asList(pFields));    
    }
    
    public RowFormat makeFromFieldNameList(List pFields) {
        // dbgMsg("makeFromFieldNameList: "+StringUtils.collectionToString(pFields, ", "));
        ArrayList fieldList = new ArrayList();                                    
        PushBackIterator tokenIter = new PushBackIterator(pFields.iterator());                                 
                
        while (tokenIter.hasNext()) {
            String tok = (String)tokenIter.next();
            if (mMultiPartFieldNames.contains(tok)) {
                // a multi part field without definitions for the invidual component field names
                fieldList.add(new MultiPartFieldId(tok));
            }
            else if (mFieldNameByAtomicFieldId.containsKey(tok)) {
                // a atomic component field of a multi part field
                tokenIter.pushBack();
                fieldList.add(parseMultiPartFieldId(tokenIter));                    
            }            
            else {
                // hopefully a basic field
                fieldList.add(new BasicFieldId(tok));                
            }
        }
                
        RowFormat format = new RowFormat((FieldId[])ConversionUtils.collectionToArray(fieldList, FieldId.class), this);
        return format;                        
                                             
    }
    
    
                      
}
