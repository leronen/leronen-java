package util.process;

import util.dbg.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

class RunnableStreamReader implements Runnable {
        private InputStream mInputStream;  
        private String mProcessName;
        private String mStreamName;
        private String[] mResult;
        private StreamListener mListener;
        private Pattern[] mListenerPatterns;
        private boolean mOutputAsInfo;        
        
        RunnableStreamReader(String pProcessName, String pStreamName, 
                                    InputStream pInStream, 
                                    StreamListener pListener,
                                    boolean pOutputAsInfo) {
            mStreamName = pStreamName;
            mProcessName = pProcessName;
            mInputStream = pInStream;
            mOutputAsInfo = pOutputAsInfo;
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
                    output(line);
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
        
        private void output(String pMsg) {
            if (mOutputAsInfo) {
                // Logger.info(mProcessName+" output ("+mStreamName+"): "+pMsg);
                Logger.info(mProcessName+" ("+mStreamName+"): "+pMsg);
            }
        }                
    }
