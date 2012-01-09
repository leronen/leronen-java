package util.algorithm.clustering;

import java.util.*;

import util.CollectionUtils;
import util.StringUtils;
import util.collections.MultiMap;
import util.converter.Converter;

public class Clustering_old<T> {
    
    private MultiMap<Integer, T> mElementsByClusterNum;    
    private IDistanceFunction mDistanceFunc;
    private int mMaxNum;
    
    private Converter<T, String> mElementFormatter;
    
    public Clustering_old(MultiMap<Integer, T> pElementsByClusterNum,
                      IDistanceFunction pDistanceFunc) {
        mElementsByClusterNum = pElementsByClusterNum;
        mDistanceFunc = pDistanceFunc;
        mMaxNum = CollectionUtils.findMax(pElementsByClusterNum.keySet()).intValue();
    }
            
    public Clustering_old(IDistanceFunction pDistanceFunc) {
        mElementsByClusterNum = new MultiMap<Integer, T>();
        mDistanceFunc = pDistanceFunc;
        mMaxNum = 0;
    }
    
    public void setFormatter(Converter<T,String> p) {
        mElementFormatter = p;
    }
    
    
    /** @return the id of the new cluster */
    public int addCluster(Collection<T> pElements) {
        mMaxNum++;
        mElementsByClusterNum.putMultiple(mMaxNum, pElements);
        return mMaxNum;
    }    
    
    public void addToCluster(int clusterId, T pObj) {
        mElementsByClusterNum.put(clusterId, pObj);
    }
    
    public Set<Integer> getClusterIds() {
        return mElementsByClusterNum.keySet();
    }
    
    public Set<T> getMembers(int pClusterId) {
        return mElementsByClusterNum.get(pClusterId);
    }
    
    public List<T> centroids() {
        
        ArrayList<T> result = new ArrayList();
        
        for (Set<T> clusterMembers: mElementsByClusterNum.getValuesAsCollectionOfSets()) {
            T centroid = (T)ClusteringUtils.centroid(clusterMembers, mDistanceFunc);
            result.add(centroid);
        }
        
        return result;
    }
    
    public String toString() {
        return StringUtils.multiMapToString(mElementsByClusterNum, null, mElementFormatter);
    }
}
