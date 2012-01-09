package gui.plot;

import java.util.*;

public class DefaultPlottableData implements Data1D {
        
    private ArrayList mKeys;
    private ArrayList mValues;            
    private double mMinVal;
    private double mMaxVal;
    
    public DefaultPlottableData() {
    	mKeys = new ArrayList();
        mValues = new ArrayList();            
        mMinVal = Double.MAX_VALUE;
        mMaxVal = Double.MIN_VALUE;
    }
        
    public void add(String pName, double pVal) {
        mKeys.add(pName);
        mValues.add(new Double(pVal));
        if (mMinVal>pVal) {
            mMinVal = pVal;
        }
        if (mMaxVal<pVal) {
            mMaxVal = pVal;
        }
    }        
                
    public double getValueAt(int pInd) {
        return ((Double)mValues.get(pInd)).doubleValue();
    }
        
    public double getMaxVal() {
        return mMaxVal;
    }
  
    public double getMinVal() {
        return mMinVal;
    }
    
    public int getNumXIndices() {
        return mKeys.size();
    }   
    
    public String getXIndexAt(int pInd) {
        return (String)mKeys.get(pInd);
    }                
    
    public void setMaxVal(double pVal) {
        mMaxVal = pVal;    
    }
    
    public void setMinVal(double pVal) {
        mMinVal = pVal;    
    }

}
