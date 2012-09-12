package util.collections.tree;

import java.util.*;


/**
 * Adapts a node of a tree data structure to enable use by various utility classes,
 * without the stupid requirement (of NodeAdapter(1)) of having the nuisance methods
 * firstChild, nextBrother and parent. 
 *   
 **/
public interface NodeAdapter2<T> {
    
    /**
     * Return empty list, if no children.
     */
    public List<T> children(T pNode);
}    

