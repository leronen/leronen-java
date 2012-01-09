package util.collections;

import java.util.ArrayList;

public final class ArrayStack<T> extends ArrayList<T> {

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
