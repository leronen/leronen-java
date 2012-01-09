package util.collections;

import java.util.*;
import java.io.*;

public class Triple<T1, T2, T3> extends AbstractList implements Serializable {
    private T1 mObj1;
    private T2 mObj2;
    private T3 mObj3;
        
    // uh...
    // TODO!
    static final long serialVersionUID = 45792157952989L;
    
    public Triple(T1 pObj1, T2 pObj2, T3 pObj3) {
        mObj1 = pObj1;
        mObj2 = pObj2;
        mObj3 = pObj3;
    }
          
    public T1 getObj1() {
        return mObj1;
    }
    
    public T2 getObj2() {
        return mObj2;
    }
    
    public T3 getObj3() {
        return mObj3;
    }    
    
    ////////////////////////////////////////////////////////////
    // implementation for interface List
    ////////////////////////////////////////////////////////////    
    public Object get(int pInd) {
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
    
    public Object set(int pInd, Object pObj) {
        Object old; 
        if (pInd==0) {
            old = mObj1;
            mObj1 = (T1)pObj;
        }
        else if (pInd==1) {
            old = mObj2;
            mObj2 = (T2)pObj;
        }
        else if (pInd==2) {
            old = mObj3;
            mObj3 = (T3)pObj;
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
