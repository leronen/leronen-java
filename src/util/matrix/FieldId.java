package util.matrix;


public abstract class FieldId {
    protected String mData;
    
    protected FieldId(String pIdString) {
        mData = pIdString;
    }
            
    public String getName() {
        return mData;                
    }
    
    public abstract FieldId createClone();
    
    public String toString() {
        return mData;
    }
    
    public boolean equals(Object pObj) {
        FieldId other = (FieldId)pObj;
        return mData.equals(other.mData);
    }
        
    public int hashCode() {
        return mData.hashCode();
    }        
        
    abstract int getNumAtomicFields();
    
}            
