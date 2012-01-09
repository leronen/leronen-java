package util.algorithm.clustering2;

import java.util.Collection;
import java.util.List;

import util.StringUtils;
import util.dbg.Logger;


/**
 * A "flat" cluster
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
public class FCluster<T> implements ICluster<T> {
    
    /**
     * Distance measure (transformed score) for the cluster; e.g. avg. distance 
     * between members of the cluster. Note that this may differ from the distance
     * between child clusters, which may be also used as an alternative distance 
     * measure... Actually, should be called a "score", but as this is transformed
     * such that smaller is better, we seem to have no better term...
     * 
     * TODO: introduce field childDistance...
     */     
    private double mCost = Double.NaN;
   
    /** Elements (~leaf nodes) in the cluster */
    private Collection<T> mMembers;
            
    /**
     * A running integer assigned to clusters (in no particular order) 
     * to clusters that are part of the final clustering.
     */
    private Integer mClusterId = null;
        
    private boolean mOutlier;
    
    public FCluster(Collection<T> pMembers, boolean pIsOutlier) {                       
        mMembers = pMembers;        
        mOutlier = pIsOutlier;
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
        
     
    public void setCost(double pCost) {
        mCost = pCost;
    }
    
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
     */    
    public double getCost() {
        return mCost;
    }
    
    public Collection<T> members() {
        return mMembers;
    }
              
    /** set oultier status of this cluster to true */
    public void setOutlier() {
        Logger.warning("Outlier status should already be set in FCluster "+
                       "(was:"+mOutlier+", setting to: true");                       
        mOutlier = true;
    }
    
    /** May be unsupported by non-list based implementations! */
    public T get(int pInd) {
        if (mMembers instanceof List) {
            return ((List<T>)mMembers).get(pInd);
        }
        else {
            throw new UnsupportedOperationException("Member collection not implemented as a list, but instead as a: "+mMembers.getClass().getName());
        }
    }
    
    @Override
    public boolean isOutlier() {
        return mOutlier;
    }
    
    public String toString() {
        return StringUtils.collectionToString(mMembers, ",")+"("+isOutlier()+")";
//        return "cluster="+getId()+ 
//               ", numelements="+mMembers.size()+
//               ", distance="+mCost;
    }

    @Override
    public void remove(T elem) {
        mMembers.remove(elem);         
    }

    @Override
    public void add(T elem) {
        mMembers.add(elem);        
    }

    
    public boolean equals(Object o) {
        FCluster<T> other = (FCluster<T>)o;
        return mMembers.equals(other.mMembers);
    }
    
    public int hashCode() {
        return mMembers.hashCode();
    }
    
    
}
 