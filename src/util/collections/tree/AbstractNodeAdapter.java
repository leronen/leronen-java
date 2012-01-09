package util.collections.tree;

import java.util.*;


/**
 * Adapts a node of a tree data structure to enable use by various utility classes.
 * 
 *
 * Hmm, having an adapter or making the handled structure implement the node interface are  
 * quite different approaches. Here we take the first of these alternatives...
 **/
public abstract class AbstractNodeAdapter<T> implements NodeAdapter<T> {
    
    public T firstChild(T pNode) { throw new UnsupportedOperationException(); }                
    public T nextBrother(T pNode) { throw new UnsupportedOperationException(); }
    public T parent(T pNode) { throw new UnsupportedOperationException(); }
    public List<T> children(T pNode) { throw new UnsupportedOperationException(); }            
}
