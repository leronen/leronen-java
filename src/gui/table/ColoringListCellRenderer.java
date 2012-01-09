package gui.table;

import gui.color.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;


public class ColoringListCellRenderer implements ListCellRenderer {    
    
    private IColoring mColoring;
    
    public ColoringListCellRenderer(IColoring pColoring) {
        mColoring = pColoring;
    }
        
     public Component getListCellRendererComponent(JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {
       
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setFont(new Font("Dialog", Font.PLAIN, 12));
        if (value != null) {
            label.setText(value.toString());
        }

        // set cell's foreground to default cell foreground color
        label.setForeground(list.getForeground());
         
         Color color = mColoring.getColor(value);
         if (isSelected) {                          
             color = ColorUtils.average(color, list.getSelectionBackground());
             label.setBorder(new LineBorder(Color.black, 1));
         }                 
         label.setBackground(color);
        
         // draw border on cell if it has focus
         if (cellHasFocus) {
             label.setBorder(new LineBorder(Color.black, 2));
         }
        
         // position cell text at center
         // label.setHorizontalAlignment(SwingConstants.CENTER);
        
         return label;
    }
}

