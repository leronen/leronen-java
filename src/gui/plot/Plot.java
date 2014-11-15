package gui.plot;

import javax.swing.*;

import util.dbg.Logger;

import java.awt.*;

public abstract class Plot extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5500958530784787691L;

	public static final int DEFAULT_PIXELS_CELL = 3;

    protected boolean mOpaqueRuler = true; 
    
    // colors
    protected static final Color COLOR_PAD = new Color(100,100,100);
//     protected static final Color COLOR_BACKGROUND = new Color(100,100,100);
    protected static final Color COLOR_RULER_BACKGROUND= Color.lightGray;
    protected static final Color COLOR_RULER_FOREGROUND= Color.black;


    protected static final Color COLOR_MIN= Color.white;
    protected static final Color COLOR_MAX= Color.black;
    protected static final Color COLOR_UNKNOWN = Color.blue;

    // pads
    protected static final int PAD_LEFT = 10;
    protected static final int PAD_RIGHT = 10;
    protected static final int PAD_HORIZ = PAD_LEFT + PAD_RIGHT;
    protected static final int PAD_TOP = 10;
    protected static final int PAD_BOTTOM = 10;
    protected static final int PAD_VERT = PAD_TOP + PAD_BOTTOM;

    // ruler dimensions
    protected static final int RULER_HEIGHT_UPPER = 10;
    protected static final int RULER_HEIGHT_LOWER_X = 20;
    protected static final int RULER_HEIGHT_LOWER_Y = 50;
    // protected static final int RULER_HEIGHT = RULER_HEIGHT_LOWER + RULER_HEIGHT_UPPER;
    
    protected static final int RULER_HEIGHT_X = RULER_HEIGHT_LOWER_X + RULER_HEIGHT_UPPER;
    protected static final int RULER_HEIGHT_Y = RULER_HEIGHT_LOWER_Y + RULER_HEIGHT_UPPER;
    protected static final int RULER_STEP = 20;

    ////////////////////////////
    // instance data
    /////////////////////////
    protected int mPixels_cell;

    protected boolean mPaintMaxValueInRed;

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.translate(PAD_LEFT+RULER_HEIGHT_Y, PAD_TOP);
    }

    public abstract void setData(Data pData);

    public abstract Data getData();

    public void setPaintMaxValueInRed(boolean pFlag) {
        mPaintMaxValueInRed = pFlag;
    }

    protected void paintXRuler(Graphics g, Data pData, int pX, int pY) {
        // draw x ruler
        // ruler bounds
        int leftX = pX;
        int upperY = pY;
        int middleY = upperY+RULER_HEIGHT_UPPER;
        int lowerY = middleY+RULER_HEIGHT_LOWER_X;
        int w = pData.getNumXIndices()*mPixels_cell;
        int h = RULER_HEIGHT_X;
        
        Logger.info("Painting x ruler with params: "+
                    "leftX"+leftX+",\n"+
                    "upperY"+upperY+",\n"+
                    "middleY"+middleY+",\n"+
                    "lowerY"+lowerY+",\n"+
                    "w"+w+",\n"+
                    "h"+h);
        
        // upper line        
        g.setColor(COLOR_RULER_FOREGROUND);
        g.drawLine(leftX, upperY, leftX+w, upperY);
         // background
        if (mOpaqueRuler) {
            g.setColor(COLOR_RULER_BACKGROUND);
            g.fillRect(leftX-PAD_LEFT, upperY+1, w+PAD_HORIZ, h-1);
        }
        /*
        // middle line
        g.setColor(COLOR_RULER_FOREGROUND);
        g.drawLine(leftX, middleY, leftX+w, middleY);
        */
        // scale
        FontMetrics fm = getFontMetrics(g.getFont());
        g.setColor(COLOR_RULER_FOREGROUND);
        
        
        int LOCAL_RULER_STEP = 30;
        int space = LOCAL_RULER_STEP;                
        for(int x=0; x<pData.getNumXIndices(); x++) {
            Logger.info("x: "+x);
            space += mPixels_cell;
            if (space>LOCAL_RULER_STEP) {                        
                int screenX = (int)((x+0.5)*mPixels_cell);
                g.drawLine(screenX, upperY, screenX, middleY);
                String number = ""+pData.getXIndexAt(x);
                int stringWidth = fm.stringWidth(number);
                g.drawString(number, screenX-stringWidth/2, lowerY);
                space = 0;
            }
        }
    }


}