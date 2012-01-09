package util.process;

public interface ProcessOwner {
    
    
    /**
     * Must be synchronized, as we generally cannot expect synchronized 
     * access to process owner.
     */
    public void registerExternalProcess(Process pProc,                                        
                                        String pCmd);    
    
}
