package gui.table;

import javax.swing.table.*;

import util.*;
import util.collections.*;

import java.util.*;

public class MapTableModel extends DefaultTableModel {        

    /**
	 * 
	 */
	private static final long serialVersionUID = 6133180562115577777L;
	private ArrayList mKeys;        
    private LinkedHashMap mMap;
    
    public MapTableModel() {
        super();
        mMap = new LinkedHashMap();
        mKeys = new ArrayList();
    }
    
    public MapTableModel(Map pMap) {
        super();
        putAll(pMap);
    }
                
    public void put(Object pKey, Object pVal) {
        if (mMap.containsKey(pKey)) {            
            mMap.put(pKey, pVal);
            int row = mKeys.indexOf(pKey);
            fireTableRowsUpdated(row, row);
        }
        else {
            // new key
            mMap.put(pKey, pVal);
            mKeys.add(pKey);            
            int row = mKeys.size()-1;
            fireTableRowsInserted(row, row);
        }
    }

    public void putAll(Map pMap) {
        CollectionUtils.forEach(pMap, new MapEntryOperation() {
            public void doOperation(Object pKey, Object pVal) {
                put(pKey, pVal);       
            }
        });                                                   
    }            
    
    public int getRowCount() {
        // avoid common pitfall: superclass constructor is rude enough to query this before we even have 
        // change to set our data fields!
        if (mMap==null) {
            return 0;
        }
        else {
            return mMap.size();
        }                    
    }

    public int getColumnCount() {
        return 2;
    }
        
    public Object getValueAt(int row, int column) {        
        if (column==0) {            
            return mKeys.get(row);
        }
        else if (column == 1)  {
            Object key = mKeys.get(row);
            return mMap.get(key);
        }
        else {
            throw new RuntimeException("Invalid col: "+column);
        }
    }
}
