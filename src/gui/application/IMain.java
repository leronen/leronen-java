package gui.application;

import gui.*;
import gui.action.*;
import gui.form.*;
import gui.menu.*;
import gui.view.*;

import util.process.*;

import java.io.*;
import java.util.*;




/** The main class that is responsible for an GUI of an application */ 
public interface IMain extends ProcessOwner {

    // public static final int GUI_TYPE_FRAMES = 1;
    // public static final int GUI_TYPE_INTERNAL_FRAMES = 2;
    
    // public Dimension getMainWindowSize();
    // public Dimension getInternalFrameSize();
        
    public void exit();
    
    /** Once, the grand view was to enable different types of gui (one of GUI_TYPE_XXX) */
    // public int getGuiType();    
    
    public MenuManager_old getMenuManager_old();    
    public ViewManager getViewManager();
    public boolean hasGUI();    
    
    public CommandHandler getDefaultCommandHandler();    
    
    // various simple user interaction (error dialogs, query dialogs)
    public void reportError(String pError);
    public void reportError(Exception pEx);
    public void reportError(String pErrorText, Exception e);    
    public void reportInfo(String pInfoText);
    public void showReport(String pTitle, String pText);
    
    // non-blocking queries from user...
    public void queryFromUser(String pTitle, String[] pKeys, UserQueryListener pListener);
    public void queryFromUser(String pTitle, String[] pKeys, UserQueryListener pListener, Map pDefaultValues);
    public void queryFromUser(FormData pFormModel, UserQueryListener pListener);
    
    /** A Blocking query from user... */    
    public Object queryFromUser(Collection pOptions, String pTitle, String pMsg, boolean pRememberLastValue);            
    
    public void getFileSelection(File pDir, String pTitle, UserQueryListener pListener, boolean pWholeScreen);            
       
    
    /** Boldly open any file; hmm, why is this commented out? */   
//    public void openFile(File pFile);
    public void openWithEditor(File pFile);
    
    public void openTextFile(File pFile) throws IOException;
    
    public void setClipboardContents(String pText);
            
            
}
