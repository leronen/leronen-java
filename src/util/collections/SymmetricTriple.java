package util.collections;

import java.util.*;


public class SymmetricTriple <T> extends AbstractList<T> {
    private T mObj1;
    private T mObj2;
    private T mObj3;        
    
    /** Elements shall be nulls */
    public SymmetricTriple() {
        // no-op
    }
    
    public SymmetricTriple(T pObj1, T pObj2, T pObj3) {
        mObj1 = pObj1;
        mObj2 = pObj2;
        mObj3 = pObj3;
    }
    
    public SymmetricTriple(List<T> pList) {
    	if (pList.size() != 3) {
    		throw new RuntimeException();
    	}
    	else {
    		mObj1 = pList.get(0);
    		mObj2 = pList.get(1);
    		mObj3 = pList.get(2);    		
    	}    		
    }
    
    public T getObj1() {
        return mObj1;
    }
    
    public T getObj2() {
        return mObj2;
    }
    
    public T getObj3() {
        return mObj3;
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
        else if (pInd==2) {
            return mObj3;
        }
        else {
            throw new IndexOutOfBoundsException();
        }            
    }
    
    public int size() {
        return 3;        
    }
    
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
        else if (pInd==2) {
            old = mObj3;
            mObj3 = pObj;
        }
        else {
            throw new IndexOutOfBoundsException();
        }
        return old;
    }                        
    
    ////////////////////////////////////////////////////////////
    // Common Object methods
    ////////////////////////////////////////////////////////////
    public String toString() {
        return "("+mObj1+","+mObj2+","+mObj3+")";    
    }

}
