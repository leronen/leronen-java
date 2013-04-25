package util;
import util.dbg.*;

import java.io.*;
import java.util.regex.*;

public class RegExTester {

    public static void main (String[] args) throws Exception {
        // test1(args);
        // test2(args);
    	// test3(args);
    	test4(args);
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

    private static void log(String pMsg) {
        Logger.info(pMsg);
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg(pMsg);
    }
    

} 
