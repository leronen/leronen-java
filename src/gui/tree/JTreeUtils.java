package gui.tree;

import util.*;
import util.converter.*;
import util.collections.tree.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

public class JTreeUtils {
    
    public static Object getFirstSelectedUserObject(JTree pTree) {        
        TreePath path = pTree.getSelectionModel().getSelectionPath();        
        if (path == null) {            
            return null;
        }        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();                        
        Object result = node.getUserObject();                                
        return result;           
    }
    
    public static void setSelectedUserObject(JTree pTree, Object pUserObject) {
        Map nodeByUserObject = makeNodeByUserObjectMap(pTree);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeByUserObject.get(pUserObject);        
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            pTree.getSelectionModel().setSelectionPath(path);
            pTree.scrollPathToVisible(path);
        }                                                 
    }
    
    public static Object[] getSelectedUserObjects(JTree pTree) {
        TreePath[] paths = pTree.getSelectionModel().getSelectionPaths();
        if (paths == null) {
            return new Object[0];
        }
        Object[] result = new Object[paths.length];
        for (int i=0; i<result.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
            result[i] = node.getUserObject(); 
        }
                                                                
        return result;           
    }
        
    private static Map makeNodeByUserObjectMap(JTree pTree) {        
        Set allNodes = TreeUtils.descendants_depthFirst(pTree.getModel().getRoot(), new SwingNodeAdapter(), true);        
        Map treeNodeByUserObject = CollectionUtils.makeMap(allNodes, new TreeNodeToUserObjectConverter());
        return treeNodeByUserObject;           
    }
    
    public static class TreeNodeToUserObjectConverter implements Converter {
        public Object convert(Object p) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)p;
            return node.getUserObject();
        }
    }
        
}


