package util.collections.tree;

import java.util.*;


/**
 * Adapts a node of an arbitrary tree data structure. Type paremeter T specifies the class of the actual node implementation. 
 */
public interface TreeNodeAdapter<T> {
    
    /**
     * Return empty list, if no children.
     */
    public List<T> children(T node);
}    

