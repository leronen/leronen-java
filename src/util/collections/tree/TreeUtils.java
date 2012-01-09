package util.collections.tree;

import util.*;
import java.util.*;

/**
 * Ultimate goal: we want to have tree-related common functionality, independent of 
 * the dreaded javax.swing.tree package...
 * 
 */
public class TreeUtils {
    
    public static boolean isDescendant(Object pPotentialDescendant,
                                       Object pPotentialAncestor,
                                       NodeAdapter pNodeAdapter) {
        Set ancestors = ancestors(pPotentialDescendant, pNodeAdapter);
                                    
        boolean result = ancestors.contains(pPotentialAncestor);
                
        // dbgMsg(""+pPotentialDescendant+" is "+(result ? "" : "not ")+"an descendant of "+pPotentialAncestor);
        
        return result;
    }
    
    /** 
     * Path to ancestor, including pNode but not pAncestor.
     * if pNode is not and descendant of pAncestor, return null. 
     *
     */
    public static List getPathToAncestor(Object pNode, Object pAncestor, NodeAdapter pNodeAdapter) {
        ArrayList path = new ArrayList();
        
        path.add(pNode);
        
        Object curNode = pNode;        
        Object parent = pNodeAdapter.parent(curNode);
        
        while(!(parent.equals(pAncestor)) && parent != null) {
            path.add(parent);
            curNode = parent;
            parent = pNodeAdapter.parent(curNode);
        }
        
        if (parent == null) {
            // throw new RuntimeException("Did not find a path from "+pNode+" to "+pAncestor);            
            return null;
        }                
        
        return path;
    }
    
    public static Set ancestors(Object pNode, NodeAdapter pNodeAdapter) {
        return ancestors(pNode, pNodeAdapter, false);
    }
    
    public static Set ancestors(Object pNode, NodeAdapter pNodeAdapter, boolean pIncludeSelf) {
                          
        LinkedHashSet ancestors = new LinkedHashSet();
        if (pIncludeSelf) {
            ancestors.add(pNode);
        }
        
        Object curNode = pNode;        
        Object parent = pNodeAdapter.parent(curNode);
        
        while(parent != null) {
            ancestors.add(parent);
            curNode = parent;
            parent = pNodeAdapter.parent(curNode);
        }
        
        // dbgMsg("Ancestors of "+pNode+":\n"+
               // StringUtils.collectionToString(ancestors));
        
        return ancestors;
        
    }
    
//    public static <T> Set<T> closure_depthfirst(T pRoot, NodeAdapter<T> pNodeAdapter) {
//        Iterator<T> nodeIter = new DepthFirstTreeIterator(pRoot, pNodeAdapter);
//        LinkedHashSet<T> allNodes = new LinkedHashSet(CollectionUtils.makeArrayList(nodeIter));                                
//
//        return allNodes;    
//    }
        
        
    public static <T> Set<T> descendants_depthFirst(T pRoot, NodeAdapter<T> pNodeAdapter, boolean pIncludeRoot) {
                   
        Iterator<T> descendantIter = new DepthFirstTreeIterator(pRoot, pNodeAdapter);
        LinkedHashSet<T> descendants = new LinkedHashSet(CollectionUtils.makeArrayList(descendantIter));
        
        if (!pIncludeRoot) {
            descendants.remove(pRoot);
        }

        return descendants;        
    }
    
    /**
     * Find out the leaves of a tree. If pRoot has no children, then return
     * a list containing just the root.
     */ 
    public static <T> List<T> leaves(T pRoot, NodeAdapter<T> pNodeAdapter) {
        
        if (pNodeAdapter.children(pRoot).size() == 0) {
            return Collections.singletonList(pRoot);
        }
        else {                   
            List<T> result = new ArrayList();
        
            Iterator<T> descendantIter = new BreadthFirstTreeIterator(pRoot, pNodeAdapter);
            while (descendantIter.hasNext()) {
                T o = descendantIter.next();
                if (pNodeAdapter.children(o).size() == 0) {
                    result.add(o);
                }                
            }
            return result;
        }

                
    }
    
    public static <T> Set<T> descendants_breadthfirst(T pNode, NodeAdapter<T> pNodeAdapter, boolean pIncludeRoot) {
        
        Iterator<T> descendantIter = new BreadthFirstTreeIterator(pNode, pNodeAdapter);
        LinkedHashSet<T> descendants = new LinkedHashSet<T>(CollectionUtils.makeArrayList(descendantIter));
        
        if (!pIncludeRoot) {
        	descendants.remove(pNode);
        }

        return descendants;        
    }

    /** 
     * Return the most specific common ancestor. The ancestor may be in the
     * set given as parameter.
     * Return null, if no common ancestor.
     */
    public static <T> T mostSpecificCommonAncestor(Set<T> pNodes, NodeAdapter<T> pNodeAdapter) {
        Set<T> commonAncestors = null;
        
        // Compute set of nodes which are ancestors to all nodes in pNodes
        for (T n: pNodes) {
            Set<T> ancestors = ancestors(n, pNodeAdapter);
            if (commonAncestors == null) {
                // first set
                commonAncestors = ancestors;                
            }
            else {
                commonAncestors.retainAll(ancestors);
            }
        }
        if (commonAncestors.size() == 0) {
            return null;
        }        
        
        // it is now trivial to find the most specific one; it is the one
        // that has no children in the set (OK, a bit slowish solution...)
        for (T ancestor: commonAncestors) {
            if (CollectionUtils.intersection(pNodeAdapter.children(ancestor), commonAncestors).size()==0) {
                return ancestor;
            }
        }
        // OK, the most specific one should have been found, so this is an error!
        throw new RuntimeException("No most specific common ancestor?!?");
    }
    
    
}




