package util.converter;

import java.io.File;


/** 
 * Converts strings to Integer objects specifying the string lenght.
 */
public final class FileToFileNameConverter implements Converter<File, String> {         
    
    public String convert(File pFile) {
        return pFile.getName();
    }
}
