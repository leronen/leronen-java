package util.algorithm.clustering;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import util.CollectionUtils;
import util.algorithm.clustering2.PairClassification;
import util.collections.MultiMap;
import util.collections.UnorderedPair;
import util.dbg.Logger;

public class ClusteringUtils {
    
    /** 
     * Return the object for which the average distance to the others is smallest.
     * (todo: should we actually use squared distances?)
     * 
     * Break ties arbitrarily.
     * 
     * Unfortunately, the implementation here may not be the fastest possible. 
     */
    public static <T> T centroid(Collection<T> pClusterMembers, IDistanceFunction<T> pDist) {
        
        if (pClusterMembers.isEmpty()) {
            throw new RuntimeException("No elements in cluster!");
        }        
        else if (pClusterMembers.size() == 1) {
            return pClusterMembers.iterator().next();
        }
        else {
            // we actually have to do some work        
            T c = null;
            double minDistance = Double.MAX_VALUE; 
            
            for (T o: pClusterMembers) {
                double d = avgDistance(o, pClusterMembers, pDist);
                if (d < minDistance) {
                    minDistance = d;
                    c = o;
                }
            }
            return c;
        }
        
        
    }
    
    /** Compute the average distance (not squared!) between the elements of a cluster */
    public static <T> double avgDistance(Collection<T> pElements, IDistanceFunction<T> pDist) {      
        List<UnorderedPair<T>> pairs = 
            CollectionUtils.makeUnorderedPairs(new LinkedHashSet<T>(pElements));
        
        double sum = 0;
        for (UnorderedPair<T> pair: pairs) {
            sum+=pDist.dist(pair.getObj1(), pair.getObj2());
        }
        return sum / pairs.size();
    }
    
    public static <T> double maxDistance(Collection<T> pElements, IDistanceFunction<T> pDist) {
        List<UnorderedPair<T>> pairs = CollectionUtils.makeUnorderedPairs(new LinkedHashSet<T>(pElements));
        
        double max = -Double.MAX_VALUE;
        for (UnorderedPair<T> pair: pairs) {
            double d = pDist.dist(pair.getObj1(), pair.getObj2());
            if (d > max) {
                max = d;
            }
        }
        return max;
    }
    
    /**
     * Return the average distance from pElement to entries of pElements 
     * check if pElement is contained in pElements and if so, ignore it for
     * the purposes of the calculations.
     */  
    public static <T> double avgDistance(T pElement, Collection<T> pElements, IDistanceFunction<T> pDist) {
        
        int numElements = 0;
        double sum = 0;
        
        for (T other: pElements) {
            if (!(pElement.equals(other))) {
                numElements++;
                sum+=pDist.dist(pElement, other);
            }
        }
        
        return sum/numElements;
    }
    
    /**
     * Evaluate a clustering with respect to a "real" classification,
     * using the principles of the good old "Rand index".
     * 
     * Some terminology: "Cluster" is the predicted class, "class" is the real class

     * 
     * OLD COMMENT CONCERNING OUT-COMMENTED IMPL: Given two clusterings, 
     * "real" and "predicted", find for each real cluster
     * the predicted cluster that best matches it. It is possible (though hopefully
     * unlikely...) that the same predicted cluster will be a best match for
     * more than one cluster...
     * 
     * We shall then compute an evaluation score as the fraction of all nodes 
     * that are assigned to the respective best cluster of their original cluster.
     * 
     * Also, create a new attribute "<realattr>_mapped" to pGraph, which tries to 
     * map the (id:s of) real clusters into the best predicted ones, with the 
     * aim of seeing which ones eventually are the ones where the mapping failed.
     * Hmm, it could be done the other way as well, as the whole thing...
     * 
     * @param nullClass if not null, consider each member of this class as a class
     *        of its own
     * @param nullCluster if not null, consider each member of this class as a cluster
     *        of its own  
     */
    public static <T> ClusteringEvaluation<T> evaluateClustering(
                Map<T,String> pClassMap,
                Map<T,String> pClusterMap,
                String classAttr,
                String clusterAttr,
                String nullClass,
                String nullCluster) throws ParseException {        
//        Set<String> allClasses = new HashSet<String>(pClassMap.values());
//        Set<String> allClusters= new HashSet<String>(pClusterMap.values());
        
        if (!(pClassMap.keySet().equals(pClusterMap.keySet()))) {
            throw new RuntimeException("classMap and clusterMap have different keysets!");
        }
        
        Set<T> objects = pClassMap.keySet();
        
        Map<T,String> classMap = null;
        Map<T,String> clusterMap = null;
        
        if (nullClass != null) {
            // split null class to single clusters...
            classMap = new HashMap();
            int i = 1;
            for (T o: objects) { 
               String oldClass = pClassMap.get(o);
               String newClass;
               if (oldClass.equals(nullClass)) {
                   newClass = nullClass+"_"+i;
                   i++;
               }
               else {
                   newClass = oldClass;
               }
               classMap.put(o,newClass);               
            }                       
        }
        else {
            // no need to tamper
            classMap = pClassMap;
        }
        
        if (nullCluster != null) {
            // split null cluster to single clusters...
            clusterMap = new HashMap();
            int i = 1;
            for (T o: objects) { 
               String oldCluster = pClusterMap.get(o);
               String newCluster;
               if (oldCluster.equals(nullCluster)) {
                   newCluster = nullCluster+"_"+i;
                   i++;
               }
               else {
                   newCluster = oldCluster;
               }
               clusterMap.put(o,newCluster);               
            }                       
        }
        else {
            // no need to tamper
            clusterMap = pClusterMap;
        }
        
        MultiMap<String, T> membersByClass = CollectionUtils.inverseMultiMap(classMap, null);
        MultiMap<String, T> membersByCluster = CollectionUtils.inverseMultiMap(clusterMap, null);
                        
       
        List<UnorderedPair<T>> pairs = CollectionUtils.makeUnorderedPairs(objects);
        
        Map<UnorderedPair<T>, PairClassification> pairClassifications = 
            new LinkedHashMap<UnorderedPair<T>, PairClassification>();
        
        int TP=0, TN=0, FP=0, FN=0;
        for (UnorderedPair<T> pair: pairs) {
            T n1 = pair.getObj1();
            T n2 = pair.getObj2();
            String cluster1 = clusterMap.get(n1);
            String cluster2 = clusterMap.get(n2);
            String class1 = classMap.get(n1);
            String class2 = classMap.get(n2);
            boolean sameClass = class1.equals(class2);
            boolean sameCluster = cluster1.equals(cluster2);
            if (sameClass) {
                if (sameCluster) {
                    // members of same class assigned to same cluster, good!
                    pairClassifications.put(pair, PairClassification.TP);
                    TP++;
                }
                else {
                    // members of same class assigned to different clusters, bad.
                    pairClassifications.put(pair, PairClassification.FN);
                    FN++;
                }
            }
            else {
                if (sameCluster) {
                    // members of different classes assigned to same cluster, bad.
                    pairClassifications.put(pair, PairClassification.FP);
                    FP++;
                }
                else {
                    // members of different classes assigned to different clusters, good!
                    pairClassifications.put(pair, PairClassification.TN);
                    TN++;
                }
            }
        }
        Logger.info("TP="+TP+", TN="+TN+", FN="+FN+", FP="+FP);
        
//        double RI = ((double)(TP+TN)) / (TP+TN+FP+FN);
//        Logger.info("RI: "+RI);
        
        ClusteringEvaluation eval =          
               new ClusteringEvaluation(classAttr, 
                                        clusterAttr,
                                        membersByClass,
                                        membersByCluster,
//                                        bestClusterByClass,
//                                        bestClusterSizeByClass,
//                                        fractionMappedToBestCluster,
//                                        fractionNotClustered,
//                                        RI,
                                        pairClassifications);
        
        eval.TP = TP;
        eval.FP = FP;
        eval.TN = TN;
        eval.FN = FN;        
        
        return eval;
    }
    
    
    
    
    /**
     * Evaluate a predicted binary classification with respect to a "real" classification.
     * 
     */
    public static <T> BinaryClassificationEvaluation<T> evaluateBinaryClassification(
                Map<T,String> trueMap,
                Map<T,String> predMap,
                String positiveClass,
                String negativeClass) throws ParseException {        
                                                      
        if (!(trueMap.keySet().equals(predMap.keySet()))) {
            throw new RuntimeException("Different keysets for true and predicted classifications!");
        }
        
        Set<T> objects = new LinkedHashSet(trueMap.keySet());
        
        int TP=0, TN=0, FP=0, FN=0;
        
        for (T o: objects) {            
            String trueClass = trueMap.get(o);            
            String predClass = predMap.get(o);
                        
            if (trueClass.equals(positiveClass)) {
                if (predClass.equals(positiveClass)) {
                    TP++;
                }
                else if (predClass.equals(negativeClass)) {
                    FN++;
                }
                else {
                    throw new RuntimeException("Illegal predicted class: "+predClass);
                }
            }
            else if (trueClass.equals(negativeClass)) {
                if (predClass.equals(positiveClass)) {
                    FP++;
                }
                else if (predClass.equals(negativeClass)) {
                    TN++;
                }
                else {
                    throw new RuntimeException("Illegal predicted class: "+predClass);
                }               
            }
            else {
                throw new RuntimeException("Illegal true class: "+trueClass);
            }

        }
        Logger.info("TP="+TP+", TN="+TN+", FN="+FN+", FP="+FP);
        
        BinaryClassificationEvaluation<T> eval =          
               new BinaryClassificationEvaluation<T>(TP, FP, TN, FN);                                         
               
        return eval;
    }

   
        
    /** 
     * For holding the results of {@link #evaluateClustering}. 
     */
    public static class ClusteringEvaluation<T> {
        private String attr1;        
        private String attr2;
        
        private MultiMap<String, T> realClusters;        
        private MultiMap<String, T> predClusters;
//        Map<String,String> bestMatches;
//        Map<String,Integer> bestCounts;
        private Map<UnorderedPair<T>, PairClassification> pairClassifications; 
//        double fractionMappedToBestCluster;
//        double fractionNotClustered;
        /** the "Rand Index", defined for _pairs_ as (TP+TN)/(TP+TN+FP+FN) */ 
//        private double RI;
        /** in came class, assigned to same cluster */
        private double TP;
        /** in different classes, assigned to same cluster */
        private double FP;
        /** in different classes, assigned to different clusters */
        private double TN;
        /** in same class, assigned to different clusters */
        private double FN;
        
        public ClusteringEvaluation(String realClusterAttr,
                                    String predClusterAttr,
                                    MultiMap<String, T> realClusters,
                                    MultiMap<String, T> predClusters,
//                                    Map<String,String> bestMatches,
//                                    Map<String,Integer> bestCounts,
//                                    double fractionMappedToBestCluster,
//                                    double fractionNotClustered,
//                                    double RI,
                                    Map<UnorderedPair<T>, PairClassification> pairClassifications) {
            this.attr1 = realClusterAttr;
            this.attr2 = predClusterAttr;
            this.realClusters = realClusters;
            this.predClusters = predClusters;
//            this.bestMatches = bestMatches;
//            this.bestCounts = bestCounts;
//            this.fractionMappedToBestCluster = fractionMappedToBestCluster;
//            this.fractionNotClustered = fractionNotClustered;
//            this.RI = RI;            
            this.pairClassifications = pairClassifications;
        }
        
        public String getAttr1() {
            return attr1;
        }

        public String getAttr2() {
            return attr2;
        }
        
        public MultiMap<String, T> getRealClusters() {
            return realClusters;
        }

        public MultiMap<String, T> getPredClusters() {
            return predClusters;
        }
        
        /** 
         * number of pairs that actually belong to same class,
         * that is TP+FN 
         */
        public double getNumPositivePairs_true() {
            return TP+FN;
        }
        
        /** 
         * number of pairs predicted to belong to same class,
         * that is TP+FP.
         */
        public double getNumPositivePairs_predicted() {
            return TP+FP;
        }
        
        /** 
         * number of pairs that actually belong to different classes,
         * that is TN+FP
         */
        public double getNumNegativePairs_true() {
            return TN+FP;
        }
        
        /** 
         * number of pairs that actually belong to different classes,
         * that is TN+FN
         */
        public double getNumNegativePairs_predicted() {
            return TN+FN;
        }
        
        /** number of pairs correctly classified, that is TP+TN */
        public double getNumTruePairs() {
            return TP+TN;
        }
        
        /** number of pairs incorrectly classified, that is FP+FN */
        public double getNumFalsePairs() {
            return  FP+FN;
        }
        
        public double getNumPairs() {
            return TP+FP+TN+FN;
        }
        
        public double getTP_scaled() {
            return TP / getNumPairs();
        }
        
        public double getFP_scaled() {
            return FP / getNumPairs();
        }
        
        public double getFN_scaled() {
            return FN / getNumPairs();
        }
        
        public double getTN_scaled() {
            return TN / getNumPairs();
        }        
        
        /** Fraction of true positives of the predicted true pairs */         
        public double getPrecision() {
            return TP / getNumPositivePairs_predicted(); 
        }
        
        /** Fraction of pairs in same class predicted to be in same class */
        public double getRecall() {
            return TP / getNumPositivePairs_true(); 
        }
        
        /** Fraction of correctly predicted pairs */
        public double getAccurary() {
            double RI = ((double)(TP+TN)) / (TP+TN+FP+FN);
            return RI; 
        }
        
        public Map<UnorderedPair<T>, PairClassification> getPairClassifications() {
            return pairClassifications;
        }
        
    }
    
    /** 
     * For holding the results of evaluating a binary ("positive" vs. "negative")
     * classification. Counts are absolute (integers).   
     */
    public static class BinaryClassificationEvaluation<T> {

        private double TP;
        private double FP;
        private double TN;
        private double FN;
        
        /** Counts are absolute (integers). */
        public BinaryClassificationEvaluation(int tp, 
                                              int fp, 
                                              int tn,
                                              int fn) {            
            TP = tp;
            FP = fp;
            TN = tn;
            FN = fn;
        }
        
                         
        public int getFP() {
            return (int)FP;
        }
        
        public int getTP() {
            return (int)TP;
        }
        
        public int getFN() {
            return (int)FN;
        }
        
        public int getTN() {
            return (int)TN;
        }
        
        /** 
         * number of pairs that actually belong to same class,
         * that is TP+FN 
         */
        public double getNumPositive_true() {
            return TP+FN;
        }
        
        /** 
         * number of  predicted to belong to same class,
         * that is TP+FP.
         */
        public double getNumPositive_predicted() {
            return TP+FP;
        }
        
        /** 
         * number of  that actually belong to different classes,
         * that is TN+FP
         */
        public double getNumNegative_true() {
            return TN+FP;
        }
        
        /** 
         * number of  that actually belong to different classes,
         * that is TN+FN
         */
        public double getNumNegative_predicted() {
            return TN+FN;
        }
        
        /** number of  correctly classified, that is TP+TN */
        public double getNumTrue() {
            return TP+TN;
        }
        
        /** number of  incorrectly classified, that is FP+FN */
        public double getNumFalse() {
            return  FP+FN;
        }
        
        public double getNumObjects() {
            return TP+FP+TN+FN;
        }
        
        public double getTP_scaled() {
            return TP / getNumObjects();
        }
        
        public double getFP_scaled() {
            return FP / getNumObjects();
        }
        
        public double getFN_scaled() {
            return FN / getNumObjects();
        }
        
        public double getTN_scaled() {
            return TN / getNumObjects();
        }        
        
        public double getTPRate() {
            return TP / getNumPositive_true(); 
        }

        public double getFPRate() {
            return FP / getNumNegative_true(); 
        }
        
//        public double getP() {
//            return TP+FN;
//        }
//       
//        
//        public double getP() {
//            return TP+FN;
//        }

        
        /** Fraction of true positives of the predicted posives */         
        public double getPrecision() {
            return TP / getNumPositive_predicted(); 
        }
        
        /** Fraction of pairs in same class predicted to be in same class */
        public double getRecall() {
            return TP / getNumPositive_true(); 
        }
        
        /** Fraction of correctly predicted pairs */
        public double getAccurary() {
            double RI = ((double)(TP+TN)) / (TP+TN+FP+FN);
            return RI; 
        }
        
        /** Fraction of correctly predicted pairs */
        public double getFScore() {
            double precision = getPrecision();
            double recall = getRecall();
            double fScore = 2.d * (precision*recall) / (precision+recall);
            return fScore; 
        }
                
    }
        

    
    
}
