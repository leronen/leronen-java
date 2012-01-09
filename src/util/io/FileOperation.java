package util.io;

import java.io.*;

/** 
 * Instances of this can perform some operation on as given file 
 *
 * Note that this does not extend util.collections.Operation, as file operations 
 * throw IOExceptions, so things would become ugly.
 **/  
public interface FileOperation {
  
    public void doOperation(File pFile) throws IOException;

}
