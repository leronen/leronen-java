package util.collections.tree;

import java.util.*;

/**
 * Ultimate goal: we want to have tree-related common functionality, independent of 
 * the dreaded javax.swing.tree package...
 *
 * Let's have this class as a modest vantage point.
 * 
 * Iterates the nodes of a tree in a depth-first order, "esijärjestyksessä"
 *
 * TODO: remove dependency on clumsy firstChild(), nextBrother() and parent();
 * use a dedicated stack instead.
 * 
 */
public class DepthFirstTreeIterator implements Iterator {
    
    private NodeAdapter mNodeAdapter;    
    
    private Object mNextNode;
    
    public DepthFirstTreeIterator(Object pRoot,
                                  NodeAdapter pAdapter) {
        this (pRoot, pAdapter, false);
    }            
    
    public DepthFirstTreeIterator(Object pRoot,
                                  NodeAdapter pAdapter,
                                  boolean pIncludeRoot) {
        mNodeAdapter = pAdapter;
        if (pIncludeRoot) {
            mNextNode = pRoot;
        }
        else {
            mNextNode = findNextNode(pRoot);
        }
    }                                       
        
    public boolean hasNext() {
        return mNextNode != null;
    }

    public Object next() {
        if (mNextNode == null) {
            throw new NoSuchElementException();
        }
        Object result = mNextNode;
        mNextNode = findNextNode(mNextNode);
        return result;        
    }

    private Object findNextNode(Object pNode) {        
        Object child = mNodeAdapter.firstChild(pNode);
        if (child != null) {    
            return child;
        }
        // OK, no children...
        Object curNode = pNode;
        Object brotherOrUncle = mNodeAdapter.nextBrother(curNode);
        while (brotherOrUncle == null && curNode != null) {
            // OK, no brother for curNode, 
            // let's try an uncle, then...
            curNode = mNodeAdapter.parent(curNode);
            if (curNode != null) {
                brotherOrUncle = mNodeAdapter.nextBrother(curNode);
            }
        }                        
        return brotherOrUncle;          
    }        
    
    public void remove() {
        throw new UnsupportedOperationException();    
    }    
    
}

