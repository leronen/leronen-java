package gui.action;

public interface MenuAwareCommandHandler extends CommandHandler {
    
    /** return true, if the command was handled */
    public boolean handleCommand(BasicAction pCommand);        
    
}



