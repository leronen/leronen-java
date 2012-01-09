package util.factory;

import java.util.*;

public class ArrayListFactory<T> implements Factory<List<T>> {

	private int mDefaultSize;
	
	public ArrayListFactory() {
		mDefaultSize = 10;
	}
	
	public ArrayListFactory(int pDefaultSize) {
		mDefaultSize = pDefaultSize;
	}
	
	public List<T> makeObject() {
		return new ArrayList<T>(mDefaultSize);
	}

}
