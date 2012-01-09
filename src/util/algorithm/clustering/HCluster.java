package util.algorithm.clustering;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.MathUtils;
import util.collections.IPair;
import util.collections.UnorderedPair;
import util.collections.graph.IGraph;
import util.collections.tree.DefaultNodeAdapter;
import util.collections.tree.TreeUtils;
import util.comparator.ByFieldComparator;
import util.comparator.ComparatorChain;
import util.converter.Converter;
import util.dbg.Logger;

/**
 * A cluster in hierarchical clustering (only internal nodes)  
 */   
public class HCluster<T> {
    
    /**
     * One of: 
     *  (obj,obj)
     *  (cluster,obj)
     *  (cluster,cluster)
     */
    private UnorderedPair mChildren;
    
    /** Distance between the child clusters (depends on the used linkage criteria) */     
    public double mChildDistance;
    
    /**
     * Average distance between members of cluster (computed lazily)
     * Note that even for avg. linkage this is not the same as
     * distance between child clusters, as here we also consider distances
     * within each child cluster
     */
    public Double mAvgDistance;
    public int mNumElements;
    
    
    /**
     * Tells the order in which the clusters were created. That is, ancestor
     * always has a larger number than a descendant.
     */
    int mNum;
    
    public Object getChild1() {
        return mChildren.getObj1();
    }
    
    public Object getChild2() {
        return mChildren.getObj2();
    }
        
    public UnorderedPair getChildren() {
        return mChildren;
    }
    
    /** All nodes in the subtree, including the Cluster itself. */
    public Set descendants() {
        return TreeUtils.descendants_breadthfirst(this, new HCluster.TreeNodeAdapter(), true);
    }
    
    /** All leaves, that is the actual objects */
    public List members() {
        return TreeUtils.leaves(this, new HCluster.TreeNodeAdapter());
    }    

    /** Not so efficient! */
    public T centroid(IDistanceFunction<T> pDist) {
        List<T> members = members();
        return ClusteringUtils.centroid(members, pDist);
    }
    
    public HCluster(UnorderedPair pChildren, double pDistance, int pNum) {
        Logger.dbg("Creating cluster "+pNum+": "+pChildren+" (distance="+pDistance+")");
        mChildren = pChildren;
        mChildDistance = pDistance;
        mNum = pNum;
        Object o1 = pChildren.getObj1();
        Object o2 = pChildren.getObj2();
        mNumElements = 0;
        if (o1 instanceof HCluster) {
            mNumElements += ((HCluster )o1).mNumElements;
        }
        else {
            mNumElements += 1;
        }
        if (o2 instanceof HCluster) {
            mNumElements += ((HCluster)o2).mNumElements;
        }
        else {
            mNumElements += 1;
        }
        
    }
    
    /** To be called on the root only, to make any sense */
    public Map<Object, HCluster> createParentLinks() {
        HashMap<Object, HCluster> result = new HashMap();
        for (Object p: descendants()) {
            if (p instanceof HCluster) {
                HCluster hcp = (HCluster)p;
                for (Object c: hcp.getChildren()) {
                    result.put(c, hcp);
                }                
            }
        }
        return result;
    }
    
//    /** 
//     *     * A bit heavy, as has to collect objects from children... 
//     */
//    public List<T> getContainedObjects() {
//        ArrayList result = new ArrayList();
//        for (Object o: GraphUtils.reachableNodes(new GraphWrapper(), mChildren, true)) {
//            if (!(o instanceof Cluster)) {
//                result.add((T)o);
//            }
//        }
//        return result;
//    }
    
    public Map<String, Number> computeStatistics(IDistanceFunction<T> pDistanceF) {
        Map<String, Number> result = new LinkedHashMap();
        List<T> memberList = members();
        // Set<T> memberSet = new LinkedHashSet(memberList);
        double avgD = getAvgDistance(pDistanceF);
        double maxD = computeMaxDistance(pDistanceF);
        // double avgD2 = computeAvgDistance2(pDistanceF);
        result.put("nummembers", memberList.size());
        /// result.put("numelements", mNumElements);
        result.put("avg_distance",avgD);
        result.put("max_distance",maxD);
        // result.put("avg_distance2",avgD2);
        result.put("Hclust_distance",mChildDistance);
        return result;
    }
           
    public int getNumPairs() {
        return mNumElements * (mNumElements-1) / 2;
    }
    
    /**
     * The number of data points (="objects"="elements"="leaf nodes") in the 
     * cluster 
     */
    public int getNumElements() {
        return mNumElements;
    }
    
    /** Compute average distance of element pairs within the cluster from scratch */
    public double computeAvgDistance_slow(IDistanceFunction<T> pDistanceF) {
        List<T> memberList = members();
        // Set<T> memberSet = new LinkedHashSet(memberList);
        return ClusteringUtils.avgDistance(memberList, pDistanceF);
    }
    
    /**
     * Compute average distance of an arbitrary object to all the members of the cluster.
     */ 
    public double computeAvgDistance(IDistanceFunction<T> pDistanceF,
                                     T pObj) {
        
        List<T> members = (List<T>)members();
        double sum = 0;
        
        for (T member: members) {
            sum += pDistanceF.dist(pObj, member);
        }
                       
        return sum / members.size();
        
    }
    
    /**
     * The maximum distance between any pair of objects in this cluster.
     * For complete linkage clustering, will be the same as mChildDistance. 
     * 
     */
    public double computeMaxDistance(IDistanceFunction<T> pDistanceF) {
        List<T> memberList = members();
        // Set<T> memberSet = new LinkedHashSet(memberList);
        return ClusteringUtils.maxDistance(memberList, pDistanceF);
    }
    
    /**
     * This is just to check that we have computed the between-cluster distances
     * for the child clusters correctly.
     * 
     * Let C1 and C2 denote the child clusters of this cluster.
     * 
     * Compute as a weighted average of elements of the form:
     *   -(c1, c2)
     *   -(c1, c1)
     *   -(c2, c2)
     *   
     * This should be the same as the average distance between elements 
     * of this cluster computed from scratch  
     */
    public double getAvgDistance(IDistanceFunction<T> pDistanceF) {
        
        if (mAvgDistance == null) {
            // not cached yet       
            Object child1 = getChild1();
            Object child2 = getChild2();        
            double wcD_child1 = (child1 instanceof HCluster) ? ((HCluster)child1).getAvgDistance(pDistanceF) : 0;
            double wcD_child2 = (child2 instanceof HCluster) ? ((HCluster)child2).getAvgDistance(pDistanceF) : 0;
            double w_child1 =  (child1 instanceof HCluster) ? ((HCluster)child1).getNumPairs() : 0;
            double w_child2 =  (child2 instanceof HCluster) ? ((HCluster)child2).getNumPairs() : 0;
            int nElems_child1 = (child1 instanceof HCluster) ? ((HCluster)child1).mNumElements : 1;
            int nElems_child2 = (child2 instanceof HCluster) ? ((HCluster)child2).mNumElements : 1;
            double bcD = mChildDistance;
            double bcW = nElems_child1 * nElems_child2;
            mAvgDistance = MathUtils.weightedAvg(wcD_child1, w_child1, // pairs withing C1 
                                                 wcD_child2, w_child2, // pairs withing C1
                                                 bcD, bcW);            // pairs between C1 and C2
        }
        
        return mAvgDistance;
            
            
       
    }
        
    @SuppressWarnings("unused")
    private static class GraphWrapper implements IGraph {
        
        /** Should actually represent a Set, but is a Collection for some historical reasons */
        public Collection nodes() {
            throw new UnsupportedOperationException();
        }
        
        /** 
         * In an undirected graph, it always holds that
         * "A in followers(B) <=> B in followers(A)" 
         */
        public Iterable followers(Object p) {
            if (p instanceof HCluster) {
                HCluster c = (HCluster)p;
                return c.mChildren;
            }
            else {
                return Collections.EMPTY_LIST;
            }
        }
        
        public Set<IPair> edges() {
            throw new UnsupportedOperationException();
        }
    }
    
    public String toString() {
        return "cluster "+mNum;
    }
    
    /**
     * probably wasteful, should not be used for processing-intensive purposes 
     */
    public static class TreeNodeAdapter extends DefaultNodeAdapter {
        public List children(Object p) {
            if (p instanceof HCluster) {
                HCluster c = (HCluster)p;
                return new ArrayList(c.mChildren);
            }
            else {
                return Collections.EMPTY_LIST;
            }
        }        
    }

    static class NodeFormatter implements Converter {
        
        public Object convert(Object p) {
            if (p instanceof HCluster) {
                HCluster c = (HCluster)p;
                return "Cluster "+c.mNum+" d="+c.mChildDistance;
            }
            else if (p instanceof Point2D) {
                Point2D point = (Point2D)p;
                return "("+point.getX()+","+point.getY()+")";
            }
            else {
                return p.toString();
            }
        }
    }
    
    static class NewickNodeFormatter implements Converter {
        
        private Map<Object, HCluster> mParentLinks;
        
        public NewickNodeFormatter(Map<Object, HCluster> pParentLinks) {
            mParentLinks = pParentLinks;
        }
        
        public Object convert(Object p) {
            if (p instanceof HCluster) {
                HCluster c = (HCluster)p;                
                
                double branchlength;
                
                HCluster parent = mParentLinks.get(c);
                if (parent == null) {
                    branchlength = 0;
                }
                else {
                    // branch length is the difference in this node's
                    // height and its parent's height
                    branchlength = parent.mChildDistance-c.mChildDistance;  
                }
                
                return "Cluster_"+c.mNum+':'+branchlength; // +" d="+c.mChildDistance;
            }
//            else if (p instanceof Point2D) {
//                Point2D point = (Point2D)p;
//                return "("+point.getX()+","+point.getY()+")";
//            }
            else {
                HCluster parent = mParentLinks.get(p);
                // branch length is just the parent's height, as leaves
                // are on height 0
                double branchlength = parent.mChildDistance;
                return p.toString().replace(':', '.')+':'+branchlength;
            }
        }
    }
    
    /**
     * Considers node which should be split first the LARGEST (have to reverse...)
     */ 
    public static class TreeCuttingComparator extends ComparatorChain {
        public TreeCuttingComparator() {
            super(new InternalByDistanceComparator(), new CreationOrderComparator());
        }
    }
    
    /**
     * Compare tree nodes by the distance between their child nodes. 
     * For leaf nodes, consider the distance to be zero.
     */
    private static class InternalByDistanceComparator extends ByFieldComparator {
                
        public InternalByDistanceComparator() {
            super(new DistanceExtractor());
        }        
    }
    
    /**
     * Used to break ties between descendant/ancestor that have the same
     * "distance". An ancestor should always be considered "larger" than
     * a descendant.     
     */
    private static class CreationOrderComparator extends ByFieldComparator {
        public CreationOrderComparator() {
            super(new CreationIndexExtractor());
        }
    }
    
    /** 
     * Should be 1 for the first created node and so on. Return 0 for leaf nodes
     * (actual objects).
     */
    private static class CreationIndexExtractor implements Converter<Object, Integer> {
        public Integer convert(Object p) {         
            if (p instanceof HCluster) {
                // a non-leaf node
                return ((HCluster)p).mNum;
            }
            else {
                // a leaf node; define "distance between children" to be zero
                return 0;
            }           
        }
    }
    
    
    private static class DistanceExtractor implements Converter<Object, Double> {
        public Double convert(Object p) {         
            if (p instanceof HCluster) {
                // a non-leaf node
                return ((HCluster)p).mChildDistance;
            }
            else {
                // a leaf node; define "distance between children" to be zero
                return 0.d;
            }
            
        }
    }
}
 