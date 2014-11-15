package gui.list;

import util.*;
import util.collections.*;
import util.converter.*;
import util.dbg.*;

import javax.swing.*;

import java.util.*;
import java.io.*;

public class RecentObjectsListModel extends AbstractListModel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 495277165867945695L;
	private LinkedList mData;             
    private int mMaxItems;
    
    private File mFile;
    private Converter mElementToStringConverter;
    private Converter mStringToElementConverter;
        
    private HashSet mLists = new HashSet();
    
    private boolean mAddingItems = false;        
        
    public void registerList(JList pList) {
        mLists.add(pList);    
    }
    
    public RecentObjectsListModel(int pMaxItems) {
        this(null, null, null, pMaxItems);        
    }                                     
                                     
    public RecentObjectsListModel(File pFile, 
                                  Converter pElementToStringConverter,
                                  Converter pStringToElementConverter,                                                
                                  int pMaxItems) {
        mMaxItems = pMaxItems;
        mData = new LinkedList();
        mFile = pFile;            
        mElementToStringConverter = pElementToStringConverter;
        mStringToElementConverter = pStringToElementConverter;
        loadIfNeeded();        
    }
    
    public void addItems(Object[] pItems) {
        addItems(Arrays.asList(pItems));        
    }
    
    public void addItem(Object pItem) {
        addItems(Collections.singletonList(pItem));                         
    }        
        
    private MultiMap storeSelectedValuesForEachListener() {
        MultiMap result = new MultiMap();
        Iterator lists = mLists.iterator();
        while(lists.hasNext()) {
        	
            JList list = (JList)lists.next();            
			@SuppressWarnings("deprecation")
            Object[] selectedValues = list.getSelectedValues();
            result.putMultiple(list, Arrays.asList(selectedValues));
        }                        
        return result;
    }                      
                      
    public boolean isAddingItems() {
        return mAddingItems;
    }                      
    
    private void resetSelectedValuesForEachListener(MultiMap pStoredSelections) {                
        Iterator lists = mLists.iterator();
        while(lists.hasNext()) {                             
            JList list = (JList)lists.next();
            Set selectedValues = pStoredSelections.get(list);
            if (selectedValues != null) {
                List asList = ConversionUtils.listModelToArrayList(this);
                int[] selectedIndices = CollectionUtils.getIndicesOfContainedObjects(asList, selectedValues);
                list.setSelectedIndices(selectedIndices);
            }                                             
        }        
    }        
        
    public void addItems(Collection pItems) {
        mAddingItems = true;
        MultiMap selections = storeSelectedValuesForEachListener();
        // Logger.info("Selections: "+StringUtils.MultiMapToString(selections));
        int oldSize = mData.size();                
        mData.removeAll(pItems);
        mData.addAll(pItems);                 
        trim();
        int newSize = mData.size();
        fireContentsChanged(this, 0, oldSize-1);
        if (oldSize < newSize) {
            // note: should never get smaller; stays the same once mMaxItems is reached...
            fireIntervalAdded(this, oldSize, newSize-1);
        }
        resetSelectedValuesForEachListener(selections);                
        saveIfNeeded();
        mAddingItems = false;                
    }
            
    
    private void saveIfNeeded() {        
        if (mFile != null) {
            try {                        
                File parentDir = mFile.getParentFile();
                parentDir.mkdirs();                                                                            
                java.util.List stringList = ConversionUtils.convert(mData, mElementToStringConverter);
                String[] lines = ConversionUtils.stringCollectionToArray(stringList);
                IOUtils.writeToFile(mFile, lines);
            }            
            catch (Exception e) {
                Logger.warning("Failed saving recent objects!");
                e.printStackTrace();
            }                                          
        }
    }
    
    private void loadIfNeeded() {        
        if (mFile != null && mFile.exists()) {
            try {                                          
                String[] lines = IOUtils.readLineArray(mFile);                                                                
                java.util.List objectList = ConversionUtils.convert(Arrays.asList(lines), mStringToElementConverter);
                objectList = new ArrayList(objectList);                
                CollectionUtils.removeNullObjects(objectList);
                mData = new LinkedList(objectList);
            }
            catch (Exception e) {
                Logger.warning("Failed loading recent objects!");
                e.printStackTrace();
            }                                                                       
        }
    }
    
    private void trim() {
        while(mData.size()>mMaxItems) {
            mData.removeFirst();    
        }
    }        
    
    public Object getElementAt(int index) {
        // note that we give the elements in reverse order:
        return mData.get(mData.size()-1-index);    
    }
    
    
    public int getSize() {
        return mData.size();    
    }                           
    
    
}
