package util.algorithm.clustering2;

/**
 * Basically just int, but we need to also carry along the information
 * about the "outlier status" of each cluster.  
 */
public class ClusterSize {
    int size;
    boolean isOutlier;

    public ClusterSize(int size, boolean isOutlier) {
        super();
        this.size = size;
        this.isOutlier = isOutlier;
    }
    
    public String toString() {
        return ""+size; // +","+new Boolean(isOutlier).toString().charAt(0);
    }
    
}
