package gui.list;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.*;



/**
 * 
 * Adapts a java.util.List to ListModel.
 * 
 * Actually, it makes no sense attempting to adapt an arbitrary list
 * that will have elements added or removed. All subsequent changes that 
 * modify the number of elements in the list
 * must be done using this adapter; otherwise it is next to impossible to keep
 * the adapter up-to-date changes in the list...
 * However, changing elements and then calling fireContentsChanged() 
 * is perfectly acceptable, but probably a rare case...
 */
public final class ListModelAdapter<T> extends AbstractListModel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3626340239150604897L;
	private int mLastNotifiedSize = 0;
    private java.util.List<T> mData;    
    
    public ListModelAdapter(List<T> pData) {        
        mData = pData;
        mLastNotifiedSize = mData.size();
    }    
    
    public void setData(List<T> pData) {
       mData = pData;
       
       fireIntervalRemoved(this, 0, mLastNotifiedSize-1);       
       fireIntervalAdded(this, 0, mData.size()-1);
       
       mLastNotifiedSize = pData.size();       
    }
    
    /** Get data as an unmodifiable list */
    public List<T> getData() {
        return Collections.unmodifiableList(mData);
    }
    
    /** Clear the underlying list */
    public void clear() {
        if (mData.size() == 0) {
            // no action needed
        }
        else {
            mData.clear();
            fireIntervalRemoved(this, 0, mLastNotifiedSize-1);            
        }
        
        mLastNotifiedSize = 0;
    }
    
    public void add(T p) {
        mData.add(p);
        int start = mLastNotifiedSize+1;
        int end = mLastNotifiedSize+1;
        // Logger.info("Firing interval added: "+start+", "+end);
        fireIntervalAdded(this, start, end);
        mLastNotifiedSize = mData.size();
    }

    public void addAll(Collection<T> pCollection) {
        mData.addAll(pCollection);
        int start = mLastNotifiedSize+1;
        int end = mData.size()-1;
        // Logger.info("Firing interval added: "+start+", "+end);
        fireIntervalAdded(this, start, end);
        mLastNotifiedSize = mData.size();
    }    
    
    public Object getElementAt(int index) {
        return mData.get(index);    
    }
        
    public int getSize() {
        return mData.size();    
    }   
    
//    /** Contents changed and list size changed. TODO: get rid of this! */
//    public void fireContentsChanged_heavy() {
//        Logger.info("Firing contents changed (heavy), mLastNotifiedSize=="+mLastNotifiedSize+", data.size()=="+mData.size());
//        if (mLastNotifiedSize > 0) {            
//            fireIntervalRemoved(this, 0, mLastNotifiedSize-1);
//        }
//        if (mData.size() > 0) {
//            fireIntervalAdded(this, 0, mData.size()-1);
//        }
//
//        mLastNotifiedSize = mData.size();
//    }
    
    /**
     * Call this after modifying the underlying list. 
     * Be warned that this is only guaranteed to work if the list size has 
     * not changed!     
     */
    public void fireContentsChanged() {
        if (mData.size() != mLastNotifiedSize) {
            throw new RuntimeException("List model is not in sync with wrapped data!");
        }
        fireContentsChanged(this, 0, getSize()-1);
        
    }

}
