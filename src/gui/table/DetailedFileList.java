package gui.table;

import gui.*;

import util.*;

import java.io.*;
import java.util.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class DetailedFileList extends AbstractTableModel implements CellColorCommander, ListDataListener {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3990648399523859895L;
	public static final String ID_NAME = "NAME";
    public static final String ID_DIR = "DIR";
    public static final String ID_MODIFIED = "MODIFIED";
    public static final String ID_SIZE = "SIZE";    
    
    private ListModel mData;
    // private File[] mData;    
    private String[] mFields;
        
    private FileColorCommander mFileColorCommander;        
        
    public static final String[] DEFAULT_FIELDS = {
        ID_NAME,
        // ID_DIR,
        ID_MODIFIED,
        ID_SIZE
    };
    
    public static final String[] ALL_FIELDS = {
        ID_NAME,
        ID_DIR,
        ID_MODIFIED,
        ID_SIZE
    };
                                            
    public DetailedFileList() {
        mData = new DefaultListModel();
        mFields = DEFAULT_FIELDS;
    }
    
    public DetailedFileList(ListModel pModel) {
        mData = pModel;
        mFields = DEFAULT_FIELDS;
        pModel.addListDataListener(this);
    }

    public DetailedFileList(ListModel pModel, String[] pFieldsToDisplay) {
        mData = pModel;
        mFields = pFieldsToDisplay;
        pModel.addListDataListener(this);
    }    
    
    public DetailedFileList(String[] pFileNames) {
        this(pFileNames,
             DEFAULT_FIELDS);
    }
    
    public DetailedFileList(Collection pFileNames) {
        this((String[])(new ArrayList(pFileNames)).toArray(new String[pFileNames.size()]),
             DEFAULT_FIELDS);
    }
                                
    public DetailedFileList(Collection pFileNames, String[] pFieldsToDisplay) {
        this((String[])(new ArrayList(pFileNames)).toArray(new String[pFileNames.size()]),
             pFieldsToDisplay);
    }
    
    public DetailedFileList(File[] pFiles, String[] pFieldsToDisplay) {
        setData(pFiles);
        mFields = pFieldsToDisplay;
    }
    
    public DetailedFileList(File[] pFiles) {
        setData(pFiles);
        mFields = DEFAULT_FIELDS;
    }        
    
    public DetailedFileList(String[] pFileNames, String[] pFieldsToDisplay) {        
        setData(pFileNames);
        mFields = pFieldsToDisplay;
    }
   
    public void setData(File[] pFiles) {        
        mData = new DefaultListModel();
        for (int i=0; i<pFiles.length; i++) {
            if (pFiles[i].exists()) {
                ((DefaultListModel)mData).addElement(pFiles[i]);
            }
        }
        fireTableDataChanged();
    }
        
    public void setFileColorCommander(FileColorCommander pFileColorCommander) {
        mFileColorCommander = pFileColorCommander;
    }        
        
    public void setData(String[] pFileNames) {
        /*
        mData = new File[pFileNames.length];
        for (int i=0; i<pFileNames.length; i++) {
            mData[i] = new File(pFileNames[i]);    
        }
        */
        mData = new DefaultListModel();
        for (int i=0; i<pFileNames.length; i++) {
            File file = new File(pFileNames[i]);
            if (file.exists()) {
                ((DefaultListModel)mData).addElement(file);
            }                
        }        
        fireTableDataChanged();
    }
    
    public File[] getFiles() {
        List asList = ConversionUtils.listModelToArrayList(mData);
        return (File[])ConversionUtils.collectionToArray(asList, File.class);        
    }
       
   
    ////////////////////////////////////////////////////////////
    // implementation for interface TableModel
    ////////////////////////////////////////////////////////////            
    public int getRowCount() {
        return mData.getSize();
    }
    public int getColumnCount() {
        return mFields.length;
    }
    public Object getValueAt(int row, int column) {
        return getField(row, mFields[column]);    
    }
    public String getColumnName(int column) {
        return mFields[column];        
    }
            
    public File getFile(int pRow) { 
        // return mData[pRow];
        return (File)mData.getElementAt(pRow);

    }

    
    public File[] getFiles(int[] pRows) { 
        List fileList = ConversionUtils.listModelToArrayList(mData);
        List desiredFiles = CollectionUtils.selectObjects(fileList, pRows);
        return (File[])ConversionUtils.collectionToArray(desiredFiles, File.class);
    }                
            
    ////////////////////////////////////////////////////////////
    // TableModel implementation ends. 
    ////////////////////////////////////////////////////////////

            
    public Color getCellColor(int pRow, int pCol) {
        File file = getFile(pRow);
        
        if (mFileColorCommander != null) {
            return mFileColorCommander.getColor(file);
        }
        else {
            // default behauviour            
            if (getFile(pRow).exists()) {
                return Color.yellow;
            }
            else {
                return Color.red;
            }
        }            
    }        
            
    private String getField(int pRow, String pField) {
        if (pField.equals(ID_NAME)) {
            return getFile(pRow).getName();
        }            
        else if (pField.equals(ID_DIR)) {
            return getFile(pRow).getParent();
        }
        else if (pField.equals(ID_SIZE)) {
            return StringUtils.formatLong(getFile(pRow).length());
        }
        else if (pField.equals(ID_MODIFIED)) {
            // Calendar calendar = Calendar.getInstance();
            long millis = getFile(pRow).lastModified();
            // Date date = new Date(millis);
            // DateFormat dateFormat = DateFormat.getInstance();
            // DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);            
            // return dateFormat.format(date);
            return DateUtils.formatDate(millis);
                                         
            // return ""+getFile(pRow).lastModified();         
        }
        else {
            throw new RuntimeException("Unknown field: "+pField);
        }
    }
    
    
    //////////////////////////////////////////////////////
    // ListDataListener implementation...            
    public void contentsChanged(ListDataEvent e) {
        fireTableDataChanged();                            
    }
    public void intervalAdded(ListDataEvent e) { 
        fireTableRowsInserted(e.getIndex0(), e.getIndex1());
    }          
    public void intervalRemoved(ListDataEvent e) { 
        fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
    }
}
