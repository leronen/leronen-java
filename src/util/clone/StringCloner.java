package util.clone;

public class StringCloner implements Cloner<String> {
     
    public String createClone(String s) {
        // no need to clone, as strings are immutable!
        return s;
    }        
    
} 

