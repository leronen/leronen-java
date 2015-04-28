package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.collections.Distribution;
import util.collections.MultiMap;
import util.collections.Pair;
import util.converter.Converter;
import util.converter.IdentityConverter;
import util.converter.ObjectToStringConverter;

/** Todo: rename to ConvUtils */
public final class ConversionUtils  {                                       
          
    /**
     * Create list containing converted elements of the source collection, 
     * as converted by pConverter. 
     */
    public static <T1, T2> ArrayList<T2> convert(Collection<? extends T1> pCollection, Converter<T1,T2> pConverter) {
        ArrayList<T2> result = new ArrayList<T2>(pCollection.size());
        for (T1 o: pCollection) { 
            result.add(pConverter.convert(o));    
        }
        return result;                 
    }
    
    /**
     * Create list containing converted elements of the source collection, 
     * as converted by pConverter. 
     * 
     * @param pDiscardNulls if true, discard nulls (nulls produced by the converter; 
     * nulls still go in to the converter, in hopes of producing something else!)
     */
    public static <T1, T2> ArrayList<T2> convert(Collection<? extends T1> pCollection, 
                                                 Converter<T1,T2> pConverter, 
                                                 boolean pDiscardNulls) {
        ArrayList<T2> result = new ArrayList<T2>(pCollection.size());
        for (T1 o: pCollection) {
            T2 tmp = pConverter.convert(o);
            if (!pDiscardNulls || tmp != null) {
                result.add(pConverter.convert(o));
            }
        }
        return result;                 
    }

    
    public static List convert(Collection pCollection, Converter pConverter, Class pExceptionClassToSuppress) {
        return convert(pCollection, pConverter, Collections.singleton(pExceptionClassToSuppress));   
    }
    
    /**
     * Create list containing converted elements of the source collection, 
     * as converted by pConverter. 
     */
    public static List convert(Collection pCollection, Converter pConverter, Set pExceptionClassesToSuppress) {
        ArrayList result = new ArrayList(pCollection.size());
        Iterator i = pCollection.iterator();
        while (i.hasNext()) {
            try {
                result.add(pConverter.convert(i.next()));
            }
            catch (RuntimeException e) {
                Class exceptionClass = e.getClass();
                Class[] exceptionClasses = ReflectionUtils.getSuperClasses(exceptionClass);
                if (CollectionUtils.intersection(new HashSet(Arrays.asList(exceptionClasses)),
                                                 pExceptionClassesToSuppress).size()==0) {
                    // throw onwards
                    throw e;                                        
                }
                else {
                    // suppress with callers permission... 
                }
            }
        }
        return result;                 
    }
    
    /**
     * Create list containing converted elements of the source collection, 
     * as converted by pConverter. It is UNDER MOST SEVERE PENALTY assumed 
     * that pConverter produces Strings, instead of some nonsense.
     */
    public static String[] convertToStrings(Object[] pObjects, Converter pConverter) {
        return (String[])convert(pObjects, pConverter, String.class);                
    }
        
    
    /**
     * Create list containing converted elements of the source collection, 
     * as converted by pConverter. It is UNDER MOST SEVERE PENALTY assumed 
     * that pConverter produces Strings, instead of some nonsense.
     */
    public static String[] convertStrings(String[] pObjects, Converter pConverter) {
        return (String[])convert(pObjects, pConverter, String.class);                
    }
    
    
    /** Converts to strings using toString method of the objects. */    
    public static String[] convertToStrings(Object[] pObjects) {
        return (String[])convert(pObjects, new ObjectToStringConverter(), String.class);                
    }
    
    /** Converts to strings using toString method of the objects. */    
    public static String[] convertToStringArray(Collection pObjects) {
        List tmp = convertToStrings(pObjects);
        return stringCollectionToStringArray(tmp);                        
    }        
    
    public static Integer anyToInteger(Object pObj) {                
        if (pObj instanceof Integer) {
            return (Integer)pObj;             
        }
        else if (pObj instanceof String) {
            return new Integer((String)pObj);
        }
        else if (pObj instanceof Double) {
            double d = ((Double)pObj).doubleValue();                                    
            return new Integer((int)d);
        }
        else if (pObj instanceof Boolean) {
            boolean val = ((Boolean)pObj).booleanValue();
            if (val == true) {
                return new Integer(1);
            }
            else {
                return new Integer(0);
            }
        }
        else {
            throw new NumberFormatException("Cannot convert object to double: "+StringUtils.toMoreInformativeString(pObj));
        }            
    }
        
    /** Converts to strings using the toString() method */    
    public static List<String> convertToStrings(Collection pObjects) {
        return convert(pObjects, new ObjectToStringConverter());                
    }
    
    /** Converts to strings using the toString() method */    
    public static List convertToStrings(Collection pObjects, Converter pObjectToStringConverter) {
        return convert(pObjects, pObjectToStringConverter);                
    }
    
    public static String[] stringCollectionToStringArray(Collection pCol) {
        return (String[]) collectionToArray(pCol, String.class);            
    }

    public static File[] fileCollectionToFileArray(Collection pCol) {
        return (File[]) collectionToArray(pCol, File.class);            
    }    
    
    /**
     * Create list containing converted elements of the source collection, 
     * as kindly converted by pConverter. 
     */
    public static Object[] convert(Object[] pObjects, Converter pConverter, Class pRuntimeClassOfResultArray) {
        ArrayList result = new ArrayList(pObjects.length);
        for (int i=0; i<pObjects.length; i++) {        
            result.add(pConverter.convert(pObjects[i]));    
        }
        return collectionToArray(result, pRuntimeClassOfResultArray);
    }
    
    public static Object[] cast(Object[] pObjects, Class pRuntimeClassOfResultArray) {
        return convert(pObjects, new IdentityConverter(), pRuntimeClassOfResultArray);        
    }    
     
   /**
    * Convert values in a MultiMap; conversion done by the always-reliable pConverter      
    */
    public static MultiMap convert(MultiMap pMap, Converter pConverter) {
        MultiMap result = new MultiMap();
        Iterator keys = pMap.keySet().iterator();
        while (keys.hasNext()) {            
            Object key = keys.next();            
            Iterator values = pMap.get(key).iterator();
            while (values.hasNext()) {            
                Object value = values.next();    
                result.put(key, pConverter.convert(value));
            }
        }
        return result;                 
    }
    
   /**
    * Convert keys (into a new copy; pMap is not modified); hopefully 
    * converter is an injective mapping...!      
    * pResultMap may be null, in which case a LinkedHashMap is automatically created
    * to hold the result.
    */
    public static Map convertKeys(Map pMap, 
                                  Converter pConverter,
                                  Map pResultMap) {
        Map result;
    
        if (pResultMap != null) {
            result = pResultMap;
        }
        else {
            result = new LinkedHashMap(pMap.size());
        }
 
        Iterator keys = pMap.keySet().iterator();
        while (keys.hasNext()) {            
            Object key = keys.next();
            Object val = pMap.get(key);            
            Object convertedKey = pConverter.convert(key);                        
            result.put(convertedKey, val);
        }
        return result;                
    }
    
    public static Distribution convertValues(Distribution pDistribution, Converter pConverter) {
        Distribution result = new Distribution(); 
        Iterator values = pDistribution.iterator();
        while (values.hasNext()) {            
            Object original = values.next();            
            double weight = pDistribution.getWeight(original);
            Object converted = pConverter.convert(original);            
            result.add(converted, weight);
        }
        return result;
    }
        
    /**
     * Convert values of a map. Can also do this in-place, by giving pResultMap == pMap.       
     */
    public static <K,V1,V2> Map<K,V2> convertValues(Map<K,V1> pMap,
                                                    Converter<V1,V2> pConverter,
                                                    Map<K,V2> pResultMap) {
        Map result;
        
        if (pResultMap != null) {
            result = pResultMap;
        }
        else {
            result = new LinkedHashMap(pMap.size());
        }
        
        for (K key: pMap.keySet()) {
            V1 oldVal = pMap.get(key);
            V2 newVal = pConverter.convert(oldVal);
            result.put(key, newVal);
        }               
        return result;
    }
    
    /**
     * Convert values of a multimap using pConverter. @param pResultMap Results are 
     * stored here; if null, results are stored to a new MultiMap created with the default constructor.
     * keywords: convertvals.      
     */
    public static <K,V1,V2> MultiMap<K,V2> convertValues(MultiMap<K,V1> pMap,
                                                         Converter<V1,V2> pConverter,
                                                         MultiMap<K,V2> pResultMap) {
        MultiMap<K,V2> result;
        
        if (pResultMap != null) {
            result = pResultMap;
        }
        else {
            result = new MultiMap();
        }
        
        for (K key: pMap.keySet()) {                    
            for (V1 oldVal: pMap.get(key)) {
                V2 newVal = pConverter.convert(oldVal); 
                result.put(key, newVal);               
            }
        }
                
        return result;
    }
    
    
    /**
    * Convert values of a map inplace      
    */
    public static void convertVals_inplace(Map pMap, Converter pConverter) {        
        Iterator keys = pMap.keySet().iterator();
        while (keys.hasNext()) {            
            Object key = keys.next();
            Object val = pMap.get(key);            
            Object convertedVal = pConverter.convert(val);                        
            pMap.put(key, convertedVal);
        }                    
    }
    
   /**
    * Convert multi-map to map; pConverter must be a Set->Object converter.      
    */
    public static <K,V> Map<K,V> multiMapToMap(MultiMap<K,V> pMap, Converter<Set<V>, V> pConverter) {
        LinkedHashMap<K,V> result = new LinkedHashMap<K,V>();
        Iterator<K> keys = pMap.keySet().iterator();
        while (keys.hasNext()) {            
            K key = keys.next();
            Set<V> values = pMap.get(key);
            result.put(key, pConverter.convert(values));                                             
        }
        return result;                 
    }
         
    /** 
     *  Return an array whose runtime class is that of the most specific class that is a (not necessarily proper)
     *  superclass of the objects contained in pCol.
     *
     *  WARNING the implementation may be somewhat slow, as moderate kludging is involved.
     * 
     *  Note: would the correct place for this be in reflectionUtils, as mycket reflectioning is done. 
     */    
    public static Object[] collectionToArray(Collection pCol) {
        if (pCol.size()==0) {
            // no objects in the collection, cannot do anything but return an Object array with size 0
            return new Object[0];
        }
        else {
            Class[] allClasses = ReflectionUtils.getClassesOfContainedObjects(pCol);
            Class mostSpecificSuperClass = ReflectionUtils.getMostSpecificCommonSuperClass(allClasses);
            return collectionToArray(pCol, mostSpecificSuperClass);
        }                
    }                
    
        
    /** @param pClass the component class of the returned array */         
    public static Object[] collectionToArray(Collection pCol, Class pClass) {
        List list;
        if (pCol instanceof List) {
            list = (List)pCol;
        }
        else {
            list = new ArrayList(pCol);
        }                
        return list.toArray((Object[])java.lang.reflect.Array.newInstance(pClass, list.size()));        
    }
                     
    public static String[] stringCollectionToArray(Collection<String> pCol) {
        return (String[])collectionToArray(pCol, String.class);                
    }              
     
    public static int booleanToInt(boolean pBool) {
        if (pBool) return 1; else return 0;
    }
    
    public static BitSet boolArrToBitSet(boolean[] pArr) {
        int len = pArr.length;
        BitSet result = new BitSet(len);        
        for (int i=0; i<len; i++) {
            result.set(i, pArr[i]);    
        }
        return result;        
    }
              
    public static boolean[] bitSetToBoolArr(BitSet pSet, int pArrLen) {
        boolean[] arr = new boolean[pArrLen];                
        for (int i=0; i<pArrLen; i++) {
            arr[i] = pSet.get(i);    
        }
        return arr;        
    }
    
    public static List<Integer> bitSetToIntegerList(BitSet bs) {
        List<Integer> list = new ArrayList(bs.cardinality());
        for (int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
            list.add(i);
        }
        return list;
                
    }

    public static int[] toIntArr(boolean[] pBoolArr) {
        int len = pBoolArr.length;
        int[] intArr = new int[len];
        for (int i=0; i<len; i++) {
            if (pBoolArr[i]) intArr[i]=1; else intArr[i]=0;    
        }
        return intArr;    
    }
    
    /** Note that the idea is following:  {1,3,6} to  {0,1,0,1,0,0,1} */
    public static boolean[] indexArrToBoolArr(int[] pTrueIndices, int pLengthOfResult) {
        boolean[] result = new boolean[pLengthOfResult];
        int numTrue = pTrueIndices.length;
        for(int i=0; i<numTrue; i++) {
            result[pTrueIndices[i]] = true;
        }        
        return result;        
    }
    
    public static int[] doubleArrToIntArr(double[] pDoubleArr) {
        int len = pDoubleArr.length;
        int[] intArr = new int[len];
        for (int i=0; i<len; i++) {
            intArr[i] = (int)pDoubleArr[i];    
        }
        return intArr;
    }
    
    public static int asInt(double pDouble) {
    	if (pDouble % 1.d != 0.d) {
    		throw new RuntimeException("Trying to add object with a non-integer weight into a Multi-set!");    
    	}
    	else {
    		return (int)pDouble;
    	}
    }
    
    public static int[] integerCollectionToIntArray(Collection<Integer> pCol) {
        int len = pCol.size();
        int[] intArr = new int[len];
        Iterator<Integer> iter = pCol.iterator();
        for (int i=0; i<len; i++) {
            Integer integer = iter.next();
            intArr[i]=integer.intValue();    
        }
        return intArr;
    }
    
    /** keywords: integer collection to array */
    public static int[] toIntArray(Collection<Integer> pCol) {
        int len = pCol.size(); int[] arr = new int[len]; int i=0; 
        for (int val: pCol) arr[i++]=val;            
        return arr;
    }
    
    public static long[] longCollectionToLongArray(Collection pCol) {                
        int len = pCol.size();
        long[] longArr = new long[len];                
        Iterator iter = pCol.iterator();
        for (int i=0; i<len; i++) {
            Long theLong = (Long)iter.next();
            long thelong = theLong.longValue();
            longArr[i]=thelong;    
        }
        return longArr;        
    }    
    
    public static ArrayList listModelToArrayList(javax.swing.ListModel pListModel) {        
        int numElems = pListModel.getSize();
        ArrayList result = new ArrayList();
        for (int i=0; i<numElems; i++) {
            result.add(pListModel.getElementAt(i));
        }
        return result;                                        
    }
    
    public static File[] pathsToFiles(String[] pPaths) {
        File[] result = new File[pPaths.length];
        for (int i=0; i<pPaths.length; i++) {
            result[i] = new File(pPaths[i]);
        }        
        return result;
    }
    
    /**
     * Note the unorthodox use of big and small letters. This is needed
     * to disambiguate Double and double. 
     */
    public static double[] DoubleCollectionTodoubleArray(Collection pCol) {
        int len = pCol.size();
        double[] doubleArr = new double[len];
        Iterator iter = pCol.iterator();
        for (int i=0; i<len; i++) {
            Double dapl = (Double)iter.next();
            doubleArr[i]=dapl.doubleValue();    
        }
        return doubleArr;
    }
            
    
    /**
     * Note the unorthodox use of big and small letters. This is needed
     * to disambiguate Integer and Integer. 
     */
    public static int[] IntegerCollectionToIntArray(Collection pCol) {
        int len = pCol.size();
        int[] intArr = new int[len];
        Iterator iter = pCol.iterator();
        for (int i=0; i<len; i++) {
            Integer integer = (Integer)iter.next();
            intArr[i]=integer.intValue();    
        }
        return intArr;
    }
    
    public static char anyToChar(Object p) {
        if (p instanceof Character) {
            return ((Character)p).charValue();    
        }
        else if(p instanceof String) {
            String s = (String)p;
            if (s.length() != 1) {
                throw new RuntimeException("String length is not 1("+s.length()+")");
            }
            else {
                return s.charAt(0);
            }                                
        }
        else if (p instanceof Integer) {
            int val= ((Integer)p).intValue();
            return (char)('0'+val);            
        }
        else {
            throw new RuntimeException("Unconvertible object: "+p);     
        }
    }
    
    public static double anyToDouble(Object pObj) {
        if (pObj instanceof Double) {
            Double d = (Double)pObj;
            return d.doubleValue();
        }
        else if (pObj instanceof Number) {
            Number n = (Number)pObj; 
            return n.doubleValue();
        }
        else if (pObj instanceof String) {
            return Double.parseDouble((String)pObj);
        }
        else if (pObj instanceof Boolean) {
            boolean val = ((Boolean)pObj).booleanValue();
            if (val == true) {
                return 1.0;
            }
            else {
                return 0.0;
            }
        }
        else {
            throw new RuntimeException("Cannot convert object to double: "+StringUtils.toMoreInformativeString(pObj));
        }    
    }
    
    public static boolean isNumeric(Object pObj) {        
        if (pObj instanceof Number) {
        	return true;
        }
        else if (pObj instanceof String) {
        	return StringUtils.isNumeric((String)pObj);            
        }        
        else {
            return false;
        }    
    }
    
    
    public static int anyToInt(Object pObj) {
        if (pObj instanceof Integer) {
            Integer i = (Integer)pObj;
            return i.intValue();
        }
        else if (pObj instanceof Number) {
            Number n = (Number)pObj; 
            return n.intValue();
        }
        else if (pObj instanceof String) {
            return Integer.parseInt((String)pObj);
        }
        else if (pObj instanceof Boolean) {
            boolean val = ((Boolean)pObj).booleanValue();
            if (val == true) {
                return 1;
            }
            else {
                return 0;
            }
        }
        else {
            throw new RuntimeException("Cannot convert object to int: "+StringUtils.toMoreInformativeString(pObj));
        }    
    }
    
    public static boolean toBoolean(double pVal) {
        if (pVal == 0.0) {
            return false;
        }
        else if (pVal == 1.0) {
            return true;
        }
        else {
            throw new RuntimeException("Cannot convert to boolean: "+pVal);    
        }
    }
    
    public static boolean anyToBoolean(Object pObj) {       
        if (pObj instanceof Number) {
            Number n = (Number)pObj;
            double val = n.doubleValue();
            if (val == 1.0) {
                return true;
            }
            else if (val == 0.d) {
                return false;
            }
            else {
                throw new RuntimeException("Invalid val: "+val);
            }            
        }
        else if (pObj instanceof String) {
            if (pObj.equals("true")) {
                return true;
            }
            else if (pObj.equals("false")) {
                return false;
            }
            if (pObj.equals("1")) {
                return true;
            }
            else if (pObj.equals("0")) {
                return false;
            }
            else {
                throw new RuntimeException("Invalid val: <"+pObj+">");
            }
        }
        else if (pObj instanceof Boolean) {
            return ((Boolean)pObj).booleanValue();
        }
        else {
            throw new RuntimeException("Cannot convert object to boolean: "+StringUtils.toMoreInformativeString(pObj));
        }    
    }
           
    
    public static double[] stringArrToDoubleArr(String[] pStrings) {
        List doubleList = ConversionUtils.convert(Arrays.asList(pStrings),
                                                  new util.converter.StringToDoubleConverter());
        double[] doubleArr = DoubleCollectionTodoubleArray(doubleList);                                                     
        return doubleArr;
    }
    
    public static int[] stringArrToIntArr(String[] pStrings) {
        List integerList = ConversionUtils.convert(Arrays.asList(pStrings),
                                                   new util.converter.StringToIntegerConverter());
        int[] intArr = IntegerCollectionToIntArray(integerList);                                                     
        return intArr;
    }

    public static List<Integer> stringListToIntegerList(List<String> pStrings) {
        return ConversionUtils.convert(pStrings, new util.converter.StringToIntegerConverter());        
    }

    
    /**
     * @todo modify these list "wrappers" into proper wrappers implementing the list interface; 
     * that is, modifications to the list (setting values at indices) would modify the array as well!
     * Of course, modifications to the list size would not be possible, only setting new values
     *
     * Or, more bra still: make alternative versions, for example: "asModifiableList()"
     */
    public static List<Integer> asList(int[] pArr) {
        ArrayList<Integer> result = new ArrayList(pArr.length);                
        for (int i=0; i<pArr.length; i++) {
            result.add(new Integer(pArr[i]));                
        }
        return result;        
    }        
    
    public static List<Double> asList(double[] pArr) {
        ArrayList result = new ArrayList(pArr.length);                
        for (int i=0; i<pArr.length; i++) {
            result.add(new Double(pArr[i]));                
        }
        return result;        
    }
    
    public static List<Boolean> asList(boolean[] pArr) {
        ArrayList result = new ArrayList(pArr.length);                
        for (int i=0; i<pArr.length; i++) {
            result.add(new Boolean(pArr[i]));                
        }
        return result;        
    }    
    
    public static List<Character> asList(char[] pArr) {
        ArrayList result = new ArrayList(pArr.length);                
        for (int i=0; i<pArr.length; i++) {
            result.add(new Character(pArr[i]));                
        }
        return result;        
    }
    
    public static List<Character> asList(CharSequence pSeq) {
        int len = pSeq.length();
        ArrayList result = new ArrayList(len);                         
        for (int i=0; i<len; i++) {
            result.add(new Character(pSeq.charAt(i)));                
        }
        return result;        
    }
    
    public static List<Character> asList(String pString) {
        return new StringAsListAdapter(pString);   
    }
        
    public static Pair[] asPairArray(Collection pCol1, Collection pCol2) {
        if (pCol1.size() != pCol2.size()) {
            throw new RuntimeException("Collections have different size!");
        }
        int numObjects = pCol1.size();        
        Iterator iter1 = pCol1.iterator();
        Iterator iter2 = pCol2.iterator();
        Pair[] result = new Pair[numObjects];
        for (int i=0; i<numObjects; i++) {
            result[i] = new Pair(iter1.next(), iter2.next());                
        }                
        return result;
    }    
    
    public static String[] toStringArray(String pVal) {
        String[] result = new String[1];
        result[0] = pVal;
        return result;
    }
    
    public static String[] objectArrayToStringArray(Object[] pObjs) {
        List stringList = ConversionUtils.convert(Arrays.asList(pObjs), new ObjectToStringConverter());
        return (String[])stringList.toArray(new String[stringList.size()]);                    
    }
    
    public static List objectListToStringList(List pObjs) {
        return ConversionUtils.convert(pObjs, new ObjectToStringConverter());                            
    }


    /** Constructs a new arraylist */
    public static List<Integer> toList(int[] pArr) {
        ArrayList<Integer> result = new ArrayList<Integer>(pArr.length);
        for (int i: pArr) {
            result.add(i);
        }
        return result;
    }
    
    public static char[] characterCollectionToCharArray(Collection pCol) {
        int len = pCol.size();
        char[] charArr = new char[len];
        Iterator iter = pCol.iterator();
        for (int i=0; i<len; i++) {
            Character character = (Character)iter.next();
            charArr[i]=character.charValue();    
        }
        return charArr;
    }                     
}
