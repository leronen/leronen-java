package util.algorithm.clustering2;

import java.util.Collection;
import java.util.List;

import util.MinMax;
import util.dbg.Logger;

public class ExhaustiveClust {
    
    /**
     * perform clustering by exhaustively testing all options with 
     * a predefined number and sizes of clusters.
     * 
     * @param pDataPoints
     * @param pClusterSizes
     * @param pClusterDistanceFunction
     * @pHaveOutlierCluster if true, the first cluster will have the special
     *                       meaning of being an "outlier cluster".
     * @pMinMax should we strive for the clustering with an minimal or maximal score,
     *         as computed by pClusteringCostFunction (hmm, maybe this should
     *         be made a property of the function itself!)
     */
    public static <T> Clustering<T> performClustering(
            Collection<T> pDataPoints,
            int pMinClusterSize,
            Integer pMaxClusterSize,
            Constraint pConstraint,
            ClusteringCostFunction<T> pClusteringCostFunction,
            boolean pHaveOutlierCluster,
            MinMax pMinMax) {
                        
        List<Clustering<T>> clusterings = 
            Clustering.formAllClusterings(pDataPoints, pMinClusterSize, pMaxClusterSize, 
                                          pConstraint, pHaveOutlierCluster, null);  
                                                  

        double optCost = pMinMax.worstValue;
        Clustering bestClustering = null;
        
//        Logger.info("Formed "+clusterings.size()+" different clusterings");
//                     SU.toString(clusterings,"\n\n"));        
        
        for (Clustering<T> c: clusterings) {
            double cost = pClusteringCostFunction.compute(c);
            // Logger.info("Computed cost for clustering: "+cost);
            c.setScore(cost);
//            Logger.info("Computed cost for clustering: "+c+": "+cost);
            
            if (pMinMax == MinMax.MIN) {
                if (cost < optCost) {
                    optCost = cost;
                    bestClustering = c;
                }
            }
            else if (pMinMax == MinMax.MAX) {
                if (cost > optCost) {
                    optCost = cost;
                    bestClustering = c;
                }
            }
        }
        
        pClusteringCostFunction.compute(bestClustering, true);
        
        Logger.info("Best clustering has "+pMinMax.costOrScore+": "+bestClustering.getScore());
        
        return bestClustering;
        
    }
       
//    public static <T> Clustering<T> performClustering(
//            Collection<T> pDataPoints,
//            List<Integer> pClusterSizes,
//            ClusteringCostFunction<T> pClusteringCostFunction,
//            boolean pHaveOutlierCluster) {
//        return performClustering(pDataPoints, 
//                                 new ClusterSizesConstraint(pClusterSizes),
//                                 pClusteringCostFunction,
//                                 pHaveOutlierCluster,
//                                 MinMax.MIN);
//    }
    
//    public static <T> Clustering<T> performClustering(
//            Collection<T> pDataPoints,
//            int pK,          
//            ClusteringCostFunction<T> pClusteringCostFunction,
//            boolean pHaveOutlierCluster) {
//        return performClustering(pDataPoints, 
//                                 new KConstraint(pK),
//                                 pClusteringCostFunction,
//                                 pHaveOutlierCluster,
//                                 MinMax.MIN);
//                                 
//    }
    
//    public static void main(String[] args) throws Exception {        
//        int k = Integer.parseInt(args[0]);
//        List<String> elements = IOUtils.readLines();
//        SymmetricPair<Integer> clusterSizes = new SymmetricPair(k, elements.size()-k);
//        Logger.info("Performing clustering with following cluster sizes: "+clusterSizes);
//        Clustering<String> result = performClustering(elements,
//                                                      new ExhaustiveClust.ClusterSizesConstraint(clusterSizes), 
//                                                      new ConstantFunction<Clustering<String>, Double>(1.d));
//        System.out.println("The optimal clustering: "+result);
//        
//    }
    
    public interface Constraint {
        
    }
    
    public static class KConstraint implements Constraint {

        public int k;
        
        public KConstraint(int k) {
            this.k = k;
        }
    }
    
    public static class MaxKConstraint implements Constraint {

        public int maxk;
        
        public MaxKConstraint(int maxk) {
            this.maxk = maxk;
        }
    }
    
    public static class ClusterSizesConstraint implements Constraint {
       
        List<ClusterSize> sizes;
        
        public ClusterSizesConstraint(List<ClusterSize> sizes) {
            this.sizes = sizes;
        }
    }
}
