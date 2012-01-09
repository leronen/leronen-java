package util.process;

public interface StreamListener {

    /** by implementing this method the listener states which messages he is intrested in */
    public String[] getRegularExpressions();
    
    /** By this method the listener receives the desired messages */ 
    public void notify(String pString, int pPatternIndex);
} 
