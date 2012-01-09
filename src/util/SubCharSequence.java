package util;


public class SubCharSequence implements CharSequence {
    int mStart;
    int mEnd;
    CharSequence mOriginal;
    
    public SubCharSequence(CharSequence pOriginal, int pStart, int pEnd) {
        mOriginal = pOriginal;
        mStart = pStart;
        mEnd = pEnd;
    }
 
    public char charAt(int index) {
        return mOriginal.charAt(mStart+index);
    }
     
    public int length() {     
        return mEnd-mStart;
    }
    
    public CharSequence subSequence(int pStart, int pEnd) {        
        return new SubCharSequence(this, pStart, pEnd);     
    }
    
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        int len = length();
        for (int i=0; i<len; i++) {
            tmp.append(charAt(i));
        }
        return tmp.toString();                
    }
    
    public boolean equals(Object p) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }
    

}
