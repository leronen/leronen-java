package util;

import java.util.Arrays;


/** Wraps a char array as a CharSequence */
public class CharArrayWrapper implements CharSequence {
    private char[] mData;
    
    public CharArrayWrapper(char[] pData) {
        mData = pData;
    }
    
    public char charAt(int index) {
        return mData[index];
    }
     
    public int length() {     
        return mData.length;
    }

    public CharSequence subSequence(int pStart, int pEnd) {        
        return new SubSequenceWrapper(this, pStart, pEnd);     
    }
    
    public String toString() {
        return new String(mData);
    }
    
    public boolean equals(Object p) {
        return Arrays.equals(mData, ((CharArrayWrapper)p).mData);
    }

    public int hashCode() {
        return Arrays.hashCode(mData);            
    }
    
    private static class SubSequenceWrapper implements CharSequence {
        int mStart;
        int mEnd;
        CharSequence mOriginal;
        
        private SubSequenceWrapper(CharSequence pOriginal, int pStart, int pEnd) {
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
            return new SubSequenceWrapper(this, pStart, pEnd);     
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
}



            
