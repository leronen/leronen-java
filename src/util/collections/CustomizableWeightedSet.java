package util.collections;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import util.CollectionUtils;
import util.ConversionUtils;
import util.IOUtils;
import util.MathUtils;
import util.ObjectWeightPair;
import util.ObjectWithSettableWeight;
import util.StringUtils;
import util.comparator.NaturalOrderComparator;
import util.converter.Converter;
import util.converter.ObjectToStringConverter;
import util.converter.StringToObjectWeightPairConverter;
import util.dbg.Logger;
import util.factory.Factory;


/**
 * A hash table-based implementation of the WeightedSet interface.
 * Manages a multiset of objects. A multi set is like a set, excep
 * t for the fact that an object may appear multiple times in a multi set.
 *
 * The current implementation is based on a HashMap, so it implictly
 * uses hashCode and equals for managing objects.
 *
 *
 * Note that we define that the set cannot contain objects with weight 0; an attempt to add entries with weight 0
 * Leads just to discarding of the entry.
 *
 */
public class CustomizableWeightedSet<T> extends AbstractSet<T>
                             implements WeightedSet<T>, Serializable {

    /**
     * keys: the objects in the set.
     * values: the counts of the objects (Double instances)
     */
    private Map<T, Double> mCountByObject;

    private boolean mAllowZeros = false;

    private static Comparator sDefaultComparator = new NaturalOrderComparator();

    /** Needed for some obscure output purposes */
    private Comparator<T> mDefaultComparator = sDefaultComparator;

    // @todo: own serialization system
    static final long serialVersionUID = 871766029897990691L;

    private Factory<? extends Map> mMapFactory;
    private Factory<? extends Set> mSetFactory;

    protected CustomizableWeightedSet(Factory<? extends Map> pMapFactory,
    		                          Factory<? extends Set> pSetFactory) {
    	mMapFactory = pMapFactory;
    	mSetFactory = pSetFactory;
    	mCountByObject = mMapFactory.makeObject();
    }

    public void setDefaultComparator(Comparator<T> pComparator) {
        mDefaultComparator = pComparator;
    }

    protected CustomizableWeightedSet(Factory<? extends Map> pMapFactory,
									  Factory<? extends Set> pSetFactory,
									  Collection<T> pCol) {
    	this(pMapFactory, pSetFactory);
    	addAll(pCol);
    }

    @Override
    public void setAllowZeros(boolean p) {
    	mAllowZeros = p;
    }

	protected CustomizableWeightedSet(Factory<? extends Map> pMapFactory,
								      Factory<? extends Set> pSetFactory,
								      WeightedSet<T> pWeightedSet) {
		this(pMapFactory, pSetFactory);
		addAll(pWeightedSet);
	}


	/** Just the number of objects (ignoring counts!) */
    @Override
    public int size() {
        return mCountByObject.size();
    }

    @Override
    public double countTotalWeight() {
        return MathUtils.sum(ConversionUtils.DoubleCollectionTodoubleArray(mCountByObject.values()));
    }

    public double getMaxWeight() {
        return MathUtils.max(ConversionUtils.DoubleCollectionTodoubleArray(mCountByObject.values()));
    }

    public double getMinWeight() {
        return MathUtils.min(ConversionUtils.DoubleCollectionTodoubleArray(mCountByObject.values()));
    }

    @Override
    public boolean remove(Object pObj) {
        Object weight = mCountByObject.remove(pObj);
        // return true, if the object was present
        return weight!=null;
    }

    @Override
    public Iterator<T> iterator() {
        return mCountByObject.keySet().iterator();
    }

    @Override
    public boolean add(T pObj) {
        add(pObj, 1);
        return true;
    }

    /**
     * Note all other add methods use this method to add stuff! So overriding this (plus of course the remove method also!
     * is sufficient for Subclasses that want to control the adding of objects.
     **/
    @Override
    public void add(T pObj, double pCount) {
    	// Logger.info("add(T, "+pCount+")");
        Double oldCount = mCountByObject.get(pObj);
        Double newCount;
        if (oldCount==null) {
             newCount = new Double(pCount);
        }
        else {
            newCount = new Double(oldCount.doubleValue()+pCount);
        }

        internalPut(pObj, newCount);
    }

    @Override
    public void set(T pObj, double pCount) {
        internalPut(pObj, pCount);
    }


    /**
       This should be used for all putting!
     * TODO: customizable behauvior?
     */
    private void internalPut(T pObj, double pWeight) {
    	if (mAllowZeros == false && pWeight == 0.d ) {
    		// no zeros!
    		if (mCountByObject.containsKey(pObj)) {
    			mCountByObject.remove(pObj);
    		}
        }
        else {
        	mCountByObject.put(pObj, pWeight);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> pCol) {
    	// Logger.info("addAll(Collection)");
        for (T obj: pCol) {
        	add(obj);
        }
        return true;
    }

    @Override
    public void addAll(WeightedSet<? extends T> pWeightedSet) {
    	// Logger.info("addAll(WeightedSet)");
    	for (T elem: pWeightedSet) {
			double weight = pWeightedSet.getWeight(elem);
			add(elem, weight);
		}
    }

    @Override
    public void addAll(Collection<? extends T> pCol, double pCount) {
    	// Logger.info("addAll(Collection, "+pCount+")");
        if (pCount == 0) {
            // no action
            return;
        }
        for (T obj: pCol) {
            add(obj, pCount);
        }
    }

    /** Decreases the weight of pObj by 1 */
    @Override
    public void decrease(T pObj) {
    	add(pObj, -1.d);
    }

    /** Possibly removes the object from the collection! */
    public void decrease(T pObj, double pWeightDecrement) {
    	add(pObj, -pWeightDecrement);
    }

    /** Decreases the weight of all objects in pSet by 1 */
    @Override
    public void decreaseAll(Collection<T> pCol) {
    	// Logger.info("DecreaseAll(Collection)");
    	addAll(pCol, -1);
    }

    /** Override in subclassses, please */
    @Override
    public WeightedSet<T> makeCompatibleWeightedSet() {
    	return new CustomizableWeightedSet(mMapFactory, mSetFactory);
    }

    @Override
    public T extractSingletonObject() {
    	if (size() != 1) {
    		throw new RuntimeException();
    	}
    	T obj = iterator().next();
    	double w = getWeight(obj);
    	if (w != 1.d) {
    		throw new RuntimeException();
    	}
    	return obj;
    }

    /** Note that multiple objects may share the same min weight */
    @Override
    public Set<T> findMin() {
    	if (size() == 0) {
    		Logger.warning("Strange: trying to find min from a set of size 0!");
    	}
    	HashSet<T> result = new HashSet<T>();
    	double minW = Double.MAX_VALUE;
    	for (T o: this) {
    		double w = getWeight(o);
    		if (w<minW) {
    			result.clear();
    			result.add(o);
    			minW = w;
    		}
    		else if (w == minW) {
    			result.add(o);
    		}
    		else {
    			// no action
    		}
    	}
    	if (result.size() == 0) {
    		throw new RuntimeException("WhatWhatWhat!!!");
    	}
    	return result;
    }



    /** Convenience wrapper for #decrease(Object) */
    @Override
    public WeightedSet<T> minus(T pObj) {
    	// Logger.info("minus(T)");
    	WeightedSet<T> result = makeCompatibleWeightedSet();
    	result.addAll(this);
    	result.decrease(pObj);
    	return result;
    }

    @Override
    public WeightedSet<T> minus(Collection<T> pOther) {
    	// Logger.info("minus(Collection)");
    	WeightedSet<T> result = makeCompatibleWeightedSet();
    	// Logger.info("calling addAll(this)");
    	result.addAll(this);
    	result.decreaseAll(pOther);
    	return result;
    }

    @Override
    public WeightedSet<T> minus(WeightedSet<T> pOther) {
    	// Logger.info("minus(WeightedSet)");
    	WeightedSet<T> result = makeCompatibleWeightedSet();
    	// Logger.info("calling addAll(this)");
    	result.addAll(this);
    	result.decreaseAll(pOther);
    	return result;
    }

    @Override
    public void decreaseAll(WeightedSet<T> pOther) {
    	// Logger.info("decreaseAll(WeightedSet)");
    	for (T o: pOther) {
    		double w = pOther.getWeight(o);
    		decrease(o, w);
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

    /** List the weights corrosponding to the objects, in the same order of iteration */
    @Override
    public List<Double> weights() {
        List<Double> result = new ArrayList(size());
        for (T obj: this) {
            result.add(getWeight(obj));
        }
        return result;
    }

    public void assignWeightsToObjects() {
        Iterator<T> objs = iterator();
        while(objs.hasNext()) {
            ObjectWithSettableWeight obj = (ObjectWithSettableWeight)objs.next();
            obj.setWeight(getWeight(obj));
        }
    }

    @Override
    public double getWeight(Object pObj) {
        Double count = mCountByObject.get(pObj);
        if (count==null) {
            return 0;
        }
        else {
            return count.doubleValue();
        }
    }

    @Override
    public Object[] getSortedObjects(boolean pMostFrequentFirst, boolean pResortToNaturalOrder) {
        return WeightedSetUtils.toSortedArray(this, pMostFrequentFirst, pResortToNaturalOrder);
    }

    @Override
    public List<T> asSortedList(boolean pbiggestFirst, boolean pResortToNaturalOrder) {
        return WeightedSetUtils.toSortedList(this, pbiggestFirst, pResortToNaturalOrder);
    }


    @Override
    public Map<T, Double> asObjToWeightMap() {
        return Collections.unmodifiableMap(mCountByObject);
    }

    public Comparator getSmallestFirstWeightComparator(boolean pResortToNaturalOrder) {
        return WeightedSetUtils.getSmallestFirstWeightComparator(this, pResortToNaturalOrder);
    }

    public Comparator getBiggestFirstWeightComparator(boolean pResortToNaturalOrder) {
        return WeightedSetUtils.getBiggestFirstWeightComparator(this, pResortToNaturalOrder);
    }

    /** Try to output the objects in some sensible order */
    @Override
    public String toString() {
        if (size() == 0) {
            return "";
        }

        Map mapToPrint;

        if (iterator().next() instanceof Comparable) {
            mapToPrint = CollectionUtils.asKeySortedMap(asObjToWeightMap(), mDefaultComparator);
        }
        else {
            // values not comparable, have to convert to strings before print...
            // (assume string rep is unique...)
            TreeMap<String, Double> tmp = new TreeMap();
            ConversionUtils.convertKeys(asObjToWeightMap(),
                                        new ObjectToStringConverter(),
                                        tmp);
            if (tmp.size() == size()) {
                // OK, seems the string rep is unique...
                mapToPrint = tmp;
            }
            else {
                // string rep is not unique, cannot come up with ordering
                // easily, let's forget about the ordering...
                mapToPrint = asObjToWeightMap();
            }

        }

//        Map asKeySortedMap = CollectionUtils.asKeySortedMap(asObjToWeightMap(), mDefaultComparator);

        return StringUtils.mapToString(mapToPrint);

    }

    @Override
    public String toString(String pCountValSeparator) {
        Map asKeySortedMap = CollectionUtils.asKeySortedMap(asObjToWeightMap(), mDefaultComparator);
        return StringUtils.format(asKeySortedMap, " ", "\n");
    }

    public void readFromStream(InputStream pStream, StringToObjectWeightPairConverter pLineParser) throws IOException {
        String[] lines = IOUtils.readLineArray(pStream);
        for (int i=0; i<lines.length; i++) {
            ObjectWeightPair<T> objWeightPair = pLineParser.convertStringToObjectWeightPair(lines[i]);
            add(objWeightPair.getObject(), objWeightPair.getWeight());
        }
    }


    @Override
    public <T2> WeightedSet<T2> convert(Converter<T, T2> pConverter) {
        CustomizableWeightedSet<T2> result = new CustomizableWeightedSet(mMapFactory, mSetFactory);
        internalConvert(result, pConverter);
        return result;
    }

    public <T2> void internalConvert(WeightedSet<T2> pResultSet, Converter<T, T2> pConverter) {
        for (T obj:mCountByObject.keySet()) {
        	T2 converted = pConverter.convert(obj);
        	double weight = getWeight(obj);
            pResultSet.add(converted, weight);
        }
    }

    @Override
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
        HashSet allObjs = new HashSet();
        allObjs.addAll(this);
        allObjs.addAll(otherSet);
        for (Object obj: allObjs) {
            if (getWeight(obj) != otherSet.getWeight(obj)) {
                return false;
            }
        }
        // passed all tests: we, with our godly authority, deem the sets as equal!
        return true;

    }

    @Override
    public int hashCode() {
        return mCountByObject.hashCode();
//        throw new UnsupportedOperationException("Hashcode not currently implemented!");
    }

    @Override
    public Converter asValToWeightConverter() {
        return new ValToWeightConverter();
    }

    /** Uhh... */
    @Override
    public void canonizeWeights(Map<Double,Double> pCanonicalWeightByOriginalWeight) {
        for (T obj: mCountByObject.keySet()) {
            Double weight = mCountByObject.get(obj);
            if (pCanonicalWeightByOriginalWeight.containsKey(weight)) {
                Double canWeight = pCanonicalWeightByOriginalWeight.get(weight);
                mCountByObject.put(obj, canWeight);
            }
        }
    }

    public class ValToWeightConverter implements Converter {

        @Override
        public Object convert(Object pObj) {
            return mCountByObject.get(pObj);
        }
    }



}
