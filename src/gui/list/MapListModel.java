package gui.list;

import javax.swing.*;

import java.util.*;

public final class MapListModel extends AbstractListModel {
    private ArrayList mKeys;
    private ArrayList mData;
    private Map mDataMap;
    private Map mKeyToIndexMap;
    
    public MapListModel() {
        mData = new ArrayList();
        mKeys = new ArrayList();
        mDataMap = new HashMap();
        mKeyToIndexMap = new HashMap();            
    }
    
    public void addMsg(String pKey, String pMsg) {
        if (mDataMap.containsKey(pKey)) {
            // old key
            Integer indexAsInteger = (Integer)mKeyToIndexMap.get(pKey);
            int index = indexAsInteger.intValue(); 
            
            mData.set(index, pMsg);                
            // mDataMap.put(pKey, pMsg);
            // mKeyToIndexMap.put(pKey, new Integer(index));
            fireContentsChanged(this, index, index);
        }
        else {
            // new key
            int index = mData.size();
            mData.add(pMsg);
            mKeys.add(pKey);
            // mDataMap.put(pKey, pMsg);
            mKeyToIndexMap.put(pKey, new Integer(index));
            fireIntervalAdded(this, index, index);                    
        }                       
    }
    
    public Object getElementAt(int index) {
        return mKeys.get(index)+": "+mData.get(index);    
    }        
    
    public int getSize() {
        return mData.size();    
    }                           

}

