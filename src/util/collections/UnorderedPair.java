package util.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import util.StringUtils;
import util.converter.Converter;

/**
 * A pair with 2 elements of the same type; order does not matter,
 * so (x,y) equals (y,x)
 *
 * Actually, leaves a large fraction of Set methods unimplemented...
 * 
 * Hmm, to respect Set semantics, it should be asserted that
 * mObj1 != mObj2; is this indeed the case everywhere!?!?!?
 * Maybe in those cases we should forget about the fact that 
 * we implement Set...
 *  
 */
public class UnorderedPair<T> implements Set<T> {
 
    protected T mObj1;
    protected T mObj2;                
      
    public UnorderedPair(T pObj1, T pObj2) {
        mObj1 = pObj1;
        mObj2 = pObj2;    
    }    
			    
    public UnorderedPair(SymmetricPair<T> pPair) {    	
    	mObj1 = pPair.mObj1;
    	mObj2 = pPair.mObj2;    		    	    	
    }
    
    public T getObj1() {
        return mObj1;
    }
    
    public T  getObj2() {
        return mObj2;
    }                     
            
    public T getOtherObj(T pObj) {
        if (pObj.equals(mObj1)) {
            return mObj2;
        }
        else if (pObj.equals(mObj2)) {
            return mObj1;
        }
        else {
            throw new RuntimeException("No such element in pair: "+pObj);
        }
   
    }
    
    /** If objects are not comparable, use string rep for comparison */
    public UnorderedPair<T> canonicalVersion() {
        if (mObj1 instanceof Comparable) {
            if (((Comparable) mObj1).compareTo(mObj2) < 0) {
                return this;
            }
            else {
                return new UnorderedPair<T>(mObj2, mObj1);
            }
        }
        else {
            if (mObj1.toString().compareTo(mObj2.toString()) < 0) {
                return this;
            }
            else {
                return new UnorderedPair<T>(mObj2, mObj1);
            }
                
        }
    }
    
    /**
     * Try to obey the common equals contract in {@link #Set}.
     */
    public boolean equals(Object p) {
        
        if (p == this) {
            return true;
        }
        
        if (p instanceof UnorderedPair) {
            // one of our kind; what pleasant occurence...
            UnorderedPair<T> other = (UnorderedPair<T>)p;
            return (mObj1.equals(other.mObj1) && mObj2.equals(other.mObj2)) 
                || (mObj1.equals(other.mObj2) && mObj2.equals(other.mObj1));
        }
        
        // OK, we are off familiar waters, imitate impl from class AbstractSet 
        if (!(p instanceof Set)) {
            return false;
        }
        
        // a Set it is               
        Set<T> otherSet = (Set<T>)p;
        
        if (otherSet.size() != size()) {
            return false;
        }
        
       // a Set of same size, even.
       return containsAll(otherSet);
                
        
        // old impl that was not consistent with Set requirements:
//        UnorderedPair<T> other = (UnorderedPair<T>)p;
//        return (mObj1.equals(other.mObj1) && mObj2.equals(other.mObj2)) 
//            || (mObj1.equals(other.mObj2) && mObj2.equals(other.mObj1));       
    }
    
    /**
     * Try to obey the common equals contract in {@link #Set}.     
     */
    public int hashCode() {
        return (mObj1 != null ? mObj1.hashCode() : 0)
             + (mObj2 != null ? mObj2.hashCode() : 0);
                
               
//        return HashUtils.hash(HashUtils.SEED, mObj1.hashCode()+mObj2.hashCode());                
    }
    
    ////////////////////////////////////////////////////////////
    // Common Object methods
    ////////////////////////////////////////////////////////////
    public String toString() {
        return "("+mObj1+","+mObj2+")";    
    }
    
    public static void main(String[] args) throws Exception {
        Set<UnorderedPair> set = new HashSet<UnorderedPair>();
        for (int i=0; i<4; i++) {
            for (int j=0; j<4; j++) {
                set.add(new UnorderedPair<Integer>(i,j));
            }
        }
        
        System.out.println(StringUtils.collectionToString(set));
        
    }
    
    public boolean contains(Object p) {
        return mObj1.equals(p) || mObj2.equals(p);
    }
    
    public Iterator<T> iterator() {
        return new UOPIterator();
    }
    
    public boolean add(T p) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }
    
    private class UOPIterator implements Iterator<T>{
        int ind = 0;
        
        public T next() {
            if (ind == 0) {
                ind = 1;
                return mObj1;
            }
            else if (ind == 1) {
                ind = 2;
                return mObj2;
            }
            else {
                throw new NoSuchElementException();
            }
        }
        
        public boolean hasNext() {
            return ind <= 1;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();        
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {            
                return false;
            }
        }
        
        // all objects were contained
        return true;
    }
        

    public boolean isEmpty() {        
        return false;
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return 2;
    }

    public Object[] toArray() {
        Object[] result = new Object[2];
        result[0] = mObj1;
        result[1] = mObj2;
        return result;
        
    }
	
	public <E> E[] toArray(E[] a) {
		throw new UnsupportedOperationException();
	}

	public static class Formatter implements Converter<UnorderedPair, String> {
	    public String convert(UnorderedPair p) {
	        return p.mObj1+","+p.mObj2;    
	    }
	}
        


}
