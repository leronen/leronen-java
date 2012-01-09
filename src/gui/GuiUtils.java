package gui;

import gui.application.*;
import gui.form.LinkedHashMapFormData;
import gui.menu.IMenuManager;
import gui.menu.MenuManager_old;
import gui.view.*;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import util.CollectionUtils;
import util.ConversionUtils;
import util.dbg.Logger;

public class GuiUtils {
    
    private static IMain sMain;
    
    public static boolean ignoreEventDispatchThreadChecks;
    
    public static void showComponent(JComponent pComponent) {        
        showComponent(pComponent, "-");                        
    }

    public static void showComponent(JComponent pComponent, String pTitle) {        
        showComponent(pComponent, pTitle, false);
    }
 
    public static void showComponent(JComponent pComponent, String pTitle, boolean pShowInScrollPane) {
        ViewManager.getDefaultInstance().showView(pComponent, pTitle, pShowInScrollPane);        
    }

    public static void setMain(IMain pMain) {
        sMain = pMain;            
    }    
    
    public static void center(JFrame pFrame) {
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Dimension frameSize = pFrame.getSize();    
    	int extraX = (int)(screenSize.getWidth() - frameSize.getWidth());
    	int extraY = (int)(screenSize.getHeight() - frameSize.getHeight());    		
    	pFrame.setLocation(extraX/2, extraY/2);
    }
    
    public static Dimension sum(Dimension d1, Dimension d2) {
        return new Dimension(d1.width+d2.width, 
                             d1.height+d2.height);
    }
    
    public static IMain getMain() {
        return sMain;            
    }
    
    /** Needed due to the crappy api of JList */
    public static void setSelectedObjects(JList pList,
                                          Collection<?> pObjectsToSelect) {        
        List<?> list = ConversionUtils.listModelToArrayList(pList.getModel());
        int[] selectedIndices =
            CollectionUtils.getIndicesOfContainedObjects(list,
                                                         pObjectsToSelect);
        pList.setSelectedIndices(selectedIndices);                  
    }
    
    public static void ensureWeAreInEventDispatchThread() {
        
        if (ignoreEventDispatchThreadChecks) {
            return;
        }
        
        if (!(EventQueue.isDispatchThread())) {
            throw new RuntimeException("What the heck, some thread other than the\n"+
                                       "event dispatch thread is at malicious work here:\n"+
                                       Thread.currentThread());    
        }        
    }
    
    /** Non-blocking query from user, with default values */
    public static void queryFromUser(String pTitle, 
                                     Collection<String> pKeys, 
                                     UserQueryListener pListener, 
                                     Map pDefaultValues,
                                     IMenuManager pMenuManager) {
        UserQueryFormData formModel = new UserQueryFormData(pTitle, pKeys, pListener);                 
        if (pDefaultValues != null) {
            formModel.asMap().putAll(pDefaultValues);
        }
        SimpleFormView queryView = new SimpleFormView(formModel, pMenuManager, false);        
        queryView.addViewListener(formModel);
        ViewManager.getDefaultInstance().showView(queryView, false);
    }
    
    public static void warnIfNotInEventDispatchThread(String pText) {
        if (ignoreEventDispatchThreadChecks) {
            return;
        }
        
        if (!(EventQueue.isDispatchThread())) {
            Logger.warning("It seems that some other thread other than the\n"+
                           "event dispatch thread is at malicious work here:\n"+
                           Thread.currentThread() +
                           "action performed in suspicious thread: "+pText);
        }
        
        
    }    
    
    
    /** Purpose of this class is to start running test on remote machine when user clicks OK */                
    public static class UserQueryFormData extends LinkedHashMapFormData 
                                          implements gui.view.ViewListener {
                                                                            
        private String mTitle;
        private String[] mFields;        
        private UserQueryListener mListener;
        
        private UserQueryFormData(String pTitle, String[] pKeys, UserQueryListener pListener) {
            mTitle = pTitle;
            mFields = pKeys;            
            mListener = pListener;            
        }                                                                                                
        
        private UserQueryFormData(String pTitle, Collection<String> pKeys, UserQueryListener pListener) {
            mTitle = pTitle;
            mFields = ConversionUtils.stringCollectionToArray(pKeys);
                    
            mListener = pListener;            
        }
                        
        public void viewClosed(String pCmdId) {
            if (pCmdId.equals(MenuManager_old.CMD_ID_OK)) {
                mListener.onOk(this);                                                
            }
            else {
                mListener.onCancel();
            }
        }                    

        public String getFormName() {
            return mTitle;
        }                                                                                         
                               
        public String[] getAllKeys() {
            return mFields;
        }
                                                                           
                                
        public void writeToStream(PrintWriter pWriter) {
            throw new UnsupportedOperationException("Not possible");
        }                
    }
       
}



