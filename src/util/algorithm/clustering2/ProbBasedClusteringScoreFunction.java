package util.algorithm.clustering2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import util.CollectionUtils;
import util.IOUtils;
import util.MathUtils;
import util.SU;
import util.StringUtils;
import util.collections.Function;
import util.collections.HashMultiSet;
import util.collections.MultiSet;
import util.collections.UnorderedPair;
import util.dbg.Logger;
import util.math.PiecewiseConstantPDF;

public class ProbBasedClusteringScoreFunction<T> extends ClusteringCostFunction<T> {
    
    int mTotalNumElements;
    PiecewiseConstantPDF mTruePDF;    
    PiecewiseConstantPDF mNullPDF;
    Function<UnorderedPair<T>, Double> mPairwiseFunc;

    /** Indexed by degree (unused degrees represented by null entries */
    private PiecewiseConstantPDF[] mTruePDFByDegreeSum;
    private PiecewiseConstantPDF mFallBackTruePDF;
    /** Indexed by degree (unused degrees represented by null entries */
    private PiecewiseConstantPDF[] mNullPDFByDegreeSum;
    private PiecewiseConstantPDF mFallBackNullPDF;
        
    /** log({@link #mPriorClusterProbability}) */
    private double mPriorPosLogProbability = Double.NaN;  
    /** 1-log({@link #mPriorClusterProbability}) */
    private double mPriorNegLogProbability = Double.NaN;
    
    private Map<T, Integer> mDegByElement;
    
    /**
     * Uniform prior for all possible clusterings, that is in practice: 
     * 1/num_possible_clusterings.
     *  
     * Of course, use the log of the prob for computational reasons.
     */
    private double mUniformLogPrior = Double.NaN;

    private Prior mPrior;
    
    /**
     * Uniform prior for all possible k; for each k, then the prior is uniform  
     * over all clusterings with that k: 1/num_possible_clusterings(k).
     *  
     * A special case; when intepret k=1 as k=2 here, to avoid giving outrageous
     * probability to the case of single cluster.
     * 
     * Of course, use the log of the prob for computational reasons.
     * 
     * @param pSizeCounts number of possible clusterings for each k.
     */
    private Map<Integer, Double> mKBasedLogPrior = null;

    /**
     * Note that one has to call setPDFFunctions of setBinwisePDFFunctions
     * after calling this constructor! 
     */
    public ProbBasedClusteringScoreFunction(int totalNumElements,                                           
                                           Function<UnorderedPair<T>, Double> pairwiseFunc,
                                           Double pPriorPosProbability,
                                           MultiSet<Integer> pSizeCounts,
                                           Prior pPrior) {
        super();
        mTotalNumElements = totalNumElements;

        mPairwiseFunc = pairwiseFunc;
        
        // initialize prior
        mPrior = pPrior;
        switch (mPrior) {
            case POS_PROB:
                if (pPriorPosProbability == null
                        || pPriorPosProbability < 0 
                        || pPriorPosProbability > 1 ) {
                    throw new IllegalArgumentException("Prior == POS_PROB, which means that a prior pos probability in [0,1] should be provided (was: "+pPriorPosProbability);
                }
                mPriorPosLogProbability = Math.log(pPriorPosProbability);
                mPriorNegLogProbability = Math.log(1-pPriorPosProbability);
                break;
            case UNIFORM:
                int count = 0;
                for (Integer k: pSizeCounts) {
                    count += pSizeCounts.getCount(k);
                }
                int numPossibleClusterings = count;
                Logger.info("Counted number of possible clusterings: "+numPossibleClusterings);
                mUniformLogPrior = Math.log(1.d/numPossibleClusterings);
                Logger.info("Uniform log prior: "+mUniformLogPrior);
                break;
            case UNIFORM_K:
                mKBasedLogPrior = new HashMap<Integer, Double>();
                
                boolean hasK1 = false;
                int countDifferentKs = 0;                               
                
                for (Integer k: pSizeCounts) {
                    if (k == 1) {
                        hasK1 = true;
                    }
                    else {
                        countDifferentKs++;
                    }
                }
                if (countDifferentKs==0) {
                    throw new RuntimeException("WhatWhatWhat?!?!?!?");                    
                }
                
                MultiSet<Integer> sizeCounts;
                
                if (hasK1) {
                    sizeCounts = new HashMultiSet<Integer>();
                    for (Integer k: pSizeCounts) {
                        if (k == 1) {
                            // interpret 1 as 2 here...
                            sizeCounts.add(2, pSizeCounts.getWeight(1));
                        }
                        else {
                            // default behavior
                            sizeCounts.add(k, pSizeCounts.getWeight(k));
                        }
                    }
                }
                else {
                    // no need to tamper
                    sizeCounts = pSizeCounts;
                }
                
                for (Integer k: sizeCounts) {
                    double c = sizeCounts.getWeight(k);
                    double prob = 1.d/(c*countDifferentKs);                                                                                                    
                    double logProb = Math.log(prob);
                    mKBasedLogPrior.put(k, logProb);
                    Logger.info("Added prior for clusterings of size "+k+": "+prob);
                }
                break;                
            default:
                throw new AssertionError("WhatWhatWhat?!?!?!?");
        }
    }
    
    public void setBinwisePDFFunctions(String pBinsFile_pos, 
                                       String pBinsFile_neg,
                                       Map<T,Integer> pDegByElement) throws IOException {
        setBinwisePDFFunction(pBinsFile_pos, Distribution.POS);
        setBinwisePDFFunction(pBinsFile_neg, Distribution.NULL);
        mDegByElement = pDegByElement;
    }
        
    public void setPdfFunctions(String pTrueDistFile,
                                String pNullDistFile) throws IOException {
        mTruePDF = new PiecewiseConstantPDF(pTrueDistFile);
        mNullPDF = new PiecewiseConstantPDF(pNullDistFile);
    }
    
    public void setBinwisePDFFunction(String pBinsFile, Distribution pDistribution) throws IOException {
        List<String> lines_pos = IOUtils.readLines(pBinsFile);
        ArrayList<PiecewiseConstantPDF> pdfList = new ArrayList();
        
        boolean firstBin = true;
        int start;
        int end;
        PiecewiseConstantPDF pdf =  null;
        for (String line: lines_pos) {
            String[] tok = line.split("\\s+");
            start = new Integer(tok[1]);
            end = new Integer(tok[2]);            
            String file = tok[3];
            CollectionUtils.addNulls(pdfList, end+1);
            pdf = new PiecewiseConstantPDF(file);
            for (int i=firstBin ? 0 : start; i<= end; i++) {
                pdfList.set(i, pdf);
            }
            firstBin = false;
        }
        
        PiecewiseConstantPDF[] pdfArr = new PiecewiseConstantPDF[pdfList.size()];
        for (int i=0; i<pdfArr.length; i++) {
            pdfArr[i] = pdfList.get(i);
        }
        
        if (pDistribution == Distribution.POS) {            
            mTruePDFByDegreeSum = pdfArr;
            mFallBackTruePDF = pdf; // the last pdf for sums above last range
        }
        else if (pDistribution == Distribution.NULL) {            
            mNullPDFByDegreeSum = pdfArr;
            mFallBackNullPDF = pdf; // the last pdf for sums above last range
        } 
        else {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }
        
        
   }     
    
    /**
     * after calling this with a non-null val, the function should always consider
     * the cost of the outlier cluster to be a constant pVal. Recall that
     * if set, the first cluster is always considered to be the outlier cluster!
     */
    public void setOutlierClusterCost(Double pVal) {
        throw new UnsupportedOperationException(); 
    }            

    
    /**
     * Compute the (unnormalized) log-probability of a clustering;
     * That is, logarithm of the probability of the data (matrix) given the clustering,
     * multiplied by the prior probability of the clustering (prior probabilities
     * may be uniform, in which case they are not factored in).
     *  
     * In other words: score = log(P(M|c) * p(c)), where M is the data and c is the 
     * clustering. Posterior probability is then obtained as exp(score) / P(M)),
     * where P(M) = sum_c{P(M|c) * p(c)).
     * 
     */
    @Override
    public double compute(Clustering<T> pClustering, boolean pStoreExplanation) {
        if (pClustering == null) {
            throw new RuntimeException("Null clustering!");
        }               
        
        boolean firstClusterIsOutlier = pClustering.hasOutlierCluster();
        
        int n = mTotalNumElements;        
        
        int[] membersArr = new int[n];
        List<T> objectsList = new ArrayList<T>(n);
        for (int i=0; i<mTotalNumElements; i++) {
            objectsList.add(null);
        }
        
        List<? extends ICluster<T>> clusters = pClustering.getClusters();
        int nc = clusters.size();
        int i=0;
        for (int ci=0; ci<nc; ci++) {
            ICluster<T> c = clusters.get(ci);
            int cs = c.size();
            for (int ei=0; ei<cs; ei++) {
                membersArr[i] = ci;
                objectsList.set(i, c.get(ei));
                i++;
            }                  
        }
        
        double withinClusterSum = 0;
        double nullSum = 0;
        double betweenClustersSum = 0;
        
        int numWithinClusterPairs = 0;
        int numBetweenClusterPairs = 0;
        int numNullPairs = 0;
                       
        Relation rel;
                
        for (i=0; i<n; i++) {                
            for (int j=i+1; j<n; j++) {
                int ci = membersArr[i];
                int cj = membersArr[j];
                if (ci == cj) {
                    // elements in same cluster...
                    if (firstClusterIsOutlier && ci==0) {
                        // ...the null cluster, that is
                        rel = Relation.NULL_CLUSTER;                        
                    }
                    else {
                        // not the null cluster
                        rel = Relation.SAME_CLUSTER;
                    }
                }
                else {
                    // in different clusters
                    rel = Relation.DIFFERENT_CLUSTERS;
                }
                
                T oi = objectsList.get(i);
                T oj = objectsList.get(j);
                double goodness = mPairwiseFunc.compute(new UnorderedPair(oi,oj));
                double density;                                                  
                
                switch (rel) {
                    case SAME_CLUSTER:
                        numWithinClusterPairs++;
                        
                        if (mTruePDFByDegreeSum != null) {
                            PiecewiseConstantPDF pdf;
                            int degSum = mDegByElement.get(oi)+mDegByElement.get(oj);
                            if (degSum > mTruePDFByDegreeSum.length-1) {
                                pdf = mFallBackTruePDF;
                            }
                            else { 
                                 pdf = mTruePDFByDegreeSum[degSum];
                            } 
                            density = pdf.f(goodness);
                        }
                        else {
                            density = mTruePDF.f(goodness);
                        }
                        
                        withinClusterSum += Math.log(density);
                        break;
                    case DIFFERENT_CLUSTERS:
                        numBetweenClusterPairs++;
                        
                        if (mNullPDFByDegreeSum != null) {
                            int degSum = mDegByElement.get(oi)+mDegByElement.get(oj);
                            PiecewiseConstantPDF pdf;
                            if (degSum > mNullPDFByDegreeSum.length-1) {
                                pdf = mFallBackNullPDF;
                            }
                            else { 
                                 pdf = mNullPDFByDegreeSum[degSum];
                            }
                             
                            density = pdf.f(goodness);
                        }
                        else {
                            density = mNullPDF.f(goodness);
                        }
                        
                        betweenClustersSum += Math.log(density);
                        break;
                    case NULL_CLUSTER:
                        numNullPairs++;
                        if (mNullPDFByDegreeSum != null) {
                            int degSum = mDegByElement.get(oi)+mDegByElement.get(oj);
                            PiecewiseConstantPDF pdf;
                            if (degSum > mNullPDFByDegreeSum.length-1) {
                                pdf = mFallBackNullPDF;
                            }
                            else { 
                                 pdf = mNullPDFByDegreeSum[degSum];
                            } 
                            density = pdf.f(goodness);
                        }
                        else {
                            density = mNullPDF.f(goodness);
                        }
                        nullSum += Math.log(density);
                        break;
                    default:
                        throw new AssertionError("Unknown Relation: " + rel);
                }
                
            }
        }
            
        
       
            
        double dataConditionalLogDensity = withinClusterSum + nullSum + betweenClustersSum;
        
        double score;            
        double priorLogProb = computePriorLogProb(pClustering);            
                
        score = dataConditionalLogDensity + priorLogProb;    
        
        if (pStoreExplanation) {
            StringBuffer expl = new StringBuffer();
            // TODO: re-enable!
            expl.append("within_cluster: sum="+SU.format(withinClusterSum)+   
                             " numpairs="+numWithinClusterPairs);
            expl.append("\n");
            expl.append("between_cluster: sum="+SU.format(betweenClustersSum)+ 
                                  " numpairs="+numBetweenClusterPairs);
            expl.append("\n");
            expl.append("null: sum="+SU.format(nullSum)+
                             " numpairs="+numNullPairs);
            
            expl.append("\n");            
            expl.append("total num. pairs="+(numWithinClusterPairs+numBetweenClusterPairs+numNullPairs));
            expl.append("\n");
            expl.append("expected total num. pairs="+MathUtils.numCombinations(mTotalNumElements, 2));
            
            expl.append("\n");
            expl.append("\n");
            expl.append("total score: ");
            expl.append(StringUtils.formatSum(withinClusterSum, nullSum, betweenClustersSum));                
            pClustering.setExplanation(expl.toString());                
            pClustering.setStatistic("CLUSTERING_PRIOR_PROB", Math.exp(priorLogProb));
            pClustering.setStatistic("CLUSTERING_PRIOR_LOG_PROB", priorLogProb);
            pClustering.setStatistic("CLUSTERING_CONDITIONAL_LOG_DENSITY", dataConditionalLogDensity);
            pClustering.setStatistic("CLUSTERING_CONDITIONAL_DENSITY", Math.exp(dataConditionalLogDensity));
            pClustering.setStatistic("CLUSTERING_SCORE", score);
            List<Integer> sizes = pClustering.getClusterSizes();
            pClustering.setStatistic("CLUSTER_SIZES",SU.toString(sizes,","));
            pClustering.setStatistic("NUM_CLUSTERS",sizes.size());
            
            
        }                                     
        return score;
    }

    public enum Prior {
        /**
         * Computed according to prior probability of a node belonging to a 
         * non-null clusters. Cannot currently be combined with k-based prior.  
         */
        POS_PROB,
        /** Uniform over all possible clusterings */
        UNIFORM,
        /** Uniform over all k; for a fixed k, uniform over each clustering 
         * with the same k.  
         */
        UNIFORM_K;
        
        public static List<String> names() {
            ArrayList<String> result = new ArrayList();
            for (Prior f: values()) {
                result.add(f.name());
            }
            return result;
        }
    }
    
    public double computePriorLogProb(Clustering<T> pClustering) {
        switch (mPrior) {
            case UNIFORM:
                return mUniformLogPrior;
            case POS_PROB:
                ICluster<T> c_null = pClustering.getCLuster(0);
                int numNeg = c_null.size();
                int numPos = mTotalNumElements - numNeg;
                return mPriorPosLogProbability * numPos 
                       + mPriorNegLogProbability * numNeg;
            case UNIFORM_K:
                int numClusters = pClustering.getNumClusters();
                if (numClusters == 1) numClusters = 2; // a special case
                return mKBasedLogPrior.get(numClusters);
            default:
                throw new RuntimeException("WhatWhatWhat?!?!?!?");
        
        }
    }        
    
    public double computePriorProb(Clustering<T> pClustering) {
        return Math.exp(computePriorLogProb(pClustering));                
    }
        
                
    
    
    @Override
    public Double compute(Clustering<T> p) {
        return compute(p, false);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @SuppressWarnings("unused")
    private class SquareMatrix {
        
        private double[][] data;
        
        private SquareMatrix(int n) {                   
            data = new double[n][];
            
            for (int i=0; i<n; i++) {
                data[i] = new double[n];
            }
        }
        
        private double get(ClusterElement<T> e1,
                           ClusterElement<T> e2) {
            return data[e1.mIndex][e2.mIndex];
        }
        
        private void set(ClusterElement<T> e1,
                         ClusterElement<T> e2,
                         double val) {
            data[e1.mIndex][e2.mIndex] = val;
        }
    }
    
    public enum Relation {
        NULL_CLUSTER,
        SAME_CLUSTER,
        DIFFERENT_CLUSTERS;
    }
    
    private enum Distribution {
        POS,
        NULL;
    }
       

}
