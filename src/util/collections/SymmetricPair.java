package util.collections;

import java.text.ParseException;
import java.util.*;
import util.CollectionUtils;

/**
 * A pair with 2 elements of the same type. Do not confuse with UnorderedPair,
 * where the order of the elements does not matter! 
 */
public class SymmetricPair<T> extends AbstractList<T> {
    protected T mObj1;
    protected T mObj2;
                
    // uh...
    static final long serialVersionUID = 4579409562157952989L;
    
    public SymmetricPair(T pObj1, T pObj2) {
        mObj1 = pObj1;
        mObj2 = pObj2;    
    }
    
    public SymmetricPair(Collection<T> pAlleles) {    	
		this(CollectionUtils.makeArrayList(pAlleles.iterator()));
    }
			    
    public SymmetricPair(List<T> pList) {
    	if (pList.size() != 2) {
    		throw new RuntimeException("List size == "+pList.size() + " != 2!!!! Data: "+pList);
    	}
    	else {
    		mObj1 = pList.get(0);
    		mObj2 = pList.get(1);    		
    	}    		
    }
    
    public T getObj1() {
        return mObj1;
    }
    
    public T  getObj2() {
        return mObj2;
    }          
    
    ////////////////////////////////////////////////////////////
    // implementation for interface List
    ////////////////////////////////////////////////////////////    
    public T get(int pInd) {
        if (pInd==0) {
            return mObj1;
        }
        else if (pInd==1) {
            return mObj2;
        }
        else {
            throw new IndexOutOfBoundsException();
        }            
    }
    
    public int size() {
        return 2;        
    }
    
    /** Todo: define semantics for null case */
    public boolean hasSameObjects(SymmetricPair<T> pOther) {    	
    	return new HashSet(this).equals(new HashSet(pOther));
    }
    
    public SymmetricPair<T> reverseVersion() {
        return new SymmetricPair<T>(mObj2, mObj1);
    }    
    
    /** Inherent type-insafety in this */
    public T set(int pInd, T pObj) {
        T old; 
        if (pInd==0) {
            old = mObj1;
            mObj1 = pObj;
        }
        else if (pInd==1) {
            old = mObj2;
            mObj2 = pObj;
        }
        else {
            throw new IndexOutOfBoundsException();
        }
        return old;
    }                
    
    public void setObj1(T pObj) {
        mObj1 = pObj;
    }
    
    public void setObj2(T pObj) {
        mObj2 = pObj;
    }
             
       
    ////////////////////////////////////////////////////////////
    // Common Object methods
    ////////////////////////////////////////////////////////////
    public String toString() {
        return "("+mObj1+","+mObj2+")";    
    }

    public static class Parser implements util.converter.Converter<String,SymmetricPair<String>> {
        
        public SymmetricPair<String> convert(String p) {
            String[] tok = p.split("\\s+");
            if (tok.length < 2) {
                throw new RuntimeException(new ParseException("Parse Error", 0));
            }
            else {
                return new SymmetricPair<String>(tok[0], tok[1]);
            }
                                         
        }
    }
    
    public static class SPConverter<T1,T2> implements util.converter.Converter<SymmetricPair<T1>,SymmetricPair<T2>> {
        
        private util.converter.Converter<T1,T2> mBaseConverter;
        
        public SPConverter(util.converter.Converter<T1,T2> pBaseConverter) {
            mBaseConverter = pBaseConverter; 
        }
        
        public SymmetricPair<T2> convert(SymmetricPair<T1> p) {
            return new SymmetricPair<T2>(mBaseConverter.convert(p.mObj1),  
                                         mBaseConverter.convert(p.mObj2));
        }        
        
    }
}
