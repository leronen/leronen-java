package gui.action;



/** 
 * A proxy that redirects commands to an actual command handler.
 * The motivation for this is that we can make the action system see the command handler as
 * an "static" entity, altough the actual command handler may vary.
 * It might also be that the actual handler does not yet exist while creating 
 * a menu; an example is creating the main menu before any plugins are 
 * actually instantiated. 
 */ 
public class CommandHandlerProxy implements MenuAwareCommandHandler {
        
    private CommandHandler mCommandHandler;    
    
    public CommandHandlerProxy() {        
        mCommandHandler = null;
    }
            
    public CommandHandlerProxy(CommandHandler pCommandHandler) {        
        mCommandHandler = pCommandHandler;
    }
        
        /*
    public void redirect(CommandHandler pHandler) {
        if (pHandler instanceof MenuAwareCommandHandler) {
            // the command handler can take into account the additional information provided
            // by the action object
            ((MenuAwareCommandHandler)pHandler).handleCommand(this);
        }
        else {
            // the command handler only knows about action names
            pHandler.handleCommand(mCommandId);
        }    
    }*/
    
    public void setCommandHandler(CommandHandler pHandler) {
        mCommandHandler = pHandler;
    }
    
    public boolean handleCommand(String pCommand) {
        if (mCommandHandler != null) {
            return mCommandHandler.handleCommand(pCommand);
        }       
        else {
            return false;
        }
    }
    
    public boolean handleCommand(BasicAction pCommand) {
        if (mCommandHandler != null) {
            return pCommand.redirect(mCommandHandler);
        }
        else {
            return false;
//               to the void.................
//                                           .....
//                                                ...  ,  .
//                                                    ..     x         s .                                               
//                                                       ..      x          .
//                                                     .. .. .XXX  x   x  ,  . 
//                                                 .           X          --  . 
        }
            
    }

}  
