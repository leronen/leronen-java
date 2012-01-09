package util.collections;

import java.util.*;
import java.io.*;

public class SymmetricQuad<T> extends AbstractList<T> implements Serializable {
    private T mObj1;
    private T mObj2;
    private T mObj3;
    private T mObj4;
        
    // uh...
    // TODO!
    static final long serialVersionUID = 457934546249L;
    
    public SymmetricQuad(T pObj1, T pObj2, T pObj3, T pObj4) {
        mObj1 = pObj1;
        mObj2 = pObj2;
        mObj3 = pObj3;
        mObj4 = pObj4;
    }
    
    public SymmetricQuad(List<T> pList) {
    	if (pList.size() != 3) {
    		throw new RuntimeException();
    	}
    	else {
    		mObj1 = pList.get(0);
    		mObj2 = pList.get(1);
    		mObj3 = pList.get(2);
            mObj4 = pList.get(3);
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
    
    public T getObj4() {
        return mObj4;
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
        else if (pInd==3) {
            return mObj4;
        }
        else {
            throw new IndexOutOfBoundsException();
        }            
    }
    
    public int size() {
        return 4;        
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
        else if (pInd==3) {
            old = mObj4;
            mObj4 = pObj;
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
        return "("+mObj1+","+mObj2+","+mObj3+","+mObj4+")";    
    }

}
