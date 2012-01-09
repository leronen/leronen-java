package gui.view;

import gui.action.*;
import gui.form.*;
import gui.menu.*;
import util.dbg.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

public class SimpleFormView extends DefaultView {
    
    // data             
    private FormData mModel;
                 
    // gui                     
    private IMenuManager mMenuManager;
    private JPanel mMainPanel;                    
    private JPanel mButtonPanel;
    private FormPanel mFormPanel;
      
    public static final String[] STANDARD_BUTTONS = {
        MenuManager_old.CMD_ID_OK,
        MenuManager_old.CMD_ID_CANCEL
    };
    
    public static final String[] PRISONER_BUTTONS = {
        MenuManager_old.CMD_ID_OK,
        MenuManager_old.CMD_ID_YOU_WONT_GET_IT
    };
    
//    public SimpleFormView(FormData pModel, IMenuManager pMenuManager, boolean pPrisonerButtons) {
//        
//    }
    
    public SimpleFormView(FormData pModel, IMenuManager pMenuManager, boolean pPrisonerStyle) {
        super(pModel. getFormName());
        mModel = pModel;
        mMenuManager = pMenuManager;                        
       
        initGui(pPrisonerStyle);
    }       
                    
    public void initGui(boolean pPrisonerStyle) {                
        mMainPanel = new JPanel();
        mMainPanel.setLayout(new BorderLayout());        
                                                                        
        mButtonPanel = new JPanel(new FlowLayout());
                
        List<JButton> buttons;
        if (pPrisonerStyle) {
            buttons = mMenuManager.makeButtons(new ButtonHandler(), Arrays.asList(PRISONER_BUTTONS));
        }
        else {
            buttons = mMenuManager.makeButtons(new ButtonHandler(), Arrays.asList(STANDARD_BUTTONS));
        }
        
        for (JButton b: buttons) {
            mButtonPanel.add(b);    
        }                    
                                   
        mFormPanel = new FormPanel(mModel);                
        mFormPanel.modelToGUI();

        mFormPanel.addFormListener(new FormListener() {
            public void enterPressed() {
                mFormPanel.guiToModel();
                SimpleFormView.this.close(MenuManager_old.CMD_ID_OK);
            }
            public void formDataChanged() {
                // toughly indeed, do not care.
            }
            public void formFieldValueChanged(String pFieldName) {
                // radical enough, we are unmoved
            }
        });
                                                                    
                                                                                                    
        mMainPanel.add(mFormPanel, BorderLayout.CENTER);
        mMainPanel.add(mButtonPanel, BorderLayout.SOUTH);
                
        
        setComponent(mMainPanel);                                        
    }                                     

    private class ButtonHandler implements CommandHandler {
        public boolean handleCommand(String pCommand) {
            if (pCommand.equals(MenuManager_old.CMD_ID_OK)) {
                dbgMsg("OK pressed.");
                mFormPanel.guiToModel();
                SimpleFormView.this.close(MenuManager_old.CMD_ID_OK);
                return true;
            }
            else if (pCommand.equals(MenuManager_old.CMD_ID_YOU_WONT_GET_IT)) {
                dbgMsg("Cancel pressed.");
                SimpleFormView.this.close(MenuManager_old.CMD_ID_CANCEL);
                return true;
            }
            else if (pCommand.equals(MenuManager_old.CMD_ID_CANCEL)) {
                dbgMsg("Cancel pressed.");
                SimpleFormView.this.close(MenuManager_old.CMD_ID_CANCEL);
                return true;
            }
            else {
                dbgMsg("Unknown command: "+pCommand);
                return false;
            }
        }
    }                                                        
                                                
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("SimpleFormView: "+pMsg);
    }
    
                       
}
