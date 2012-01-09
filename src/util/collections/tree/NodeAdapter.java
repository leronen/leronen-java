package util.collections.tree;

import java.util.*;


/**
 * Adapts a node of a tree data structure to enable use by various utility classes.
 *  
 * Hmm, having an adapter or making the handled structure implement the node interface are  
 * quite different approaches. Here we take the first of these alternatives...
 **/
public interface NodeAdapter<T> {
    
    /** Return null, if no children */
    public T firstChild(T pNode);
    
    /**
     * Return null, if pNode is the last sibling. 
     */ 
    public T nextBrother(T pNode);
    
    /** Return null, if no parent */
    public T parent(T pNode);
    
    /**
     * Return empty list or null, if no children
     * (unfortunately it seems that this has not been fixed anywhere...
     */
    public List<T> children(T pNode);
    
    // TODO: should this be included for optimization purposes? /      
    // public int numChildren(T pNode);
}
