package gui.table;

import util.converter.Converter;
import gui.color.*;

import java.awt.*;
import java.util.Set;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;


public class SubsetRenderer<T> implements TableCellRenderer {
    
     private RenderingComponent mRenderingComponent = new RenderingComponent();         
     private Object mValue;
     private boolean mSelected;
     private JTable mTable;
     public boolean mShowElemNumbers;                                
     private Converter<T, String> mFormatter;
     private Converter<T, Color> mColoring;
     private Set<T> mHighlight;
     
     public SubsetRenderer(Converter<T, String> pFormatter) {
         mFormatter = pFormatter;         
     }
     
     public Component getTableCellRendererComponent(JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row, 
                                                    int column) {
//         Logger.info("Getting rendering component at subsetrenderer!");
                                                               
         mValue = value;
         mTable = table;
         mSelected = isSelected;
         
         return mRenderingComponent;
    }
     
    public void setFormatter(Converter<T, String> pFormatter) {
        mFormatter = pFormatter;       
    }
    
    public void setColoring(Converter<T, Color> pColoring) {
        mColoring = pColoring;       
    }
    
    public void setHighlight(Set<T> pSet) {
        mHighlight = pSet;       
    }
    
    private class RenderingComponent extends JComponent {
        /**
		 * 
		 */
		private static final long serialVersionUID = -4725779467446561967L;

		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle bounds = g.getClipBounds();            
            if (mValue instanceof IndexedSubset) {
//                Logger.info("SubsetRenderer: rendering an indexed subset!)");
                IndexedSubset<T> subset = (IndexedSubset<T>)mValue;
                int superSetSize = subset.getSuperSetSize();
                int elemWidth_pixels = (int) ((bounds.getWidth())/superSetSize);
                int height_pixels = (int)bounds.getHeight();
                int startX;
                for (int i=0; i<superSetSize; i++) {   
                    T obj = subset.objectAt(i);                    
                    String text = null;
                    Color color;
                    
                    boolean highlight = false;
                    
                    if (obj != null) {
                        color = mColoring != null ? mColoring.convert(obj) : Color.RED;
                        if (mFormatter != null) {
                            text = mFormatter.convert(obj);
                        }
                        else {
                            text = obj.toString();
                        }
                        
                        highlight = mHighlight != null && mHighlight.contains(obj);
                    }
                    else {
                        color = Color.black;
//                        T superSetObj = subset.supersetObjectAt(i);
//                        highlight = mHighlight != null && mHighlight.contains(superSetObj);
                    }
                                
                    if (color == null) {
                        throw new RuntimeException("Failed getting color for index: "+i);
                    }                                                                                       
                    
                    // color is modified, if column is selected
                    if (mSelected) {
                        color = ColorUtils.average(color, mTable.getSelectionBackground());
                    }                                                                           
                    
                    startX = i*elemWidth_pixels;                    
//                    g.fillRect(startX, -8*height_pixels, elemWidth_pixels, 16*height_pixels);
                                        
                    
                    // draw a rectangle to represent a single item
                    if (highlight) {         
//                        Logger.info("Drawing highlight rect");                        
                        g.setColor(ColorUtils.LIGHT_BLUE);
                        g.fillRect(startX, -4*height_pixels, elemWidth_pixels, 8*height_pixels);                        
                        g.setColor(color);
                        g.fillRect(startX+3, 2, elemWidth_pixels-5, height_pixels-4);
                        
                        // g.drawRect(startX, -8*height_pixels, elemWidth_pixels, 16*height_pixels);
                    }
                    else {
                        g.setColor(color);
                        g.fillRect(startX, -4*height_pixels, elemWidth_pixels, 8*height_pixels);
                    }
                     
                    // draw vertical line as item separator 
                    g.setColor(Color.black);
                    g.drawLine(startX, 0, startX, height_pixels);
                    
                    if (elemWidth_pixels >= 6) {
                        if (mShowElemNumbers) {
                            g.drawString(""+i, startX+elemWidth_pixels/3, 12);                                
                        }
                        else {
                            if (text != null) {
                                g.drawString(text, startX+elemWidth_pixels/3, 12);
                            }
                        }
                    }
                    
                }
            }    
            else {
//                Logger.info("SubsetRenderer: value not an indexed subset, cannot render...");
            }
        }        
    }
    
     

}
