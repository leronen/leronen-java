package gui.list;

import javax.swing.*;

import java.util.*;

public final class BasicListModel<T> extends AbstractListModel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 9169478994475943307L;
	private ArrayList<T> mData;
    private int mMaxNumLines;
    
    public BasicListModel(int pMaxNumLines) {
        mMaxNumLines = pMaxNumLines;
        mData = new ArrayList();            
    }    
    
    private void clearCache() {
        int oldNumLines = mData.size();        
        int numLinesToPreserve = mMaxNumLines/2;
        int numLinesToDelete = oldNumLines-numLinesToPreserve;        
        int firstLineToPreserve = numLinesToDelete;        
        mData = new ArrayList(mData.subList(firstLineToPreserve, oldNumLines));
        fireIntervalRemoved(this, 0, firstLineToPreserve-1);
    }    
    
    public void addMessages(List pMessages) {
        if (mData.size()>=mMaxNumLines) {
            clearCache();
        }
        int numMessages = pMessages.size();
        if (numMessages == 0) {
            return;
        }
        int startInd = mData.size();
        int endInd = startInd+numMessages-1;
        mData.addAll(pMessages);         
        fireIntervalAdded(this, startInd, endInd);
    }
    
    public void add(T pMsg) {
        if (mData.size()>=mMaxNumLines) {
            clearCache();
        }
        mData.add(pMsg);
        int indexOfNew = mData.size();        
        fireIntervalAdded(this, indexOfNew, indexOfNew);
    }
    
    public Object getElementAt(int index) {
        return mData.get(index);    
    }
    
    
    public int getSize() {
        return mData.size();    
    }                           

   }
