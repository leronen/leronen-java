package gui.plot;

import util.*;
import util.converter.*;

import java.util.*;


/** interface that enables a Plot1D class to plot a histogram of 1D data */
public class MapData1D implements Data1D  {    
    
    private int mNumVals;
    private List mKeys;
    private List mVals;
    
    private double mMin;
    private double mMax;
    
    public MapData1D(Map pData) {
        mNumVals = pData.size();
        mKeys = new ArrayList(pData.keySet());
        mVals = ConversionUtils.convert(pData.values(), new AnyToDoubleConverter());
        mMax = MathUtils.max(ConversionUtils.DoubleCollectionTodoubleArray(mVals));                        
        mMin = MathUtils.min(ConversionUtils.DoubleCollectionTodoubleArray(mVals));
        if (mMin > 0) {
            // let's klugde this for more beautiful display:         
            mMin = 0;
        }               
    }
    
    public int getNumXIndices() {
        return mNumVals;    
    }
    
    public String getXIndexAt(int pInd) {
        return mKeys.get(pInd).toString();        
    }
    public double getValueAt(int pInd) {
        return ((Double)mVals.get(pInd)).doubleValue();
    }        
    public double getMaxVal() {
        return mMax;        
    }
    public double getMinVal() {
        return mMin;        
    }
            
}
