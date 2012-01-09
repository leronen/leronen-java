package gui.table;

import util.*;
import util.dbg.*;


import java.util.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class TableUtils {

    public static int[] getSelectedIndices(JTable pTable) {
        ListSelectionModel selectionModel = pTable.getSelectionModel();
        if (selectionModel == null) {
            return new int[0];
        }
        else {
            return getSelectedIndices(selectionModel);
        }           
    }
    
    public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
        if (!(table.getParent() instanceof JViewport)) {
            Logger.warning("Could not scroll to visible");        
            return;
        }
        JViewport viewport = (JViewport)table.getParent();
    
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
    
        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();
    
        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);
    
        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    
    public static void setSelectedIndices(DefaultListSelectionModel pModel, int[] pIndices) {                                
        if (pIndices.length == 0) {
            pModel.clearSelection();            
        }        
        else {
            pModel.setSelectionInterval(pIndices[0],pIndices[0]);
            for (int i=1; i<pIndices.length; i++) {                
                pModel.addSelectionInterval(pIndices[i],pIndices[i]);
            }
        }                
    }
    
    public static int[] getSelectedIndices(ListSelectionModel pSelectionModel) {                
        ArrayList result = new ArrayList();
        int minSelectedIndex = pSelectionModel.getMinSelectionIndex();
        int maxSelectedIndex = pSelectionModel.getMaxSelectionIndex();
        if (minSelectedIndex != -1 && maxSelectedIndex != -1) {
            for (int i=minSelectedIndex; i<=maxSelectedIndex; i++) {
                if (pSelectionModel.isSelectedIndex(i)) {
                    result.add(new Integer(i));                        
                }
            }
        }
        return ConversionUtils.integerCollectionToIntArray(result);          
    }   
    
    public static void maximizeColumn(JTable pTable, int pColInd) {
        TableColumnModel columnModel = pTable.getColumnModel();        
        for (int i=0; i<columnModel.getColumnCount(); i++) {            
            TableColumn column = columnModel.getColumn(i);
            if (i == pColInd) {
                column.setPreferredWidth(column.getMaxWidth());
            }
            else {
                column.setPreferredWidth(column.getMinWidth());
            }
        }          
        pTable.doLayout();     
    }
    
    
    public static void addMouseListenerForColumn(JTable pTable, int pColInd, MouseListener pListener) {
        JTableHeader header = pTable.getTableHeader();
        header.addMouseListener(new ColumnMouseListenerAdapter(header, pListener, pColInd));                
    }
    
    private static class ColumnMouseListenerAdapter implements MouseListener {
        
        private int mCol;                
        private MouseListener mActualListener;
        private JTableHeader mTableHeader;

        private ColumnMouseListenerAdapter(JTableHeader pTableHeader, MouseListener pActualListener, int pColInd) {
            mCol = pColInd;
            mActualListener = pActualListener;
            mTableHeader = pTableHeader;                 
        }                        
        
        public void mouseClicked(MouseEvent e) { 
            if (isOurCol(e)) {
                mActualListener.mouseClicked(e);    
            }
        }
        public void mouseEntered(MouseEvent e)  { 
            if (isOurCol(e)) {
                mActualListener.mouseEntered(e);    
            }
        }
        public void mouseExited(MouseEvent e)  { 
            if (isOurCol(e)) {
                mActualListener.mouseExited(e);    
            }
        }
        public void mousePressed(MouseEvent e)  { 
            if (isOurCol(e)) {
                mActualListener.mousePressed(e);    
            }
        }
        public void mouseReleased(MouseEvent e)  { 
            if (isOurCol(e)) {
                mActualListener.mouseReleased(e);    
            }
        }                
        
        public boolean isOurCol(MouseEvent e) {
            // dbgMsg("isOurCol: "+e);
            return mTableHeader.columnAtPoint(e.getPoint()) == mCol;    
        }
                        
    }   

}
