package util.matrix;

/** Note: does not override equals or hashcode, so comparison to string works */
public final class BasicFieldId extends FieldId {
    
    public BasicFieldId(String pFieldName) {
        super(pFieldName);    
    }
    public int getNumAtomicFields() {
        return 1;
    }         

    public FieldId createClone() {
        return new BasicFieldId(mData);   
    }
    
    public boolean equals(Object pObj) {
        if (!(pObj instanceof BasicFieldId)) {
            return false;
        }
        BasicFieldId other = (BasicFieldId)pObj;
        return mData.equals(other.mData);
    }
        
    public int hashCode() {
        return mData.hashCode();
    }                
    
}
