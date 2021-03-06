package util.algorithm.clustering;

import java.util.*;


public interface IDataManagingDistanceFunction<T> extends IDistanceFunction<T> {
    
    /**
     * Note that it might be that the distance between 2 objects is not known,
     * in which case it should be assumed to be "infinite"
     */
    public Double dist(T p1, T p2);
    public Set<T> getDataPoints();
}
 