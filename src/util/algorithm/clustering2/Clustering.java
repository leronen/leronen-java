package util.algorithm.clustering2;

import java.util.*;

import util.CollectionUtils;
import util.ConversionUtils;
import util.IllegalParamsException;
import util.MathUtils;
import util.ObjectWrapper;
import util.Range;
import util.SU;
import util.StringUtils;
import util.Utils;
import util.algorithm.clustering2.ExhaustiveClust.ClusterSizesConstraint;
import util.algorithm.clustering2.ExhaustiveClust.Constraint;
import util.algorithm.clustering2.ExhaustiveClust.KConstraint;
import util.algorithm.clustering2.ExhaustiveClust.MaxKConstraint;
import util.collections.Function;
import util.collections.MultiMap;
import util.collections.MultiplyFunction;
import util.converter.Converter;
import util.converter.ConverterChain;
import util.converter.DoubleToStringConverter;
import util.converter.FunctionWrapperConverter;
import util.dbg.Logger;

/** 
 * A heavily loaded class, this is. e.g. variable stuff releted to "binary"
 * clustering that should not reside here...
 * 
 *
 * @param <T>
 */
public class Clustering<T> {
    
    /** Just for checking some invariants */
    private int mTotalNumElements;
    
    private List<ICluster<T>> mClusters;
    
    private transient Map<String, Object> mStatistics;    
    private transient String mExplanation;
    private transient Converter<T, String> mElementFormatter;
    
    /** 
     * A clustering just might be lucky enough to have a composite score.
     * Or then not.
     */
    private transient Double mScore;
    
    /** 
     * A clustering just might be lucky enough to have a posterior probability
     * Or then not.
     */
    private transient Double mPosteriorProb;
              
    private void resetDerivedData() {
        mStatistics = null;
        mExplanation = null;
        mScore = null;
        mPosteriorProb = null;
    }
    
    public boolean hasOutlierCluster() {
        return mClusters.get(0).isOutlier();
    }
        
    public void setStatistic(String pKey, Object pVal) {
        if (mStatistics == null) {
            mStatistics = new HashMap();
        }
        mStatistics.put(pKey, pVal);
    }
    
    /** May return null, if no statistics have been set */
    public Map<String, Object> getStatistics() {
        return mStatistics;
    }
    
    /** Reset score field of individual clusters */
    public void resetClusterScores() {
        for (ICluster c: getClusters()) {
            c.setCost(Double.NaN);
        }
    }    
    
    /** Move a node into another cluster */
    public void performMove(Move pMove) {
        resetDerivedData();
        pMove.src.remove(pMove.elem);
        pMove.dst.add(pMove.elem);
    }
    
    /** Move node(s) into another cluster */
    public void performMove(IMove pMove) {
        resetDerivedData();
        if (pMove instanceof Move) {
            Move m = (Move)pMove;
            m.src.remove(m.elem);
            m.dst.add(m.elem);
        }
        else if (pMove instanceof DoubleMove){
            DoubleMove dm = (DoubleMove)pMove;
            dm.src1.remove(dm.elem1);
            dm.dst1.add(dm.elem1);
            dm.src2.remove(dm.elem2);
            dm.dst2.add(dm.elem2);
        }
        else {
            throw new RuntimeException("Unknown IMove instance class: "+pMove.getClass().getName());
        }
    }
    
    public Clustering(int pTotalNumElements) {
        mClusters = new ArrayList();
        // TODO!
        mTotalNumElements = pTotalNumElements;
    }
    
    public Clustering(int capacity, int pTotalNumElements) {
        mClusters = new ArrayList(capacity);
        mTotalNumElements = pTotalNumElements;
    }
    
    public Clustering(Collection<? extends ICluster<T>> pClusters,
                      int pTotalNumElements) {
        mClusters = new ArrayList(pClusters);
        mTotalNumElements = pTotalNumElements; 
    }
    
    public Clustering(Collection<? extends ICluster<T>> pClusters,
            boolean pCombineSingletonClustersIntoOne,
            int pTotalNumElements) {
        if (pCombineSingletonClustersIntoOne) {
            // first cluster shall be an "outlier" cluster
            LinkedHashSet<T> outliers = new LinkedHashSet<T>();            
            for (ICluster<T> c: pClusters) {
                if (c.size() == 1) {
                    outliers.addAll(c.members());
                }
            }
            FCluster outlierCluster = new FCluster(outliers, true);
            mClusters = new ArrayList();            
            mClusters.add(outlierCluster);
            for (ICluster<T> c: pClusters) {
                if (c.size() > 1) {
                    mClusters.add(c);
                }
            }      
        }
        else {
            // simply copy the list
            mClusters = new ArrayList(pClusters);
        }

        mTotalNumElements = pTotalNumElements;
    }
    
    /**
     * Make a clone of this clustering, such that clusters are represented
     * by {@link FCluster} instances.
     */
    public Clustering<T> cloneAsFlat() {
        Clustering<T> clone = new Clustering(mTotalNumElements);
        for (ICluster<T> c: mClusters) {
            FCluster<T> fc = new FCluster<T>(c.members(), c.isOutlier());
            Integer id = c.getId();
            if (id != null) {
                fc.setId(id);
            }
            clone.mClusters.add(fc);            
        }
        return  clone;
    }
    
    
    
    public void numberElements() {
        
//        if (getNumClusters() == 1 && !(getCLuster(0).isOutlier())) {
//            // a special case; do not give id 0 to a non-outlier singleton cluster
//            mClusters.get(0).setId(2);
//            return;
//        }
        int firstId;
        if (getCLuster(0).isOutlier()) {
            firstId = 1;
        }
        else {
            // start numbering from 2
            firstId = 2;
        }
                
        for (int i=0; i<mClusters.size(); i++) {
            mClusters.get(i).setId(i+firstId);
        }
    }       
    
    public void setScore(double pScore) {
        mScore = pScore;
    }
    
    public Double getPosteriorProb() {
        return mPosteriorProb;
    }

    public void setPosteriorProb(double posteriorProb) {
        mPosteriorProb = posteriorProb;
    }
    
    public void setExplanation(String pExplanation) {
        mExplanation = pExplanation;
    }
    
    public String getExplanation() {
        return mExplanation;
    }
    
    public Double getScore() {
        return mScore;        
    }
    
    public List<? extends ICluster<T>> getClusters() {
        return mClusters;
    }
    
    /** Assume that the first or none of the clusters are outliers */
    public List<? extends ICluster<T>> getNonOutlierClusters() {
        if (mClusters.get(0).isOutlier()) {
            return CollectionUtils.tailList(mClusters, 1);
        }
        else {
            return mClusters;
        }
    }
    
    public int getNumClusters() {
        return mClusters.size();
    }
   
    
    /** Constructs a new ArrayList => do not call repeatedly */
    public List<Integer> getClusterSizes() {
        
        ArrayList<Integer> sizes = new ArrayList(mClusters.size());
        for (ICluster<T> c: mClusters) {
            sizes.add(c.size());
        }
        return sizes;
    }
    
    public ICluster<T> getCLuster(int pInd) {
        return mClusters.get(pInd);
    }
    
    
    public void setFormatter(Converter<T,String> p) {
        mElementFormatter = p;
    }    
        
    
    /** add a new cluster. do not generate an id automagically! */
    public void add(ICluster c) {        
        mClusters.add(c);
    }    
    
    
    
    /**
     * @param elementLists. can only put elements in each list into same cluster;
     * rest will be labeled outliers.
     * 
     * OK, this has not been developed to the fullest yet, as can be seen...
     */
//    @SuppressWarnings("unused")
//    public static <T> List<Clustering<T>> formAllClusterings_conncomps(int k,                                                             
//                                                                       List<List<T>> connComps) {                                                                                                                          
//        throw new UnsupportedOperationException();
//    }
    
    /* 
     * @param pPossibleClusterSizes return here all the possible size 
     * combinations for e.g. logging purposes. May be null, in which case
     * nothing is returned
     */  
    public static <T> List<Clustering<T>> formAllClusterings(
            Collection<T> pDataPoints,
            int pMinClusterSize,
            Integer pMaxClusterSize,
            Constraint pConstraint,            
            boolean pHaveOutlierCluster,
            ObjectWrapper<List<List<ClusterSize>>> pPossibleClusterSizes)
    {                    
        List<List<FCluster<T>>> rawResult = new ArrayList();        
        
        if (pConstraint instanceof ClusterSizesConstraint) {

            ClusterSizesConstraint csc = (ClusterSizesConstraint)pConstraint;

            rawResult = new ArrayList();                 
            formAllClusterings_fixed_cluster_sizes(csc.sizes, new ArrayList(pDataPoints), rawResult);
            
            if (pPossibleClusterSizes != null) {
                // return back the sizes given to us for logging purposes...
                List<List<ClusterSize>> sizes= Collections.singletonList(csc.sizes);
                pPossibleClusterSizes.obj = sizes;
            }
            
        }
        else if (pConstraint instanceof KConstraint) {
            KConstraint kc = (KConstraint)pConstraint;            
            rawResult = formAllClusterings(new ArrayList(pDataPoints), pMinClusterSize, pMaxClusterSize, kc.k, kc.k, pHaveOutlierCluster, pPossibleClusterSizes);                       
        }
        else if (pConstraint instanceof MaxKConstraint) {
            MaxKConstraint mkC = (MaxKConstraint)pConstraint;
            rawResult = formAllClusterings(new ArrayList(pDataPoints), pMinClusterSize, pMaxClusterSize, mkC.maxk, 1, pHaveOutlierCluster, pPossibleClusterSizes);
        }
        else {
            throw new RuntimeException("Unknown constraint: "+pConstraint);
        }
        
        Logger.info("Formed "+rawResult.size()+" different clusterings, converting to Cluster instances...");
        ArrayList<Clustering<T>> result = new ArrayList(rawResult.size());
        
        for (List<FCluster<T>> raw: rawResult) {
            Clustering<T> clustering = new Clustering(raw.size(), pDataPoints.size());
            for (FCluster<T> c: raw) {                
                clustering.add(c);
                
            }
            clustering.numberElements();                          
            result.add(clustering);
        }
        
        
        return result;
    }
    
    /**
     * @param pFirstClusterHasSpecialMeaning causes the first
     * of the returned clusters to have a special role; consequently,  e.g.
     * clustering (A,B) will be considered different from (B,A)
     * (where A and B are subsets of S). The intended use is 
     * for the first cluster to be considered a "outlier" cluster,
     * with uniform contribution to the cost function.
     */
//    public static <T> List<Clustering<T>> formAllClusterings_k(int k,                                 
//                                                               List<T> elements, 
//                                                               boolean pFirstClusterHasSpecialMeaning) {
//        int numElements = elements.size();
//        if ( k != 2) {
//            throw new UnsupportedOperationException("Unsupported k:" +k);
//        }
//                                        
//        ArrayList<Clustering<T>> allClusterings = new ArrayList();
//       
//        if (pFirstClusterHasSpecialMeaning) { 
//            for (int s1=1; s1 <= numElements-1; s1++) {
//                int s2 = numElements - s1;
//                Logger.info("Forming all clusterings with sizes: ("+s1+", "+s2+")");
//                allClusterings.addAll(formAllClusterings(s1, s2, elements, true));            
//            }
//        }
//        else {
//            // first cluster does not have the interpretation of being "outlier";
//            // disallow clusters of size 1, as those would really have to be
//            // designated as outlier clusters...
//            int maxSmallerClusterSize = numElements / 2;
//            
//            for (int s1=2; s1 <= maxSmallerClusterSize; s1++) {
//                int s2 = numElements - s1;
//                Logger.info("Forming all clusterings with sizes: ("+s1+", "+s2+")");
//                allClusterings.addAll(formAllClusterings(s1, s2, elements, false));            
//            }
//        }
//            
//        
//        
//        return allClusterings;
//        
//    }
    
    public static <T> List<Clustering<T>> formAllClusterings(int clusterSize1,
                                                             int clusterSize2,
                                                             List<T> elements,
                                                             boolean pFirstClusterIsOutlier) {
        if (clusterSize1 + clusterSize2 > elements.size()) {
            // have to decrease given cluster sizes. do this proportionally 
            // to specified sizes...
            if (pFirstClusterIsOutlier) {
                Utils.die("Do not know how to decrease cluster sizes in presence"+
                          " of outliers!");                		
            }
            int numElements = elements.size();
            int largerSize = Math.max(clusterSize1, clusterSize2);
            int smallerSize = Math.min(clusterSize1, clusterSize2);
            int totalGivenSize = smallerSize+largerSize;
            int totalDecrement = totalGivenSize-numElements;
            Logger.info("totalDecrement: "+totalDecrement);
            double relSmallerSize = ((double)smallerSize) / totalGivenSize;
            Logger.info("relSmallerSize: "+relSmallerSize);
            int smallerDecrement = (int)(relSmallerSize * totalDecrement);
            int largerDecrement = totalDecrement-smallerDecrement;
            int adjustedSmallerSize = smallerSize-smallerDecrement;
            int adjustedLargerSize = largerSize-largerDecrement;
            Logger.warning("Too large cluster sizes given, adjusting "+
                           "("+smallerSize+","+largerSize+") to: "+
                           "("+adjustedSmallerSize+","+adjustedLargerSize+")");
                           
        }
        else if (clusterSize1 + clusterSize2 < elements.size()) {
            throw new RuntimeException("Too small total cluster size given!");
        }
        
        int n = elements.size();
        int k = clusterSize1;
        
        List<BitSet> bitSets = CollectionUtils.allSubsets(n, k);
        
        List<Clustering<T>> result = new ArrayList();
        
        for (BitSet bs: bitSets) {
            ArrayList members1 = new ArrayList(bs.cardinality());
            ArrayList members2 = new ArrayList(elements.size()-bs.cardinality());
            for (int i = 0; i<n; i++) {
                if (bs.get(i)) {
                    members1.add(elements.get(i));
                }
                else {
                    members2.add(elements.get(i));
                }                   
            }
            FCluster c1 = new FCluster(members1, pFirstClusterIsOutlier);
//            if () {
//                c1.setOutlier();
//            }
            FCluster c2 = new FCluster(members2, false);
            Clustering<T> clustering = new Clustering(elements.size());
            clustering.add(c1);
            clustering.add(c2);
            clustering.numberElements();
            result.add(clustering);
            
        }
        
        return result;
        
    }
    
//    /** For "binary" clusterings only! */
//    public ICluster<T> getOutlierCluster() {
//        return mClusters.get(0);
//    }
    
    /** For "binary" clusterings only! */
    public ICluster<T> getPositiveCluster() {
        return mClusters.get(1);
    }
    
    public static <T> void formAllClusterings_fixed_cluster_sizes(
            List<ClusterSize> pClusterSizes,                                                            
            List<T> pElements,                                                                                                    
            List<List<FCluster<T>>> pResult) 
    {        
        internalFormAllClusterings(pClusterSizes, pElements, new ArrayList(), pResult);
    }
        
    /**
     * @param pPossibleClusterSizes return here all the possible size 
     * combinations for e.g. logging purposes.
     */
    public static <T> List<List<FCluster<T>>> formAllClusterings(
            List<T> pElements,
            Integer pMinClusterSize,
            Integer pMaxClusterSize,
            Integer pMaxNumClusters,
            Integer pMinNumClusters,
            boolean pHaveOutlierCluster,
            ObjectWrapper<List<List<ClusterSize>>> pPossibleClusterSizes)  
    {                    
        
        if (pPossibleClusterSizes != null && pPossibleClusterSizes.obj != null) {
            throw new IllegalParamsException(
                    "The object wrapper pPossibleClusterSizes should not "+
                    "initially contain any object!");
                    
        }
    
        List<List<ClusterSize>> possibleSizes = 
            formAllClusteringSizes(pElements.size(), pMinClusterSize, pMaxClusterSize, pMaxNumClusters, pMinNumClusters, pHaveOutlierCluster);
        
        if (pPossibleClusterSizes != null) {
            pPossibleClusterSizes.obj = possibleSizes;
        }
        
        List<List<FCluster<T>>> result = new ArrayList();
        
        for (List<ClusterSize> sizes: possibleSizes) {
            formAllClusterings_fixed_cluster_sizes(sizes, pElements, result);
        }       
               
        return result;        
        
    }
            
    /**
     * Recursively form all clusterings by performing a binary clustering at
     * each step.
     * 
     * @param pElements elements to cluster at this level (not the whole set of 
     * original elements)
     * @param pPrefix already formed part of current clustering
     * @param pResult list of all clusterings (each of which is a list of lists (sic))
     * 
     */
    public static <T> void internalFormAllClusterings(List<ClusterSize> pClusterSizes,                                                            
                                                      List<T> pElements,                                                      
                                                      List<FCluster<T>> pPrefix,
                                                      List<List<FCluster<T>>> pResult) {
//        Logger.loudInfo("Forming all clusterings for sizes: "+pClusterSizes);
//        Logger.info("Number of elements: "+pElements.size());        
//        Logger.info("pPrefix: "+pPrefix);               
        
        int n = pElements.size();
        int n1 = pClusterSizes.get(0).size;
        int n2 =n-n1; 
        
        if (n2 == 0) {
            // add just one cluster consisting of all remaining elements to prefix            
            ArrayList<FCluster<T>> clustering = new ArrayList(pPrefix.size()+1);
            clustering.addAll(pPrefix);
            FCluster c = new FCluster(pElements, pClusterSizes.get(0).isOutlier);            
            clustering.add(c);            
            pResult.add(clustering);
        }
        else {
            // form all binary clustering to (B1,B2))  so that first real cluster 
            // is B1 and the union of all other real clusters is B2;
            // B2 will then recursively be partitioned
        
//            Logger.info("Calling CollectionUtils.allSubsets("+n+", "+n1+")");
            List<BitSet> bitSets = CollectionUtils.allSubsets(n, n1);
//            Logger.info("Got bitsets: "+bitSets);
        
            for (BitSet bs: bitSets) {
                ArrayList<T> members1 = new ArrayList(n1);
                ArrayList<T> members2 = new ArrayList(n2);
                for (int i = 0; i<n; i++) {
                    if (bs.get(i)) {
                        members1.add(pElements.get(i));
                    }
                    else {
                        members2.add(pElements.get(i));
                    }                   
                }
                // OK, binary partition done, now do the recursive part...
                ArrayList<FCluster<T>> newPrefix = new ArrayList(pPrefix);
                FCluster c = new FCluster(members1, pClusterSizes.get(0).isOutlier); 
                newPrefix.add(c);                
                internalFormAllClusterings(CollectionUtils.tailList(pClusterSizes, 1),
                                           members2,
                                           newPrefix,
                                           pResult);
            }
        }
                        
    }
        
    

    /** 
     * @param pAlreadyDecidedSizes must contain the sizes of the first clusters that
     * have already been decided. these must be in descending order. 
     * the smallest of these has to be greater or equal to pMaxClusterSize. 
     *
     * No outlier clusters formed here, that must be done externally. 
     */
    private static void internalFormAllClusteringSizes(int pNumObjectsLeft,
                                                       int pTotalNumObjects,
                                                       int pMinClusterSize,
                                                       int pMaxClusterSize,
                                                       Integer pMaxNumClusters,
                                                       Integer pMinNumClusters,
                                                       List<ClusterSize> pAlreadyDecidedSizes,
                                                       List<List<ClusterSize>> pResult) {                                                      
        
//       Logger.loudInfo("Starting internalFormAllClusteringSizes...");
//       Logger.info("pNumObjectsLeft="+pNumObjectsLeft);
//       Logger.info("pTotalNumObjects="+pTotalNumObjects);
//       Logger.info("pMinClusterSize="+pMinClusterSize);
//       Logger.info("pMaxClusterSize="+pMaxClusterSize);
//       Logger.info("pMaxNumClusters="+pMaxNumClusters);
//       Logger.info("pMinNumClusters="+pMinNumClusters);
//       Logger.info("pAlreadyDecidedSizes="+pAlreadyDecidedSizes);
        
       if (pNumObjectsLeft == 0) {
           if (pMinNumClusters != null && pMinNumClusters > 0) {
               // no clusterings can be formed
               return;
           }
           // there shall be no more clusters, just take the
           // prefix as the singleton new clustering
           pResult.add(pAlreadyDecidedSizes);
           
           return;
       }
       
        if (pMinClusterSize > pNumObjectsLeft) {
            // no more clusters can be formed
            return;
        }
        
        if (pMinNumClusters != null && pMinNumClusters * pMinClusterSize > pNumObjectsLeft) {
            // no more clusters can be formed
            return;
        }
        
//        Logger.info("At CHECKPOINT 1");
        
        if (pMaxNumClusters != null && pMaxNumClusters.intValue() == 1) {            
            // put all remaining nodes to the last cluster            
            
            if (pNumObjectsLeft > pMaxClusterSize) {                                    
                return;
            }
            
            if (pNumObjectsLeft == 0) {
//                Logger.info("At CHECKPOINT 2");
                pResult.add(pAlreadyDecidedSizes);
            }
            else {
//                Logger.info("At CHECKPOINT 3");
                ArrayList<ClusterSize> newDecidedSizes = new ArrayList(pAlreadyDecidedSizes);
                newDecidedSizes.add(new ClusterSize(pNumObjectsLeft, false));
                pResult.add(newDecidedSizes);
            }
        }
        else {
//            Logger.info("At CHECKPOINT 4");
            // can still have at least 2 clusters
            if (pNumObjectsLeft == 0) {
//                Logger.info("At CHECKPOINT 5");
                // no more decisions to make                
                pResult.add(pAlreadyDecidedSizes);
            }
            else if (pNumObjectsLeft >= 1) {
//                Logger.info("At CHECKPOINT 6");
                // decide the size of the next-largest cluster
                int minNextClusterSize = 1;
                Integer nextMaxNumClusters = null;
                Integer nextMinNumClusters = null;
                if (pMaxNumClusters != null) {
                    nextMaxNumClusters = pMaxNumClusters-1;
                    int remainder = pNumObjectsLeft % pMaxNumClusters;
                    // min. number of objects in largest cluster, if max. num clusters is used
                    minNextClusterSize = 
                        pNumObjectsLeft / pMaxNumClusters +
                        remainder != 0 ? 1 : 0;                                                                               
                }
                
                minNextClusterSize = Math.max(minNextClusterSize, pMinClusterSize);
                
                int maxNextClusterSize = pMaxClusterSize;
                if (pMinNumClusters != null) {
                    nextMinNumClusters = pMinNumClusters-1;
                    // when deciding how much stuff to the next (largest) cluster, 
                    // we must ensure that next (pMinNumClusters-1) clusters get at least 
                    // pMinCluster size objects each 
                    maxNextClusterSize = Math.min(pMaxClusterSize, pNumObjectsLeft - (pMinNumClusters -1)*pMinClusterSize); 
                }
//                Logger.info("Min next cluster size: "+minNextClusterSize);
                                 
            
                for (int nextSize = maxNextClusterSize; nextSize >= minNextClusterSize; nextSize--) {
                    int numObjectsLeft = pNumObjectsLeft-nextSize;
                    ArrayList<ClusterSize> newDecidedSizes = new ArrayList(pAlreadyDecidedSizes);
                    newDecidedSizes.add(new ClusterSize(nextSize, false));
                    internalFormAllClusteringSizes(numObjectsLeft,
                                                   pTotalNumObjects,
                                                   pMinClusterSize,
                                                   Math.min(nextSize, numObjectsLeft),                                                   
                                                   nextMaxNumClusters,
                                                   nextMinNumClusters,
                                                   newDecidedSizes,
                                                   pResult);
                }
            }
            else  {
                throw new RuntimeException("WhatWhatWhat, negative num. objects left: "+pNumObjectsLeft+"?!?!?!?");
            }
        }
            
    }
                
    /** 
     *  
     * @param pNumObjects number of objects to be clustered
     * @param pMinClusterSize minimum size of a non-outlier cluster
     * @param pMaxNumClusters maximum number of clusters to be formed (may be null)
     * @param pMinNumClusters @minimum number of clusters to be formed (may be null)
     * @param pHaveOutlierCluster Shall the first of the returned clusters be 
     * an "outlier cluster" (actually conceptually similar to the case
     * where outliers would be a set of single-node clusters... 
     */
    private static List<List<ClusterSize>> formAllClusteringSizes(int pNumObjects,
                                                                  int pMinClusterSize,
                                                                  Integer pMaxClusterSize,
                                                                  Integer pMaxNumClusters,
                                                                  Integer pMinNumClusters,
                                                                  boolean pHaveOutlierCluster) {
        Logger.info("Form all clustering sizes: "+
                    "num objects="+pNumObjects+","+"" +
                    "min cluster size="+pMinClusterSize+","+"" +
                    "max cluster size="+pMaxClusterSize+","+"" +
                    "max num clusters="+pMaxNumClusters+","+"" +
                    "min num clusters="+pMinNumClusters+","+"" +
                    "have outlier cluster="+pHaveOutlierCluster);
                                            
        
        List<List<ClusterSize>> result = new ArrayList();
        
        if (pMaxNumClusters != null && pMaxNumClusters.intValue() == 0) {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }
        else if (pMinNumClusters != null && pMinNumClusters.intValue() > pNumObjects) {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }        
        else if (pMinNumClusters != null && pMaxNumClusters != null 
                    && pMinNumClusters > pMaxNumClusters)   
        {
            throw new  RuntimeException("WhatWhatWhat?!?!");
        }
        else if (pMaxNumClusters != null && pMaxNumClusters == 1) {            
            // now this is boring: only 1 cluster (a case which shall not be 
            // likely to pop up in any practical applications...
            if (pMinNumClusters != null && pMinNumClusters > 1) {
                throw new IllegalParamsException(""+pMinNumClusters +" == min number of clusters > max number of clusters == 1");
            }
            
            result.add(Collections.singletonList(new ClusterSize(pNumObjects, false)));
            
            if (pHaveOutlierCluster) {
                // also version with the outlier cluster only
                result.add(Collections.singletonList(new ClusterSize(pNumObjects, true)));
                
            }            
        }
        else {
            // the common case: at least 2 clusters allowed 
            if (pHaveOutlierCluster) {
                // always count the outlier cluster as a cluster in the 
                // "cluster bugdeg" defined by min/max num clusters,
                // even if it's size is 0.
                Integer maxNumRealClusters = 
                        pMaxNumClusters != null
                        ? pMaxNumClusters - 1
                        : null;
                Integer minNumRealClusters = 
                    pMinNumClusters != null
                    ? pMinNumClusters - 1
                    : null;
            
                for (int numOutliers = 0; numOutliers <= pNumObjects; numOutliers++) {                                    
                    int numNonOutliers = pNumObjects-numOutliers;
                    List<ClusterSize> prefix = new ArrayList();
                    prefix.add(new ClusterSize(numOutliers, true));
                    if (numNonOutliers > 0) {
                        if (minNumRealClusters == null || minNumRealClusters <= numNonOutliers) {
                            int maxClusterSize = 
                                    pMaxClusterSize != null
                                    ? Math.min(pMaxClusterSize, numNonOutliers)
                                    : numNonOutliers;
                            internalFormAllClusteringSizes(numNonOutliers, numNonOutliers, pMinClusterSize, maxClusterSize, maxNumRealClusters, minNumRealClusters, prefix, result);
                        }
                        else {
                            // presumably nothing can be formed?
                        }
                    }
                    else {                        
                        // just the outlier cluster                        
                        if (pMinNumClusters <= 1) {
                            result.add(prefix);
                        }
                    }
                }
            }
            else {
                // no outlier cluster
                int maxClusterSize = pMaxClusterSize != null 
                                     ? Math.min(pMaxClusterSize, pNumObjects)
                                     : pNumObjects; 
                
                internalFormAllClusteringSizes(pNumObjects, pNumObjects, pMinClusterSize, maxClusterSize, pMaxNumClusters, pMinNumClusters, new ArrayList(), result);
            }
        }
        
        return result;
        
    }
                               
    
    
    
    /** */ 
//    private static <T> void internalFormAllClusterings (int clusterSize1,
//                                                        int clusterSize2,
//                                                        List<T> elements,
//                                                        boolean pFirstClusterIsOutlier) {        
//        
//        int n = elements.size();
//        int k = clusterSize1;
//        
//        List<BitSet> bitSets = CollectionUtils.allSubsets(n, k);
//        
//        List<Clustering<T>> result = new ArrayList();
//        
//        for (BitSet bs: bitSets) {
//            ArrayList members1 = new ArrayList(bs.cardinality());
//            ArrayList members2 = new ArrayList(elements.size()-bs.cardinality());
//            for (int i = 0; i<n; i++) {
//                if (bs.get(i)) {
//                    members1.add(elements.get(i));
//                }
//                else {
//                    members2.add(elements.get(i));
//                }                   
//            }
//            FCluster c1 = new FCluster(members1);
//            if (pFirstClusterIsOutlier) {
//                c1.setOutlier();
//            }
//            FCluster c2 = new FCluster(members2);
//            Clustering<T> clustering = new Clustering();
//            clustering.add(c1);
//            clustering.add(c2);
//            clustering.numberElements();
//            result.add(clustering);
//            
//        }
//        
////        return result;
//        
//    }
//    
    
    
    
    public abstract static class AbstractCostFunctionImpl<T> extends ClusteringCostFunction<T> {
                
        protected Function<Collection<T>, Double> mSingleClusterFunction;
        protected Double mOutlierCost;
        
        protected AbstractCostFunctionImpl(Function<Collection<T>, Double> f) {                                           
            mSingleClusterFunction = f;
        }               
        
        public Double compute(Clustering<T> pClustering) {
            return compute(pClustering, false);
        }

        @Override
        public void setOutlierClusterCost(Double val) {
            mOutlierCost = val;
            
        }
        
    }

    public ICluster<T> getOutlierCluster() {        
        ICluster<T> firstCluster = mClusters.get(0); 
        if (!firstCluster.isOutlier()) {
            throw new RuntimeException("WhatWhatWhat, first cluster not outlier cluster?!?!?!?");
        }
        
        return firstCluster;            
    }
//        for (ICluster<T> c: mClusters) {
//            if (c.isOutlier()) {
//                return c;
//            }
//        }
//        
//        // no outlier clusters
//        return null;
//    }
    
    /**
     * For debug purposes (read: not efficient). 
     * Actually, return the first non-outlier (lazily enough, no checks done to ensure
     * the singletonness!)
     */
    public ICluster<T> getSingletonNonOutlierCluster() {
        for (ICluster<T> c: mClusters) {
            if (!c.isOutlier()) {
                return c;
            }
        }
        throw new RuntimeException("WhatWhatWhat?!?!?!?");
    }
    
    public static <T> AbstractCostFunctionImpl<T> getCostFunctionImpl(
                Function<Collection<T>, Double> pBaseFunction,
                CostFunction pCompositeFunction) {
        if (pCompositeFunction == CostFunction.SUM) {
            return new CostFunctionImpl_SUM(pBaseFunction);
        }
        else if (pCompositeFunction == CostFunction.WEIGHTED_SUM) { 
            return new CostFunctionImpl_WEIGHTED_SUM(pBaseFunction);            
        }
        else if (pCompositeFunction == CostFunction.LOG_DIVIDED_WEIGHTED_SUM) { 
            return new CostFunctionImpl_LOG_DIVIDED_WEIGHTED_SUM(pBaseFunction);            
        }
        else {
            // TODO: add more
            throw new RuntimeException();
        }
    }
                           
    
    public static class CostFunctionImpl_SUM<T> extends AbstractCostFunctionImpl<T> {
                        
        public CostFunctionImpl_SUM(Function<Collection<T>, Double> f) {
            super(f);            
        }
        
        public String getName() {           
            return "SUM of: "+mSingleClusterFunction.getName();
        }
        
        public double compute(Clustering<T> pClustering, boolean pStoreExplanation) {
            
            if (pClustering == null) {
                throw new RuntimeException("WhatWhatWhat, a null clustering!");
            }
            
            double totalCost = 0;
            List<Double> terms = pStoreExplanation  
                                 ? new ArrayList()
                                 : null;
           
            
            // if we have an outlier cost, first cluster shall be considered
            // an outlier
            boolean isOutlierCluster = mOutlierCost != null;
            
            for (ICluster c: pClustering.getClusters()) {
                double cost = c.getCost();
                if (Double.isNaN(cost)) {
                    // need to compute
                    if (isOutlierCluster) {
                        cost = mOutlierCost;                        
                    }
                    else if (c.size() < 2) {
                        // interpret cluster with size 1 as outlier cluster
                        // for the purposes of cost evaluation (otherwise,
                        // we get the wildly exaggarated avg. goodness = 1)!
                        cost = mOutlierCost;
                    }
                    else {
                        // actually compute the cost
                        cost = mSingleClusterFunction.compute(c.members());
                    }
                    c.setCost(cost);
                }
                else {
                    // cost already cached
                    if (isOutlierCluster && cost != mOutlierCost.doubleValue()) {
                        throw new RuntimeException("Not specified what to do " +
                                                   " when the outlier cluster already has" +
                                                   " an assigned cost and it " +
                                                   " differs from mOutlierCost"); 
                    }
                }
                                
                totalCost+=cost;
                
                if (pStoreExplanation) {
                    terms.add(cost); 
                }
                

                // only the first cluster can be the (priviledged indeed) outlier cluster! 
                isOutlierCluster = false;
            }
            
            if (pStoreExplanation) {
                Converter<Double, String> formatter = 
                    new DoubleToStringConverter(3);
                String expl = 
                    SU.toString(terms, "+", formatter)+
                    "="+StringUtils.formatFloat(totalCost, 3);
                pClustering.setExplanation(expl);                
            }
            
            return totalCost;

        }
                
    }
    
    public static class CostFunctionImpl_WEIGHTED_SUM<T> extends AbstractCostFunctionImpl<T> {
                        
        public CostFunctionImpl_WEIGHTED_SUM(Function<Collection<T>, Double> f) {
            super(f);
        }
        
        public String getName() {           
            return "WEIGHTED_SUM of: "+mSingleClusterFunction.getName();
        }
        
        public double compute(Clustering<T> pClustering, boolean pStoreExplanation) {
            
            if (pClustering == null) {
                throw new RuntimeException("WhatWhatWhat, a null clustering!");
            }
            
            double totalCost = 0;
            List<Double> terms = pStoreExplanation  
                                 ? new ArrayList()
                                 : null;
                       
            int totalSize = 0;
            
            // if we have an outlier cost, first cluster shall be considered
            // an outlier
            boolean isOutlierCluster = mOutlierCost != null;
            
            for (ICluster c: pClustering.getClusters()) {
                double cost = c.getCost();
                if (Double.isNaN(cost)) {
                    // need to compute
                    if (isOutlierCluster) {
                        cost = mOutlierCost;                        
                    }
                    else if (c.size() < 2) {
                        // interpret cluster with size 1 as outlier cluster
                        // for the purposes of cost evaluation (otherwise,
                        // we get the wildly exaggarated avg. goodness = 1)!
                        cost = mOutlierCost;
                    }                    
                    else {
                        // actually compute the cost
                        cost = mSingleClusterFunction.compute(c.members());
                    }
                    c.setCost(cost);
                }
                else {
                    // cost already cached
                    if (isOutlierCluster && cost != mOutlierCost.doubleValue()) {
                        throw new RuntimeException("Not specified what to do " +
                                                   " when the outlier cluster already has" +
                                                   " an assigned cost and it " +
                                                   " differs from mOutlierCost"); 
                    }
                }                
                
                int cSize = c.size();
                double wCost = cSize*cost;  
                totalCost+=wCost;
                totalSize += cSize;
            
                if (pStoreExplanation) {
                    terms.add(wCost); 
                }
                                

                // only the first cluster can be the (priviledged indeed) outlier cluster! 
                isOutlierCluster = false;
            }
            
            totalCost = totalCost / totalSize;                       
            
            if (pStoreExplanation) {
                Converter<Double, String> formatter = 
                    new ConverterChain<Double, String>(
                            new FunctionWrapperConverter(new MultiplyFunction(1.d/totalSize)), 
                            new DoubleToStringConverter(3));
                    
                String expl = 
                    SU.toString(terms, "+", formatter)+
                    "="+StringUtils.formatFloat(totalCost, 3);
                pClustering.setExplanation(expl);                
            }
            
            return totalCost;

        }
                
    }    
    
    public int getNumNonOutliers() {
        return mTotalNumElements - getOutlierCluster().size();
    }
    
    /**
     * Check if all elements are in the outlier cluster.
     */
    public boolean onlyOutliers() {
        ICluster<T> firstCluster = mClusters.get(0); 
        if (!firstCluster.isOutlier()) {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }
        
        for (int i=1; i<mClusters.size(); i++) {
            if (mClusters.get(i).size() > 0) {
                return false;
            }
        }
        
        // all other clusters were empty
        
        // (a small assertion here:)
        if (firstCluster.size() != mTotalNumElements) {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }        
        
        return true;
        
    }
    
    public static class CostFunctionImpl_LOG_DIVIDED_WEIGHTED_SUM<T> extends AbstractCostFunctionImpl<T> {
        
        public static double LOG_OFFSET = Math.E -1 ; 
        
        public CostFunctionImpl_LOG_DIVIDED_WEIGHTED_SUM(Function<Collection<T>, Double> f) {
            super(f);
        }
        
        public String getName() {           
            return "LOG-DIVIDED WEIGHTED_SUM of: "+mSingleClusterFunction.getName();
        }
        
        public double compute(Clustering<T> pClustering, boolean pStoreExplanation) {
            
            if (pClustering == null) {
                throw new RuntimeException("WhatWhatWhat, a null clustering!");
            }
            
            double totalCost = 0;
            List<Double> terms = pStoreExplanation  
                                 ? new ArrayList()
                                 : null;
                       
            int totalSize = 0;
            
            // if we have an outlier cost, first cluster shall be considered
            // an outlier
            boolean isOutlierCluster = mOutlierCost != null;
            
            for (ICluster c: pClustering.getClusters()) {
                double cost = c.getCost();
                if (Double.isNaN(cost)) {
                    // need to compute
                    if (isOutlierCluster) {
                        cost = mOutlierCost;                        
                    }
                    else if (c.size() < 2) {
                        // interpret cluster with size 1 as outlier cluster
                        // for the purposes of cost evaluation (otherwise,
                        // we get the wildly exaggarated avg. goodness = 1)!
                        cost = mOutlierCost;
                    }
                    else {
                        // actually compute the cost
                        cost = mSingleClusterFunction.compute(c.members());
                    }
                    c.setCost(cost);
                }
                else {
                    // cost already cached
                    if (isOutlierCluster && cost != mOutlierCost.doubleValue()) {
                        throw new RuntimeException("Not specified what to do " +
                                                   " when the outlier cluster already has" +
                                                   " an assigned cost and it " +
                                                   " differs from mOutlierCost"); 
                    }
                }                
                
                int cSize = c.size();
                double wCost = 
                        isOutlierCluster
                        ? cSize*cost // do not reward outlier cluster for size
                        : cSize*cost / Math.log(cSize+LOG_OFFSET); // cluster of size 1 will be unchanged, rest will have their cost reduced logarithmically with increasing size 
                        
                totalCost+=wCost;
                totalSize += cSize;
            
                if (pStoreExplanation) {
                    terms.add(wCost); 
                }
                                

                // only the first cluster can be the (priviledged indeed) outlier cluster! 
                isOutlierCluster = false;
            }
            
            totalCost = totalCost / totalSize;                       
            
            if (pStoreExplanation) {
                Converter<Double, String> formatter = 
                    new ConverterChain<Double, String>(
                            new FunctionWrapperConverter(new MultiplyFunction(1.d/totalSize)), 
                            new DoubleToStringConverter(3));
                    
                String expl = 
                    SU.toString(terms, "+", formatter)+
                    "="+StringUtils.formatFloat(totalCost, 3);
                pClustering.setExplanation(expl);                
            }
                        
            return totalCost;

        }
                
    }    
    
    public static void main(String[] args) throws Exception {
//        testClusterings_fixed_sizes(args);
//         testClusterSizes(args);
        testClusterings(args);
    }
    
        
    @SuppressWarnings("unused")
    private static void testClusterings_fixed_sizes(String[] args) throws Exception {
        List<Integer> clusterSizes_int = ConversionUtils.stringListToIntegerList(Arrays.asList(args));
        List<ClusterSize> clusterSizes = new ArrayList(clusterSizes_int.size());
        for (int s: clusterSizes_int) {
            clusterSizes.add(new ClusterSize(s, false));
        }
        int numElements = (int)MathUtils.sum(clusterSizes_int);
        Range range = new Range(1, numElements+1);
        List<Integer> elements = range.asList();
        List<List<FCluster<Integer>>> clusterings = new ArrayList();
        formAllClusterings_fixed_cluster_sizes(clusterSizes, elements, clusterings);
        for (List<FCluster<Integer>> clustering: clusterings) {
            System.out.println(clustering);
        }
    }
    
    @SuppressWarnings("unused")
    private static void testClusterSizes(String[] args) throws Exception {
        int numObjects = Integer.parseInt(args[0]);
        Integer minNumClusters = 
            !(args[1].equals("-")) 
            ? Integer.parseInt(args[1])
            : null;
        
        Integer maxNumClusters = 
            !(args[2].equals("-")) 
            ? Integer.parseInt(args[2])
            : null;
        boolean haveOutlierCluster = Boolean.parseBoolean(args[3]);
        Logger.info("Forming all clustering sizes: "+numObjects+", "+maxNumClusters+","+minNumClusters+", "+haveOutlierCluster);
        List<List<ClusterSize>> result = formAllClusteringSizes(numObjects, 1, null, maxNumClusters, minNumClusters, haveOutlierCluster);
        
        for (List<ClusterSize> entry: result) {
            System.out.println(SU.toString(entry, ","));
        }    
    }               
        
    private static void testClusterings(String[] args) throws Exception {
        
        int numElements = Integer.parseInt(args[0]);        
        
        Integer minNumClusters = 
            !(args[1].equals("-")) 
            ? Integer.parseInt(args[1])
            : null;
        
        Integer maxNumClusters = 
            !(args[2].equals("-")) 
            ? Integer.parseInt(args[2])
            : null;
        
        boolean haveOutlierCluster = Boolean.parseBoolean(args[3]);
        Range range = new Range(1, numElements+1);
        List<Integer> elements = range.asList();
        
        List<List<FCluster<Integer>>> clusterings =
            formAllClusterings(elements, 
                               2,
                               null,
                               maxNumClusters,
                               minNumClusters,
                               haveOutlierCluster,
                               null);
        
        for (List<FCluster<Integer>> clustering: clusterings) {
            System.out.println(clustering);
        }                               
        
    }
    
    public enum CostFunction {
        SUM, // trad
        WEIGHTED_SUM, // trad and popular
        LOG_DIVIDED_WEIGHTED_SUM, // trad
        PAPER, // an outlier here...
        MEWC; // uuh, maximum edge-weighted clique
        
        
        public static List<String> names() {
            ArrayList result = new ArrayList();
            for (CostFunction f: values()) {
                result.add(f.name());
            }
            return result;
        }
    }
        
    public boolean equals(Object p) {
        Clustering<T> other = (Clustering<T>)p;
        return this.mClusters.equals(other.mClusters);      
    }
    
    public int hashCode() {
        return mClusters.hashCode();
    }
    
    public String toString() {
                               
        MultiMap<Object, T> tmp = new MultiMap();
        for (ICluster<T> c: mClusters) {
            if (c.isOutlier()) {
                tmp.putMultiple(c.getId() + " (outlier)", c.members());
            }
            else {
                tmp.putMultiple(c.getId(), c.members());
            }
        }        
        
        return StringUtils.multiMapToString(tmp, null, mElementFormatter);
    }
}
