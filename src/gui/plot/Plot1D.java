package gui.plot;

import java.awt.*;
import util.*;

public class Plot1D extends Plot {

    protected static final Color COLOR_DATA_BACKGROUND = Color.white;
    protected static final Color COLOR_DATA_FOREGROUND = Color.blue;    
    protected static final int HEIGHT_DATA_AREA_PIXELS = 400;

    protected Data1D mData;
    protected int mDataAreaWidth_pixels;

    public Plot1D(boolean pOpaque) {
        setOpaque(pOpaque);
        setBackground(COLOR_PAD);
    }
    
    public Plot1D(int pPixels_cell, boolean pOpaque) {
        this(pOpaque);
        mPixels_cell = pPixels_cell;
    }

    public void setData(Data pData) {
        mData = (Data1D)pData;
        mDataAreaWidth_pixels = mData.getNumXIndices()*mPixels_cell;
        int prefW = mDataAreaWidth_pixels + PAD_HORIZ + RULER_HEIGHT_Y;
        int prefH = HEIGHT_DATA_AREA_PIXELS + RULER_HEIGHT_X + PAD_VERT;
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
        int x,height;
        g.setColor(COLOR_DATA_BACKGROUND);
        g.fillRect(0, 0, mDataAreaWidth_pixels, HEIGHT_DATA_AREA_PIXELS);
        g.setColor(COLOR_DATA_FOREGROUND);                            
        for (x = 0; x < mData.getNumXIndices(); x++) {                        
                double freq = mData.getValueAt(x);
                height = (int)(freq/mData.getMaxVal()*HEIGHT_DATA_AREA_PIXELS);
                g.fillRect(x*mPixels_cell, HEIGHT_DATA_AREA_PIXELS-1-height,
                           mPixels_cell, height);
                
        }

        int rulerLeft = 0;
        int rulerTop = HEIGHT_DATA_AREA_PIXELS;
        paintXRuler(g, mData, rulerLeft, rulerTop);        

    }
    
    protected void paintYRuler(Graphics g) {
        if (mData==null) {
            return;
        }
        // draw y ruler
        
        // ruler bounds        
        int w = RULER_HEIGHT_Y;
        int h = HEIGHT_DATA_AREA_PIXELS + PAD_BOTTOM;
        int leftX = -w;
        int middleX = leftX + RULER_HEIGHT_LOWER_Y;
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
            // kludge...
            int numDecimalsNeeded = 2;            
            if (maxval <0.001d) {
                numDecimalsNeeded = 5;
            }
            else if (maxval <0.01d) {
                numDecimalsNeeded = 4;
            }
            else if (maxval <0.1d) {
                numDecimalsNeeded = 3;
            }
            String number = StringUtils.formatFloat(val, numDecimalsNeeded);
            int stringWidth = fm.stringWidth(number);
            int stringHeight = fm.getHeight();
            g.drawString(number, middleX-stringWidth-5, y_screen + stringHeight/2);            
        }        
    }

    protected int calculateHeight(double pMinVal, double pMaxVal, double pVal) {
        double range = pMaxVal-pMinVal;
        double offset = pVal-pMinVal;
        return (int)(offset/range*HEIGHT_DATA_AREA_PIXELS);        
    }
    
}