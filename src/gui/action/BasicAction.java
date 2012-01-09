package gui.action;

import java.awt.event.*;

import javax.swing.*;

public class BasicAction extends AbstractAction {
    
    private String mCommandId;
    private String mMenuId;
    private CommandHandler mDefaultCommandHandler;    
    
    public BasicAction(String pCommandId, String pMenuId, 
    				   CommandHandler pCommandHandler) {
        super(pCommandId);
        mCommandId = pCommandId;
        mMenuId = pMenuId;
        mDefaultCommandHandler = pCommandHandler;
    }
    
    public String getCommandId() {
        return mCommandId;    
    }
    
    public String getMenuId() {
        return mMenuId;
    }
    
    public void actionPerformed(ActionEvent e) {
        directToDefaultHandler();
    }
    
    public boolean redirect(CommandHandler pHandler) {
        if (pHandler instanceof MenuAwareCommandHandler) {
            // the command handler can take into account the additional information provided
            // by the action object
            return ((MenuAwareCommandHandler)pHandler).handleCommand(this);
        }
        else {
            // the command handler only knows about action names
            return pHandler.handleCommand(mCommandId);
        }
    }
    
    public String toString() {
        return "BasicAction, menuId="+mMenuId+", commandId="+mCommandId;
    }
    
    private boolean directToDefaultHandler() {
        return redirect(mDefaultCommandHandler);
    }

} 
