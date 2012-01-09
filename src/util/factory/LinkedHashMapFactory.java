package util.factory;

import java.util.*;

public class LinkedHashMapFactory<K,V> implements ParametrizedFactory<Map<K,V>, Integer> {

    private int mDefaultCapacity;
    
    /**
     * Construct a factory with the traditional java default map capacity of 16
     * (the load factor is always the default 0.75), as governed by java
     * internals...) 
     */
    public LinkedHashMapFactory() {
        this(16);
    }
    
    public LinkedHashMapFactory(int pDefaultCapacity) {
        mDefaultCapacity = pDefaultCapacity;
    }
    
	public Map makeObject() {
		return new LinkedHashMap(mDefaultCapacity);
	}

    public Map<K,V> makeObject(Integer pCapacity) {
        return new LinkedHashMap(pCapacity);
    }
    
}
