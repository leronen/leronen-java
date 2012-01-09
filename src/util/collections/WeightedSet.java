package util.collections;

import util.converter.*;

import java.util.*;


/**
 * 
 * A multiset of objects. A multi set is like a set, except
 * for the fact that an object may appear multiple times in a
 * multi set.
 * 
 * Note that we define that the set cannot contain objects 
 * with weight 0; an attempt to add entries with weight 0
 * Leads just to discarding of the entry.
 * 
 * We shall probably allow objects with negative weights?
 * 
 * Should it be made customizable, what happens when weight
 * becomes 0? Currently, the object just disappears!
 * 
 * Todo: specify semantics for the case that ObjectWithWeight 
 *       instances are added to the WeightedSet with operation add 
 *       (is this done? Anyway, ObjectWithWeight seems like a 
 *       bad concept, in retrospective view...)
 * 
 */
public interface WeightedSet<T> extends Set<T> {
    
    /**
     * Add object with weight one (actually this is specified by Set, but is restated here for clarity, because of 
     * the additional semantics.
     */                       
    public boolean add(T pObj);
               
    /**
     * Add objects with weight one for each (actually this is specified by Set, 
     * but is restated here for clarity, because of the additional semantics.
     */
    public boolean addAll(Collection<? extends T> pCol);
                
    /** add object with given weight, adding weight to existing weight if 
     * already there. */
    public void add(T pObj, double pWeight);
    
    /** Set weight, possibly overriding old weight */
    public void set(T pObj, double pCount);
            
    /**
     * add objects; all objects will have the specified weight, possibly 
     * added to any existing weights. 
     */
    public void addAll(Collection<? extends T> pCol, double pWeight);
    
    public void addAll(WeightedSet<? extends T> pWeightedSet);
                                        
    /** Return the weight of an object in the weighted set. If the object is not in the set, return 0 */
    public double getWeight(Object pObj);
    
    /** Decreases the weight of pObj by 1 */ 
    public void decrease(T pObj);
    
    /** Decreases the weight of all objects in pSet by 1 */ 
    public void decreaseAll(Collection<T> pCol);
    
    public void decreaseAll(WeightedSet<T> pOther);       

    /** Convenience wrapper for #decrease(Collection) */
    public WeightedSet<T> minus(Collection<T> pCol);
    
    /** Convenience wrapper for #decrease(T) */
    public WeightedSet<T> minus(T pObj);
    
    /** Convenience wrapper for #decrease(WeightedSet) */
    public WeightedSet<T> minus(WeightedSet<T> pOther);
    
    public WeightedSet<T> makeCompatibleWeightedSet();
    
    /** Note that multiple objects may share the same min weight */
    public Set<T> findMin();    	   
    
    /**
     * List the weights corresponding to the objects, in the same 
     * order of iteration */
    public List<Double> weights();
    
    /** 
     * OKOK, this is in the super interface Set also;
     * but we want to emphasize here that this removes
     * the entry, instead of e.g. just decreasing the 
     * weight by one!
     * 
     * Hmm, why was this commented out (at some distant past already)?!?!
     */
    // public boolean remove(Object pObj);        
    
    /**     
     * Returns an Object->Number map containing the object-weight-mappings of the weighed set
     * The returned map is assumed immutable, so attempts to modify it should throw an UnsupportedOperationException. 
      */
    public Map<T, ? extends Number> asObjToWeightMap();
    
    public <T2> WeightedSet<T2> convert(Converter<T, T2> pConverter);

    public T extractSingletonObject();
    
    public void setAllowZeros(boolean p);
    
    public Converter asValToWeightConverter();

    public Object[] getSortedObjects(boolean pMostFrequentFirst, boolean pResortToNaturalOrder);                                                                     

    public List<T> asSortedList(boolean pbiggestFirst, boolean pResortToNaturalOrder);
    
    public String toString(String pCountValSeparator);
    
    /** Uhhh... */
    public void canonizeWeights(Map<Double,Double> pCanonicalWeightByOriginalWeight);
    
    public double countTotalWeight();
            
}
