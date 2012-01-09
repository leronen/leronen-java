package gui.action;

import gui.menu.*;

import util.CollectionUtils;
import util.dbg.*;


import java.awt.event.*;
import java.util.List;

import javax.swing.*;


/** 
 * An unfortunate hybrid which uses both MenuManager_old and the new 
 * MenuManager... TODO: THIS MUST DEFINITELY BE REFACTORED
 *
 *
 */
public class PopupMouseAdapter extends MouseAdapter {

    private Mode mMode;
    private MenuManager_old mMenuManager;
    private CommandHandler mCommandHandler;
    private String mMenuId;
    private List<String> mMenuDef;
    private JComponent mComponent;
    
    public PopupMouseAdapter(MenuManager_old pMenuManager, 
                             CommandHandler pCommandHandler, 
                             String pMenuId,
                             JComponent pComponent) {
        mMode = Mode.MENUMANAGER_KNOWS_MENU_DEF;
        mMenuManager = pMenuManager;
        mCommandHandler = pCommandHandler;
        mMenuId = pMenuId;
        mComponent = pComponent;
    }                        
    
    public PopupMouseAdapter(CommandHandler pCommandHandler,                              
                             JComponent pComponent,
                             String... pMenuIds) {
        mMode = Mode.WE_KNOW_MENU_DEF;
        mMenuDef = CollectionUtils.makeList(pMenuIds);
        mCommandHandler = pCommandHandler;        
        mComponent = pComponent;
    }
    
    public void maybePopupTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
            dbgMsg("popup trigger!");            
            JPopupMenu menu;
            if (mMode == Mode.MENUMANAGER_KNOWS_MENU_DEF) {
                menu = mMenuManager.makePopupMenu(mMenuId, mCommandHandler);
            }
            else if (mMode == Mode.WE_KNOW_MENU_DEF) {
                // use the new menu manager system!
                menu = new MenuManager().makePopupMenu(mCommandHandler, mMenuDef);
            }            
            else {
                throw new RuntimeException();
            }
            menu.show(mComponent, e.getX(), e.getY());
        }        
    }   
             
    public void mousePressed(MouseEvent e) {
        maybePopupTrigger(e);
    }
        
    public void mouseReleased(MouseEvent e) {
        maybePopupTrigger(e);
    }                                
    
    public void mouseClicked(MouseEvent e) {
        maybePopupTrigger(e);                    
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg("PopupMouseAdapter: "+pMsg);
    }
    
    
    private enum Mode {
        MENUMANAGER_KNOWS_MENU_DEF,
        WE_KNOW_MENU_DEF
    }
    
}
