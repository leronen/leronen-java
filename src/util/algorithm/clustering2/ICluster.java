package util.algorithm.clustering2;

import java.util.Collection;

public interface ICluster<T> {
    public Collection<T> members();
    public int size();
    
    /** Currently, always -log(cost) by convention */    
    public double getScore();
    
    /** NaN, if not set */
    public double getCost();        
    public void setCost(double val);
    
    public Integer getId();           
    public void setId(int pId);
    
    public boolean isOutlier();
    
    /** set oulier status of this cluster to true */
    public void setOutlier();
    
    /** May be unsupported by non-list based implementations! */
    public T get(int pInd);
    
    public void remove(T pElem);
    public void add(T pElem);            
}
