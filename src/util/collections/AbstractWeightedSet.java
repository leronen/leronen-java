package util.collections;

import util.*;
import util.factory.*;
import util.converter.*;


import java.util.*;
import java.io.*;

/** 
 * A hash table-based implementation of the WeightedSet interface.
 * Manages a multiset of objects. A multi set is like a set, excep
 * t for the fact that  * an object may appear multiple times in a multi set.
 *
 * The current implementation is based on a HashMap, so it implictly 
 * uses hashCode and equals for managing objects.
 *   
 *
 * Note that we define that the set cannot contain objects with weight 0; an attempt to add entries with weight 0
 * Leads just to discarding of the entry.
 *
 * TODO: what is the relationship of this to the class CustomizableWeightedSet? 
 */ 
public abstract class AbstractWeightedSet<T> extends AbstractSet<T> 
                             implements WeightedSet<T>, Serializable {                    
    
    /** 
     * keys: the objects in the set.
     * values: the counts of the objects (Double instances) 
     */ 
    protected Map<T, Double> mCountByObject;
    
    // @todo: own serialization system    
    static final long serialVersionUID = 871766029897990691L;        
        
    private Factory<Map> mMapFactory;
    
    protected AbstractWeightedSet(Factory<Map> pMapFactory) {
    	mMapFactory = pMapFactory;
    	mCountByObject = mMapFactory.makeObject();
    }
    
    protected AbstractWeightedSet(Factory<Map> pMapFactory, 
                                  Collection<T> pCol) {
    	this(pMapFactory);
    	addAll(pCol);
    }    

	protected AbstractWeightedSet(Factory<Map> pMapFactory,
								  WeightedSet<T> pWeightedSet) {
		this(pMapFactory);
		for (T elem: pWeightedSet) {                    
			double weight = pWeightedSet.getWeight(elem);
			add(elem, weight);
		}                
	}    
    
    public int size() {
        return mCountByObject.size();    
    }
    
    public double getMaxWeight() {
        return MathUtils.max(ConversionUtils.DoubleCollectionTodoubleArray(mCountByObject.values()));    
    }
            
                    
    
    public boolean remove(Object pObj) {
        Object weight = mCountByObject.remove(pObj);
        // return true, if the object was present
        return weight!=null;
        
    }
                    
    public Iterator<T> iterator() {
        return mCountByObject.keySet().iterator();
    }
            
    public boolean add(T pObj) {
        add(pObj, 1);
        return true;         
    }
    
    /** 
     * Note all other add methods use this method to add stuff! So overriding this (plus of course the remove method also!
     * is sufficient for Subclasses that want to control the adding of objects. 
     **/
    public void add(T pObj, double pCount) {
        if (pCount == 0) {
            // no action
            return;
        }
        Double oldCount = mCountByObject.get(pObj);
        Double newCount;
        if (oldCount==null) {
             newCount = new Double(pCount);                                            
        }
        else {
            newCount = new Double(oldCount.doubleValue()+pCount);                          
        }   
        // replace oldCount by newCount
        mCountByObject.put(pObj, newCount);                 
    }
    
    
    
    public boolean addAll(Collection<? extends T> pCol) {
        addAll(pCol.iterator());
        return true;        
    }        
                   
    
    public boolean addAll(Iterator<? extends T> pIter) {
        while(pIter.hasNext()) {
            add(pIter.next());    
        }
        return true;        
    }
    
    
    public void addAll(Collection<? extends T> pCol, double pCount) {
        if (pCount == 0) {
            // no action
            return;
        }
        addAll(pCol.iterator(), pCount);                
    }        
    
    
    public void addAll(Iterator<? extends T> pIter, double pCount) {
        if (pCount == 0) {
            // no action
            return;
        }
        while(pIter.hasNext()) {
            add(pIter.next(), pCount);    
        }                
    }        
    
    /** remove all objects whose frequency is smaller than pFreqTresh */
    public void removeNonFrequentItems(double pFreqTresh) {
        WeightedSetUtils.removeNonFrequentItems(this, pFreqTresh);                                 
    }
        
    /** get all objects whose frequency is smaller than pFreqTresh */
    public List getNonFrequentObjects(double pFreqTresh) {
        return WeightedSetUtils.getNonFrequentObjects(this, pFreqTresh);           
    }
    
    public void assignWeightsToObjects() {
        Iterator<T> objs = iterator();
        while(objs.hasNext()) {
            ObjectWithSettableWeight obj = (ObjectWithSettableWeight)objs.next();
            obj.setWeight(getWeight(obj));
        }
    }
        
    public double getWeight(Object pObj) {
        Double count = mCountByObject.get(pObj);
        if (count==null) {
            return 0;
        }
        else {
            return count.doubleValue();            
        }        
    }              
    
    /** Uhh... */
    public void canonizeWeights(Map<Double,Double> pCanonicalWeightByOriginalWeight) {
        for (T obj: mCountByObject.keySet()) {
            Double weight = mCountByObject.get(obj);
            if (pCanonicalWeightByOriginalWeight.containsKey(weight)) {
                Double canWeight = pCanonicalWeightByOriginalWeight.get(weight);
                mCountByObject.put(obj, canWeight);
            }
        }
    }
    
    public Object[] getSortedObjects(boolean pMostFrequentFirst, boolean pResortToNaturalOrder) {
        return WeightedSetUtils.toSortedArray(this, pMostFrequentFirst, pResortToNaturalOrder);        
    }                        
    
    public Map asObjToWeightMap() {
        return Collections.unmodifiableMap(mCountByObject);
    }        
    
    public Comparator getSmallestFirstWeightComparator(boolean pResortToNaturalOrder) {
        return WeightedSetUtils.getSmallestFirstWeightComparator(this, pResortToNaturalOrder);        
    }
    
    public Comparator getBiggestFirstWeightComparator(boolean pResortToNaturalOrder) {
        return WeightedSetUtils.getBiggestFirstWeightComparator(this, pResortToNaturalOrder);        
    }
        
    public String toString() {
        IPair[] objectCountPairs = CollectionUtils.mapToKeyValuePairs_keysorted(asObjToWeightMap());
        Matrix asMatrix = new Matrix(objectCountPairs);
        return asMatrix.toString();       
        // return StringUtils.arrayToString(objectCountPairs, "\n");                 
    }
    
    public void readFromStream(InputStream pStream, StringToObjectWeightPairConverter pLineParser) throws IOException {
        String[] lines = IOUtils.readLineArray(pStream);        
        for (int i=0; i<lines.length; i++) {
            ObjectWeightPair objWeightPair = pLineParser.convertStringToObjectWeightPair(lines[i]);
            add((T)objWeightPair.getObject(), objWeightPair.getWeight());
        }                 
    }
        
    /*
    public <T2> WeightedSet<T2> convert(Converter<T, T2> pConverter, ) {        
        HashWeightedSet<T2> result = new HashWeightedSet<T2>();        
        internalConvert(result, pConverter);
        return result;
    } 
    */      
               
    public <T2> void convert(WeightedSet<T2> pResultSet, Converter<T, T2> pConverter) {                                        
        for (T obj:mCountByObject.keySet()) {            
        	T2 converted = pConverter.convert(obj);
        	double weight = getWeight(obj);            
            pResultSet.add(converted, weight);                                    
        }                                
    }    
            
    public boolean equals(Object pObj) {
        if (!(pObj instanceof WeightedSet)) {
            // the other object is not a weighted set
            return false;    
        }
        WeightedSet otherSet = (WeightedSet)pObj;
        
        if (size() != otherSet.size()) {
            // the sets have different number of objects
            return false;    
        }
            
        if (isEmpty() && otherSet.isEmpty()) {
            // both are empty
            return true;
        }            
        
        // OK,  both have the same number of objects and are non-empty
        // check whether the other set has the same objects with same weights
        HashSet allObjs = new HashSet(mCountByObject.keySet());
        allObjs.addAll(otherSet);                
        Iterator<T> i = allObjs.iterator();        
        while(i.hasNext()) {
            T obj = i.next();
            if (getWeight(obj) != otherSet.getWeight(obj)) {
                return false;
            }            
        }
        // passed all tests: we, with our godly authority, deem the sets as equal!        
        return true;
        
    }
    
    public int hashCode() {
        throw new UnsupportedOperationException("Hashcode not currently implemented!");
    }
    
    public Converter asValToWeightConverter() {
        return new ValToWeightConverter();
    }

    public class ValToWeightConverter implements Converter {
                        
        public Object convert(Object pObj) {
            return mCountByObject.get(pObj);                
        }
    }
    
    
    
}
