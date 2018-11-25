package util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.dbg.Logger;

public class RegExTester {

    public static void main (String[] args) throws Exception {
        // test1(args);
        // test2(args);
    	// test3(args);
    	// test4(args);
        // test5(args);
        // test6(args);
        targetDatasetTest();
    }

    public static void test6(String... args) {
        String pattern = "foo";
        for (String arg: args) {            
            log(arg + ": " + pattern.matches(arg));            
        }
    }       
    
    public static void test3 (String[] args) {
    	String pattern = ".*foo.*";
    	System.out.println("pattern: "+pattern);
    	for (String s: args) {
    		boolean matches = s.matches(pattern);
    		if (matches) {
    			System.out.println(s+" matches");
    		}
    		else {
    			System.out.println(s+"does not match");
    		}
    	}
    }

    public static void test4 (String[] args) throws IOException  {
    	Pattern pattern1 = Pattern.compile("\t");
    	for (String s: IOUtils.readLines()) {
    		log("Split using pattern1: "+pattern1);
    		String[] tok1 = pattern1.split(s);
    		log(StringUtils.arrayToString(tok1,","));
    	}
    }

    public static void test5(String[] args) throws IOException  {
        Pattern pattern1 = Pattern.compile(" *\\|\\|");
        for (String s: IOUtils.lines(System.in)) {
            Matcher m = pattern1.matcher(s);
            if (m.matches()) {
                System.out.println(s+": match");
            }
            else {
                System.out.println(s+": no match");
            }
            System.out.flush();
        }
    }

    public static void test2 (String[] args) {
        String[] result = args[0].split("(,)|(\\n)");
        System.out.println(StringUtils.arrayToString(result, " and "));
    }

    public static void test1 (String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Pattern p = Pattern.compile("^\\w+(\\s+\\w+)*\\(.+\\)$");
        // esim: a b c (2.4)

        // esim: a b c (60.0%)
        String patternString = "^\\w+(\\s+\\w+)*\\s*\\((.+)\\%\\)\\s*$";
        //                         a    b c           (60.0%  )

        Pattern p = Pattern.compile(patternString);
        dbgMsg("Pattern: "+patternString);

        // esim: a b c : 1.01
        // Pattern p = Pattern.compile("^\\w+(\\s+\\w+)*\\s*\\:\\s*(.+)$");

        try {
        String line = reader.readLine();

            while(line != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    dbgMsg("line: "+line+" matches");
                    dbgMsg("number of groups: "+m.groupCount());
                }
                else {
                    dbgMsg("line: "+line+" does " + "not match");
                }
                line = reader.readLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void targetDatasetTest() throws IOException {
        Pattern targetDatasetPattern = java.util.regex.Pattern.compile(">>> To:.*\\((.*)\\)$");        
        for (String line: IOUtils.readLines()) {
            Matcher m = targetDatasetPattern.matcher(line);
            if (m.matches()) {
                System.out.println(m.group(1));
            }
        }
    }
    
    
    private static void log(String pMsg) {
        Logger.info(pMsg);
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg(pMsg);
    }


}
