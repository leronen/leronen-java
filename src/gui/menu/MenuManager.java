package gui.menu;

import util.*;
import util.collections.*;
import util.dbg.Logger;

import gui.action.*;


import javax.swing.*;

import java.util.*;

/** 
 *
 * Idea: all menu listeners implement CommandHandler, menus popup menus and 
 * buttons are handled similarly.
 *             
 * Again, let's get a fresh start with this for the purposes of 
 * BMVis plugins (and preferably BMVis itself). Let's make this 
 * more flexible this time; no contents are specified in
 * the menu manager itself; instead, everything is added on the fly
 * (possibly read from a file).             
 *              
 */
public class MenuManager implements IMenuManager {
                                                                                                 
    protected HashMap ACCELERATOR_MAP = new HashMap();                                                                   
    protected HashMap<String,String[]> mMenuBarDefsById = new HashMap();
    
    /** first item is the menu name, other items are the cmd ids  */
    protected HashMap<String,String[]> mMenuDefsById = new HashMap();    
                  
    protected MultiMap mReservedMnemonicCharactersByMenuBarId = new MultiMap();
                    
    public static String ID_SEPARATOR = "SEPARATOR";
    
    public MenuManager() {        
        // no action
    }
                            
    public void registerMenu(String pMenuId, String... pValues) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    public JMenu makeMenu(String pMenuBarId, String pMenuId, CommandHandler pCommandHandler) {
        List<BasicAction> actions = makeActions(pMenuId, pCommandHandler, true);
        JMenu menu = new JMenu(pMenuId);
        Set reservedMnemonics = mReservedMnemonicCharactersByMenuBarId.get(pMenuBarId);        
        Character mnemonic = StringUtils.findFirstCharacterNotInSet(pMenuId, reservedMnemonics);
        if (mnemonic != null) {
            menu.setMnemonic(mnemonic.charValue());
            mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toLowerCase(mnemonic.charValue())));
            mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toUpperCase(mnemonic.charValue())));
        }
                
        for (BasicAction action: actions) {
            if (action == null) {
                menu.addSeparator();
            }
            else {  
                menu.add(action);
            }                        
        }  
        
        return menu;
    }
    
    public void addAcceleratorDefs(Object[][] pDefs) {
        addAcceleratorDefs((Map<String,KeyStroke>)(Map)CollectionUtils.makeMap(pDefs));
    }
    
    public void addAcceleratorDefs(Map<String, KeyStroke> pDefs ) {
        ACCELERATOR_MAP.putAll(pDefs);
    }
    
    /**
     * Make menu without knowledge of the menu bar where it will be used 
     * This does not enable automatic creation of mnemonic chars,
     * as we have no way of knowing which ones are reserved...
     * 
     * Also provide pMenuBarId to allow detection of reserved mnemonics.
     * (correct functionality would then require a possibility to also unregister  menus...)
     * If null, do not use mnemonics.
     */
    public JMenu makeMenu(String pMenuId, CommandHandler pCommandHandler, Iterable<String> pCmdIds, String pMenuBarId) {
        List<BasicAction> actions = makeActions(pMenuId, pCommandHandler, false, pCmdIds);            
        
        JMenu menu = new JMenu(pMenuId);
        
        for (BasicAction action: actions) {
            if (action == null) {
                menu.addSeparator();
            }
            else {  
                menu.add(action);
            }                        
        }
        
        if (pMenuBarId != null) {
            Set reservedMnemonics = mReservedMnemonicCharactersByMenuBarId.get(pMenuBarId);        
            Character mnemonic = StringUtils.findFirstCharacterNotInSet(pMenuId, reservedMnemonics);
            if (mnemonic != null) {
                menu.setMnemonic(mnemonic.charValue());
                mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toLowerCase(mnemonic.charValue())));
                mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toUpperCase(mnemonic.charValue())));
            }
        }

        
        return menu;
    }
    
    /** 
     * Set accelerators according to mappings specified earlier by calling
     * addAcceleratorDef(). 
     */
    public void setAccelerators(JMenuBar pMenuBar) {
                        
        for (int i=0; i<pMenuBar.getComponentCount(); i++) {
            JMenu menu = pMenuBar.getMenu(i);

            for (int j=0; j<menu.getItemCount(); j++) {                
                JMenuItem menuItem = menu.getItem(j);
                if (menuItem != null) {
                    String cmdId = menuItem.getActionCommand();
                    KeyStroke keyStroke = (KeyStroke)ACCELERATOR_MAP.get(cmdId);
                    if (keyStroke != null) {
                        menuItem.getAction().putValue(Action.ACCELERATOR_KEY, keyStroke);
                    }
                }
            }
        }
        

    }
    
    /**
     * Set mnemonics for a whole menu at one go, storing reserved mnemonics 
     * by the key pMenuBarId. In case pMenuBarId == null do not store mnemonics.
     * Assume no mnemonics have been set so far. 
     */
    public void setMnemonics(String pMenuBarId, JMenuBar pMenuBar) {
        if (pMenuBarId != null) {
            Set reservedMnemonicsForMenuBar = mReservedMnemonicCharactersByMenuBarId.get(pMenuBarId);
            
            for (int i=0; i<pMenuBar.getComponentCount(); i++) {
                JMenu menu = pMenuBar.getMenu(i);
                String menuId = menu.getActionCommand();
                Character mnemonic = StringUtils.findFirstCharacterNotInSet(menuId, reservedMnemonicsForMenuBar);
                if (mnemonic != null) {
                    menu.setMnemonic(mnemonic.charValue());
                    mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toLowerCase(mnemonic.charValue())));
                    mReservedMnemonicCharactersByMenuBarId.put(pMenuBarId, new Character(Character.toUpperCase(mnemonic.charValue())));
                }
                
                Set<Character> reservedMnemonicsForSingleMenu = new HashSet();
                for (int j=0; j<menu.getItemCount(); j++) {                    
                    JMenuItem menuItem = menu.getItem(j);
                    if (menuItem != null) {
                        // remember that we may have nulls as separators?                   
                        String cmdId = menuItem.getActionCommand(); 
                        mnemonic = StringUtils.findFirstCharacterNotInSet(cmdId, reservedMnemonicsForSingleMenu);                        
                        if (mnemonic != null) {
                            menuItem.setMnemonic(mnemonic);                        
                            reservedMnemonicsForSingleMenu.add(new Character(Character.toLowerCase(mnemonic.charValue())));
                            reservedMnemonicsForSingleMenu.add(new Character(Character.toUpperCase(mnemonic.charValue())));
                        }
                    }
                }                      
                
            }
                    
            
        }
    }
    
    /** Enable a menu or menu item. Caller needs to ensure that pId is unique within the menu bar.  */
    public static void setEnabled(JMenuBar pMenuBar, String pId, boolean pVal) {
        for (int i=0; i<pMenuBar.getComponentCount(); i++) {
            JMenu menu = pMenuBar.getMenu(i);
            String menuId = menu.getActionCommand();                
            if (menuId.equals(pId)) {
                menu.setEnabled(pVal);
                return;
            }            
            
            for (int j=0; j<menu.getItemCount(); j++) {                    
                JMenuItem menuItem = menu.getItem(j);
                if (menuItem != null) {
                    String cmdId = menuItem.getActionCommand();
                    if (cmdId.equals(pId)) {                        
                        menuItem.setEnabled(pVal);
                        return;
                    }
                }
            }
        }                    
        
        Logger.warning("Did not find menu item: "+pId);
    }
    
           
     /**
      * Making menu bars in one go would definitely require registering 
      * the individual menus of the menu bar...
      */
//    public JMenuBar makeMenuBar(String pMenuBarId, CommandHandler pCommandHandler) {
//        String[] menuBarDef = (String[])mMenuBarDefsById.get(pMenuBarId);
//        JMenuBar menuBar = new JMenuBar();                 
//                   
//        for (int i=0; i<menuBarDef.length; i++) {
//            String menuId = menuBarDef[i];
//            JMenu menu = makeMenu(pMenuBarId, menuId, pCommandHandler);                            
//            menuBar.add(menu);
//        }
//        return menuBar;
//    }    
            
    public List<BasicAction> makeActions(String pMenuName, CommandHandler pCommandHandler, boolean pMnemonics) {
        String[] menuDef = (String[])mMenuDefsById.get(pMenuName);
        List<String> defAsList = Arrays.asList(menuDef);
        List cmdIds = CollectionUtils.tailList(defAsList, 1);
        return makeActions(pMenuName, pCommandHandler, pMnemonics, cmdIds);        
    }
    
    /** The base case; just make a menu with no name */
    public List<BasicAction> makeActions(CommandHandler pCommandHandler, boolean pMnemonics, Iterable<String> pCommandIds) {
        return makeActions(null, // no menu name known nor longed-for
                           pCommandHandler,
                           pMnemonics,
                           pCommandIds);                           
    }
    
    
    /** @param pMenuName may be null */
    public List<BasicAction> makeActions(String pMenuName,
                                         CommandHandler pCommandHandler,
                                         boolean pMnemonics,
                                         Iterable<String> pCommandIds) {        
        List<BasicAction> actions = new ArrayList();
        Set reservedMnemonics = new HashSet();
       
        for (String cmdId: pCommandIds) {
                        
            if (cmdId.equals(ID_SEPARATOR)) {
                // separators are brutally represented by null...                
                actions.add(null);
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
                    Character mnemonic = StringUtils.findFirstCharacterNotInSet(cmdId, reservedMnemonics);
                    if (mnemonic != null) {
                        action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic.charValue()));
                        reservedMnemonics.add(mnemonic);
                    }
                }                      
                actions.add(action);
            }
            
                              
        }
        return actions;
    }
    
    /** Make a popup menu according to an existing def for pMenuName */ 
    public JPopupMenu makePopupMenu(String pMenuName, CommandHandler pCommandHandler) {
        List<BasicAction> actions = makeActions(pMenuName, pCommandHandler, false);
        JPopupMenu menu = new JPopupMenu();
        for (BasicAction action: actions) {
            if (action == null) {
                menu.addSeparator();
            }
            else {                
                menu.add(action);
            }
        }  
        return menu;
    }
    
    /** Make a popup menu containing a given list of commands */ 
    public JPopupMenu makePopupMenu(CommandHandler pCommandHandler, Iterable<String> pCmdIds) {
        List<BasicAction> actions = makeActions(pCommandHandler, true, pCmdIds);
        JPopupMenu menu = new JPopupMenu();
        
        for (BasicAction action: actions) {
            if (action == null) {
                menu.addSeparator();
            }
            else {                
                menu.add(action);
            }
        }  
        return menu;
    }
    
    /** Make a popup menu containing commands pCommandIds */
    public List<JButton> makeButtons(CommandHandler pCommandHandler, Iterable<String> pCommandIds) {
        List<BasicAction> actions = makeActions(pCommandHandler, false, pCommandIds);
        List<JButton> buttons = new ArrayList();        
        for (BasicAction action: actions) {
            if (actions !=null ) { 
                buttons.add(new JButton(action));
            }
            else {
                // ignore separators for now...    
            }
        }  
        return buttons;        
    }
              
    public JComponent makeHorizontalButtonPanel(CommandHandler pCommandHandler, Iterable<String> pCommandIds) {
        List<JButton> buttons = makeButtons(pCommandHandler, pCommandIds);
        
        Box buttonBox = Box.createHorizontalBox();        
                                        
        for (JButton b: buttons) {
            buttonBox.add(b);            
        }
        
        return buttonBox;
    }

    
                  
    protected Box makeVerticalButtonPanel(CommandHandler pCommandHandler, Iterable<String> pCommandIds) {
        List<JButton> buttons = makeButtons(pCommandHandler, pCommandIds);
        
        Box buttonBox = Box.createVerticalBox();        
        
        for (JButton b: buttons) {
            buttonBox.add(b);            
        }
        
        return buttonBox;
    }
    
    
}


