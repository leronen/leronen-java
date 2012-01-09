package util.condition;

import java.util.regex.*;

/** Condition that tests whether strings match a regexp */
public class MatchesRegexCondition implements Condition {
    private Pattern mRegex;
    
    public MatchesRegexCondition(String pRegex) {
        mRegex = Pattern.compile(pRegex);
    }
    
    public MatchesRegexCondition(Pattern pRegex) {
        mRegex = pRegex;
    }
    
    public boolean fulfills(Object pObj) {        
        return mRegex.matcher((String)pObj).matches();    
    }        
    
}
