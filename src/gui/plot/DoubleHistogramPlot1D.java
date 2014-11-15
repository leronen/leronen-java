package gui.plot;

import java.awt.*;

public final class DoubleHistogramPlot1D extends HistogramPlot1D {    
        
    /**
	 * 
	 */
	private static final long serialVersionUID = -3898749391863920727L;
	private Data1D mData2;
     
    public DoubleHistogramPlot1D() {
        super();
    } 
     
    public DoubleHistogramPlot1D(int pPixels_cell) {
        super(pPixels_cell);                
    }
           
    public void setData2(Data1D pData) {
        mData2 = pData;            
    }    
    
    public void paintComponent(Graphics g) {        
        super.paintComponent(g);
        if (mData == null) {
            // nothing to paint
            return;
        }
        // dbgMsg("min and max of data1: "+mData.getMinVal()+","+mData.getMaxVal());
        // dbgMsg("min and max of data2: "+mData2.getMinVal()+","+mData2.getMaxVal());
        int x,height1, height2;
        g.setColor(COLOR_DATA_BACKGROUND);
        g.fillRect(0, 0, mDataAreaWidth_pixels, HEIGHT_DATA_AREA_PIXELS);
        double dbgOldVal = Double.MIN_VALUE;
        
        for (x = 0; x < mData.getNumXIndices(); x++) {
            double val1 = mData.getValueAt(x);
            if (val1<dbgOldVal) {
                // dbgMsg("val gets smaller, now this gets rudimentary!!");
            }
            dbgOldVal = val1;
            double val2 = mData2.getValueAt(x);
            double diff = val2-val1;
            // height1 = (int)(val1/mData.getMaxVal()*HEIGHT_DATA_AREA_PIXELS);
            // height2 = (int)(val2/mData2.getMaxVal()*HEIGHT_DATA_AREA_PIXELS);
            height1 = calculateHeight(mData.getMinVal(), mData.getMaxVal(), val1);
            height2 = calculateHeight(mData2.getMinVal(), mData2.getMaxVal(), val2);
            int heightdiff = height2-height1;
            int maxHeight = Math.max(height1, height2);
            g.setColor(COLOR_DATA_FOREGROUND);
            g.fillRect(x*mPixels_cell, HEIGHT_DATA_AREA_PIXELS-1-height1,
                       mPixels_cell, height1);
            if (diff>0) {
                g.setColor(Color.green);
            }
            else {
                g.setColor(Color.red);
            }
            g.fillRect(x*mPixels_cell, HEIGHT_DATA_AREA_PIXELS-1-maxHeight,
                       mPixels_cell, Math.abs(heightdiff));                       
        }
        
        int rulerLeft = 0;
        int rulerTop = HEIGHT_DATA_AREA_PIXELS;
        paintXRuler(g, mData, rulerLeft, rulerTop);
        paintYRuler(g);                

    }
        
}
