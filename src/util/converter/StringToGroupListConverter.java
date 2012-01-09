package util.converter;


import java.util.*;
import java.util.regex.*;



/** 
 * Converts strings to lists, given a delimeter pattern 
 * The returned lists are immutable.
 */
public final class StringToGroupListConverter implements Converter {
    private Pattern mPattern;    
    
    public StringToGroupListConverter(Pattern pPattern) {
        mPattern = pPattern;                
    }
            
    public Object convert(Object pObj) {            
        String s = (String)pObj;
        // dbgMsg("Converting: "+s);
        Matcher m = mPattern.matcher(s);
        if (m.matches()) {        
            int numGroups = m.groupCount();
            //dbgMsg("numGroups: "+numGroups);
            ArrayList result = new ArrayList(numGroups);
            for (int i=1; i<=numGroups; i++) {
                // dbgMsg("getting group: "+i);
                result.add(m.group(i));
            }
            return result;             
        }
        else {
            throw new RuntimeException("All is lost; did not match!");
        }            
            
    }
    
    
}
