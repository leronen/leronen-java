package util.io;

import util.*;
import java.io.*;

/** Vulgar scanner */
public class SimpleScanner {
          
    private SimpleLinewiseScanner mLinewiseScanner;
    
    private int mTokenPtr;
    private String[] mCurrentTokens;
            
              
    public SimpleScanner(String pFilename) throws IOException {
        this(new File(pFilename));
    }
    
    public SimpleScanner(File pFile) throws IOException {
        mLinewiseScanner = new SimpleLinewiseScanner(pFile);        
        init();                
    }          
    
    private void init() {
        moveToNextToken();                 
    }
    
    private void moveToNextToken() {
        // dbgMsg("moveToNextToken"); // , current line+mLinewiseScanner.getCurrentLine());
        if (mCurrentTokens == null || mTokenPtr+1 >= mCurrentTokens.length) {            
            // find the next line that constains tokens
            boolean foundToken = false;            
            while(!foundToken && mLinewiseScanner.hasMoreLines()) {
                // dbgMsg("mentiin while-looppiin...");
                String line = mLinewiseScanner.readLine();
                // dbgMsg("read line: "+line);
                mCurrentTokens = StringUtils.removeEmptyStrings(line.split("\\s+"));
                // dbgMsg("current tokens: "+StringUtils.arrayToString(mCurrentTokens));                        
                if (mCurrentTokens.length > 0) {
                    foundToken = true;
                    mTokenPtr = 0;                    
                }
            }
            if (!foundToken) {
                // OK, scanned to the end of input, but no token found;
                // kludgishly increase token ptr to notify hasMoreTokens that
                // no more tokens are available...
                mTokenPtr++;
            }
        }
        else {
            // dbgMsg("just move to next token...");
            // just move to next token on this line
            mTokenPtr++;
        }
        // dbgMsg("returning from moveToNextToken, mTokenPtr="+mTokenPtr+",\n"+
                // "mCurrentTokens="+StringUtils.arrayToString(mCurrentTokens,",")+"\n, mCurrentTokens.length="+mCurrentTokens.length);
    }        
        
    public boolean hasMoreTokens() {        
        // should always point to a token when this is called... otherwise there are no more tokens
        return mTokenPtr < mCurrentTokens.length;
    }
        
    public String nextToken() {
        if (!hasMoreTokens()) {
            throw new RuntimeException("No more tokens!");
        }
        else{
            String result = currentToken();
            moveToNextToken();
            return result;
        }
    }            
    
    public String currentToken() {
        return mCurrentTokens[mTokenPtr];
    }

    public static void main (String[] args) {                
        // testSplit(args[0]);
        testScanner(args[0]);                                        
   }
    
    private static void testScanner(String pFileName) { 
        try {                                
            SimpleScanner scanner = new SimpleScanner(pFileName);
            while(scanner.hasMoreTokens()) {
                String token = scanner.nextToken();
                System.out.println("Token: "+token);                
            }            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
}
