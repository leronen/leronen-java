package util.matrix;

import util.*;

import java.util.*;

/** Note: does not override equals or hashcode, so comparison to string works */
public final class MultiPartFieldId extends FieldId {
    
    // Integer mNumParts;    
     
    /**
     * Contains strings.
     * May be null?!
     */
    ArrayList mAtomicParts;
    
    /** leaves the invidual fields unspecified */
    MultiPartFieldId(String pFieldName) {
        super(pFieldName);    
    }
            
    MultiPartFieldId(String pFieldName, List pParts) {            
        super(pFieldName);    
        mAtomicParts = new ArrayList(pParts);
    }
    
    public FieldId createClone() {
        return new MultiPartFieldId(mData, new ArrayList(mAtomicParts));   
    }
    
    public void removeAtomicFields(int[] pFields) {
        int[] fields = new int[pFields.length];
        for (int i=0; i<fields.length; i++) {
            fields[i] = pFields[i];
        }
        Arrays.sort(fields);                
        // note: markers are removed one at a time, starting from the end;
        // this (starting from end) avoids adjusting the indices after each delete
        for (int i=fields.length-1; i>=0; i--) {                
            mAtomicParts.remove(fields[i]);
        }                                    
    }
    
    int getNumAtomicFields() {
        return mAtomicParts.size();
    }
    
    public List getAtomicParts() {
        return mAtomicParts;
    }
    
    public String toString() {
        if (mAtomicParts == null) {
            return super.toString();
        }
        else {
            return StringUtils.collectionToString(mAtomicParts, " ");    
        }        
    }
    
    public boolean equals(Object pObj) {
        if (!(pObj instanceof MultiPartFieldId)) {
            return false;
        }
        MultiPartFieldId other = (MultiPartFieldId)pObj;
        if (mAtomicParts == null) {
            return other.mAtomicParts == null;
        }
        else {                        
            return mData.equals(other.mData) && mAtomicParts.equals(other.mAtomicParts);
        }
    }
    
    public int hashCode() {
        if (mAtomicParts == null) {            
            return mData.hashCode();
        }
        else {
            return mData.hashCode() + mAtomicParts.hashCode();
        }
    }
    
        
    
    
    
    
}
