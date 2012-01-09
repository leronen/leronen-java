package gui.action;

import util.dbg.*;

import java.util.*;

public abstract class AbstractMenuAwareCommandHandler implements MenuAwareCommandHandler {
    
    private CommandHandler mFallBackHandler;
    
    protected AbstractMenuAwareCommandHandler(CommandHandler pFallBackHandler) {
        mFallBackHandler = pFallBackHandler;        
    }
    
    /** Keys: menu ids. Values: CommandHandlers */
    private HashMap mCommandHandlersToRedirectByMenuName  = new HashMap();
        
    public void redirect(String pMenuId, CommandHandler pCommandHandler) {        
        mCommandHandlersToRedirectByMenuName.put(pMenuId, pCommandHandler);
    }            
        
    public boolean handleCommand(BasicAction pCommand) {
        dbgMsg("handleCommand: "+pCommand);        
        String menuId = pCommand.getMenuId();
        dbgMsg("menuId="+menuId);
        CommandHandler handler = (CommandHandler)mCommandHandlersToRedirectByMenuName.get(menuId);
        dbgMsg("command handler by menu name:"+handler);
        boolean handled = false;
        if (handler != null) {
            // there is a handler registered to handle events from this menu
            dbgMsg("Redirecting command for menu id "+menuId+": "+pCommand);
            handled = pCommand.redirect(handler);             
        }        
        if (!handled) {
            dbgMsg("Not handled, handling ourself by command id...");
            // no handler registered, or registered handler refused to handle.        
            // So, lets gather our courage and try to handle this ourself
            handled = handleCommand(pCommand.getCommandId());
        }
        if (!handled && mFallBackHandler != null) {
            // ok, still not handled. Det är nog perkele. As a final hope, let's try the fall back handler
            dbgMsg("Redirecting to fall back handler: "+pCommand);
            handled = pCommand.redirect(mFallBackHandler);
        }
        return handled;                  
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("AbstractMenuAwareCommandHandler: "+pMsg);
    }
    
    
         
    
}



