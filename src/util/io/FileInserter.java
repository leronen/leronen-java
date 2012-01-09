package util.io;

import java.util.*;
import java.io.*;

/** 
 *The sole purpose of this class is to act as front-end to FileUtils.insertLinesIntoFile
 * 
 * Args: arg[0] = name of file
 *       arg[1] = line to insert
 *       rest of args: stuff to insert (all goes on a single line) 
 */
public class FileInserter {

    public static void main (String[] args) {
        if (args.length < 3) {
            throw new RuntimeException("Usage: java util.io.FileInserter <filename> <wheretoinsert> <stufftoinsert>");    
        }
        try {            
            String fileName = args[0];
            int insertIndex = Integer.parseInt(args[1]);
            List argList = Arrays.asList(args);            
            List stuffToInsert = argList.subList(2, argList.size());                         
            FileUtils.insertLinesIntoFile(stuffToInsert, insertIndex, new File(fileName));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


}
