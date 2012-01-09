package gui.view;

import gui.action.*;

import java.awt.*;
import javax.swing.*;

/**
 * It seems the comment below is highly obsolete; actually, views generally
 * do not inherit JComponent:
 * 
 *  //  Note that, altough impossible to enforce it by this interface (zzt, swing is shitly planned).
 *  //  All instances of IView should also be instances of JComponent, as this kind 
 *  //  of typecasting is expected to occur frequently.
 */ 
public interface IView {
   
    public String getName();
    public JComponent getComponent();
    
    /** May return null, in which case default menubar is used */
    public String getMenubarId();      
    
    public CommandHandler getCommandHandler();
    public ViewContainer getViewContainer();
    public void setViewContainer(ViewContainer pViewContainer);
    public void setViewManager(ViewManager pViewManager);
    public void afterClose(String pCmdId);
    public void close(String pCmdId);
    public void refresh();   
    public boolean prefersToFillTheWholeScreen();
    /** if null, then getComponent().pack() is used... */
    public Dimension getPreferredSize();
    public void afterShownInitialization();
    
    /** post-post-total annihilation after all has been already lost */ 
    public void annihilate();
       
         
}
