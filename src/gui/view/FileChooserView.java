package gui.view;

import gui.action.*;
import gui.menu.*;
import util.dbg.*;
import java.awt.*;
import javax.swing.*;

import java.io.*;

public class FileChooserView extends DefaultView {
                         
    // gui                     
    private MenuManager_old mMenuManager;
    private JPanel mMainPanel;
    private JFileChooser mFileChooser;                    
    private JPanel mButtonPanel;
    private File mDir;            
    private boolean mWholeScreen;
    
    public static final String[] BUTTONS = {
        MenuManager_old.CMD_ID_OK,
        MenuManager_old.CMD_ID_CANCEL
    };
                                                          
    public FileChooserView(File pDir, String pTitle, MenuManager_old pMenuManager, boolean pWholeScreen) {
        super(pTitle);        
        mMenuManager = pMenuManager;
        mDir = pDir;
        mWholeScreen = pWholeScreen;                                  
        initGui();
    }       
    
    public boolean prefersToFillTheWholeScreen() {
        return mWholeScreen;
    }
    
    
    public File getSelectedFile() {
        return mFileChooser.getSelectedFile();
    }
                    
    public void initGui() {                
        mMainPanel = new JPanel();
        mMainPanel.setLayout(new BorderLayout());        
                                                                        
        mButtonPanel = new JPanel(new FlowLayout());
                
        mFileChooser = new JFileChooser(mDir);
        mFileChooser.setControlButtonsAreShown(false);                
                
        JButton[] buttons = mMenuManager.makeButtons(BUTTONS, new ButtonHandler());
        
        for (int i=0; i<buttons.length; i++) {
            mButtonPanel.add(buttons[i]);    
        }                    
                                                                                                                                                               
        mMainPanel.add(mFileChooser, BorderLayout.CENTER);
        mMainPanel.add(mButtonPanel, BorderLayout.SOUTH);                
        
        setComponent(mMainPanel);                                        
    }                                     

    private class ButtonHandler implements CommandHandler {
        public boolean handleCommand(String pCommand) {
            if (pCommand.equals(MenuManager_old.CMD_ID_OK)) {
                dbgMsg("OK pressed.");                
                FileChooserView.this.close(MenuManager_old.CMD_ID_OK);
                return true;
            }
            else if (pCommand.equals(MenuManager_old.CMD_ID_CANCEL)) {
                dbgMsg("Cancel pressed.");
                FileChooserView.this.close(MenuManager_old.CMD_ID_CANCEL);
                return true;
            }
            else {
                dbgMsg("Unknown command: "+pCommand);
                return false;
            }
        }
    }                                                        
                                                
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("FileChooserView: "+pMsg);
    }
    
                       
}
