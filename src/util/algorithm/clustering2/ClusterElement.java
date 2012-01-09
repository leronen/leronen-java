package util.algorithm.clustering2;



/**
 * For wrapping objects to be clustered. main purpose is to 
 * produce integer id:s (starting from 0) for faster access in 
 * various data structures.
 */ 
public class ClusterElement<T> {    
    
    T mKey;
    int mIndex;
    
    public ClusterElement(T pKey, int pInd) {       
        mKey = pKey;
        mIndex = pInd;               
    }
    
    public boolean equals(Object p) {
        ClusterElement ce = (ClusterElement)p;
        return mIndex == ce.mIndex;
    }
    
    public int hashCode() {
        return mIndex;
    }

}
