package util.io;

import util.condition.*;

import java.io.*;

public class FileExistsCondition implements Condition {
 
    public boolean fulfills(Object p) {
        return ((File)p).exists();    
    }

}
