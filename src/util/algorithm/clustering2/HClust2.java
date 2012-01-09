package util.algorithm.clustering2;

import java.util.*;

//import util.SU;
import util.collections.BinaryHeap;
import util.collections.Function;
import util.collections.MultiMap;
import util.collections.UnorderedPair;
import util.collections.BinaryHeap.Mode;
import util.dbg.Logger;


/**
 * Agglomerative clustering allowing pluggable methods for computing cluster
 * scores. For the sake of generality and modularity, does not include a CLI. 
 * 
 * This is hopefully a step towards a more generic implementation from
 * the stagnated class util.algorithm.clustering.HClust, which contains the
 * erraneous design decision of making not using a dedicated class for implementing
 * the leaf nodes of the hierarchical clustering tree, as well as being too 
 * dependent on the concept distance-based clustering (that is, everything is 
 * based on the pairwise distances). This class is actually an edit of the 
 * class Hclust_rel (residing in the package util.algorithm.clustering.reliability).
 * 
 * Type param T is the type of objects to be clustered.
 * 
 * A key question actually is, whether one should: 
 *    (I) directly aim for as good as possible 
 *        clusters (e.g. use avg. within cluster distance of new cluster) 
 *      ,or just:
 *    (II) try to minimize the reduction in the scores when two clusters are combined 
 *          into one. 
 *  
 *  More specifically, can (I) lead to any grave consequences, as (II) seems
 *  to be the de-facto standard? If there is a really good cluster C1 and a
 *  clearly worse one C2, it would probably just lead to adding any remaining
 *  stuff to C1. On the other hand, it is very difficult to somehow normalize
 *  the score in (II). If one has composite score, it would indeed be in principle
 *  easy to define the cost of combining 2 clusters as the change in the composite
 *  score. For something like avg linkage, a composite score is not well 
 *  defined, and seems not to be standardized in the literature.
 *  It seems that the space of possible normalizations is too high to make 
 *  any sense out of it all. Say that the cluster sizes would not matter; then 
 *  the composite score could be defined as the avg within-cluster distance,
 *  averaged over all possible clusters. But then, small clusters could dominate
 *  the score. Say then that the total score S would be the avg. avg. within-cluster
 *  distance, weighted by the cluster sizes. But then, it would always be
 *  more advisable to form small clusters, as their formation cannot change
 *  the total score much. The same problem (even worse) would appear, if 
 *  the clusters would be weighted by the number of pairs in the clusters 
 *  (actually, ~cluster size to the power of 2). 
 *  
 *  One useful piece of information to consider could be the total number of 
 *  pairs within all the clusters. In each step, this of course increases
 *  by the amount of inter-cluster pairs between combined clusters C1 and C2,
 *  which is just |C1|*|C2|. And if one thinks of the quantity S=sum_s,t(d(st))
 *  (over all pairs that are same cluster), the traditional avg.linkage
 *  criterion just measures a change in S, normalized by the number of
 *  new s,t-pairs, which is the mentioned |C1|*|C2|.
 *  
 *  Now, let's try to think what will happen if one cannot compute the total 
 *  cluster score as a simple function (e.g. product) of the individual pairwise
 *  distances, but it is something more complicated. Then one just has a function
 *  f(C) to compute score for a complete cluster. If one tries
 *  to simulate the same kind of logic as is done in normal avg. linkage clustering,
 *  the "cost" for pairing clusters C1 and C2 into C would then be as follows 
 *  (assuming a cluster "cost" is the average within-cluster distance). 
 * 
 * cost(C1,C2) 
 *   = avg_c1_c2(d(c1,c2))
 *   = sum(d(c1,c2)) / |C1||C2|                                  (c1 in C1, c2 in C2)
 *   = (sum(d(c,c')) - sum(d(c1,c1')') - sum(d(c2,c2'))) / npairs(C1,C2)  (c,c' in C and so on)
 *   = (avg(d(c,c')) * npairs(C) - avg(d(c1,c1')) * npairs(C1) - avg(d(c2,c2')) * npairs(C2)) / npairs(C1,C2)   
 *   ~ (cost(C) * npairs(C) - cost(C1) * npairs(C1) - cost(C2) * npairs(C2)) / npairs(C1,C2)    
 *    
 *  - eli tässä tulee tulkittua kunkin klusterin painoksi sen sisältämien parien määrä
 *  - kokonaisscoren muutokseen  tulee ylläolevan mukaisesti laskettua |C1||C2| paria,
 *  joten luontevasti muutosta laskettaessa klusterin sisäisten parien määrä 
 *  toimii painona klusterille. Tuossa siis saadaan jotenkin laskettua 
 *  muutoksen relevantti osa (uusien parien tuomisen vaikutus) järkevästi painotettuna
 *  siten että klusterin koon ei pitäisi vaikuttaa hommaan. Probleeksi tulee
 *  nyt jossain mielessä, että mielivaltaisessa pisteytyksessä ei voida odottaa
 *  vanhojen klustereiden C1 ja C2 sisäisten parien vaikutuksen uudessa klusterissa
 *  pysyvän vakiona, kuten yllä tehdään eliminoimalla ne uuden klusterin scoresta
 *  simppelillä painotetulla miinuslaskulla.
 *  
 * Noh, joka tapauksessa klusterien yhdistämiskriteeriksi tulee nyt mielivaltaisella
 * clusteri scorella yllä kuvailtu kludge. Saattaapi olla että tehokkuus tulee 
 * olemaan huono...
 *        
 * TODO: efficient set implementation (maybe using bitsets?) for the
 * objects to be clustered. Requires that some running id:s are generated 
 * on the fly... 
 * 
 * Note that we must keep working with the concept of distance, as that allows
 * for the above mentioned normalizations when the cluster score is interpreted 
 * as the average distance between elements in the cluster. If we were to directly
 * use distances between 0 and 1, this kinds of normalizations would be made 
 * greatly more difficult.
 *  
 *     
 * @author leronen 
 * 
 */
public class HClust2<T> {   
      
//    private static Object[][] OPTION_DEFINITIONS = {                                                               
//        {"clusterfile", null, true}, // write clustering into this file in bmgraph format as node attribute "cluster"
//        {"loglevel", null, true},
//        {"join_criterion", null, true,  },  
//        {"help", "h", false}
//    };
   
//    private static String[] NAMES_OF_NON_OPT_ARGS = { 
//        "clusterscoresfile" // file which contains pre-computed scores for all clusters        
//    };       
              
    /**
     * Assume clusters have a score between 0 and 1.
     * Assume we are just given a distance function which somehow measures 
     * avg distance within subsets of the data points (the potential clusters).
     * We always say that the distance of a single-node cluster is 0.
     * 
     * @param pMaxDistance stop clustering once distance of new cluster would 
     * exceed this. If null, cluster everything.
     */ 
    public static <T> List<HCluster<T>> performClustering(Set<T> pDataPoints,
                                                          Function<Collection<T>, Double> pClusterDistanceFunction,
                                                          JoinCriterion pJoinCriterion,
                                                          Double pMaxDistance,
                                                          Integer pMinNumClusters)
    {
                                                                        
        Logger.info("Performing clustering for "+pDataPoints.size()+" data points.");
        
        if (pMaxDistance != null) {
            Logger.info("Using max distance: "+pMaxDistance);
        }

        if (pMinNumClusters != null) {
            Logger.info("Using min num clusters: "+pMinNumClusters);
        }
        
        if (pDataPoints.size() < 2) {
            throw new RuntimeException("Cannot cluster less than 2 data points!");
        }       
        
        // first, create singleton clusters
        int numClusters = 0;
        ArrayList<HCluster> singletonClusters = new ArrayList();
        for (T s: pDataPoints) {
                        
            HCluster<String> cluster = new HCluster(s, ++numClusters, 0);
            Logger.info("Created cluster: "+cluster);
            singletonClusters.add(cluster);
        }
                
        Logger.info("There are "+singletonClusters.size()+" singleton clusters");
        
        Set<HCluster<T>> clustersWithoutParent = new HashSet(singletonClusters);

        // heap for potential clusters to be formed (actually form the clusters
        // but only put them to tree when they are gotten from the heap
        // (most clusters will never be used in the tree)
        BinaryHeap<HCluster<T>, Double> heap = new BinaryHeap(Mode.MIN);
//        MultiMap<Cluster<T>, UnorderedPair<Cluster<T>>> pairsByCluster = MultiMap.makeHashMapBasedMultiMap();
        
        // use this to know what to remove from heap when it has been decided
        // which pair of children will be combined
        MultiMap<HCluster<T>, HCluster<T>> parentByChild = MultiMap.makeHashMapBasedMultiMap();
        
        // put pairs of singleton clusters to heap as the initial set 
        // of potential clusters to be formed
        int n = singletonClusters.size();
        for (int i=0; i<n; i++) {                
            for (int j=i+1; j<n; j++) {
                HCluster<T> c_i = singletonClusters.get(i);
                HCluster<T> c_j= singletonClusters.get(j);
                UnorderedPair<HCluster<T>> children = 
                    new UnorderedPair(c_i, c_j);                
                HCluster<T> c_ij = new HCluster(children);                
                double dist = pClusterDistanceFunction.compute(c_ij.members());
                c_ij.mCost = dist;
                
                if (pMaxDistance == null || pMaxDistance >= dist) {
                    // The candidate cluster does not exceed max distance                                                   
                    double heapKey;                   
                                        
                    if (pJoinCriterion == JoinCriterion.CLUSTER_SCORE) {                
                        heapKey = dist;
                    }
                    else if (pJoinCriterion == JoinCriterion.JOIN_DISTANCE 
                                 || pJoinCriterion == JoinCriterion.NORMALIZED_JOIN_DISTANCE
                                 || pJoinCriterion == JoinCriterion.NORMALIZED_JOIN_DISTANCE_MEDOID) {
                        c_ij.computeJoinCost(pJoinCriterion);
                        heapKey = c_ij.mJoinCost;
                    }
                    else {
                        throw new RuntimeException();
                    }
                                            
                    heap.add(c_ij, heapKey);
                    parentByChild.put(c_i, c_ij);
                    parentByChild.put(c_j, c_ij);                
                }
                // else the cluster is discarded without further ado
            }
        }

        Logger.info("Added "+heap.size()+" potential 2-node clusters to heap");       
        
        // start actual algorithm: get potential clusters from 
        // the heap one by one, removing any that become obsolete on the way
        while (heap.size() > 0              
               && (pMinNumClusters == null || clustersWithoutParent.size() > pMinNumClusters))
        {                                                          
            // get cluster to be formed from heap
            HCluster<T> newCluster = heap.pop();
            // assign cluster number at this stage:
            newCluster.mTreeNumber = ++numClusters;            
            Logger.info("The new cluster is: "+newCluster);
            
            // we shall now combine the closest pair into a new cluster...
            // first, remove the elements of the pair from the set of clusters:
            clustersWithoutParent.remove(newCluster.getChild1());
            clustersWithoutParent.remove(newCluster.getChild2());            
            
            // also remove deprecated potential clusters from heap
            Set<HCluster<T>> clustersToRemove = parentByChild.getMultiple(newCluster.getChildren());

            for (HCluster<T> parent: clustersToRemove) {
                if (parent == newCluster) {
                    // already removed by heap.pop() above
                    continue;
                }
                heap.remove(parent);
                parentByChild.remove(parent.getChild1(), parent);
                parentByChild.remove(parent.getChild2(), parent);                
            }  
           
            
            // compute scores that would be obtained by combining new cluster 
            // with each existing combinable cluster
            for (HCluster<T> oldCluster: clustersWithoutParent) {
                UnorderedPair<HCluster<T>> candidatePair = new UnorderedPair(newCluster, oldCluster);
                HCluster<T> candidateCluster = new HCluster(candidatePair);                
                double dist = pClusterDistanceFunction.compute(candidateCluster.members());
                candidateCluster.mCost = dist;
                
                if (pMaxDistance == null || pMaxDistance >= dist) {
                    // The new candidate cluster does not exceed max cost (~avg within-cluster distance)
                    double heapKey;                   
                                        
                    if (pJoinCriterion == JoinCriterion.CLUSTER_SCORE) {                
                        heapKey = dist;
                    }
                    else if (pJoinCriterion == JoinCriterion.JOIN_DISTANCE
                                || pJoinCriterion == JoinCriterion.NORMALIZED_JOIN_DISTANCE
                                || pJoinCriterion == JoinCriterion.NORMALIZED_JOIN_DISTANCE_MEDOID) {
                        candidateCluster.computeJoinCost(pJoinCriterion);
                        heapKey = candidateCluster.mJoinCost;
                    }
                    else {
                        throw new RuntimeException();
                    }
                                
                    heap.add(candidateCluster, heapKey);
                    parentByChild.put(oldCluster, candidateCluster);
                    parentByChild.put(newCluster, candidateCluster);
                }                
            }
                        
           // finally, add the new cluster to the set of clusters            
            clustersWithoutParent.add(newCluster);                
        }
        
//        if (heap.size() > 0) {
//            throw new RuntimeException("There are still "+heap.size()+" objects in the heap!");
//        }
        
        Logger.info("Obtained "+clustersWithoutParent.size()+" top-level clusters");
        
        return new ArrayList(clustersWithoutParent);              
        
    }               
              
    /** For converting a score function to a distance function */
    public static class MinusLogFunctionWrapper<T> implements Function<Collection<T>, Double> {
        private Function<Collection<T>, Double> mBaseFunction;
        
        public MinusLogFunctionWrapper(Function<Collection<T>, Double> pBaseFunction) {
            mBaseFunction = pBaseFunction;            
        }
        
        public Double compute(Collection<T> p) {
            return -Math.log(mBaseFunction.compute(p));
        }
                
        public String getName() {
            return "-log("+mBaseFunction.getName()+")";
        }
        
        
                
    }
//    public static void main(String[] pArgs) throws Exception {
//                            
//        Logger.setProgramName("hclust2");
//        
//        CmdLineArgs args = new CmdLineArgs(pArgs, 
//                                           true, 
//                                           OPTION_DEFINITIONS, 
//                                           NAMES_OF_NON_OPT_ARGS);
//
//        if (args.isDefined("help")) {
//            helpAndExit(args);
//        }                                       
//
//        String clusterScoresFile = args.getNonOptArg("clusterscoresfile");                               
//        WeightedSet<Set<String>> clusterScores = 
//            ClusterFileUtils.readWeightedClusters(clusterScoresFile);
//        
//        Logger.info("Read "+clusterScores.size()+" cluster candidates");
//        
////        Logger.info("Starting clustering...");
//        
//        List<Cluster<String>> roots = performClustering(clusterScores);
//        Logger.info("Performed clustering, got "+roots.size()+" top-level clusters:");
//        Logger.info(""+SU.toString(roots));
//                
////      for (Object o: members) {                         
////          ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
////      }
//        
//        Clustering<String> clustering = new Clustering(null);
//        for (Cluster<String> root: roots) {                
//            clustering.addCluster(root.elements());
//        }
//                
//        String clusterFile = args.getOpt("clusterfile");
//        if (clusterFile != null) {
//            PrintStream ps = new PrintStream(new FileOutputStream(clusterFile));                    
//            for (Integer clusterId: (Set<Integer>)clustering.getClusterIds()) {
//                Set members = clustering.getMembers(clusterId);
//                for (Object o: members) {                         
//                    ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
//                }
//            }
//            
//            ps.close();        
//        }       
//    }
        
    
    public enum JoinCriterion {
        CLUSTER_SCORE,
        NORMALIZED_JOIN_DISTANCE,
        NORMALIZED_JOIN_DISTANCE_MEDOID,
        JOIN_DISTANCE;
        
        
        public static List<String> names() {
            ArrayList result = new ArrayList();
            for (JoinCriterion jc: values()) {
                result.add(jc.name());
            }
            return result;
        }
    }
    
   
    
}
