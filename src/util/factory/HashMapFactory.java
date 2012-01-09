package util.factory;

import java.util.*;

public class HashMapFactory implements ParametrizedFactory<HashMap, Integer> {

	private int mDefaultSize;
	
	public HashMapFactory() {
		mDefaultSize = 16;
	}
	
	public HashMapFactory(int pDefaultSize) {
		mDefaultSize = pDefaultSize;
	}
	
	public HashMap makeObject() {
		return new HashMap(mDefaultSize);
	}
    
    public HashMap makeObject(Integer pSize) {
        return new HashMap(pSize);
    }

}
