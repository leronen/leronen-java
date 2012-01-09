package util.io;

import util.*;
import java.io.*;

public abstract class AbstractLinewiseFileCondition implements FileCondition {

    public boolean fulfills(File pFile) throws IOException{    
        String[] lines = IOUtils.readLineArray(pFile.getAbsolutePath());
        for (int j=0; j<lines.length; j++) {
            if (checkLine(lines[j])) {
                // line matched
                return true;
            }
        }
        return false;        
    }
    
    /** This is all that is required from the pesky subclasses */
    protected abstract boolean checkLine(String pLine);

    
}
