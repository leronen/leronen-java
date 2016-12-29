package util.process;

import java.io.*;
import java.util.*;
import java.util.regex.*;

class RunnableStreamReader implements Runnable {
        private InputStream mInputStream;                  
        private String[] mResult;
        private StreamListener mListener;
        private Pattern[] mListenerPatterns;               
        
        RunnableStreamReader(InputStream pInStream, 
                             StreamListener pListener) {
                       
            mInputStream = pInStream;
            
            if (pListener != null) {
                mListener = pListener;
                String[] patternStrings = pListener.getRegularExpressions();
                mListenerPatterns = new Pattern[patternStrings.length];
                for (int i=0; i<patternStrings.length; i++) {
                    mListenerPatterns[i] = Pattern.compile(patternStrings[i]);                        
                }                
            }
        }
                
        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(mInputStream));
                LinkedList lines = new LinkedList();
                String line = reader.readLine();                                
                while (line!=null) {
                    lines.add(line);                    
                    if (mListener != null) {
                        for (int i=0; i<mListenerPatterns.length; i++) {
                            Matcher m = mListenerPatterns[i].matcher(line);
                            if (m.matches()) {
                                mListener.notify(line, i);
                            }
                        }
                    }                    
                    
                    line = reader.readLine();                    
                }
                reader.close();
                mResult = (String[])lines.toArray(new String[lines.size()]);
            }     
            catch (Exception e) {
                e.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();                    
                    }
                    catch (Exception fooE) {
                        // foo
                    }                              
                }
            }
        }
        
        public String[] getResult() {
            return mResult;    
        }
                   
    }
