package util.algorithm.clustering2;

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
 * Note that we should now aim for a clear separation between the following 
 * concepts: 
 *   - cluster score (~ avg within-cluster distance) 
 *   - score of combining clusters (~avg distance of node pairs in different clusters)
 *     * for e.g. avg linkage clustering, this in some sense tells how much the
 *       "complete score" will change, or how much worse is the new cluster
 *       compared to the old ones, but it is quite hard to work out an 
 *       exact formula 
 *
 */   
public class HCluster<T> implements ICluster<T> {
    
    /**
     * Distance measure (transformed score) for the cluster; e.g. avg. distance 
     * between members of the cluster. Note that this may differ from the distance
     * between child clusters, which may be also used as an alternative distance 
     * measure... Actually, should be called a "score", but as this is transformed
     * such that smaller is better, we seem to have no better term...
     * 
     * 
     * might not be initialized, in which case shall be NaN! 
     */     
    public double mCost = Double.NaN;

    /** 
     * the "cost of joining the child clusters", somehow normalized by the number 
     * of new node pairs formed. Somehow the distance
     * between the child clusters, which is approximately the (normalized) 
     * increase in the total cluster score, where the normalization is done
     * by the number of node pairs in a cluster.
     * 
     * for average linkage, this is should the "ordinary" join criterion,
     * which is the average distance between pairs of elements in different clusters. 
     * 
     * Of course, this is only computed if join criterion is 
     * {@link HClust2.JoinCriterion#NORMALIZED_JOIN_DISTANCE}.
     * 
     * see comment in class {@link HClust2} on details of the normalization 
     * should be done. 
     */
    public Double mJoinCost;
        
    public UnorderedPair<HCluster<T>> mChildren;
    
    /** Elements (~leaf nodes) in the cluster */
    public Set<T> mMembers;
    
    /**
     * Tells the order in which the clusters were created. That is, ancestor
     * always has a larger number than a descendant.
     */
    public int mTreeNumber = -1;
    
    /**
     * A running integer assigned to clusters (in no particular order) 
     * to clusters that are part of the final clustering.
     */
    Integer mClusterId = null;
    
    public boolean isleaf() {
        return mChildren == null;
    }    
    
    public HCluster getChild1() {
        return mChildren.getObj1();
    }
    
    public HCluster getChild2() {
        return mChildren.getObj2();
    }
        
    public UnorderedPair<HCluster<T>> getChildren() {
        return mChildren;
    }
    
   /** 
    * A running integer assigned to clusters (in no particular order)     
    * to clusters that are part of the final clustering.
    * @see #mTreeNumber.
    */   
    public Integer getId() {
        return mClusterId;
    }
    
    public void setId(int pId) {
        mClusterId = pId;
    }
    
    /** All nodes in the subtree, including the Cluster itself. */
    public Set<HCluster<T>> descendants() {
        return TreeUtils.descendants_breadthfirst(this, new HCluster.TreeNodeAdapter(), true);
    }    
        
    /** Construct a leaf node */
    public HCluster(T pObj, int pNum, double pDistance) {        
        mChildren = null;        
        mTreeNumber = pNum;
        mMembers = Collections.singleton(pObj);
        mCost = pDistance;
    }

    
    /**
     * Construct a non-leaf node. Note that we may create clusters even if 
     * we are not going to put them to the actual tree.
     * 
     * Note that we need to init mDistance and mNum later!
     */
    public HCluster(UnorderedPair<HCluster<T>> pChildren) {
        Logger.dbg("Creating cluster: "+pChildren);
        mChildren = pChildren;
        HCluster<T> c1 = pChildren.getObj1();
        HCluster<T> c2 = pChildren.getObj2();
        mMembers = new HashSet(c1.mMembers.size()+c2.mMembers.size());
        mMembers.addAll(c1.mMembers);
        mMembers.addAll(c2.mMembers);
    }
    
    /** 
     * Compute {@link #mJoinDistance} using within-cluster distances of this cluster
     * and child clusters.
     */
    public void computeJoinCost(HClust2.JoinCriterion jc) {
        HCluster c1 = mChildren.getObj1();
        HCluster c2 = mChildren.getObj2();

        double d = mCost;
        double d1 = c1.mCost;
        double d2 = c2.mCost;
        
        Logger.loudInfo("Computing join cost...");
        Logger.info("candidate: "+this);
        Logger.info("c1: "+c1);
        Logger.info("c2: "+c2);
        Logger.info("d: "+d);
        Logger.info("d1: "+d1);
        Logger.info("d2: "+d2);
        
        if (jc == HClust2.JoinCriterion.NORMALIZED_JOIN_DISTANCE) {
            // normalize by "number of pairs"
            int nPairs = numPairs();
            int nPairs1 = c1.numPairs();
            int nPairs2 = c2.numPairs();
            int nPairs12 = c1.size() * c2.size();
                                   
            Logger.info("npairs: "+nPairs);
            Logger.info("npairs1: "+nPairs1);
            Logger.info("npairs2: "+nPairs2);
            Logger.info("npairs12: "+nPairs2);
            
            mJoinCost = (d * nPairs - d1 * nPairs1 - d2 * nPairs2) / nPairs12;
        }
        else if (jc == HClust2.JoinCriterion.NORMALIZED_JOIN_DISTANCE_MEDOID) {
            // normalize by "number of nodes"
            // "paljonko yksittäisen solmun etäisyys medoidiin muuttuu keskimäärin
            // kun kahden pienemmän klusterin medoidit korvataan isomman klusterin
            // medoidilla?"
            
            // nyt etäisyys muuttunee vähiten silloin, kun 
            // toinen yhdistettävistä klustereista on suuri ja toinen pieni?
            // miten tämän nyt kiertäisi siten että tälläisia yhdistämisiä 
            // ei suosittaisi... oikeastaan pitäisi suosia klustereita 
            // joissa molemmat puoliskot ovat mahdollisimman samankokoisia,
            // eli silloin kuin |n1-n2| "propto" x, where x is the denominator.  
            // eli vaikka n:n sijaan normalisointitekijänä tuo |n1-n2|,
            // joka on maksimissaan n-2, minimissään 0. eli score iso, kun |n1-n2| pieni.
            // TODO: vielä yksi viritys näiden ideoiden pohjalta...
            
            int n = size();
            int n1 = c1.size();
            int n2 = c2.size();           
                                   
            Logger.info("n: "+n);
            Logger.info("n1: "+n1);
            Logger.info("n2: "+n2);            
              
            mJoinCost = (d * n - d1 * n1 - d2 * n2) / n;
            // = (d * n1+n2) - d1 * n1 - d2 * n2) / n;
            // = (d * n1 - d1 * n1) + (d * n2 - d2 * n2) / n
            // = [ (d-d1) * n1 + / (d-d2) * n2 ] / n 
        }
        else if (jc == HClust2.JoinCriterion.JOIN_DISTANCE) {
            // jus the difference 
            mJoinCost = d - d1 - d2;
        }
        else {
            throw new RuntimeException("illegal join criterion: "+jc);
        }
        
        Logger.info("computed join cost:"+mJoinCost);
    }
    
    /** number of node pairs in cluster */
    public int numPairs() {
        int size = mMembers.size();
        return size * (size-1) / 2;
    }
    
//    /** number of node pairs between elements of child clusters */
//    public int numChildPairs() {
//        return mChildren.getObj1().elements().size() * mChildren.getObj2().elements().size();  
//    }
    
    public int size() {
        return mMembers.size();
    }
    
    /**
     *  exp(-distance). Note that here "distance" is not the distance between
     * child clusters, but instead a some measure of "average within-cluster distance",
     * although it is not necessarily based on simple pairwise distance measure.       
     */
    public double getScore() {
        return Math.exp(-mCost);
    }
    
    /**
     *  -log(score).
     * 
     * Note that this is not the distance between 
     * child clusters, but e.g. some measure of "average within-cluster distance"
     * 
     * Might not be initialized!
     */    
    public double getCost() {
        return mCost;
    }
    
    public void setCost(double val) {
        mCost = val;
    }
    
    public Collection<T> members() {
        return mMembers;
    }
              
    @SuppressWarnings("unused")
    private static class GraphWrapper<T> implements IGraph<HCluster<T>> {
        
        /** Should actually represent a Set, but is a Collection for some historical reasons */
        public Collection<HCluster<T>> nodes() {
            throw new UnsupportedOperationException();
        }
        
        /** 
         * In an undirected graph, it always holds that
         * "A in followers(B) <=> B in followers(A)" 
         */
        public Iterable<HCluster<T>> followers(HCluster<T> p) {            
            return p.mChildren != null
                   ? p.mChildren
                   : Collections.EMPTY_LIST;                               
            
        }
        
        public Set<IPair<HCluster<T>,HCluster<T>>> edges() {
            throw new UnsupportedOperationException();
        }
    }
    
    public String toString() {
        return "cluster="+mTreeNumber + 
               ", numelements="+mMembers.size()+
               ", distance="+mCost;
    }
    
    /**
     * probably wasteful, should not be used for processing-intensive purposes 
     */
    public static class TreeNodeAdapter<T> extends DefaultNodeAdapter<HCluster<T>> {
        public List<HCluster<T>> children(HCluster<T> c) {
            if (c.mChildren != null) {
                return new ArrayList(c.mChildren);
            }
            else {
                return Collections.EMPTY_LIST;
            }
        }        
    }

    public String format() {
        if (isleaf()) {
            return mMembers.iterator().next().toString()+" (cluster "+mTreeNumber+")";
        }
        else {
            return "Cluster "+mTreeNumber;
        }
    }
    
    public static class NodeFormatter<T> implements Converter<HCluster<T>, String> {
        
        boolean expMinus = false;
                        
        public String convert(HCluster<T> p) {            
            return p.format()+
                   " num="+p.mTreeNumber+
                   " cost="+p.mCost+
                   " score= "+Math.exp(-p.mCost)+
                   " join cost="+p.mJoinCost+                  
                   " size="+p.size()+
                   " numpairs="+p.numPairs();
                   
        }
    }
    
    /** Mainly for bookkeeping purposes? */
    public void setJoinCost(double pCost) {
        mJoinCost = pCost;
    }
    
    public boolean isOutlier() {
        return size() == 1;        
    }
    
    
    public void setOutlier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int ind) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(T elem) {
        throw new UnsupportedOperationException();       
    }
    
    public void add(T elem) {
        throw new UnsupportedOperationException();
    }
    
}
 