package gui.tree;

import util.*;
import util.collections.iterator.EnumerationIterator;

import java.util.*;

import javax.swing.tree.*;

/**
 *  Adapts File to general-purpose tree utility methods 
 *
 *  Note that file must be a canonical file to work!
 */
public class SwingNodeAdapter implements util.collections.tree.NodeAdapter {
    
    public Object firstChild(Object pNode) {
        TreeNode node = (TreeNode)pNode;
        if (node.getChildCount() == 0) {
            return null;
        }
        else {
            return node.getChildAt(0);
        }               
    }
    
    public Object nextBrother(Object pNode) {
        TreeNode node = (TreeNode)pNode;
        TreeNode parent = node.getParent();
        if (parent == null) {
            // this is probably the root, or else...
            return null;
        }
        int index = parent.getIndex(node);
        int numBrothers = parent.getChildCount(); // this includes us
        if (index < numBrothers-1) {
            // there should be a next brother
            return parent.getChildAt(index+1);
        }
        else {
            // no more brothers
            return null;
        }            
    }
           
    public Object parent(Object pNode) {        
        TreeNode node = (TreeNode)pNode;
        return node.getParent();        
    }
    
    public List children(Object pNode) {
        TreeNode node = (TreeNode)pNode;
        return CollectionUtils.makeArrayList(new EnumerationIterator(node.children()));                                                
    }    


}
