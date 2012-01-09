package util.test;

import java.util.List;

import util.IOUtils;
import util.SU;

public class Test4 {
    public static void main(String[] args) throws Exception{
        List<String> lines = IOUtils.readLines(args[0]);
        String s = SU.toString(lines, "\n");
        System.out.println(s);
        
    }
}
