package gui.view;

import gui.application.*;

import util.*;
import util.collections.*;
import util.dbg.Logger;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

public class ViewManager {

    private static ViewManager mDefaultInstance;
            
    /** Note that the reference to IMain may be null */
    private IMain mMain;
    
    private LinkedHashMap mViewContainersByViewName;
    
    private MultiMap mViewsByComponentClass;
    private MultiMap mViewsByClass;
    
    private FrameListener mFrameListener;
                
    private HashSet mMainLevelViews;
    
    private boolean mAutoExitAfterClosingLastView = true;
    
    public String summary() {
        return "Views by class: \n"+StringUtils.multiMapToString(mViewsByClass)+"\n"+
               "\n"+
               "Views by component class: \n"+StringUtils.multiMapToString(mViewsByComponentClass)+"\n"+
               "\n"+
               "Main level views:\n"+SU.toString(mMainLevelViews)+"\n"+
               "\n"+
               "View containers by view name:\n"+SU.toString(mViewContainersByViewName);
               
    }
    
    public ViewManager() {
        this(null);
    }
    
    public void setAutoExit(boolean pVal) {
        mAutoExitAfterClosingLastView = pVal;
    }
    
    public ViewManager(IMain pMain) {        
        mMain = pMain;
        mViewContainersByViewName = new LinkedHashMap();
        mViewsByComponentClass = new MultiMap();
        mViewsByClass = new MultiMap();
        mMainLevelViews = new HashSet();
        mFrameListener = new FrameListener();        
    }
    
    public IView showView(JComponent pComponent, String pName, boolean pShowInScrollPane) {
        IView view = new DefaultView(pName, pComponent);
        showView(view, pShowInScrollPane);
        return view;
    }
                
    public String[] getViewNames() {
        return (String[])ConversionUtils.collectionToArray(mViewContainersByViewName.keySet(), String.class);    
    }
    
    public void showMainLevelView(IView pView) {
        mMainLevelViews.add(pView);
        showView(pView, false);
    }
    
    /** This does the actual business of showing a view; all other versions are just proxies */
    public void showView(IView pView, boolean pShowInScrollPane) {
        pView.setViewManager(this);
        ViewContainer viewContainer = (ViewContainer)mViewContainersByViewName.get(pView.getName());
        
        if (viewContainer != null) {
            viewContainer.makeVisible();            
        }     
        else {                  
            ViewContainerFrame frame = new ViewContainerFrame(pView, pShowInScrollPane, this);
            mViewContainersByViewName.put(pView.getName(), frame);
            mViewsByComponentClass.put(pView.getComponent().getClass(), pView);
            mViewsByClass.put(pView.getClass(), pView);
            frame.addWindowListener(mFrameListener);
            frame.makeVisible();
            pView.afterShownInitialization();                                                    
        }
    }    
    
    /** Note: splendidly enough, the runtime class of the returned array is pComponentClass! */
    public IView[] getViewsWithClass(Class pComponentClass) {
        return (IView[])ConversionUtils.collectionToArray(mViewsByClass.get(pComponentClass), pComponentClass);        
    }
    
    public IView[] getViewsWithComponentClass(Class pComponentClass) {
        return (IView[])ConversionUtils.collectionToArray(mViewsByComponentClass.get(pComponentClass), IView.class);    
    }
    
    /** Closes all non-main-level views */
    public void closeAll() {
        String[] viewNames = getViewNames();
        for (int i=0; i<viewNames.length; i++) {
            String viewName = viewNames[i];
            IView view = getView(viewName);
            if (!mMainLevelViews.contains(view)) {
                closeView(viewName);
            }            
        }        
    }
    
    public void closeView(String pViewName) {
        closeView(pViewName, null);
    }
    
    public void closeView(String pViewName, String pCmdId) {
        // dbgMsg("closeView: "+pViewName);
        ViewContainer viewContainer = (ViewContainer)mViewContainersByViewName.get(pViewName);        
        if (viewContainer != null) {
            // dbgMsg("closing view!");
            viewContainer.close();
            IView view = viewContainer.getView();
            view.afterClose(pCmdId);
        }
        else {
            // dbgMsg("No view by that name!");
        }        
    }
    
    public IMain getMain() {
        return mMain;
    }
    
    private void removeFromMaps(String pViewName) {                
        ViewContainer viewContainer = (ViewContainer)mViewContainersByViewName.get(pViewName);
        if (viewContainer != null) {                                
            mViewContainersByViewName.remove(pViewName);
            IView view = viewContainer.getView();
            mViewsByComponentClass.remove(view.getComponent().getClass(), view);
            mViewsByClass.remove(view.getClass(), view);
            mMainLevelViews.remove(view); // of course, strictly speaking the view is not always a main level view...
        }
    }        
        
    
    /** Updates data structures after view is closed */
    private class FrameListener extends WindowAdapter {
        
        public void windowClosed(WindowEvent e) {            
            ViewContainerFrame frame = (ViewContainerFrame)e.getSource();             
//            String viewName = frame.getView().getName();
            // dbgMsg("Closed viewContainerFrame: "+viewName);
            afterClose(frame.getView());                        
        }
        
        public void windowClosing(WindowEvent e) {            
            ViewContainerFrame frame = (ViewContainerFrame)e.getSource();
            // dbgMsg("Closed viewContainerFrame: "+viewName);
            afterClose(frame.getView());            
        }
    }
    
    public void afterClose(IView pView) {
        removeFromMaps(pView.getName());        

        pView.annihilate();
        
        if (mAutoExitAfterClosingLastView && mMainLevelViews.size() == 0) {
            if (mMain != null) {
                mMain.exit();
            }
            else {
                Logger.info("ViewManager exiting!");
                System.exit(0);
            }
        }        
    }
    
    public static ViewManager getDefaultInstance() {
        if (mDefaultInstance == null) {
            mDefaultInstance = new ViewManager();
        }
        
        return mDefaultInstance;
    }
    
    public IView getView(String pViewName) {
        ViewContainer container = getViewContainer(pViewName);
        if (container == null) {
            return null;
        }
        else {
            return container.getView();
        }
    }
    
    public ViewContainer getViewContainer(String pViewName) {
        return (ViewContainer)mViewContainersByViewName.get(pViewName);
    }
        
    
    
//    private void dbgMsg(String pMsg){
//        Logger.dbg("ViewManager: "+pMsg);
//    }
    
    
    
}

