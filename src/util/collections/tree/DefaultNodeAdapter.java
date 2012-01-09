package util.collections.tree;

import java.util.*;


/**
 * Adapts a node of a tree data structure to enable use by various utility classes.
 * 
 * Implementors must implement at least children() and parent(); all others have a default
 * implementation using them; the default implementation assumes that 
 * siblings are not equal,  and properly implement equals() and hashCode().
 *  
 * OK, it is possible that a different set of operations shall suffice...
 * 
 **/
public abstract class DefaultNodeAdapter<T> implements NodeAdapter<T> {
    
    public T firstChild(T pNode) {
        List<T> children = children(pNode);
        if (children == null || children.size() == 0) {
            return null;
        }
        else {
            return children.get(0);
        }
        // throw new UnsupportedOperationException(); 
    }
    
    public T nextBrother(T pNode) {
        T parent = parent(pNode);
        if (parent == null) {
            // obviously no brothers!
            return null;
        }
        else {
            List<T> siblings = children(parent);
            int index = siblings.indexOf(pNode);
            if (index == siblings.size()-1) {
                // last sibling
                return null;            
            }
            else {
                return siblings.get(index+1);
            }
        }
        // throw new UnsupportedOperationException();
    }
    
    /** 
     * Must implement this for the default implementations above to work. 
     */  
    public T parent(T pNode) { 
        throw new UnsupportedOperationException();
    }
    
    /** 
     * An absolute must for a node adapter (this is all a breadth first-iterator
     * needs, for instance).
     */
    public abstract List<T> children(T pNode);
        //throw new UnsupportedOperationException();
    // }
}
