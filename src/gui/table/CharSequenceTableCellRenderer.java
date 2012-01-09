package gui.table;

import gui.color.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/** renders char sequence with colors in HappyColorTable; a '0' is color 0, a '1' is color 1, etc... */
public class CharSequenceTableCellRenderer implements TableCellRenderer {

     private RenderingComponent mRenderingComponent = new RenderingComponent();
     
     private Object mValue;
     private boolean mSelected;
     private JTable mTable;
     
     private ColorTable mColorTable = new HappyColorTable();
        
     public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, 
                                                   int column) {                                                       
         mValue = value;
         mSelected = isSelected;
         mTable = table;
         
         return mRenderingComponent;
    }
    
    private class RenderingComponent extends JComponent {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle bounds = g.getClipBounds();            
            // dbgMsg("Rendering cell, bounds="+bounds);
            // dbgMsg("value:"+mValue);
            if (mValue instanceof CharSequence) {
                CharSequence alleles = (CharSequence)mValue;
                int numMarkers = alleles.length();
                int markerWidth_pixels = (int) ((bounds.getWidth())/numMarkers);
                int height_pixels = (int)bounds.getHeight();
                int startX;
                for (int i=0; i<numMarkers; i++) {   
                    char alleleChar = alleles.charAt(i);
                    Color color;                    
                    // actual allele, vi hoppas
                    color = mColorTable.getColor(alleleChar-'0');
                    if (mSelected) {
                        color = ColorUtils.average(color, mTable.getSelectionBackground());
                    }                                                                                                     
                    g.setColor(color);
                    startX = i*markerWidth_pixels;    
                    // note that the y coordinates in the next line are seemingly insane, kludgeed that way because of
                    // apparent JTable graphics bug that leaves ugly traces in the table otherwise 
                    g.fillRect(startX, -8*height_pixels, markerWidth_pixels, height_pixels*16);
                    g.setColor(Color.black);
                    g.drawLine(startX, -8*height_pixels, startX, height_pixels*16);
                }
            }                        
        }        
    }
    
               

}
