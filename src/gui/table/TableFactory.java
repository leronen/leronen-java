package gui.table;

import javax.swing.*;
import javax.swing.table.*;

public class TableFactory {
    
    public static JTable createTableWithCustomRenderer(TableModel pTableModel, TableCellRenderer pRenderer) {        
        JTable table = new JTable(pTableModel);
        int numColumns = table.getColumnModel().getColumnCount();
        for (int i = 0; i < numColumns; i++) {                
            TableColumn tableColumn = table.getColumnModel().getColumn(i);                
            tableColumn.setCellRenderer(pRenderer);
        }
        return table;        
    }           
    
    public static JTable createTableWithCustomColoring(TableModel pTableModel, CellColorCommander pColoring) {
        ColoringTableCellRenderer tableCellRenderer = new ColoringTableCellRenderer(pColoring);
        JTable table = new JTable(pTableModel);
        int numColumns = table.getColumnModel().getColumnCount();
        for (int i = 0; i < numColumns; i++) {                
            TableColumn tableColumn = table.getColumnModel().getColumn(i);                
            tableColumn.setCellRenderer(tableCellRenderer);
        }
        return table;        
    }           
    
}


// let's honor this mummy
// table.setShowGrid(false);



