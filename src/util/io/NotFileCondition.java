package util.io;

import java.io.*;

public class NotFileCondition implements FileCondition {
 
    private FileCondition mBaseCondition;
    
    public NotFileCondition(FileCondition pBaseCondition) {
        mBaseCondition = pBaseCondition;                        
    }
 
    public boolean fulfills(File pFile) throws IOException {
        return !mBaseCondition.fulfills(pFile);
    }
}
