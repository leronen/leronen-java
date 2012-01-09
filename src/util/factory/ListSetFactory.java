package util.factory;

import util.collections.*;


public class ListSetFactory implements ParametrizedFactory<ListSet, Integer> {

	int mDefaultSize;
	
	public ListSetFactory(int pDefaultSize) {
		mDefaultSize = pDefaultSize;
	}
	
	public ListSet makeObject() {
		return new ListSet(mDefaultSize);
	}
    
    public ListSet makeObject(Integer pSize) {
        return new ListSet(pSize);
    }

}