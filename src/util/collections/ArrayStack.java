package util.collections;

import java.util.ArrayList;

public final class ArrayStack<T> extends ArrayList<T> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8270977521504665314L;

	public T peek() {
        return get(size()-1);        
    }
    
    public T pop() {
        return remove(size()-1);        
    }
    
    public boolean push(T p) {
        return add(p);
    }
}
