package util.algorithm.clustering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import util.ConversionUtils;
import util.collections.UnorderedPair;
import util.converter.ObjectToStringConverter;
import util.dbg.Logger;

/** 
 * Distance function for any of:
 *   T,T
 *   T,cluster
 *   cluster,T
 *   cluster,cluster.
 *   
 *   Computes each distance once, caching the distances for later use.
 *   
 *   Note that does not cache the distances defined by mBaseFunction; 
 *   As computing them may also be tedious, we should maybe consider
 *   caching those as well when needed (optional operation!)
 *   
 *    Note that null for dist means "infinite" distance...
 *    Of course, we cannot allow infinite dfi
 */
public abstract class HClusterDistanceFunction<T> {
    
    private boolean mCacheBaseDistances = false;
    
    protected IDistanceFunction<T> mBaseFunction; 
    
    /**
     * Cache distances here on demand, at it may be a bit tedious to
     * calculate them anew. In current impl, entries are never removed from cache.
     */ 
    protected Map<UnorderedPair, Double> mCachedDistances = new HashMap();

    /**
     * For each (object, cluster) and (cluster, cluster) pair, store
     * which was the actual pair of objects used to define the distance.
     * 
     * Of course applicable only for single linkage and complete linkage,
     * in which the distance between clusters is explained by a single
     * pair of objects (one in each cluster).
     */ 
    protected Map<UnorderedPair, UnorderedPair<T>> mCachedPairs = new HashMap();
    
    /**
     * Create uninitialized instance, which has to be initialized later
     * by calling init()     
     */    
    protected HClusterDistanceFunction() {        
        // no action whatsoever 
    }
    
    protected HClusterDistanceFunction(IDistanceFunction<T> pBaseFunction) {                                       
        mBaseFunction = pBaseFunction;       
    }
    
    public void setCacheBaseDistances(boolean pVal) {
        mCacheBaseDistances = pVal;
    }
    
    /** Need cycle through this to enable caching */ 
    private Double dist(T p1, T p2) {
        if (mCacheBaseDistances) {
            Double result = null;
            UnorderedPair key = new UnorderedPair(p1, p2);
            if (mCachedDistances.containsKey(key)) {
                // in cache (may be null to signify infinite distance)
                result = mCachedDistances.get(key);         
            } 
            else {
                // not in cache
                result = mBaseFunction.dist(p1, p2);                
                mCachedDistances.put(key, result);
            }
            return result;
            
        }
        else {
            // no caching
            return mBaseFunction.dist(p1, p2);
        }
    }
    
    public void init(IDistanceFunction<T> pBaseFunction) {
        Logger.info("Initialized HClusterDistanceFunction with base function: "+pBaseFunction);
        mBaseFunction = pBaseFunction;
    }
        
    /** Compute distance between a cluster and an object */
    public abstract Double dist(HCluster<T> pCluster, T pObj);
    
    /** Compute distance between 2 clusters */
    public abstract Double dist(HCluster<T> p1, HCluster<T> p2);
    
    /** pair may contain any combination of cluster and object */
    public Double dist(UnorderedPair pPair) {
        if (mCachedDistances.containsKey(pPair)) {
            return mCachedDistances.get(pPair);
        }        
        
        Object o1 = pPair.getObj1();
        Object o2 = pPair.getObj2(); 
                       
        if (o1 instanceof HCluster) {
            if (o2 instanceof HCluster) {
                // both are clusters
                return dist((HCluster)o1, (HCluster)o2);                 
            }
            else {
                // only o1 is a cluster
                return dist((HCluster)o1, (T)o2);
            }
        }
        else {
            // o1 not a cluster
            if (o2 instanceof HCluster) {
                // only o2 is a cluster
                return dist((HCluster)o2, (T)o1);                 
            }
            else {
                // neither is a cluster
                return dist((T)o1, (T)o2);
            }
        }
    }
    
   /**
    * Given a pair (the elements of which are clusters or "actual objects").
    * return the pair of "actual objects" which was used to define the
    * distance between the members of pPair.
    * 
    * The distance of pPair must already have been queried for, otherwise
    * the desired result shall not be found within our cache.
    * 
    * Of course works only for single linkage and complete linkage,
     * in which the distance between clusters is explained by a single
     * pair of objects (one in each cluster).
    */  
    public UnorderedPair<T> getActualPair(UnorderedPair<T> pPair) {
        if (pPair.getObj1() instanceof HCluster || pPair.getObj2() instanceof HCluster) {
            return mCachedPairs.get(pPair);
        }
        else {
            return pPair;
        }
    }
    
    public enum Linkage {
        SINGLE("single", HClusterDistanceFunction_single_linkage.class),
        COMPLETE("complete", HClusterDistanceFunction_complete_linkage.class),
        AVG("average", HClusterDistanceFunction_average_linkage.class);

        String mName;
        Class mImplClass;
        
        private Linkage(String pName, 
                        Class pImplClass) {
            mName = pName;
            mImplClass = pImplClass;
        }

        public static HClusterDistanceFunction getImpl(String pName, 
                                                       IDistanceFunction pBaseDistanceFunction) throws Exception {
            for (Linkage l: Linkage.values()) {
                if (l.mName.equals(pName)) {
                    HClusterDistanceFunction d = (HClusterDistanceFunction)l.mImplClass.newInstance();
                    d.init(pBaseDistanceFunction);
                    return d;
                }
            }
            throw new RuntimeException("No such linkage function: " + pName);
        }
                

        public String toString() {
            return mName;
        }

        public static List<String> valueList() {
            return ConversionUtils.convert(Arrays.asList(Linkage.values()),
                    new ObjectToStringConverter());
        }
    }
}
