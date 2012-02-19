package util.io;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.condition.Condition;

public class FileNameCondition implements Condition<File> {
 
    private Pattern pattern;    
    
    public FileNameCondition(String regex) {                           
        pattern = Pattern.compile(regex);
    }
 
    public boolean fulfills(File file) {
        Matcher m = pattern.matcher(file.getName());
        return m.matches();
    }
}
