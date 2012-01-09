package util.io;

import java.io.*;

public abstract class AbstractFileNameFileCondition implements FileCondition {

    public boolean fulfills(File pFile) {    
        return checkName(pFile.getName());
    }
    
    /** This is all that is required from the pesky subclasses */
    protected abstract boolean checkName(String pName);

    
}
