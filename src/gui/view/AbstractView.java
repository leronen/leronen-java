package gui.view;

import gui.action.*;
import java.awt.*;
import java.util.*;

import util.dbg.Logger;

public abstract class AbstractView implements IView {
        
    private ViewContainer mViewContainer;
    private ViewManager mViewManager;
    
    private java.util.List mListeners = new ArrayList();
                                                                  
    public ViewContainer getViewContainer() {
        return mViewContainer;    
    }
    
    public void addViewListener(ViewListener pListener) {
        mListeners.add(pListener);
    }
    
    public void setViewContainer(ViewContainer pViewContainer) {
        mViewContainer = pViewContainer;
    }
    
    public void setViewManager(ViewManager pViewManager) {
        mViewManager = pViewManager;
    }
    
    public void close(String pCmdId) {
        Logger.info("AbstractView.close: "+getName());
        mViewManager.closeView(getName(), pCmdId);
    }
    
    public void afterClose(String pCmdId) {
        Logger.info("AbstractView.afterClose: "+getName());
        for (int i=0; i<mListeners.size(); i++) {
            ViewListener listener = (ViewListener)mListeners.get(i);
            listener.viewClosed(pCmdId);
        }
    }
    
    /** Override to provide post-post-total annihilation */
    public void annihilate() {
        // no-op by default
    }

    public void refresh() {
        // by default, do nothing!    
    }
    
    public boolean prefersToFillTheWholeScreen() {
        return false;
    }
    
    public Dimension getPreferredSize() {
        // no sense to define default size here
        return null;    
    }
    
    public CommandHandler getCommandHandler() {
        // no sense to define default command handler here
        return null;
    }
    
    /** Called by view manager after view if shown. */
    public void afterShownInitialization() {
        // by default, do nothing           
    }
    
    public void fireComponentChanged() {
        mViewContainer.viewComponentChanged();     
    }
    
}    
