package util.io;

import util.converter.Converter;

import java.io.*;


/** Converts Files to file names (only the last component of the name!) */
public class FileNameToFileConverter implements Converter {
   
    public Object convert(Object pObj) {
        String pathName = (String)pObj;
        return new File(pathName);        
    }
}
