package gui.color;

import javax.swing.*;

import util.MathUtils;
import util.StringUtils;


import java.awt.*;

public class GradientColorPanel extends JComponent {
                                
    /**
	 * 
	 */
	private static final long serialVersionUID = 2403836861932761083L;
	private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 20;
            
    private double mMinVal;
    private double mMaxVal;        
    private Color mColor1;
    private Color mColor2;
    private ColorTable mColorTable;
    
    public static void main(String[] args) {
                
        JComponent colorPanel  = new GradientColorPanel(Color.RED, Color.GREEN);         
        
        JFrame frame = new JFrame("Drawing with a Gradient Color");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(colorPanel, BorderLayout.CENTER);
        frame.pack();        
        frame.setVisible(true);               
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    public void setRange(double pMinVal, double pMaxVal) {
        mMinVal = pMinVal;
        mMaxVal = pMaxVal;
        repaint();
    }
  
    public GradientColorPanel(Color pColor1,
                              Color pColor2) {
        mColor1 = pColor1;
        mColor2 = pColor2;      
        mColorTable = null;
        mMinVal = 0;
        mMaxVal = 1;
    }         
  
   public void paintComponent(Graphics g){
      Graphics2D g2d = (Graphics2D)g;
      Color c1 = mColor1;
      Color c2 = mColor2;
      int w = getWidth();
      int h = getHeight();
      
      if (mColorTable == null || mColorTable.getNumColors() != w) {
          mColorTable = new DefaultColorTable(w, c1, c2);
      }
       
      for (int x=0; x<w; x++) {
          g.setColor(mColorTable.getColor(x));
          g2d.fillRect(x,0,x+1,h);                                    
      }
      
      int nTics = 11;
      int maxTic = nTics-1;
      
      // min tic: 0 max tic: nTics-1
      // min scrX: 0 max scrX: w
      g.setColor(Color.black);
      FontMetrics fm = g.getFontMetrics();
      for (double tic=0; tic <= maxTic; tic++) {                   
          int screenX = (int)(tic * (w-1) / maxTic);
          double x = mMinVal + tic / maxTic * (mMaxVal-mMinVal);
          MathUtils.weightedAvg(nTics, w, tic, h);//
          String xRep = StringUtils.formatFloat(x, 2);
          g.drawLine(screenX, h-4, screenX, h);
          int stringWidth = fm.stringWidth(xRep);
          if (tic == 0) {
              screenX += (stringWidth/2+1);
          }
          if (tic == maxTic) {
              screenX -= (stringWidth/2+1);
          }
          g.drawString(xRep, screenX-stringWidth/2, h-6);
        }
      
  }
  

}
