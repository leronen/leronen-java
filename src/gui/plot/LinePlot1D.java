package gui.plot;


import java.awt.*;


public final class LinePlot1D extends Plot1D {                    

    /**
	 * 
	 */
	private static final long serialVersionUID = 3052459793400251647L;

	public LinePlot1D() {
        super(true);
    }

    public LinePlot1D(int pPixels_cell) {
        super(pPixels_cell, true);
        mPixels_cell = pPixels_cell;
    }    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mData == null) {
            // nothing to paint
            return;
        }
        int x, screenX, screenY, prevScreenX, height, prevScreenY;
        g.setColor(COLOR_DATA_BACKGROUND);
        g.fillRect(0, 0, mDataAreaWidth_pixels, HEIGHT_DATA_AREA_PIXELS);
        g.setColor(COLOR_DATA_FOREGROUND);        
        
        double val = mData.getValueAt(0);
        prevScreenX = (int)(0.5*mPixels_cell);
        val = mData.getValueAt(0);
        height = calculateHeight(mData.getMinVal(), mData.getMaxVal(), val);        
        prevScreenY = HEIGHT_DATA_AREA_PIXELS-1-height;        
        
        for (x = 1; x < mData.getNumXIndices(); x++) {
            screenX = (int)((x+0.5)*mPixels_cell); 
            val = mData.getValueAt(x);
            // height = (int)(val/mData.getMaxVal()*HEIGHT_DATA_AREA_PIXELS);
            height = calculateHeight(mData.getMinVal(), mData.getMaxVal(), val); 
            screenY = HEIGHT_DATA_AREA_PIXELS-1-height;            
            g.drawLine(prevScreenX, prevScreenY,
                       screenX, screenY); 
            prevScreenX = screenX;
            prevScreenY = screenY;                       
        }
        
        int rulerLeft = 0;
        int rulerTop = HEIGHT_DATA_AREA_PIXELS;
        paintXRuler(g, mData, rulerLeft, rulerTop);
        paintYRuler(g);        
    }    
    
}
