package gui.plot;

import util.*;
import util.converter.*;

import java.util.*;


/** interface that enables a Plot1D class to plot a histogram of 1D data */
public class ListData1D implements Data1D  {    
    
    private List mData;
    
    private double mMin;
    private double mMax;
    
    public ListData1D(List pData) {
        mData = ConversionUtils.convert(pData, new AnyToDoubleConverter());
        mMax = MathUtils.max(ConversionUtils.DoubleCollectionTodoubleArray(mData));                        
        mMin = MathUtils.min(ConversionUtils.DoubleCollectionTodoubleArray(mData));
        if (mMin > 0) {
            // let's klugde this for more beautiful display:         
            mMin = 0;
        }               
    }
    
    public int getNumXIndices() {
        return mData.size();    
    }
    
    public String getXIndexAt(int pInd) {
        return ""+pInd;        
    }
    public double getValueAt(int pInd) {
        return ((Double)mData.get(pInd)).doubleValue();
    }        
    public double getMaxVal() {
        return mMax;        
    }
    public double getMinVal() {
        return mMin;        
    }
            
}
