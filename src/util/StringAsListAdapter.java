package util;

import java.util.*;

public class StringAsListAdapter extends AbstractList<Character> implements RandomAccess {
    private String mString;
    
    public StringAsListAdapter(String p) {
        mString = p;
    }
    
    public Character get(int pIndex) {
        return new Character(mString.charAt(pIndex));    
    }
    
    public int size() {
        return mString.length();
    }
     
}



            
