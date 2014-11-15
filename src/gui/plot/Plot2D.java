package gui.plot;

import gui.color.*;

import java.awt.*;

public class Plot2D extends Plot {
            
    /**
	 * 
	 */
	private static final long serialVersionUID = 7013865546971410290L;
	protected ColorTable mColorTable;                
    protected Data2D mData;
    protected int mDataAreaWidth_pixels;
    protected int mDataAreaHeight_pixels;
    
    protected int mPixels_cell_Y;

    public Plot2D() {
        this(new DefaultColorTable());    
    }
    
    public Plot2D(ColorTable pColorTable) {
        this(DEFAULT_PIXELS_CELL,
             DEFAULT_PIXELS_CELL,
             pColorTable);
    }
    
    public Plot2D(int pPixels_cell_X,
                  int pPixels_cell_Y,
                  ColorTable pColorTable) {
        setOpaque(true);
        setBackground(COLOR_PAD);        
        mPixels_cell = pPixels_cell_X;
        mPixels_cell_Y = pPixels_cell_Y;
        mColorTable = pColorTable;
    }

    public void setData(Data pData) {
        mData = (Data2D)pData;
        mDataAreaWidth_pixels = mData.getNumXIndices()*mPixels_cell;
        mDataAreaHeight_pixels = mData.getNumYIndices()*mPixels_cell_Y;
        int prefW = mDataAreaWidth_pixels + PAD_HORIZ + RULER_HEIGHT_Y;
        int prefH = mDataAreaHeight_pixels + PAD_VERT + RULER_HEIGHT_X;
        setPreferredSize(new Dimension(prefW, prefH));
        // pData.addView(this);
        repaint();
    }

    public Data getData() {
        return mData;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mData == null) {
            // nothing to paint
            return;
        }                                                            
        for (int x = 0; x < mData.getNumXIndices(); x++) {
            for (int y = 0; y < mData.getNumYIndices(); y++) {                                
                int val = mData.getValueAt(x, y);
                // dbgMsg("valueAt("+x+","+y+")="+val);
                Color color = mColorTable.getColor(val);
                g.setColor(color);                
                g.fillRect(x*mPixels_cell, y*mPixels_cell_Y,
                           mPixels_cell, mPixels_cell_Y);
            }    
        }
        int rulerLeft = 0;
        int rulerTop = mDataAreaHeight_pixels;
        paintXRuler(g, mData, rulerLeft, rulerTop);        

    }
    
    /*
    protected void paintYRuler(Graphics g) {
        if (mData==null) {
            return;
        }
        // draw y ruler
        
        // ruler bounds        
        int w = RULER_HEIGHT;
        int h = HEIGHT_DATA_AREA_PIXELS + PAD_BOTTOM;
        int leftX = -w;
        int middleX = leftX + RULER_HEIGHT_LOWER;
        int rightX = middleX+RULER_HEIGHT_UPPER;
        int upperY = 0;
        int lowerY = h;

        // line between data area and ruler
        g.setColor(COLOR_RULER_FOREGROUND);
        g.drawLine(rightX-1, upperY, rightX-1, lowerY);

        
         // background
        g.setColor(COLOR_RULER_BACKGROUND);
        g.fillRect(leftX, upperY, w-2, h-1);
        
        // scale
        FontMetrics fm = getFontMetrics(g.getFont());
        g.setColor(COLOR_RULER_FOREGROUND);
        int numsteps = 10;
        double minval = mData.getMinVal();
        double maxval = mData.getMaxVal();
        double range = maxval-minval;
        double step = range/numsteps;
        
        for (int i=0; i<=numsteps; i++) {            
            double val = minval+i*step;
            int height = calculateHeight(minval, maxval, val);
            int y_screen = HEIGHT_DATA_AREA_PIXELS-1-height;            
            g.drawLine(middleX, y_screen, rightX, y_screen);
            String number = ""+Utils.formatFloat(val);
            int stringWidth = fm.stringWidth(number);
            int stringHeight = fm.getHeight();
            g.drawString(number, middleX-stringWidth-5, y_screen + stringHeight/2);            
        }        
    }
    */        
  
    
    
}
