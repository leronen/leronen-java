package util.io;
 
import util.*;
import util.condition.*;
import util.dbg.*;


 
import java.util.*;
 
import java.io.*;
 
public class DeleteFileOperation implements FileOperation {             
                   
    private boolean mDeleteDirectories;
    /** Only has meaning when deletedirectories is true, of course */
    private boolean mRecursive;
    
    private static final Condition IS_DIR_CONDITION = new IsDirCondition();
    private static final Condition IS_ORDINARY_FILE_CONDITION = new NotCondition(IS_DIR_CONDITION);
    
    /** Operation that does not remove directories */
    public DeleteFileOperation() {
        mDeleteDirectories = false;
        mRecursive = false;
    }
    
    /** Operation that does not remove directories */
    public DeleteFileOperation(boolean pDeleteDirectories, boolean pRecursive) {
        if (!pDeleteDirectories && pRecursive) {
            throw new RuntimeException("Illegal combination of parameters: pDeleteDirectories==false, pRecursive==true");
        }
        mDeleteDirectories = pDeleteDirectories;
        mRecursive = pRecursive;
    }
                           
    public void doOperation(File pFile) throws IOException {
        dbgMsg("Deleting: "+pFile);        
        if (pFile.isDirectory()) {
            if (mDeleteDirectories) {
                deleteDir(pFile);
            }            
            // else: no action!                                                
        }
        else {
            // ordinary file, always delete!
            pFile.delete();    
        }
    }            
    
    private void deleteDir(File pDir) {
        File[] files = pDir.listFiles();
        List fileList = Arrays.asList(files);            
        List subDirs = (List)CollectionUtils.extractMatchingObjects(fileList, IS_DIR_CONDITION);
        List ordinaryFiles = (List)CollectionUtils.extractMatchingObjects(fileList, IS_ORDINARY_FILE_CONDITION);
                
        if (mRecursive) {
            // recursively delete sub directories                                    
            Iterator subDirIter = subDirs.iterator();
            while(subDirIter.hasNext()) {
                File subDir = (File)subDirIter.next();
                deleteDir(subDir);
            }                                                                                                                 
        }
        
        // delete ordinary files
        Iterator ordinaryFileIter = ordinaryFiles.iterator();
        while(ordinaryFileIter.hasNext()) {
            File file = (File)ordinaryFileIter.next();
            file.delete();
        }
        
        // delete dir itself
        pDir.delete();                
    }
    
    private static void dbgMsg(String pMsg){
        Logger.dbg("DeleteFileOperation"+pMsg);
    }
    
    
        
}
