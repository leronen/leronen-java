package util.algorithm.clustering2;

import util.collections.Function;

public abstract class ClusteringCostFunction <T> implements Function<Clustering<T>, Double> {
    
    /**
     * after calling this with a non-null val, the function should always consider
     * the cost of the outlier cluster to be a constant pVal. Recall that
     * if set, the first cluster is always considered to be the outlier cluster!
     */
    public abstract void setOutlierClusterCost(Double pVal);
                
    public abstract double compute(Clustering<T> pClustering, boolean pStoreExplanation);
             

}
