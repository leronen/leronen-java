package util.io;

import java.io.*;

public class AndFileCondition implements FileCondition {
 
    private FileCondition mBaseCondition1;
    private FileCondition mBaseCondition2;
    
    public AndFileCondition(FileCondition pBaseCondition1,
                            FileCondition pBaseCondition2) {
        mBaseCondition1 = pBaseCondition1;
        mBaseCondition2 = pBaseCondition2;                 
    }
 
    public boolean fulfills(File pFile) throws IOException {
        return mBaseCondition1.fulfills(pFile) && mBaseCondition2.fulfills(pFile);
    }
}
