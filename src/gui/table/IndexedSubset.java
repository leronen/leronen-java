package gui.table;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import util.CollectionUtils;
import util.StringUtils;
import util.collections.OneToOneBidirectionalMap;
import util.comparator.ByFieldComparator;
import util.converter.Converter;

public class IndexedSubset<T> {
    
    private Double mWeight;
    private OneToOneBidirectionalMap<Integer, T> mSubSetMap; 
    private OneToOneBidirectionalMap<Integer, T> mSuperSetMap;
    
    /** 
     * For constructing the super set map. This should be done once, and
     * be shared for all subsets. 
     */
    public static <T> OneToOneBidirectionalMap<Integer, T> 
              makeSuperSetMap(Set<T> pSuperSet) {
        Map<T, Integer> numbering = CollectionUtils.numberElements(pSuperSet, 0);
        OneToOneBidirectionalMap<Integer, T> result = new OneToOneBidirectionalMap<Integer, T>();
        for (T o: numbering.keySet()) {
            int i = numbering.get(o);
            result.put(i, o);
        }
        return result;
            
                
        
    }

    public Double getWeight() {
        return mWeight;
    }
    
    /** Construct with no weight */                
    public IndexedSubset(Collection<T> pSubSet, 
                         OneToOneBidirectionalMap<Integer, T> pSuperSetMap) {
        this(pSubSet, pSuperSetMap, null);
    }
    
    /** 
     * Elements of pSubset need to be unique.
     * 
     * @param pSuperSetMap indexing for elements of super set. Construct this
     * (before creating any subsets) by calling {@link #makeSuperSetMap(Set)},
     * and use the same super set map for all subsets.
     */
    public IndexedSubset(Collection<T> pSubSet, 
                         OneToOneBidirectionalMap<Integer, T> pSuperSetMap,
                         Double pWeight) {
        mSuperSetMap = pSuperSetMap;
        mSubSetMap = new OneToOneBidirectionalMap<Integer, T>(pSubSet.size());
        mWeight = pWeight;
        
        for (T o: pSubSet) {
//            Logger.info("Element of subset:<" +o+"> (class: "+o.getClass()+")");
//            Logger.info("Contained in super set: "+pSuperSetMap.containsDstKey(o));
            if (!(pSuperSetMap.containsTgtKey(o))) {
//                Logger.info("Super set map:\n<"+pSuperSetMap);
            }
            int ind = pSuperSetMap.getInverse(o);
            mSubSetMap.put(ind, o);            
        }
    }
    
    public Set<T> getObjects() {
        return mSubSetMap.getTgtValues();
    }
    
    public int getSuperSetSize() {
        return mSuperSetMap.size();
    }
    
    public int getSubSetSize() {
        return mSubSetMap.size();
    }
    
    public int getIndex(T pObj) {
        return mSuperSetMap.getInverse(pObj);
    }
    
    /** Return null, if no such object at subset */
    public T objectAt(int pIndex) {
        return mSubSetMap.get(pIndex);
    }
    
    /** Return null, if no such object at subset */
    public T supersetObjectAt(int pIndex) {
        return mSuperSetMap.get(pIndex);
    }
    
    public String toString() {
        return StringUtils.mapToString(mSubSetMap.getDirectMap(), ": "," ");
    }
    
    public static Comparator<IndexedSubset> makeComparator() {
        return new ByFieldComparator(new WeightExtractor()); 
    }
    
    private static class WeightExtractor implements Converter<IndexedSubset, Double> { 
        
        public Double convert(IndexedSubset p) {
            return p.mWeight;
        }
    }
}
