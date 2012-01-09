package util.io;

import util.*;

import java.util.regex.*;

import java.io.*;

/** Vulgar scanner for linewise (not tokenwise!) scanning */
public class SimpleLinewiseScanner {
            
    private Line[] mLines;    
    private int mLinePtr;
    private int mPrevNonCommented;
    
    private static Pattern COMMENTED_PATTERN = Pattern.compile("^\\s*#.*$");
    
    public SimpleLinewiseScanner(String pFilename) throws IOException {
        this(new File(pFilename));
    }
    
    public SimpleLinewiseScanner(File pFile) throws IOException { 
        String[] tmp = IOUtils.readLineArray(pFile);
        int numLines = tmp.length;
        mLines = new Line[numLines];
        for (int i=0; i<numLines; i++) {
            String s = tmp[i];
            Matcher commentedMatcher = COMMENTED_PATTERN.matcher(s);
            boolean commented = commentedMatcher.matches();            
            mLines[i] = new Line(tmp[i], commented, i+1);  
        }
        reset();
                                    
    }
    
    public boolean hasMoreLines() {
        return mLinePtr != -1;         
    }

    public boolean hasPrevLine() {
        return mPrevNonCommented != -1;         
    }        
    
    public void reset() {
        mLinePtr = -1;
        moveToNextLine();
    }
    
    public String readLine() {
        if (mLinePtr == -1) {
            throw new RuntimeException("No more lines to read!");
        }
        String result = mLines[mLinePtr].mText;
        moveToNextLine();   
        return result;        
    }
    
    public int getCurrentLine() {        
        return mLines[mLinePtr].mLineNum;
    }

    public void moveToPrevLine() {
        mLinePtr = mPrevNonCommented;
        mPrevNonCommented--;        
        while(mPrevNonCommented >= 0  && mLines[mPrevNonCommented].mCommented) {
            mPrevNonCommented--;                
        }        
    }        
    
    private void moveToNextLine() {
        mPrevNonCommented = mLinePtr;
        mLinePtr++;        
        while(mLinePtr < mLines.length && mLines[mLinePtr].mCommented) {
            mLinePtr++;                
        }
        if (mLinePtr == mLines.length) {
            // no more lines            
            mLinePtr = -1;             
        }
    }                                                
            
    private class Line {
        String mText;
        boolean mCommented;
        int mLineNum;
        
        Line(String pText,
             boolean pIsCommented,
             int pLineNum) {
            mText = pText;
            mCommented = pIsCommented;
            mLineNum = pLineNum; 
        }
        
        public String toString() {
            return "line ("+mText+"), commented: "+mCommented+", text: "+mText;
        }
            
    }        

    public static void main (String[] args) {
        try {
            SimpleLinewiseScanner scanner = new SimpleLinewiseScanner(new File(args[0]));
            while(scanner.hasMoreLines()) {
                int lineNum = scanner.getCurrentLine();
                String line = scanner.readLine();
                System.out.println(""+lineNum+": "+line);
            }
            
            System.out.println();            
            System.out.println("OK, another go, this time we push back each line...");
            scanner = new SimpleLinewiseScanner(new File(args[0]));
            while(scanner.hasMoreLines()) {
                int lineNum = scanner.getCurrentLine();
                String line = scanner.readLine();
                System.out.println(""+lineNum+": "+line);
                scanner.moveToPrevLine();
                lineNum = scanner.getCurrentLine();
                line = scanner.readLine();
                System.out.println(""+lineNum+": "+line);
            }
            
            System.out.println();            
            System.out.println("OK, yet another go, this time just reset and read stuff once again...");
            scanner.reset();
            while(scanner.hasMoreLines()) {
                int lineNum = scanner.getCurrentLine();
                String line = scanner.readLine();
                System.out.println(""+lineNum+": "+line);                
            }
            
            System.out.println();            
            System.out.println("Now this is the last effort: backtrack as fas as we can, and read the final time...");            
            while(scanner.hasPrevLine()) {
                scanner.moveToPrevLine();
                int lineNum = scanner.getCurrentLine();
                String line = scanner.readLine();
                scanner.moveToPrevLine();                
                System.out.println(""+lineNum+": "+line);                
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }        
    }
    

}
