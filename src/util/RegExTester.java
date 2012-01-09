package util;
import util.dbg.*;

import java.io.*;
import java.util.regex.*;

public class RegExTester {

    public static void main (String[] args) {
        // test1(args);
        test2(args);
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
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg(pMsg);
    }
    

} 
