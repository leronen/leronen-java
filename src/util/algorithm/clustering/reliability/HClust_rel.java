package util.algorithm.clustering.reliability;

import java.io.*;
import java.util.*;

import util.CmdLineArgs;
import util.CollectionUtils;
import util.SU;
import util.algorithm.clustering.ClusterFileUtils;
import util.algorithm.clustering.Clustering_old;
import util.collections.BinaryHeap;
import util.collections.MultiMap;
import util.collections.UnorderedPair;
import util.collections.WeightedSet;
import util.collections.BinaryHeap.Mode;
import util.dbg.Logger;


/**
 * Agglomerative clustering using pre-computed cluster scores. Scores are 
 * pre-computed for all sets of nodes (potential clusters) meeting a lower
 * bound of score (as of now, not incorporated within this class, but 
 * rather being done already in the preprocessing phase...). 
 * Clusters are combined until no more clusters can be found. Thus, unlike in traditional clustering,
 * a number of trees if found, unless scores are computed for all possible 
 * clusters.
 *  
 * @author leronen
 *
 */
public class HClust_rel {   
      
    private static Object[][] OPTION_DEFINITIONS = {                                                               
        {"clusterfile", null, true}, // write clustering into this file in bmgraph format as node attribute "cluster"        
        {"loglevel", null, true},         
        {"help", "h", false}
    };
   
    private static String[] NAMES_OF_NON_OPT_ARGS = { 
        "clusterscoresfile" // file which contains pre-computed scores for all clusters        
    };       
              
    /** see class comment */    
    public static <T> List<Cluster<T>> performClustering(WeightedSet<Set<T>> pCandidates) {
                                                        
        Set<T> dataPoints = 
            new HashSet(
                CollectionUtils.flatten(pCandidates.asObjToWeightMap().keySet()));
        
        Logger.info("There are "+dataPoints.size()+" data points.");
        
        if (dataPoints.size() < 2) {
            throw new RuntimeException("Cannot cluster less than 2 data points!");
        }
        
        
        // first, create singleton clusters
        int numClusters = 0;
        ArrayList<Cluster> singletonClusters = new ArrayList();
        for (T s: dataPoints) {
            double score = pCandidates.getWeight(Collections.singleton(s));
            if (score == 0) {
                // a kludge: set score of single-elem clusters to 1.
                score = 1.0;
            }
            
            Cluster<String> cluster = new Cluster(s, ++numClusters, score);
            Logger.info("Created cluster: "+cluster);
            singletonClusters.add(cluster);
        }
                
        Logger.info("There are "+singletonClusters.size()+" singleton clusters");
        
        Set<Cluster<T>> clustersWithoutParent = new HashSet(singletonClusters);        

        // construct data structures
        BinaryHeap<UnorderedPair<Cluster<T>>, Double> heap = new BinaryHeap(Mode.MAX);
        MultiMap<Cluster<T>, UnorderedPair<Cluster<T>>> pairsByCluster = MultiMap.makeHashMapBasedMultiMap();
        
        // put pairs of singleton clusters to heap        
        int n = singletonClusters.size();
        for (int i=0; i<n; i++) {                
            for (int j=i+1; j<n; j++) {
                Cluster<T> c_i = singletonClusters.get(i);
                Cluster<T> c_j= singletonClusters.get(j);                                
                UnorderedPair<T> elemPair = 
                    new UnorderedPair(c_i.mElements.iterator().next(),
                                      c_j.mElements.iterator().next());                                                      
                double score = pCandidates.getWeight(elemPair);
                if (score != 0) {
                    // do not consider zero-score clusters
                    UnorderedPair<Cluster<T>> clusterPair = 
                        new UnorderedPair(c_i, c_j);
                    heap.add(clusterPair, score);
                    pairsByCluster.put(c_i, clusterPair);
                    pairsByCluster.put(c_j, clusterPair);
                }                
            }
        }

        Logger.info("Added "+heap.size()+" pairs to heap");
        
        while (heap.size() > 1) {
            
            Logger.info("Getting elem from heap, heap size="+heap.size()+
                        ", num objects to be clustered: "+clustersWithoutParent.size());
            
            // find closest pair
            Double closestDistance = heap.topKey();
            UnorderedPair<Cluster<T>> closestPair = heap.peek();                        

            
            // we shall now combine the closest pair into a new cluster...
            // first, remove the elements of the pair from the set of clusters:
            clustersWithoutParent.remove(closestPair.getObj1());
            clustersWithoutParent.remove(closestPair.getObj2());            
            
            // also remove old distances from heap:
            Set<UnorderedPair<Cluster<T>>> pairsToRemove = pairsByCluster.getMultiple(closestPair);
            for (UnorderedPair<Cluster<T>> pair: pairsToRemove) {
                heap.remove(pair);
                pairsByCluster.remove(pair.getObj1(), pair);
                pairsByCluster.remove(pair.getObj2(), pair);
            }  
           
            // actually create the new cluster:
            Cluster<T> newCluster = new Cluster(closestPair, closestDistance, ++numClusters);
            
            // compute scores that would be obtained by combining new cluster 
            // with each existing cluster
            for (Cluster<T> oldCluster: clustersWithoutParent) {
                UnorderedPair<Cluster<T>> pair = new UnorderedPair(newCluster, oldCluster);
                Set<T> combinedSet = CollectionUtils.union(newCluster.elements(), oldCluster.elements()); 
                double score = pCandidates.getWeight(combinedSet);
                if (score != 0) {
                    // combinedSet has a score above threshold (was found in 
                    // the set of candidates)
                    heap.add(pair, score);
                    pairsByCluster.put(oldCluster, pair);
                    pairsByCluster.put(newCluster, pair);
                }                
            }
                        
           // finally, add the new cluster to the set of clusters            
            clustersWithoutParent.add(newCluster);                
        }
        
//        if (heap.size() > 0) {
//            throw new RuntimeException("There are still "+heap.size()+" objects in the heap!");
//        }
        
//        if (clustersWithoutParent.size() != 1) {
//            throw new RuntimeException("There should be exactly 1 cluster without " +
//            		                   " parents left! (there are "+clustersWithoutParent.size());
//        }
        
//        if (pairsByCluster.keySet().size() > 0) {
//            throw new RuntimeException("pairsByCluster is not empty!");
//        }
                                
        return new ArrayList(clustersWithoutParent);
        
    }               
           
    public static void main(String[] pArgs) throws Exception {
                            
        Logger.setProgramName("hclust_rel");
        
        CmdLineArgs args = new CmdLineArgs(pArgs, 
                                           true, 
                                           OPTION_DEFINITIONS, 
                                           NAMES_OF_NON_OPT_ARGS);

        if (args.isDefined("help")) {
            helpAndExit(args);
        }                                       

        String clusterScoresFile = args.getNonOptArg("clusterscoresfile");                               
        WeightedSet<Set<String>> clusterScores = 
            ClusterFileUtils.readWeightedClusters(clusterScoresFile);
        
        Logger.info("Read "+clusterScores.size()+" cluster candidates");
        
        Logger.info("Starting clustering...");
        List<Cluster<String>> roots = performClustering(clusterScores);
        Logger.info("Performed clustering, got "+roots.size()+" top-level clusters:");
        Logger.info(""+SU.toString(roots));
                
//      for (Object o: members) {                         
//          ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
//      }
        
        Clustering_old<String> clustering = new Clustering_old(null);
        for (Cluster<String> root: roots) {                
            clustering.addCluster(root.elements());
        }
                
        String clusterFile = args.getOpt("clusterfile");
        if (clusterFile != null) {
            PrintStream ps = new PrintStream(new FileOutputStream(clusterFile));                    
            for (Integer clusterId: (Set<Integer>)clustering.getClusterIds()) {
                Set members = clustering.getMembers(clusterId);
                for (Object o: members) {                         
                    ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
                }
            }
            
            ps.close();        
        }       
    }
        
//              if (cRoot instanceof HCCluster) {
//                  clustering.addCluster(((HCCluster)cRoot).members());
//              }
//              else {
//                  // the cluster consists of a single node
//                  clustering.addCluster(Collections.singleton(cRoot));
//              }                        
//          }
//          
//          if (clusterFile != null) {
//              PrintStream ps = new PrintStream(new FileOutputStream(clusterFile));                    
//              for (Integer clusterId: (Set<Integer>)clustering.getClusterIds()) {
//                  Set members = clustering.getMembers(clusterId);
//                  for (Object o: members) {                         
//                      ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
//                  }
//              }
//              ps.close();
//          }
//        }
//                  
//        Logger.info("Read cluster candidates:"+
//        		    clusterScores);     
        
        
//        {"clusterfile", null, true}, // write clustering into this file in bmgraph format as node attribute "cluster"
        
        
        
        
        // output tree
//        Logger.info("Output tree...");
//        
//        String spanningTreeFile = args.getOpt("spanningtreefile");
//        if (spanningTreeFile != null) {
//            Logger.info("Outputting spanning tree file...");
//            List<UnorderedPair<String>> pairs = new ArrayList();
//            Set treeNodes = TreeUtils.descendants_breadthfirst(root, new HCCluster.TreeNodeAdapter(), true);
//            for (Object treeNode: treeNodes) {
//                if (treeNode instanceof HCCluster) {
//                    HCCluster_rel c = (HCCluster)treeNode;
//                    UnorderedPair pair = c.getChildren();
//                    UnorderedPair<String> actualPair = cDist.getActualPair(pair);
//                    pairs.add(actualPair);
//                }
//            }
//            PrintStream ps = new PrintStream(new FileOutputStream(spanningTreeFile));
//            for (UnorderedPair<String> pair: pairs) {                        
//                ps.println(pair.getObj1()+" "+pair.getObj2() + " is_related_to" + " goodness=" + Math.exp(-distanceFunction.dist(pair.getObj1(), pair.getObj2())));
//            }
//            ps.close();
//        }                
//        
//        String clusterFile = args.getOpt("clusterfile");
////        String clusterStatisticsFile = args.getOpt("clusterstatisticsfile");
//        if (clusterFile != null) {
//            // Logger.info("Outputting cluster file...");
//            int numClusters = args.getIntOpt("numclusters");
//            
//            List clusterRoots = cutTree(root, numClusters);                   
//                                                    
//            Clustering clustering = new Clustering(distanceFunction);
//                                
//            for (Object cRoot: clusterRoots) {                   
//                if (cRoot instanceof HCCluster) {
//                    clustering.addCluster(((HCCluster)cRoot).members());
//                }
//                else {
//                    // the cluster consists of a single node
//                    clustering.addCluster(Collections.singleton(cRoot));
//                }                        
//            }
//            
//            if (clusterFile != null) {
//                PrintStream ps = new PrintStream(new FileOutputStream(clusterFile));                    
//                for (Integer clusterId: (Set<Integer>)clustering.getClusterIds()) {
//                    Set members = clustering.getMembers(clusterId);
//                    for (Object o: members) {                         
//                        ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
//                    }
//                }
//                ps.close();
//            }
//                                                    
//            if (clusterStatisticsFile != null) {
//                PrintStream ps = new PrintStream(new FileOutputStream(clusterStatisticsFile));
//                for (Object cRoot: clusterRoots) {                   
//                    if (cRoot instanceof HCCluster) {
//                        HCCluster_rel cluster = (HCCluster)cRoot;
//                        int clusterId = clustering.clusterId(new HashSet(cluster.members()));
//                        Map statistics = cluster.computeStatistics(distanceFunction);
//                        ps.println(""+clusterId+" "+StringUtils.mapToString(statistics, "=", ", "));
//                    }
//                    else {
//                        // the cluster consists of a single node; no intresting statistics!
//                        ps.println("No statistics for a single element cluster: "+cRoot); 
//                    }
//                    
//                }
//                ps.close();
//            }
//                                
//        }                                               
//        
//        String dendrogramFile = args.getOpt("dendrogramfile");
//        if (dendrogramFile !=  null) {
//            PrintStream ps = new PrintStream(new FileOutputStream(dendrogramFile));            
//            ps.println(StringUtils.formatTree(root, new HCCluster.TreeNodeAdapter(), 4, true, new HCCluster.NodeFormatter()));
//            ps.close();
//        }
                                                                     
//    }
    
    private static void helpAndExit(CmdLineArgs pArgs) {  
        Logger.info(pArgs.getDef().usage());
        System.exit(0);
    }
    
}
