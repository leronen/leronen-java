package util.collections.tree;

import java.util.*;

/**
 * Only requires that NodeAdapter implements children().
 */
public class BreadthFirstTreeIterator<T> implements Iterator<T> {
    
    private NodeAdapter<T> mNodeAdapter;    
    private LinkedList<T> mQueue;        
    
    public BreadthFirstTreeIterator(T pRoot,
                                    NodeAdapter<T> pAdapter) {        
        mNodeAdapter = pAdapter;
        mQueue = new LinkedList();
        mQueue.add(pRoot);                                                        
    }                                       
        
    public boolean hasNext() {
        return mQueue.size() > 0;
    }

    public T next() {                
        T node = mQueue.removeFirst();
        List<T> children = mNodeAdapter.children(node);
        for (int i=0; i<children.size(); i++) {        
            mQueue.addLast(children.get(i));
        }
        return node;        
    }    
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }    
    
}




