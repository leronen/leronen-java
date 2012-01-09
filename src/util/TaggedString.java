package util;

/** 
 * String that can have some or all of its characters tagged;
 * this can be used, for example, so that operations can be 
 * performed such that they only affect the 
 * tagged part
 */
public class TaggedString {
    boolean[] mTagged;
    String mData;
    
    /** create TaggedString with no tagged area */    
    public TaggedString(String pString) {
        mData = pString;
        mTagged = new boolean[pString.length()];
    }
    
    /** create TaggedString with no tagged area */    
    public TaggedString(String pString, boolean[] pTagged) {
        mData = pString;
        mTagged = pTagged;
    }
            
    public TaggedString(String pString, Range pTaggedRange) {
        this(pString);
        for (int i=pTaggedRange.start; i<pTaggedRange.end; i++) {
            mTagged[i] = true;
        }
    }
    
    
    
    public void tag(int pStart, int pEnd) {
        for (int i=pStart; i<pEnd; i++) {
            mTagged[i]=true;    
        }
    }
    
    public void tag(Range pRange) {
        if (!pRange.isNull) {
            tag(pRange.start, pRange.end);
        }
    }
    
    /**
     * Produce new String from tagged parts of two TaggedStrings 
     * lengths must match, and the each position must be tagged in EXACTLY one of the TaggedStrings
     */
    public String joinTaggedParts(TaggedString pOther) {
        if (mData.length() != pOther.mData.length()) {
            throw new RuntimeException("lengths must match");
        }
        int len = mData.length();
        int[] tmp1 = ConversionUtils.toIntArr(mTagged);
        int[] tmp2 = ConversionUtils.toIntArr(pOther.mTagged);
        int[] tagSum = MathUtils.sum(tmp1, tmp2);
        if (!MathUtils.containsOnlyVals(tagSum, 1)) {
            throw new RuntimeException("cannot join tagged parts: either they overlap or contain gaps");
        }
        char[] result = new char[len];
        char[] chars1 =  mData.toCharArray();
        char[] chars2 =  pOther.mData.toCharArray();
        for (int i=0; i<len; i++) {
            if (mTagged[i]) {
                result[i] = chars1[i];
            }
            else {
                // other must be tagged (ensured above)
                result[i] = chars2[i];
            }                
        }
        return new String(result);                        
    }

    /** get tagged chars from tagged string, other chars from pString */
    public String replaceTagged(String pString) {
        int len = mData.length();
        if (pString.length()!=len) {
            throw new RuntimeException("lengths must match!");
        }
        char[] buf = new char[len];
        for (int i=0; i<len; i++) {
            if (mTagged[i]) {
                buf[i] = mData.charAt(i);
            }
            else {
                buf[i] = pString.charAt(i);
            }                                
        }   
        return new String(buf);           
    }
    

}
