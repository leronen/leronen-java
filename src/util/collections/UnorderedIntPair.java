package util.collections;

import java.util.HashSet;
import java.util.Set;

import util.HashUtils;
import util.StringUtils;

/**
 * A pair with 2 elements of the same type; order does not matter,
 * so (x,y) equals (y,x)
 */
public class UnorderedIntPair {
 
    protected int mInt1;
    protected int mInt2;                
      
    public UnorderedIntPair(int pObj1, int pObj2) {
        mInt1 = pObj1;
        mInt2 = pObj2;    
    }    
			    
    public UnorderedIntPair(SymmetricPair<Integer> pPair) {    	
    	mInt1 = pPair.mObj1;
    	mInt2 = pPair.mObj2;    		    	    	
    }
    
    public int getInt1() {
        return mInt1;
    }
    
    public int  getInt2() {
        return mInt2;
    }          
                                
    public boolean equals(Object p) {
        UnorderedIntPair other = (UnorderedIntPair)p;
        return (mInt1 ==  other.mInt1 && mInt2 == other.mInt2) 
            || (mInt1 == other.mInt2 && mInt2 == other.mInt1);       
    }
    
    /** Recall that we have to have the same hash code for both objects! */
    public int hashCode() {
        return HashUtils.hash(HashUtils.SEED, mInt1+mInt2);                
    }
    
    ////////////////////////////////////////////////////////////
    // Common Object methods
    ////////////////////////////////////////////////////////////
    public String toString() {
        return "("+mInt1+","+mInt2+")";    
    }

    public static void main(String[] args) throws Exception {
        Set<UnorderedIntPair> set = new HashSet<UnorderedIntPair>();
        for (int i=0; i<4; i++) {
            for (int j=0; j<4; j++) {
                set.add(new UnorderedIntPair(i,j));
            }
        }
        
        System.out.println(StringUtils.collectionToString(set));
        
    }

    
}
