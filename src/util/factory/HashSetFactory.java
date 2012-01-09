package util.factory;

import java.util.*;


public class HashSetFactory implements ParametrizedFactory<HashSet, Integer> {

    private int mDefaultSize;
    
    public HashSetFactory() {
        mDefaultSize = 16;
    }
    
    public HashSetFactory(int pDefaultSize) {
        mDefaultSize = pDefaultSize;
    }
    
	public HashSet makeObject() {
		return new HashSet(mDefaultSize);
	}
    
    public HashSet makeObject(Integer pSize) {
        // Logger.info("Making a hashset with size="+pSize);
        return new HashSet(pSize);
    }

}
