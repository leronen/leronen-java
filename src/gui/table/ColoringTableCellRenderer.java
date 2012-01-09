package gui.table;

import gui.color.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;



public class ColoringTableCellRenderer implements TableCellRenderer {    
    
    private CellColorCommander mColoring;
    
    public ColoringTableCellRenderer(CellColorCommander pColoring) {
        mColoring = pColoring;
    }
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, 
                                                   int column) {  
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setFont(new Font("Dialog", Font.PLAIN, 12));
        if (value != null) {
            label.setText(value.toString());
        }

        // set cell's foreground to default cell foreground color
        label.setForeground(table.getForeground());
         
         Color color = mColoring.getCellColor(row, column);
         
         if (isSelected) {                          
             color = ColorUtils.average(color, table.getSelectionBackground());
             // label.setBorder(new LineBorder(Color.black, 2));
         }
                          
         label.setBackground(color);         
        
         // draw border on cell if it has focus
         
         if (hasFocus) {
             label.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
         }         
        
         // position cell text at center
         label.setHorizontalAlignment(SwingConstants.CENTER);
        
         return label;
    }
}

