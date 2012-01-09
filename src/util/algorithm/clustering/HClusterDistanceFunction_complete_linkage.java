package util.algorithm.clustering;

import util.collections.UnorderedPair;

public class HClusterDistanceFunction_complete_linkage<T> extends HClusterDistanceFunction<T> {
        
    
    public HClusterDistanceFunction_complete_linkage() {
        super();
    }
    
    public HClusterDistanceFunction_complete_linkage(IDistanceFunction<T> pBaseFunction) {                                                    
        super(pBaseFunction);               
    }
    
    /** 
     * It might be that pCluster has just been formed,
     * and we now want to compute the distance between that cluster an a
     * complete existing object. Assume distances between pObj and the cluster' 
     * children have already been computed, which drastically eases our work here.
     * 
     * On the other hand, it just might be that the value is actually found in
     * the cache (TODO: should this only be used if value is not in cache?) 
     * 
     */ 
    public Double dist(HCluster<T> pCluster, T pObj) {        
        
        // Logger.info("Computing distance between: "+pCluster+" and "+pObj);
        
        UnorderedPair key = new UnorderedPair(pCluster, pObj);
        
        if (mCachedDistances.containsKey(key)) {
            // in cache (may be null to signify infinite distance)
            return mCachedDistances.get(key);         
        }
        
        UnorderedPair pair1 = new UnorderedPair(pCluster.getChild1(), pObj);
        UnorderedPair pair2 = new UnorderedPair(pCluster.getChild2(), pObj);
        
        Double d = null;
        Double d1 = dist(new UnorderedPair(pCluster.getChild1(), pObj));
        Double d2 = dist(new UnorderedPair(pCluster.getChild2(), pObj));
        
        // pairs of original objects used in defining the distances above
        UnorderedPair actualPair = null;
        UnorderedPair<T> actualPair1;
        UnorderedPair<T> actualPair2;
        
        if (pair1.getObj1() instanceof HCluster) {
            // should be cached
            actualPair1 = mCachedPairs.get(pair1);
        }
        else {
            // pair of "base objects" is in itself trivially the "actual pair"            
            actualPair1 = pair1;            
        }
        
        if (pair2.getObj1() instanceof HCluster) {
            // should be cached
            actualPair2 = mCachedPairs.get(pair2);
        }
        else {
            // pair of "base objects" is in itself trivially the "actual pair"            
            actualPair2 = pair2;            
        }
                                                   
        if (d1 == null) {            
            d = null;
            actualPair = actualPair1;
        }
        else if (d2 == null) {
            d = null;
            actualPair = actualPair2;
        }
        else {
            if (d1 <= d2) {
                d = d2;
                actualPair = actualPair2;
            }
            else {
                d = d1;
                actualPair = actualPair1;
            }
        }
        
        mCachedDistances.put(key, d);
        mCachedPairs.put(key, actualPair);
        
        return d;
    }
            
    
    public Double dist(HCluster<T> p1, HCluster<T> p2) {
        
        UnorderedPair key = new UnorderedPair(p1, p2);
                        
        if (mCachedDistances.containsKey(key)) {
            // in cache (may be null to signify infinite distance)
            return mCachedDistances.get(key);         
        }
                        
        // have to compute. One of the clusters is "new" and the other is "old"
        HCluster<T> newC;
        HCluster<T> oldC;
        if (p1.mNum > p2.mNum) {
            newC = p1; 
            oldC = p2;
        }
        else {
            newC = p2; 
            oldC = p1;
        }
        
        // for the children of the new cluster, we already know distances to the old cluster:        
        UnorderedPair pair1 = new UnorderedPair(newC.getChild1(), oldC);
        UnorderedPair pair2 = new UnorderedPair(newC.getChild2(), oldC);

        Double d = null;
        Double d1 = dist(pair1);
        Double d2 = dist(pair2);

        // pairs of original objects used in defining the distances above
        UnorderedPair actualPair = null;       
        UnorderedPair<T> actualPair1 = mCachedPairs.get(pair1);
        UnorderedPair<T> actualPair2 = mCachedPairs.get(pair2);
        
        if (d1 == null) {            
            d = null;
            actualPair = actualPair1;
        }
        else if (d2 == null) {
            d = null;
            actualPair = actualPair2;
        }
        else {
            // neither is null
            if (d1 <= d2) {
                d = d2;
                actualPair = actualPair2;
            }
            else {
                d = d1;
                actualPair = actualPair1;
            }
        }
        
        mCachedDistances.put(key, d);
        mCachedPairs.put(key, actualPair);
        
        return d;
        
    }
        

}
