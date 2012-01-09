package util.io;

import java.util.regex.*;

public class ContainsPatternFileCondition extends AbstractLinewiseFileCondition {

    private Pattern mPattern;
    
    public ContainsPatternFileCondition(String pPattern) {
        mPattern = Pattern.compile(pPattern);    
    }
    
    /** This is all that is required from the pesky subclasses */
    protected boolean checkLine(String pLine) {
        return mPattern.matcher(pLine).matches();    
    }

    
}
