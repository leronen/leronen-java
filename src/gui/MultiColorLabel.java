package gui;

import util.*;
import util.collections.*;

import gui.color.*;

import java.util.*;

import java.awt.*;
import javax.swing.*;

public class MultiColorLabel extends JLabel {    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2475586080275165986L;
	private Distribution mColorDistribution;    
    private Color mBaseColor;
            
    public MultiColorLabel(Distribution pColorDistribution,
                           Color pBaseColor) {
        mColorDistribution = pColorDistribution;
        mBaseColor = pBaseColor;                               
                               
        setOpaque(false);                                                                                                                                                                   
    }        
    
    public void paintComponent(Graphics g) {                                                
        int y = 0;
        int w = getWidth();
        int h = getHeight();        
        
        Range totalRange = new Range(0, w);
        Map colorByRange = mColorDistribution.splitRange(totalRange);
        Iterator ranges = colorByRange.keySet().iterator();
        while(ranges.hasNext()) {
            Range range = (Range)ranges.next();
            Color color = (Color)colorByRange.get(range);
            if (mBaseColor != null) {
                color = ColorUtils.average(color, mBaseColor);
            }
            g.setColor(color);
            g.fillRect(range.start, y, range.length(), h);    
         }                    

        super.paintComponent(g);                                                                                     
    }
}

