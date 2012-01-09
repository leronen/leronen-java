package util.collections;

import java.util.*;
import java.io.*;

public class Pair<T1, T2> extends AbstractList implements IPair<T1, T2>, Serializable {
    protected T1 mObj1;
    protected T2 mObj2;
        
    // uh...
    static final long serialVersionUID = 4579409562157952989L;
    
    /** Create a (null, null)-pair */
    public Pair() {
        // no action
    }
    
    public Pair(T1 pObj1, T2 pObj2) {
        mObj1 = pObj1;
        mObj2 = pObj2;    
    }
    
    public T1 getObj1() {
        return mObj1;
    }
    
    public T2  getObj2() {
        return mObj2;
    }
    
    public void setObj1(T1 p) {
        mObj1 = p;
    }
    
    public void setObj2(T2 p) {
        mObj2 = p;
    }
    
    /** Returns a reversed version (does not swap elements inplace */
    public Pair<T2, T1> reverse() {
        return new Pair<T2,T1>(mObj2, mObj1);
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
        else {
            throw new IndexOutOfBoundsException();
        }            
    }
    
    public int size() {
        return 2;        
    }
    
    /** Inherent type-insafety in this */
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
        else {
            throw new IndexOutOfBoundsException();
        }
        return old;
    }                
             

    /** Provided for debug purposes only, of course */
    public String toString() {
        return "("+mObj1+","+mObj2+")";    
    }

}
