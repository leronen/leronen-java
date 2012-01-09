package util.io;

import java.util.regex.*;

public class ContainsPatternLineCondition implements LineCondition {

    private Pattern mPattern;
    
    public ContainsPatternLineCondition(String pPattern) {
        mPattern = Pattern.compile(pPattern);    
    }
    
    /** This is all that is required from the pesky subclasses */
    public boolean fulfills(String pLine) {
        return mPattern.matcher(pLine).matches();    
    }

    
}
