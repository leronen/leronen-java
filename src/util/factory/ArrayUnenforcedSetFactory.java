package util.factory;

import util.collections.ArrayUnenforcedSet;

public class ArrayUnenforcedSetFactory implements ParametrizedFactory<ArrayUnenforcedSet, Integer> {

    private int mDefaultSize;
    
    public ArrayUnenforcedSetFactory() {
        mDefaultSize = 4;
    }
    
    public ArrayUnenforcedSetFactory(int pDefaultSize) {
        mDefaultSize = pDefaultSize;
    }
    
    public ArrayUnenforcedSet makeObject() {
        return new ArrayUnenforcedSet(mDefaultSize);
    }
    
    public ArrayUnenforcedSet makeObject(Integer pSize) {
        // Logger.info("Making a hashset with size="+pSize);
        return new ArrayUnenforcedSet(pSize);
    }        

}
