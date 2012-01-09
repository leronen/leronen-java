package util.algorithm.clustering;

import util.MathUtils;
import util.collections.UnorderedPair;


public class HClusterDistanceFunction_average_linkage<T> extends HClusterDistanceFunction<T> {
            
    /** Remember to init() later! */
    public HClusterDistanceFunction_average_linkage() {
        super();
    }
    
    public HClusterDistanceFunction_average_linkage(IDistanceFunction<T> pBaseFunction) {                                                    
        super(pBaseFunction);               
    }
    
    /** 
     * It might be that pCluster has just been formed,
     * and we now want to compute the distance between that cluster an a
     * single existing object. Assume distances between pObj and the cluster's 
     * children have already been computed, which drastically eases our work here.
     * 
     * On the other hand, it just might be that the value is actually found in
     * the cache (TODO: should this only be used if value is not in cache?) 
     * 
     */ 
    public Double dist(HCluster<T> pCluster, T pElem) {        
        
        // Logger.info("Computing distance between: "+pCluster+" and "+pElem);
        
        UnorderedPair key = new UnorderedPair(pCluster, pElem);
        
        if (mCachedDistances.containsKey(key)) {
            // in cache (may be null to signify infinite distance)
            return mCachedDistances.get(key);         
        }
        
        Object child1 = pCluster.getChild1();
        Object child2 = pCluster.getChild2();
        
        int childWgt1 = (child1 instanceof HCluster) ? ((HCluster)child1).mNumElements : 1;
        int childWgt2 = (child2 instanceof HCluster) ? ((HCluster)child2).mNumElements : 1;            
                                        
        Double d1 = dist(new UnorderedPair(child1, pElem));
        Double d2 = dist(new UnorderedPair(child2, pElem));
                
        double d = MathUtils.weightedAvg(d1, childWgt1, d2, childWgt2);
                            
        mCachedDistances.put(key, d);        
        
        return d;
    }
            
    
    public Double dist(HCluster<T> p1, HCluster<T> p2) {
        
        UnorderedPair key = new UnorderedPair(p1, p2);
                        
        if (mCachedDistances.containsKey(key)) {
            // in cache (may be null to signifi infinite distance)
            return mCachedDistances.get(key);         
        }
                        
        // have to compute. One of the clusters is "new" and the other is "old"
        // for "old", we already have computed the distances to every other (old)
        // cluster and element, including the children of "new" 
        // (for "new", we do not assuma any distances to be computed!) 
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
        Object child1 = newC.getChild1();
        Object child2 = newC.getChild2();
        
        int childWgt1 = (child1 instanceof HCluster) ? ((HCluster)child1).mNumElements : 1;
        int childWgt2 = (child2 instanceof HCluster) ? ((HCluster)child2).mNumElements : 1;
        
        UnorderedPair pair1 = new UnorderedPair(child1, oldC);
        UnorderedPair pair2 = new UnorderedPair(child2, oldC);
        
        Double d1 = dist(pair1);
        Double d2 = dist(pair2);

        double d = MathUtils.weightedAvg(d1, childWgt1, d2, childWgt2);
                        
        mCachedDistances.put(key, d);
        
        return d;
        
    }
        

}
