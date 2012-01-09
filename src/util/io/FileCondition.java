package util.io;

import java.io.*;

public interface FileCondition {
 
    public boolean fulfills(File pFile) throws IOException;

}
