package util.condition;

import util.StringUtils;

/** Condition that tests whether string has a given prefix. No regexps! */
public class HasPrefixCondition implements Condition<String> {
    
    private String mPrefix;
    private boolean mAllowEquality;
    
    public HasPrefixCondition(String pPrefix, boolean pAllowEquality) {
        mPrefix = pPrefix;
        mAllowEquality = pAllowEquality;
    }       
    
    public boolean fulfills(String pObj) {        
        return StringUtils.hasPrefix(pObj, mPrefix, mAllowEquality);    
    }        
    
}
