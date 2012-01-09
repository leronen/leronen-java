package util.test;


import util.IOUtils;


/** keywords: misc tests, misctests, foo test footests, ... */
public class ArbitraryTests {
    
    public static void main(String[] args) throws Exception {
//        test1("foo");
//        test1("bar", 1, 2, 3);
//        test2(args);
//        nullToStringTest();
        varExpandTest(args);
    }
    
    private static void varExpandTest(String[] args) throws Exception {
        String infile = args[0];
        String[] lines = IOUtils.readLineArray(infile);
        for (String line: lines) {
            String expanded = line.replace("$BASEDIR", "/the/base/dir/");
            System.out.println(expanded);
        }
        
    }
}
