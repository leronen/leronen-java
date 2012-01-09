package util.algorithm.clustering.reliability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

import util.collections.IPair;
import util.collections.UnorderedPair;
import util.collections.graph.IGraph;
import util.collections.tree.DefaultNodeAdapter;
import util.collections.tree.TreeUtils;
import util.converter.Converter;
import util.dbg.Logger;

/**
 * A cluster rep used in the HClust_rel variant of hierarchical clustering.
 * 
 * Let's try to implement this such that there is always a Cluster object,
 * even for leaf nodes. This should reduce various if-else-hassles.
 *
 */   
public class Cluster<T> {
    
    /** Distance between the child clusters (depends on the used linkage criteria) */     
    public double mScore;
    
    public UnorderedPair<Cluster<T>> mChildren;
    
    /** Elements in the cluster */
    public Set<T> mElements;
    
    /**
     * Tells the order in which the clusters were created. That is, ancestor
     * always has a larger number than a descendant.
     */
    int mNum;
    
    public boolean isleaf() {
        return mChildren == null;
    }    
    
    public Cluster getChild1() {
        return mChildren.getObj1();
    }
    
    public Cluster getChild2() {
        return mChildren.getObj2();
    }
        
    public UnorderedPair<Cluster<T>> getChildren() {
        return mChildren;
    }
    
    /** All nodes in the subtree, including the Cluster itself. */
    public Set<Cluster<T>> descendants() {
        return TreeUtils.descendants_breadthfirst(this, new Cluster.TreeNodeAdapter(), true);
    }
    
//    /** All leaves, that is the actual objects */
//    public List<T> members() {
//        
//        
//        
//    }    
        
    /** Construct a leaf node */
    public Cluster(T pObj, int pNum, double pScore) {        
        mChildren = null;        
        mNum = pNum;
        mElements = Collections.singleton(pObj);
        mScore = pScore;
    }
    
    /** Construct a non-leaf node */
    public Cluster(UnorderedPair<Cluster<T>> pChildren, 
                   double pScore, 
                   int pNum) {
        Logger.dbg("Creating cluster "+pNum+": "+pChildren);
        mChildren = pChildren;
        Cluster<T> c1 = pChildren.getObj1();
        Cluster<T> c2 = pChildren.getObj2();
        mElements = new HashSet(c1.mElements.size()+c2.mElements.size());
        mElements.addAll(c1.mElements);
        mElements.addAll(c2.mElements);
        mScore = pScore;
        mNum = pNum;
    }
    
    public Set<T> elements() {
        return mElements;
    }
              
    @SuppressWarnings("unused")
    private static class GraphWrapper<T> implements IGraph<Cluster<T>> {
        
        /** Should actually represent a Set, but is a Collection for some historical reasons */
        public Collection<Cluster<T>> nodes() {
            throw new UnsupportedOperationException();
        }
        
        /** 
         * In an undirected graph, it always holds that
         * "A in followers(B) <=> B in followers(A)" 
         */
        public Iterable<Cluster<T>> followers(Cluster<T> p) {            
            return p.mChildren != null
                   ? p.mChildren
                   : Collections.EMPTY_LIST;                               
            
        }
        
        public Set<IPair<Cluster<T>,Cluster<T>>> edges() {
            throw new UnsupportedOperationException();
        }
    }
    
    public String toString() {
        return "cluster="+mNum + 
               ", numelements="+mElements.size()+
               ", score="+mScore;
    }
    
    /**
     * probably wasteful, should not be used for processing-intensive purposes 
     */
    public static class TreeNodeAdapter extends DefaultNodeAdapter {
        public List children(Object p) {
            if (p instanceof Cluster) {
                Cluster c = (Cluster)p;
                return new ArrayList(c.mChildren);
            }
            else {
                return Collections.EMPTY_LIST;
            }
        }        
    }

    static class NodeFormatter<T> implements Converter<Cluster<T>, String> {
        
        public String convert(Cluster<T> p) {            
            return "Cluster "+p.mNum+" score="+p.mScore+" num="+p.mNum;            
        }
    }
    
}
 