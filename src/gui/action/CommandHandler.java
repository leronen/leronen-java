package gui.action;

public interface CommandHandler {
    
    /** return true, if the command was handled */
    public boolean handleCommand(String pCommand);        
    
    public static final CommandHandler DUMMY_INSTANCE = new CommandHandler() {
        public boolean handleCommand(String pCommand) {
            return false;
        }
    };
    
}



