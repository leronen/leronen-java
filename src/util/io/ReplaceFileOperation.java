package util.io;
 
import java.io.*;

import util.dbg.*;
 
public class ReplaceFileOperation implements FileOperation {
 
    private String mPatternToReplace;
    private String mReplacementText;
    
    int mFileCount;
    int mLineCount;
    
    public ReplaceFileOperation(String pPatternToReplace,
                                String pReplacementText) {
        mPatternToReplace = pPatternToReplace;
        mReplacementText = pReplacementText;
        mFileCount = 0;
        mLineCount = 0;
    }                                    
 
    public void doOperation(File pFile) throws IOException {
        Logger.dbg("ReplaceFileOperation.doOperation()");
        int numLines = FileUtils.replaceInFile(mPatternToReplace, mReplacementText, pFile);
        if (numLines > 0) {
            mFileCount++;
            mLineCount += numLines;
        }
    }
    
    public int getFileCount() {
        return mFileCount;
    }
    
    public int getLineCount() {
        return mLineCount;
    }
    
    public String toString() {
        return ""+mLineCount+" lines in "+mFileCount+" files modified";    
    }
}
