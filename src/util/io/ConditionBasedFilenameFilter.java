package util.io;

import util.condition.*;

import java.io.*;

public class ConditionBasedFilenameFilter implements FilenameFilter {

    private Condition mCondition;

    public ConditionBasedFilenameFilter(Condition pCondition) {
        mCondition = pCondition;
    }
    
    public boolean accept(File dir, String name) {
        return mCondition.fulfills(name);
    }


}
