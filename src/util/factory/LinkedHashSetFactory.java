package util.factory;

import java.util.*;

public class LinkedHashSetFactory implements ParametrizedFactory<LinkedHashSet, Integer> {

    private int mDefaultSize;
    
    /**
     * Construct a factory with the traditional java default map capacity of 16
     * (the load factor is always the default 0.75), as governed by java
     * internals...) 
     */
    public LinkedHashSetFactory() {
        this(16);
    }
    
    public LinkedHashSetFactory(int pDefaultSize) {
        mDefaultSize = pDefaultSize;
    }
    
	public LinkedHashSet makeObject() {
		return new LinkedHashSet(mDefaultSize);
	}
    
    public LinkedHashSet makeObject(Integer pSize) {
        return new LinkedHashSet(pSize);
    }

}
