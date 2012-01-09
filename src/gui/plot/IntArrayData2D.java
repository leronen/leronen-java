package gui.plot;

import util.*; 
import java.io.*;

public class IntArrayData2D implements Data2D {
        
    private int[][] mData;
    private int mMinVal;
    private int mMaxVal;
    private int mM; // "height"
    private int mN; // "width"        
    
    public IntArrayData2D(InputStream pStream) throws IOException {        
        String[] lines = IOUtils.readLineArray(pStream);
        mM = lines.length;
        mN = lines[0].length();
        mData = new int[mM][];
        for (int i=0; i<mM; i++) {
            mData[i]=new int[mN];
            String line = lines[i];
            for (int j=0; j<mN; j++) {
                mData[i][j]=Integer.parseInt(""+line.charAt(j));    
            }
        }            
        init();
    }
        
    private void init() {
        mMinVal = Integer.MAX_VALUE;
        mMaxVal = Integer.MIN_VALUE;
        for (int i=0; i<mM; i++) {            
            for (int j=0; j<mN; j++) {
                int val = mData[i][j]; 
                if (mMinVal>val) mMinVal = val;
                if (mMaxVal<val) mMaxVal = val;
            }
        }
    }            
            
                
    public int getValueAt(int pX, int pY) {
        return mData[pY][pX];
    }
        
    public int getMaxVal() {
        return mMaxVal;
    }
  
    public int getMinVal() {
        return mMinVal;
    }
    
    public int getNumXIndices() {
        return mN;
    }   
    
    public String getXIndexAt(int pInd) {
        return ""+pInd;
    }

    public int getNumYIndices() {
        return mM;
    }   
    
    public String getYIndexAt(int pInd) {
        return ""+pInd;
    }                                        

}
