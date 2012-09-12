package util.collections.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Depth-first tree iterator without the stupid requirement of needing the methods firstChild, nextBrother
 * and parent. 
 */
public class DepthFirstTreeIterator2<T> implements Iterator<T> {
	private NodeAdapter<T> nodeAdapter;    
    private LinkedList<T> stack;        
    
    public DepthFirstTreeIterator2(T pRoot, NodeAdapter<T> pAdapter) {        
        nodeAdapter = pAdapter;
        stack = new LinkedList<T>();
        stack.add(pRoot);                                                        
    }                                       
        
    public boolean hasNext() {
//    	System.err.println("hasNext: "+(stack.size() > 0));
        return stack.size() > 0;
    }

    public T next() {	    	
        T node = stack.removeLast();
//        System.err.println("popped: "+node);
        List<T> children = nodeAdapter.children(node);
        // iterate children in reverse order:
        for (int i=children.size()-1; i>=0; i--) {
//        	System.err.println("adding "+i+"th element to stack: "+children.get(i));
            stack.addLast(children.get(i));
        }
//        System.err.println("returning: "+node);
        return node;        
    }    
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }
}