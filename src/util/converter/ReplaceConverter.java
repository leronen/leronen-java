package util.converter;

import java.util.regex.*;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class ReplaceConverter implements Converter {         

    Pattern mPattern;
    String mReplacementText;    
        
    public ReplaceConverter(String pPattern,
                            String pReplacementText) {
        mPattern = Pattern.compile(pPattern);
        mReplacementText = pReplacementText;                                   
    }                                       
    
    public Object convert(Object pObj) {
        if (pObj == null) {
            return null;
        }
        else {            
            Matcher m = mPattern.matcher((String)pObj);                        
            return m.replaceAll(mReplacementText);           
        }
    }
}
