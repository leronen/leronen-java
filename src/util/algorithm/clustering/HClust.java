package util.algorithm.clustering;

import java.io.*;
import java.util.*;

import util.CmdLineArgs;
import util.CollectionUtils;
import util.ConversionUtils;
import util.IOUtils;
import util.SU;
import util.StringUtils;
import util.Utils;
import util.algorithm.clustering.DistanceMatrix.MissingDistancePolicy;
import util.collections.BinaryHeap;
import util.collections.MultiMap;
import util.collections.UnorderedPair;
import util.collections.BinaryHeap.Mode;
import util.collections.tree.TreeUtils;
import util.converter.ObjectToStringConverter;
import util.dbg.Logger;

public class HClust {
       
    private static Object[][] OPTION_DEFINITIONS = {                               
        {"distancefile", null, true},  // file with pairwise distances of the form A B dist
        {"datapointfile", null, true}, // optional file with data points to consider (if given, do not deduce data points from distancefile)    
        {"distancefunction", null, true, null, DistanceFunctions.names()}, // initialized with "datafile" 
        {"datafile", null, true}, // goes hand in hand with "distancefunction" distance function is initialized with this file        
        {"linkage", null, true, "single", HClusterDistanceFunction.Linkage.valueList() },                
        {"minuslog", null, false}, // apply -log-transformation to the elements of the "distance" matrix
        {"missingdistancepolicy", null, true, "max", DistanceMatrix.MissingDistancePolicy.names()}, // what happens when the distance between a pair of objects is not known
        {"spanningtreefile", null, true}, // output the spanning tree (implicitly) formed during the algorithm.
                                          // (the spanning tree contains the elements (leaves of the dendrogram) only)
                                           // only applicable for single linkage clustering
        {"dendrogramfile", null, true}, // output the dendrogram into this file
        {"newickfile", null, true}, // output the dendrogram in newick format into this file
        {"clusterfile", null, true}, // cut the tree into k clusters; requires option "numclusters" ("k")
        {"numclusters", "k", true}, // number of clusters outputted to "clusterfile"
        {"clusterstatisticsfile", null, true}, // output detailed info about clusters to this file
        {"max_outliers", null, true}, // if outlier detection is in use, this is the max number of nodes labeled as outliers...
        {"outlier_detection", "o", true, "none", OutlierDetection.names()}, // how to detect outliers
        {"no_heap", null, false}, // use the old impl
        // logging (TODO: implement generic arg def into logger, use that)
        { Logger.PARAM_NAME_LOG_LEVEL, null, true,
            new Integer(Logger.LOGLEVEL_INFO) 
        },    
        {"logfile", null, true },   // write log messages to this file (in addition to stderr)
        {"warningfile", null, true, "HClust.warnings" },
        {"nowarningfile", "W", false },
        {"log_mem_usage", "lmu", false },           // write log messages to this file (in addition to stderr)
        // help:
        {"help", "h", false}
    };
    
    
    private static String[] NAMES_OF_NON_OPT_ARGS = { 
        // no non-opt args...        
    };       
    
//    private static void configureLogging(CmdLineArgs args) throws IOException {
//        
//        if (!(args.isDefined("nowarningfile"))) {
//            String warningFile = args.getOpt("warningfile");
//            Logger.addStream(warningFile, Logger.LOGLEVEL_WARNING);
//        }
//
//        if (args.isDefined("logfile")) {
//            String logFile = args.getOpt("logfile");
//            int logLevel = args.getIntOpt(Logger.PARAM_NAME_LOG_LEVEL);
//            Logger.addStream(logFile, logLevel);
//        }
//        
//        if (args.isDefined("log_mem_usage")) {
//            MemLogger.initialize();
//        }
//    }
              
    public static <T> HCluster performClustering(HClusterDistanceFunction pDistanceFunction,
                                                  Collection<T> pDataPoints) {
        if (pDataPoints.size() < 2) {
            throw new RuntimeException("Cannot cluster less than 2 data points!");
        }
        
        Logger.info("Clustering "+pDataPoints.size()+" data points...");
        
        Set clustersWithoutParent = new HashSet(pDataPoints);
//        Logger.info("Objects to be clustered: "+pDataPoints);
        
        int numClusters = 0;

        // construct heap
        BinaryHeap<UnorderedPair, Double> heap = new BinaryHeap(Mode.MIN);
        MultiMap<Object, UnorderedPair> pairsByCluster = MultiMap.makeHashMapBasedMultiMap();        
        
        List l = new ArrayList(clustersWithoutParent);
        for (int i=0; i<l.size(); i++) {                
            for (int j=i+1; j<l.size(); j++) {
                Object o_i = l.get(i);
                Object o_j = l.get(j);
                UnorderedPair pair = new UnorderedPair(o_i, o_j);
                Double d = pDistanceFunction.dist(pair);
                if (d != null) {
                    heap.add(pair, d);
                    pairsByCluster.put(o_i, pair);
                    pairsByCluster.put(o_j, pair);
                }                
            }
        }

        Logger.info("Added "+heap.size()+" pairs to heap");
        
        while (clustersWithoutParent.size() > 1) {
            
//            Logger.info("Getting elem from heap, heap size="+heap.size()+
//                        ", num objects to be clustered: "+clustersWithoutParent.size());
            
            // find closest pair
            Double closestDistance = heap.topKey();
            UnorderedPair closestPair = heap.peek();                        

//            Logger.info("Closest pair:" +closestPair+", dist="+closestDistance);
            
            // we shall now combine the closest pair into a new cluster...
            // first, remove the elements of the pair from the set of clusters:
            clustersWithoutParent.remove(closestPair.getObj1());
            clustersWithoutParent.remove(closestPair.getObj2());            
            
            // also remove old distances from heap:
            Set<UnorderedPair> pairsToRemove = pairsByCluster.getMultiple(closestPair);
//            Logger.info("Removing "+pairsToRemove.size()+" pairs from heap...");
            for (UnorderedPair pair: pairsToRemove) {
                // note that the closest pair should also be get removed here
//                Logger.info("Removing pair from heap (there are "+heap.size()+" objects in the heap)" +pair);
                heap.remove(pair);
                pairsByCluster.remove(pair.getObj1(), pair);
                pairsByCluster.remove(pair.getObj2(), pair);
            }  
            
//            pairsByCluster.removeKey(closestPair.getObj1());
//            pairsByCluster.removeKey(closestPair.getObj2());
            
//            Logger.info("pairsByCluster after removing keys:\n"+
//                        pairsByCluster);
           
            // actually create the new cluster:
            HCluster newCluster = new HCluster<T>(closestPair, closestDistance, ++numClusters);
            
            // compute distances to all old clusters and put to heap:
            for (Object oldCluster: clustersWithoutParent) {
                UnorderedPair pair = new UnorderedPair(newCluster, oldCluster);                 
                Double d = pDistanceFunction.dist(pair);
                if (d != null) {
                    heap.add(pair, d);
                    pairsByCluster.put(oldCluster, pair);
                    pairsByCluster.put(newCluster, pair);
                }                
            }
            
//            Logger.info("pairsByCluster after adding new cluster:\n"+
//                        pairsByCluster);
            
           // finally, add the new cluster to the set of clusters            
            clustersWithoutParent.add(newCluster);                
        }
        
        if (heap.size() > 0) {
            throw new RuntimeException("There are still "+heap.size()+" objects in the heap!");
        }
        
        if (clustersWithoutParent.size() != 1) {
            throw new RuntimeException("There should be exactly 1 cluster without " +
            		                   " parents left! (there are "+clustersWithoutParent.size());
        }
        
        if (pairsByCluster.keySet().size() > 0) {
            throw new RuntimeException("pairsByCluster is not empty!");
        }
                                
        return (HCluster)clustersWithoutParent.iterator().next();                
    }
    
    
    public static <T> HCluster performClustering_old(HClusterDistanceFunction pDistanceFunction,
                                                      Collection<T> pDataPoints) {
        
        if (pDataPoints.size() < 2) {
            throw new RuntimeException("Cannot cluster less than 2 data points!");
        }
        
        Set objectsToBeClustered = new HashSet(pDataPoints);
        
        int numClusters = 0;
        
        while (objectsToBeClustered.size() > 1) {
            // find closest pair of objects (including clusters)
            
            // Logger.info("There are "+objectsToBeClustered.size()+" objects to be clustered...");
            
            // TODO: use a heap for a vast (?) speedup...
            List l = new ArrayList(objectsToBeClustered);
            UnorderedPair closestPair = new UnorderedPair(l.get(0), l.get(1));
            Double closestDistance = pDistanceFunction.dist(closestPair);
            for (int i=0; i<l.size(); i++) {                
                for (int j=i+1; j<l.size(); j++) {
                    UnorderedPair pair = new UnorderedPair(l.get(i), l.get(j));
                    // Logger.info("Considering pair: "+pair);
                    Double d = pDistanceFunction.dist(pair);
                    if (d != null && (closestDistance == null || d < closestDistance)) {
                        closestPair = pair;
                        closestDistance = d;
                    }
                }
            }
            
            if (closestDistance == null) {
                // The remaining objects are not linked to each other
                // (should not occur, according to our expectations)
                throw new RuntimeException("There are still "+objectsToBeClustered.size()+
                                           " objects to clustered, and none of them are linked" +
                                           " to each other (actually, a forest should be produced)");
            }
            
            // combine the closest pair into a new cluster
            HCluster newCluster = new HCluster<T>(closestPair, closestDistance, ++numClusters);
            // double avgD = newCluster.computeAvgDistance_dbg(newCluster);
            objectsToBeClustered.removeAll(closestPair);
            objectsToBeClustered.add(newCluster);
            
            // note that there is no need to explicitly compute distances from
            // the new cluster (tree node) to the other objects, as that
            // shall be done on demand when finding the closest pairs...
            // Logger.info(""+objectsToBeClustered.size()+" objects left");
        }
                                
        return (HCluster)objectsToBeClustered.iterator().next();
    }
    
    
    /**
     * Cut the tree into a forest with k trees, by removing the 
     * edges OF THE TREE with the worst clusters as the parent node.
     * 
     * @return the roots of the obtained trees (clusters)
     * 
     * @param outliers put outliers (single-node clusters) here,
     *  if discardSingleNodeClusters == true.
     */
    public static Collection cutTree(HCluster root, 
                                     int k, 
                                     boolean discardSingleNodeClusters,
                                     Integer maxOutliers,
                                     Set outliers) {
        Set allNodes = root.descendants();
        Logger.info("Cutting tree. There are "+allNodes.size()+" nodes in the original tree.");
        List nodeSplitOrder = new ArrayList(allNodes);
        
//        Logger.info(SU.toString(nodes));
        Collections.sort(nodeSplitOrder, new HCluster.TreeCuttingComparator());
        Collections.reverse(nodeSplitOrder);
        
        // We form the result by "splitting" k-1 worst clusters; the "worseness"
        // of a cluster c is measured by the distance between its children  
        // c1 and c2. Splitting one cluster always removes the split node 
        // and adds the 2 child nodes as new roots, thus increasing the number
        // or root nodes (~clusters to be output) by one.        
//        Set nodesToBeSplit = 
//            new HashSet(nodes.subList(nodes.size()-k+1, nodes.size()));
//        Logger.info("Nodes to be split:\n"+
//                    SU.toString(nodesToBeSplit));
        // split clusters starting with the worst until we have the desired number
        // of clusters
        Set newRoots = new HashSet();
//        List newRoots = new ArrayList();
        
        int numInternalNodes = root.descendants().size()-root.members().size();
        Logger.info("Number of internal nodes in tree: "+numInternalNodes);
        Logger.info("Max outliers: "+maxOutliers);
        
        int numInternalNodesLeft = numInternalNodes;        
        
        for (Object n: nodeSplitOrder) {
            if (n instanceof HCluster) {
                HCluster c = (HCluster)n;                
                Object child1 = c.getChild1();
                Object child2 = c.getChild2();
                
                int numLeafChilds = 
                    (child1 instanceof HCluster ? 0 : 1)  
                  + (child2 instanceof HCluster ? 0 : 1);
                
//                int numRootsStillRequired =  k - newRoots.size();
                int maxRoots = newRoots.size() + numInternalNodesLeft;
                int maxRootsAfterSplit = maxRoots - numLeafChilds;
                Logger.info("Current number of clusters: "+newRoots.size());
                Logger.info("numInternalNodesLeft: "+numInternalNodesLeft);
                Logger.info("maxRoots: "+maxRoots);
                Logger.info("maxRoots after split: "+maxRootsAfterSplit);
                // must ensure that max roots does not go below k!                
                boolean allowDiscardingSingleNodeClusters =
                    outliers != null 
                    && (maxOutliers == null || outliers.size() + numLeafChilds <= maxOutliers) 
                    && (maxRootsAfterSplit >= k);
                Logger.info("Allow discarding single-node clusters: "+allowDiscardingSingleNodeClusters);
                                
                if (discardSingleNodeClusters && allowDiscardingSingleNodeClusters) {
                    // dismiss any would-be single node-clusters as outliers
                    if (numLeafChilds > 0) {
                        Logger.info("Discarding "+numLeafChilds+" single-node-childen");
                    }
                    if (child1 instanceof HCluster) {
                        newRoots.add(child1);
                    }
                    else {
                        outliers.add(child1);                        
                    }
                    if (child2 instanceof HCluster) {
                        newRoots.add(child2);
                    }
                    else {
                        outliers.add(child2);
                    }
                }
                else {
                    Logger.info("Not discarding single-node clusters");
                    // do not discard single-node clusters
                    newRoots.add(child1);
                    newRoots.add(child2);
                }
                
                // the split node is no longer a root
                newRoots.remove(c);
            }
            else {
                Logger.warning("Failed cutting tree; cannot split a leaf node: "+n);
            }                        
            
            numInternalNodesLeft--;
            
            if (newRoots.size() == k) {
                break;
            }
        }
        
        if (newRoots.size() != k) {
            Logger.info("Failed to obtain "+k+" clusters; got "+newRoots.size());
        }
        
        Logger.info("New roots:\n"+
                    SU.toString(newRoots));
        if (outliers != null && outliers.size() > 0) {
            Logger.info("Outliers:\n"+SU.toString(outliers));
        }
        
        if (newRoots.size() != k) {
            Logger.warning("Failed obtaining "+k+" clusters; got "+newRoots.size());
        }
        
        return newRoots;
    }
    
    public static <T> Clustering_old<T> cluster_avg_linkage(Collection<T> pDataPoints,
                                                        IDistanceFunction<T> pFunc,
                                                        int pK) {
        HClusterDistanceFunction_average_linkage<T> cDist = new HClusterDistanceFunction_average_linkage(pFunc);

        
        HCluster root =  HClust.performClustering_old(cDist, pDataPoints);
        Collection clusterRoots = cutTree(root, pK, false, null, null);        
        Clustering_old clustering = new Clustering_old(pFunc);
        
        for (Object cRoot: clusterRoots) {                   
            if (cRoot instanceof HCluster) {
                clustering.addCluster(((HCluster)cRoot).members());
            }
            else {
                // the cluster consists of a single node
                clustering.addCluster(Collections.singleton(cRoot));
            }                        
        }
                              
        return clustering;
        
    }
    
    public static void main(String[] pArgs) throws Exception {
                       
//        Logger.info("Arguments:\n"+
//                    SU.toString(pArgs));
        
        Logger.setProgramName("hclust");
        
        CmdLineArgs args = new CmdLineArgs(pArgs, 
                                           true, 
                                           OPTION_DEFINITIONS, 
                                           NAMES_OF_NON_OPT_ARGS);
        
        

        if (args.isDefined("help")) {
            helpAndExit(args);
        }
               
        Logger.configureLogging(args);
        
        Logger.info(args.toString2(Collections.EMPTY_SET));
        
        IDataManagingDistanceFunction distanceFunction = null;
        List dataPoints = null;            
        String distanceFile = null;
        
        if (args.isDefined("distancefile")) {
            // init distance function                
            distanceFile = args.getOpt("distancefile");
            Logger.info("Reading distance matrix...");
            
            MissingDistancePolicy mdp = DistanceMatrix.MissingDistancePolicy.getByName(args.getOpt("missingdistancepolicy")); 
            
            DistanceMatrix distanceMatrix = new DistanceMatrix(distanceFile, mdp);
            if (args.isDefined("minuslog")) {
                distanceMatrix.minusLogTransform();
            }
            
            if (args.isDefined("datapointfile")) {
                // data points given in separate file (they should
                // of course represent a subset of points in the distance file...)
                dataPoints = IOUtils.readLines(args.getOpt("datapointfile"));
            }               
            else {
                // deduce data points from distance file
                dataPoints = new ArrayList(distanceMatrix.getDataPoints());
            }
                        
            distanceFunction = distanceMatrix;
        }                
        else if (args.isDefined("distancefunction")) {
            String distanceFunctionName = args.getOpt("distancefunction");
            String datafile = args.getOpt("datafile");
            if (datafile == null) {
                Utils.die("Option datafile not given (required when using opt \"distancefunction\"");
            }
            distanceFunction = (IDataManagingDistanceFunction)DistanceFunctions.getImpl(distanceFunctionName, datafile);
            if (args.isDefined("datapointfile")) {
                // data points given in separate file (they should
                // of course represent a subset of points in the data file...)
                dataPoints = IOUtils.readLines(args.getOpt("datapointfile"));
            }               
            else {
                dataPoints = new ArrayList(distanceFunction.getDataPoints());
            }
        }
        else {
            usageAndExit(args, "No object data nor distances!");
        }
        
        String clusterdistancefunctionName = args.getOpt("linkage");
        HClusterDistanceFunction cDist = 
            HClusterDistanceFunction.Linkage.getImpl(clusterdistancefunctionName, distanceFunction);                
        
        if (args.isDefined("distancefunction")) {
            cDist.setCacheBaseDistances(true);
        }
        
        // perform clustering
        Logger.info("Start actual clustering...");        
        HCluster root = args.isDefined("no_heap")
                         ? HClust.performClustering_old(cDist, dataPoints)
                         : HClust.performClustering(cDist, dataPoints);                
        
        
        String spanningTreeFile = args.getOpt("spanningtreefile");
        if (spanningTreeFile != null) {
            
            Logger.info("Outputting spanning tree file...");
            List<UnorderedPair<String>> pairs = new ArrayList();
            Set treeNodes = TreeUtils.descendants_breadthfirst(root, new HCluster.TreeNodeAdapter(), true);
            for (Object treeNode: treeNodes) {
                if (treeNode instanceof HCluster) {
                    HCluster c = (HCluster)treeNode;
                    UnorderedPair pair = c.getChildren();
                    UnorderedPair<String> actualPair = cDist.getActualPair(pair);
                    pairs.add(actualPair);
                }
            }
            PrintStream ps = new PrintStream(new FileOutputStream(spanningTreeFile));
            for (UnorderedPair<String> pair: pairs) {                        
                ps.println(pair.getObj1()+" "+pair.getObj2() + " is_related_to" + " goodness=" + Math.exp(-distanceFunction.dist(pair.getObj1(), pair.getObj2())));
            }
            ps.close();
        }                
        
        OutlierDetection outlierDetection =  
            OutlierDetection.getByName(args.getOpt("outlier_detection"));
        Logger.info("Outlier detection: "+outlierDetection);
        
        Integer maxOutliers = args.getIntOpt("max_outliers");
        
        Set outliers = new HashSet();
                       
        String dendrogramFile = args.getOpt("dendrogramfile");
        String newickfile = args.getOpt("newickfile");
        String clusterFile = args.getOpt("clusterfile");
        String clusterStatisticsFile = args.getOpt("clusterstatisticsfile");
        
        if (dendrogramFile !=  null) {
            Logger.info("Outputting dendrogram to file: "+dendrogramFile);
            PrintStream ps = new PrintStream(new FileOutputStream(dendrogramFile));            
            ps.println(StringUtils.formatTree(root, new HCluster.TreeNodeAdapter(), 4, true, new HCluster.NodeFormatter()));
            ps.close();
        }
        
        if (newickfile !=  null) {
            Logger.info("Outputting dendrogram in newick format to file: "+newickfile);
            PrintStream ps = new PrintStream(new FileOutputStream(newickfile));            
            ps.println(StringUtils.formatTree_newick(root, new HCluster.TreeNodeAdapter(), new HCluster.NewickNodeFormatter(root.createParentLinks())));
            ps.close();
        }
                       
        if (clusterFile != null || clusterStatisticsFile != null) {
            // Logger.info("Outputting cluster file...");
            int numClusters = args.getIntOpt("numclusters");
            
            boolean discardSingleNodeClusters = 
                (outlierDetection == OutlierDetection.SINGLE_NODE_CLUSTERS_AS_OUTLIERS
                 || outlierDetection == OutlierDetection.ASSIGN_SINGLE_NODE_CLUSTERS_TO_NEAREST_CLUSTER);
            
            Logger.info("Discard single-node clusters: "+discardSingleNodeClusters);
            
            Collection clusterRoots = cutTree(root, numClusters, discardSingleNodeClusters, maxOutliers, outliers);                   
                                                    
            Clustering_old clustering = new Clustering_old(distanceFunction);
            Map<Object, Integer> clusterIdByRoot = new HashMap();
                                
            for (Object cRoot: clusterRoots) {                   
                if (cRoot instanceof HCluster) {
                    HCluster node = (HCluster)cRoot;
                    Set clusterMembers = CollectionUtils.minus(node.members(), outliers);
                    Logger.info("Adding a cluster with "+clusterMembers.size()+
                                " elements to the clustering.");
                    int clusterId = clustering.addCluster(clusterMembers);
                    clusterIdByRoot.put(cRoot, clusterId);
                }
                else {
                    // the cluster consists of a single node
//                    if (discardSingleNodeClusters) {
//                        throw new RuntimeException("Single node-clusters should not be allowed");
//                    }
                    Logger.info("Adding a single-node cluster to the clustering.");
                    int clusterId = clustering.addCluster(Collections.singleton(cRoot));
                    clusterIdByRoot.put(cRoot, clusterId);
                }                        
            }
            
            if (outlierDetection == OutlierDetection.ASSIGN_SINGLE_NODE_CLUSTERS_TO_NEAREST_CLUSTER) {
                // find the nearest cluster for each "outlier" and 
                // assign the node to that cluster...
            
                Logger.info("Assigning "+outliers.size()+" single node clusters to nearest cluster...");
                
                for (Object o: outliers) {                    
            
                    Object bestClusterRoot = null;
                    double bestDistance = Double.MAX_VALUE;
                    
                    for (Object cRoot: clusterRoots) {
                        double d;
                    
                        if (cRoot instanceof HCluster) {
                            HCluster hcc= (HCluster)cRoot;
                            d = hcc.computeAvgDistance(distanceFunction, o);
                        }
                        else {
                            // root is just a single-node cluster itself                            
                            d = distanceFunction.dist(o, cRoot); 
                        }
                        
                        if (bestClusterRoot == null || d < bestDistance) {
                            bestClusterRoot = cRoot;
                            bestDistance = d;
                        }
                    }
                    
                    // presumably found the best cluster                    
                    int bestClusterId = clusterIdByRoot.get(bestClusterRoot);                    
                    Logger.info("Adding single-node cluster:"+o+" to cluster: "+bestClusterId);
                    clustering.addToCluster(bestClusterId, o);                        
                }
            }
            
            
            if (clusterFile != null) {
                PrintStream ps = new PrintStream(new FileOutputStream(clusterFile));                    
                for (Integer clusterId: (Set<Integer>)clustering.getClusterIds()) {
                    Set members = clustering.getMembers(clusterId);
                    for (Object o: members) {                         
                        ps.println("# _attributes "+o+" cluster="+clusterId);                                                                                                
                    }
                }
                
                if (outlierDetection == OutlierDetection.SINGLE_NODE_CLUSTERS_AS_OUTLIERS) {
                    int outlierInd = 0;
                    for (Object o: outliers) {
                        ps.println("# _attributes "+o+" cluster=outlier_hc"+(++outlierInd));
                    }
                }

                ps.close();
            }
                                                    
            if (clusterStatisticsFile != null) {
                PrintStream ps = new PrintStream(new FileOutputStream(clusterStatisticsFile));
                int numSingleNodeClusters = 0;
                // sum of within cluster avg-distance, weighted by the cluster sizes
                double within_cluster_avg_distance_sum = 0;
                for (Object cRoot: clusterRoots) {                   
                    if (cRoot instanceof HCluster) {
                        HCluster cluster = (HCluster)cRoot;
//                        int clusterId = clustering.clusterId(new HashSet(cluster.members()));
                        int clusterId = clusterIdByRoot.get(cRoot);
                        Map statistics = cluster.computeStatistics(distanceFunction);
                        ps.println(""+clusterId+" "+
                                   StringUtils.format(statistics, "=", ", ")+
                                   (args.isDefined("minuslog") ? ", avg_within_cluster_goodness=" + Math.exp(-cluster.getAvgDistance(distanceFunction)) : "")); 
                        within_cluster_avg_distance_sum += cluster.getAvgDistance(distanceFunction)*cluster.getNumElements();                                                
                    }
                    else {
                        // the cluster consists of a single node; no interesting statistics!
                        numSingleNodeClusters++;
                        // avg distance == 0!
                        ps.println("No statistics for a single element cluster: "+cRoot); 
                    }                    
                }
                
                int numClustersWithAtLeast2Nodes = numClusters - numSingleNodeClusters;                
                
                // output statistics for the complete clustering...
                double avg_within_cluster_distance =
                    within_cluster_avg_distance_sum / (dataPoints.size() - outliers.size());
                    // within_cluster_avg_distance_sum / numClustersWithAtLeast2Nodes;
                
                ps.println("CLUSTERING"+                                                     
                           " distancefile="+distanceFile+
                           " numdatapoints="+dataPoints.size()+
                           " num_clustered_datapoints="+(dataPoints.size()-outliers.size())+
                           " num_outliers="+outliers.size()+
                           " k="+numClusters+
                           " linkage="+clusterdistancefunctionName+                                                                                
                           " num_single_node_clusters="+numSingleNodeClusters+
                           " num_multi_node_clusters="+numClustersWithAtLeast2Nodes+                           
                           " avg_within_cluster_distance="+avg_within_cluster_distance+
                           (args.isDefined("minuslog")
                            ? " avg_within_cluster_goodness="+Math.exp(-avg_within_cluster_distance) 
                            : ""));
                           
//                for (
                
                
                ps.close();
            }
                                
        }
        
        Logger.endLog();
                
                                                                     
    }
    
    private static void helpAndExit(CmdLineArgs pArgs) {  
        Logger.info(pArgs.getDef().usage());
        System.exit(0);
    }

    public enum OutlierDetection {
        NONE("none"),
        SINGLE_NODE_CLUSTERS_AS_OUTLIERS("single_node_clusters_are_outliers"), // trivial; just prune single-node clusters while choosing the k final clusters...        
        ASSIGN_SINGLE_NODE_CLUSTERS_TO_NEAREST_CLUSTER("assign_single_node_clusters_to_nearest_cluster"); // trivial; just prune single-node clusters while choosing the k final clusters, and later assign them to nearest cluster
        
        String mName;
        
        public static Map<String, OutlierDetection> BY_NAME;
        
        public static OutlierDetection getByName(String pName) {
            initMapIfNeeded();
            if (!(BY_NAME.containsKey(pName))) {
                Utils.die("No such outlier detection mode: "+pName+
                          ". Valid options are: "+SU.toString(OutlierDetection.BY_NAME.keySet(), ", "));
                return null;
            }
            else {
                return BY_NAME.get(pName);
            }
        }
                        
        private static void initMapIfNeeded() {
            if (BY_NAME == null) {
                BY_NAME = new HashMap();
                for (OutlierDetection od: values()) {
                    BY_NAME.put(od.mName, od);
                }
            }
        }
                        
        private OutlierDetection(String pName) {                         
            mName = pName;            
        }

        public String toString() {
            return mName;
        }

        public static List<String> names() {
            return ConversionUtils.convert(Arrays.asList(OutlierDetection.values()),
                   new ObjectToStringConverter());
        }
    }
    
    private static void usageAndExit(CmdLineArgs pArgs, String pErrorMsg) {
        Logger.error(pErrorMsg);
//        TreeSet<String> availableCommands = new TreeSet(ReflectionUtils.getPublicStaticStringFieldsWithPrefix(HClust.class, "CMD_"));
//        Logger.info("List of available commands:\n"+StringUtils.collectionToString(availableCommands));
        Logger.info(pArgs.getDef().usage());
        System.exit(-1);
    }
    
}
