package util;

import util.condition.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

public final class ReflectionUtils {

	/** 
    * @return a list containing all objects obj in pCollection that for which 
    * expression (obj instaceof pClass) returns true.
    */
    public static <T> List<T> extractObjectsOfClass(Class<T> pClass, Collection<?> pCollection) {
        ArrayList<T> result = new ArrayList<>();
        Iterator<?> i = pCollection.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (pClass.isInstance(o)) {
                result.add((T)o);
            }
        }
        return result;
    }
                                             
    /** return the set of all classses in the collection */
    public static Class[] getClassesOfContainedObjects(Collection<Object> pCollection) {
        Set classes = new HashSet();
        Iterator i = pCollection.iterator();
        while (i.hasNext()) {
            classes.add(i.next().getClass());   
        }
        return (Class[])(new ArrayList(classes)).toArray(new Class[classes.size()]);        
    }
    
    public static Class getMostSpecificCommonSuperClass(Collection pCollection) {
        Class[] classes = getClassesOfContainedObjects(pCollection);
        return getMostSpecificCommonSuperClass(classes);
    }
    
    
    public static Class getMostSpecificCommonSuperClass(Class[] pClasses) {
        // form all candidates
        HashSet candidates = new HashSet();
        for (int i=0; i<pClasses.length; i++) {
            Class[] superClasses = getSuperClasses(pClasses[i]);
            candidates.addAll(Arrays.asList(superClasses));        
        }
        // check for each candidate, if it is a superclass for all the classes
        HashSet commonSuperClasses = new HashSet();
        Iterator i = candidates.iterator();        
        while (i.hasNext()) {
            Class superClassCandidate = (Class)i.next();
            if (isSuperClassForAll(superClassCandidate, pClasses)) {
                commonSuperClasses.add(superClassCandidate);
            }
        }         
        // we now have classes that all objects in pClasses are instances of
        return getMostSpecificClass((Class[])(new ArrayList(commonSuperClasses)).toArray(new Class[commonSuperClasses.size()]));                                                                 
    }
    
    public static Object createInstance(String pClassName) throws Exception {
        return Class.forName(pClassName).newInstance();    
    }
    
    
   /**
    * Get the most specific class from the set of classes given as parameter.
    * The method assumes that the classes are fully ordered by the inheritance relation,
    * So pClasses only contains a single "line of inheritance".
    */
    public static Class getMostSpecificClass(Class[] pClasses) {                        
        for (int i=0; i<pClasses.length; i++) {
            if (isSubClassForAll(pClasses[i], pClasses)) {
                return pClasses[i];
            }
        }
        if (pClasses.length==0) {
            throw new RuntimeException("Cannot determine most specific class: array is empty!");    
        }
        else {
            throw new RuntimeException("Cannot determine most specific class; the classes do not form a single line of inheritance!\n"+
                                       "list of classes: \n"+StringUtils.arrayToString(pClasses, "\n"));
        }        
    }
        
    public static <T> Class<? super T>[] getSuperClasses(Class<T> pClass) {
        Class<? super T> currentClass = pClass;
        ArrayList<Class<? super T>> allClasses = new ArrayList<>();
        while(currentClass !=null) {
            allClasses.add(currentClass);
            currentClass=currentClass.getSuperclass();
        }
        return (Class[])allClasses.toArray(new Class[allClasses.size()]);        
    }        
                    
    /** here "superclass" includes also the class itself */                    
    public static boolean isSuperClassForAll(Class pSuperClassCandidate, Class[] pSubClassCandidates) {
        IsSubClassCondition condition = new IsSubClassCondition(pSuperClassCandidate, false);                        
        return CollectionUtils.doAllObjectsMatch(Arrays.asList(pSubClassCandidates), condition);
    }   

    public static boolean isSubClassForAll(Class pSubClassCandidate, Class[] pSuperClassCandidates) {
        IsSuperClassCondition condition = new IsSuperClassCondition(pSubClassCandidate, false);                        
        return CollectionUtils.doAllObjectsMatch(Arrays.asList(pSuperClassCandidates), condition);
    }           
        
    /** condition that checks for its argument if the argument is a superclass of the 
     * class specified by the condition 
     */
    public static class IsSubClassCondition implements Condition {        
        private Class mSuperClass;
        private boolean mProper;
        public IsSubClassCondition(Class pSuperClass, boolean pProper) {
            mSuperClass = pSuperClass;                               
            mProper = pProper;
        }
        public boolean fulfills(Object pObj) {
            Class subClassCandidate = (Class)pObj;
            if (mSuperClass.isAssignableFrom(subClassCandidate)) {                          
                if (mProper==true) {
                    // we insist that the superclass relation is proper
                    return !subClassCandidate.equals(mSuperClass);
                }
                else {
                    // we interpret that a class is superclass of itself 
                    return true;
                }
            }
            else {
                // instances of subClassCandidate cannot be assigned to instances mSuperClass 
                return false;    
            }
        }                 
    }
    
    /** condition that checks for its argument if the argument is a superclass of the 
     * class specified by the condition. 
     */
    public static class IsSuperClassCondition implements Condition {
        private Class mSubClass;
        private boolean mProper;
        public IsSuperClassCondition(Class pSubClass, boolean pProper) {
            mSubClass = pSubClass;
            mProper = pProper;                                       
        }
        public boolean fulfills(Object pObj) {
            Class superClassCandidate = (Class)pObj;
            if (superClassCandidate.isAssignableFrom(mSubClass)) {
                if (mProper==true) {
                    // we insist that the superclass relation is proper
                    return !superClassCandidate.equals(mSubClass);
                }
                else {
                    // we interpret that a class is superclass of itself 
                    return true;
                }
            }
            else {
                // instances of mSubClass cannot be assigned to instances of superClassCandidate
                return false;    
            }
        }
    }
    
    public static Set<String> getPublicStaticStringFields(Class pClass) {
        return getPublicStaticStringFieldsWithPrefix(pClass, null);
    }
    
    //** if pPrefix is null or "", just get all string fields. Return in alphabetical order */ 
    public static Set<String> getPublicStaticStringFieldsWithPrefix(Class pClass, String pPrefix) {
    	Set<String> result = new TreeSet<String>();
    	Field[] fields = pClass.getFields();
    	// Logger.info("There are "+fields.length+" fields in class "+pClass);
    	for (Field f:fields) {    		
    		String fieldName = f.getName();
    		// Logger.info("fieldName: "+f);
    		if (pPrefix == null || pPrefix.equals("") || fieldName.matches(pPrefix+".*")) {
    			try {
	    			Object val = f.get(null);
	    			if (val != null && val instanceof String) {
	    				result.add((String)val);
	    			}
    			}
    			catch (IllegalAccessException e) {
    				// not a public field, we guess
    			}
    		}    		
    	}    	
    	return result;
    }
    
    public static Object getPublicStaticField(Class pClass, String pName) {
    	try {    				
    		Field[] fields = pClass.getFields();
        	for (Field f:fields) {    		
        		if (f.getName().equals(pName)) {
        			return f.get(null);
        		}
        	}
        	throw new RuntimeException("No such field in class "+pClass+": "+pName);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
    	
    	        		    				    	
    }
    	
    /** Return fieldname->value mapping */ 
    public static Map<String, ? extends Object> getPublicStaticFieldsWithPrefix(Class pClass, 
    																  String pPrefix,    																 boolean pIncludePrefix) {
    	Map<String, Object> result = new LinkedHashMap<String, Object>();
    	Field[] fields = pClass.getFields();
    	// Logger.info("There are "+fields.length+" fields in class "+pClass);
    	for (Field f:fields) {    		
    		String fieldName = f.getName();
    		// Logger.info("fieldName: "+f);
    		Matcher m = Pattern.compile("^"+pPrefix+"(.*)$").matcher(fieldName);
    		if (m.matches()) {
    			try {    				
    				String key = pIncludePrefix ? fieldName : m.group(1);    			
	    			Object val = f.get(null);
	    			if (val != null) {
	    				result.put(key, val);
	    			}
    			}
    			catch (IllegalAccessException e) {
    				// not a public field, we guess
    			}
    		}    		
    	}    	
    	return result;
    }        
       
       
    
    
    
}
