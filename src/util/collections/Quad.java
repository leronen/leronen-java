package util.collections;

import java.io.Serializable;
import java.util.AbstractList;

/**
 * Hmm, how far can one take this...some terminology for interested implementors:
 * Single (1) (also: singleton, sole, only, etc.)
 * Double (2) (also: pair, twice)
 * Triple (3) (also: triplet, treble, thrice, threesome, troika, trio)
 * Quadruple (4)
 * Quintuple or Pentuple (5)
 * Sextuple or Hextuple (6)
 * Septuple (7)
 * Octuple (8)
 * Nonuple (9)
 * Decuple (10)
 * Hendecuple or Undecuple (11)
 * Duodecuple (12)
 * Centuple (100)
 **/
public class Quad<T1, T2, T3, T4> extends AbstractList implements Serializable {
        
    private T1 mObj1;
    private T2 mObj2;
    private T3 mObj3;
    private T4 mObj4;
        
    // uh...
    // TODO!
    static final long serialVersionUID = 4579214589L;
    
    public Quad(T1 pObj1, T2 pObj2, T3 pObj3, T4 pObj4) {
        mObj1 = pObj1;
        mObj2 = pObj2;
        mObj3 = pObj3;
        mObj4 = pObj4;
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
    
    public T4 getObj4() {
        return mObj4;
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
        else if (pInd==3) {
            old = mObj4;
            mObj4 = (T4)pObj;
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
