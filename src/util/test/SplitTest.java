package util.test;


import util.IOUtils;
import util.StringUtils;
import util.Timer;

/**
 * Test how we can do efficient splitting of integer strings separated by
 * single white spaces. We hypothesize that String.split() sucks at this task.
 *
 */ 
public class SplitTest {

    public static void main(String args[]) throws Exception {
        
        
        
        String s = "rs1848312 73583 A G";
        
        System.out.println(s.split(" ").length);
        
        System.exit(0);
        
        IOUtils.setFastStdout();
        
        String data = "123 456 432 123 3453453 123 123 2345 345 123";
        
        Timer.startTiming("split");        
        for (int i=0; i<10000; i++) {
            String[] result = data.split(" ");
            System.out.println(StringUtils.arrayToString(result, " "));
        }
        Timer.endTiming("split");

        Timer.startTiming("mysplit");
        String[] result = new String[10];
        for (int i=0; i<10000; i++) {
            StringUtils.fastSplit(data, ' ', result);
            System.out.println(StringUtils.arrayToString(result, " "));
        }
        Timer.endTiming("mysplit");

        Timer.startTiming("mysplit2");        
        for (int i=0; i<10000; i++) {
            result = StringUtils.fastSplit(data, ' ');
            System.out.println(StringUtils.arrayToString(result, " "));
        }
        Timer.endTiming("mysplit2");

        
        System.out.flush();
        
        Timer.logToStdErr();
        
    }
    
//    /**
//     * The number of cols must be known in advance, and a suitable 
//     * array for storing the results myst be  and separator is just a 
//     * single char. Otherwise results should be as with ordinary split, 
//     * with 10-fold improvement in efficiency!!
//     */
//    private static void mySplit(String pString, 
//                                char pSeparator,
//                                String[] pResult) {
//        
//        // variables "left" and "right" refer to the positions of
//        // ' '-characters around the current token of intrest
//        
//        int numCols = pResult.length;
//        
//        // find token 0
//        int left = -1;                
//        // String[] pResult = new String[numCols];
//        
//        int right = pString.indexOf(pSeparator, left);
//        if (right == -1) {
//            throw new RuntimeException("Not enough columns!");
//        }
//        
//        if (right != -1) {
//            pResult[0] = pString.substring(left+1, right);
//        }
//        else {
//            // the rightmost column
//            pResult[0] = pString.substring(left+1);
//        }
//        
//        
//        // find tokens 1..(pNumCols-1)
//        for (int i=1; i<numCols; i++) {
//            // Logger.info("i="+i);
//            left = right;
//            if (left == -1) {
//                throw new RuntimeException("Not enough columns!");
//            }
//            right = pString.indexOf(pSeparator, left+1);
//            // result[i] = left+1;
//            if (right != -1) {
//                pResult[i] = pString.substring(left+1, right);
//            }
//            else {
//                // the rightmost column
//                pResult[i] = pString.substring(left+1);
//            }
//        }
//        
//        // return result;
//        
//
//    }
    
}
