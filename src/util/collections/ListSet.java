package util.collections;

import java.util.*;

/**
 * Set based on ArrayList. Enforces uniqueness of elements. 
 * For a lighter alternative, @see ArrayunenforcedSet
 */
public class ListSet<E> extends AbstractSet<E> {
	
	private ArrayList mData;
	
	public ListSet(int pDefaultSize) {
		mData = new ArrayList(pDefaultSize);
	}
		
	public boolean add(E o) {
		if (!mData.contains(o)) {
			mData.add(o);
			return true;
		}
		else {
			return false;
		}
	}
			
	public void clear() {
		mData.clear();
	}
	
	public boolean contains(Object o) {
		return mData.contains(o);
	}
	
	public boolean remove(Object o) {
		return mData.remove(o);
	}
	
	public int size() {
		return mData.size();
	}
	
	public Iterator iterator() {
		return mData.iterator();
	}	

}
    
