package gui.menu;

import util.*;
import util.collections.*;
import gui.action.*;
import gui.application.*;


import java.awt.*;
import javax.swing.*;

import java.util.*;
import java.util.List;

/** 
 * Note that the situation in this class is a bit messy, as 
 * the new and old systems of handling actions
 * are in the use at the same time.
 * Note: as of 1.11.2005, the situation is probably no longer
 * messy 
 *
 * Old system: shit.
 * New system: all menu listeners implement CommandHandler, 
 *             menus popup menus and buttons are handled similarly
 */
public class MenuManager_old implements IMenuManager {
                                                            
    public static final String MENUBAR_ID_VIEW = "View Menubar";
 
    public static final String ID_SEPARATOR = "SEPARATOR";  
 
    // generic 
    public static final String CMD_ID_OPTIONS = "Options";
    public static final String CMD_ID_VIEW_WARNINGS = "View Warnings";
   
    // edit menu                                
    public static final String MENU_ID_EDIT = "Edit";
    public static final String CMD_ID_CUT = "Cut";
    public static final String CMD_ID_COPY = "Copy";
    public static final String CMD_ID_PASTE = "Paste";
    public static final String CMD_ID_DELETE = "Delete";        
                                                                                                                       
    // view menu        
    public static final String MENU_ID_WINDOW = "Window"; 
    public static final String CMD_ID_NEW_WINDOW = "New Window";
    public static final String CMD_ID_REFRESH = "Refresh";    
    public static final String CMD_ID_CLOSE = "Close";
    public static final String CMD_ID_CLOSE_ALL = "Close All";

    // OK-Cancel menu        
    public static final String MENU_ID_OK_CANCEL = "OK_CANCEL";
    public static final String CMD_ID_OK = "Ok";
    public static final String CMD_ID_CANCEL = "Cancel";
    public static final String CMD_ID_YOU_WONT_GET_IT = "You won't get it!";
            
    
    private static final String[] DEF_EDIT_MENU = {
        MENU_ID_EDIT,
        CMD_ID_CUT,
        CMD_ID_COPY,
        CMD_ID_PASTE,
        CMD_ID_DELETE,                
    };
                                                                                
    private static final String[] DEF_WINDOW_MENU = {
        MENU_ID_WINDOW,
        CMD_ID_NEW_WINDOW,
        CMD_ID_REFRESH,
        CMD_ID_CLOSE,
        CMD_ID_CLOSE_ALL,             
    };
    
    private static final String[] DEF_OK_CANCEL_MENU = {
        MENU_ID_OK_CANCEL,
        CMD_ID_OK,
        CMD_ID_CANCEL              
    };
            
    private static final String[] DEF_VIEW_MENU_BAR = {            
        MENU_ID_WINDOW
    };    
                               
    protected HashMap ACCELERATOR_MAP = new HashMap();                                                                   
    protected HashMap mMenuBarDefsById = new HashMap();
    protected HashMap<String, String[]> mMenuDefsById = new HashMap();    
    
    protected IMain mMain;            
    
    protected MultiMap mReservedMnemonicCharactersByMenuBarId = new MultiMap();
    // protected MultiMap mReservedMnemonicCharactersByMenuBarIdAndMenuId = new MultiMap();
                    
    /** Have to init() later */
    protected MenuManager_old() {
        // 
    }
    
    public MenuManager_old(IMain pMain) {        
        init(pMain);
    }
    
    public void init(IMain pMain) {
        mMain = pMain;
        
        mMenuBarDefsById.put(MENUBAR_ID_VIEW, DEF_VIEW_MENU_BAR);                                       
        mMenuDefsById.put(MENU_ID_EDIT, DEF_EDIT_MENU);
        mMenuDefsById.put(MENU_ID_WINDOW, DEF_WINDOW_MENU);
        mMenuDefsById.put(MENU_ID_OK_CANCEL, DEF_OK_CANCEL_MENU);
    }
                                                  
    public final JMenu makeMenu(String pMenuBarId, String pMenuId, CommandHandler pCommandHandler) {
        BasicAction[] actions = makeActions(pMenuId, pCommandHandler, true);
        JMenu menu = new JMenu(pMenuId);
        Set reservedMnemonics = mReservedMnemonicCharactersByMenuBarId.get(pMenuBarId);        
        Character mnemonic = StringUtils.findFirstCharacterNotInSet(pMenuId, reservedMnemonics);
        if (mnemonic != null) {
            menu.setMnemonic(mnemonic.charValue());
            mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toLowerCase(mnemonic.charValue())));
            mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toUpperCase(mnemonic.charValue())));
        }
        for (int i=0; i<actions.length; i++) {
            if (actions[i]==null) {
                menu.addSeparator();
            }
            else {  
                menu.add(actions[i]);
            }                        
        }  
        
        return menu;
    }
     
    public final JMenuBar makeMenuBar(String pMenuBarId, CommandHandler pCommandHandler) {
        String[] menuBarDef = (String[])mMenuBarDefsById.get(pMenuBarId);
        JMenuBar menuBar = new JMenuBar();                 
                   
        for (int i=0; i<menuBarDef.length; i++) {
            String menuId = menuBarDef[i];
            JMenu menu = makeMenu(pMenuBarId, menuId, pCommandHandler);                            
            menuBar.add(menu);
        }
        return menuBar;
    }    
            
    public final BasicAction[] makeActions(String pMenuName, CommandHandler pCommandHandler, boolean pMnemonics) {
        String[] menuDef = (String[])mMenuDefsById.get(pMenuName);
        java.util.List defAsList = Arrays.asList(menuDef);
        java.util.List cmdIdsList= CollectionUtils.tailList(defAsList, 1);
        String[] cmdIds = (String[])ConversionUtils.collectionToArray(cmdIdsList, String.class);
        return makeActions(cmdIds, pMenuName, pCommandHandler, pMnemonics);        
    }
    
    public final BasicAction[] makeActions(String[] pCommandIds, CommandHandler pCommandHandler, boolean pMnemonics) {
        return makeActions(pCommandIds, 
                           null, // no menu name known nor longed-for
                           pCommandHandler,
                           pMnemonics);                           
    }
    
    
    /** @param pMenuName may be null */
    public final BasicAction[] makeActions(String[] pCommandIds, 
                                           String pMenuName,
                                           CommandHandler pCommandHandler,
                                           boolean pMnemonics) {        
        int numActions = pCommandIds.length;        
        BasicAction[] actions = new BasicAction[numActions];
        Set reservedMnemonics = new HashSet();
        for (int i=0; i<numActions; i++) {            
            String cmdId = pCommandIds[i];
            
            if (cmdId.equals(ID_SEPARATOR)) {
                // separator is brutally represented by null...
                actions[i] = null;
            }
            else {                            
                // make action
                BasicAction action = new BasicAction(cmdId, pMenuName, pCommandHandler); 
                
                // set accelerator
                KeyStroke keyStroke = (KeyStroke)ACCELERATOR_MAP.get(cmdId);
                if (keyStroke != null) {
                    action.putValue(Action.ACCELERATOR_KEY, keyStroke);
                }
                     
                if (pMnemonics) {                 
                    // char mnemonicChar = cmdId.charAt(0);
                    Character mnemonic = StringUtils.findFirstCharacterNotInSet(cmdId, reservedMnemonics);
                    if (mnemonic != null) {
                        action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic.charValue()));
                        reservedMnemonics.add(mnemonic);
                    }
                }                      
                actions[i] = action;
            }                
                              
        }
        return actions;
    }
    
    public final JPopupMenu makePopupMenu(String pMenuName, CommandHandler pCommandHandler) {
        BasicAction[] actions = makeActions(pMenuName, pCommandHandler, false);
        JPopupMenu menu = new JPopupMenu();
        for (int i=0; i<actions.length; i++) {
            if (actions[i] == null) {
                menu.addSeparator();
            }
            else {                
                menu.add(actions[i]);
            }
        }  
        return menu;
    }
    
    /** To implement IMenuManager... */
    public List<JButton> makeButtons(CommandHandler pCommandHandler, Iterable<String> pCommandIds) {
        Iterator<String> i = pCommandIds.iterator();
        List<String> l = CollectionUtils.makeArrayList(i);
        String[] a = ConversionUtils.stringCollectionToArray(l);
        return Arrays.asList(makeButtons(a, pCommandHandler));
    }
    
    public final JButton[] makeButtons(String[] pCommandIds, CommandHandler pCommandHandler) {
        BasicAction[] actions = makeActions(pCommandIds, pCommandHandler, false);
        JButton[] buttons = new JButton[actions.length];        
        for (int i=0; i<actions.length; i++) {
            if (actions[i]!=null) { 
                buttons[i]=new JButton(actions[i]);
            }
            else {
                // ignore separators for now...    
            }
        }  
        return buttons;        
    }
              
    public JComponent makeHorizontalButtonPanel(String[] pCommands, CommandHandler pCommandHandler) {
        JButton[] buttons = makeButtons(pCommands, pCommandHandler);
        
        Box buttonPanel = Box.createHorizontalBox();        
                                        
        for (int i=0; i<buttons.length; i++) {
            buttonPanel.add(buttons[i]);            
        }
        
        return buttonPanel;
    }

    public JComponent makeVerticalButtonPanel(String[] pCommands, CommandHandler pCommandHandler) {
        JButton[] buttons = makeButtons(pCommands, pCommandHandler);
        
        // Box buttonPanel = Box.createVerticalBox();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());        
                                        
        for (int i=0; i<buttons.length; i++) {
            panel.add(buttons[i]);            
        }
        
        return panel;
    }              
                  
    
}


