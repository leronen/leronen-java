package gui.plot;


import java.awt.*;

public class HistogramPlot1D extends Plot1D {                       

    /**
	 * 
	 */
	private static final long serialVersionUID = 3157725888909190729L;

	public HistogramPlot1D() {
        super(true);
    }

    public HistogramPlot1D(int pPixels_cell) {
        super(pPixels_cell, true);               
    }    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mData == null) {
            // nothing to paint
            return;
        }
        int x,height;
        g.setColor(COLOR_DATA_BACKGROUND);
        g.fillRect(0, 0, mDataAreaWidth_pixels, HEIGHT_DATA_AREA_PIXELS);
        g.setColor(COLOR_DATA_FOREGROUND);
        for (x = 0; x < mData.getNumXIndices(); x++) {
            double val = mData.getValueAt(x);
            height = calculateHeight(mData.getMinVal(), mData.getMaxVal(), val);
            g.fillRect(x*mPixels_cell, HEIGHT_DATA_AREA_PIXELS-1-height,
                       mPixels_cell, height);
        }
        
        int rulerLeft = 0;
        int rulerTop = HEIGHT_DATA_AREA_PIXELS;
        paintXRuler(g, mData, rulerLeft, rulerTop);
        paintYRuler(g);                

    }

}