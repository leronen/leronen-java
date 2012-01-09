package util.collections;

import util.*;

import java.util.*;

/** utilities for handling weighted sets */
public final class WeightedSetUtils {
                                            
    /** Add all objects in pIter to pWeightedSet, with weight pWeight */                    
    public static <T> void addAll(WeightedSet<T> pWeightedSet, Iterator<T> pIter, double pWeight) {
        while(pIter.hasNext()) {
            pWeightedSet.add(pIter.next(), pWeight);    
        }        
    }
        
    /** remove all objects whose frequency is smaller than pFreqTresh */
    public static void removeNonFrequentItems(WeightedSet pWeightedSet, double pFreqTresh) {
        List nonFrequentObjects = getNonFrequentObjects(pWeightedSet, pFreqTresh);        
        pWeightedSet.removeAll(nonFrequentObjects);
    }
        
    /** get all objects whose frequency is (properly) smaller than pFreqTresh */
    public static List getNonFrequentObjects(WeightedSet pWeightedSet, double pFreqTresh){
        ArrayList nonFrequentObjects = new ArrayList();
        Iterator allObjects = pWeightedSet.iterator();
        while(allObjects.hasNext()) {
            Object obj = allObjects.next();
            double weight = pWeightedSet.getWeight(obj);
            if (weight < pFreqTresh) {
                nonFrequentObjects.add(obj);
            }                
        }
        return nonFrequentObjects;         
    }
    
    /** Normalize by dividing all weigths by pDenominator */
    public static <T> void normalize(WeightedSet<T> pSet, double pDenominator) {
        for (T o: pSet) {
            double w = pSet.getWeight(o) / pDenominator;
            pSet.set(o, w);
        }
    }
    
    /** get all objects whose frequency is greater than or equal to pFreqTresh */
    public static List getFrequentObjects(WeightedSet pWeightedSet, double pFreqTresh){
        ArrayList frequentObjects = new ArrayList();
        Iterator allObjects = pWeightedSet.iterator();
        while(allObjects.hasNext()) {
            Object obj = allObjects.next();
            double weight = pWeightedSet.getWeight(obj);
            if (weight >= pFreqTresh) {
                frequentObjects.add(obj);
            }                
        }
        return frequentObjects;         
    }
                 
                 
    /** 
     * Get a list of the objects in pSet.
     * If pLargestFirst, then largest objects come first in the list; else smallest come first.
     */
     /*
    public List makeSortedList(WeightedSet pSet, boolean pLargestFirst) {
         ArrayList result = new  ArrayList(pSet);
         if (pLargestFirst) {        
            Collections.sort(result, mReverseComparator);
        }
        else {
            Collections.sort(result, mStraightComparator);            
        }                 
        return result;         
    }
    */      

    public static List getBiggest(WeightedSet pSet, int pNumToGet, boolean pResortToNaturalOrder) {                 
        return CollectionUtils.extractFirst(pNumToGet, toSortedList(pSet, true, pResortToNaturalOrder));
    }                 
                 
                                                                    
    public static <T> List<T> toSortedList(WeightedSet<T> pWeightedSet, boolean pbiggestFirst, boolean pResortToNaturalOrder) {
        ArrayList<T> sortedList = new ArrayList(pWeightedSet);
        WeightComparator comparator = new WeightComparator(pWeightedSet, pbiggestFirst, pResortToNaturalOrder);
        Collections.sort(sortedList, comparator);
        return sortedList;
    }
    
    public static Object[] toSortedArray(WeightedSet pWeightedSet, boolean pbiggestFirst, boolean pResortToNaturalOrder) {
        Object[] sortedObjects = new ArrayList(pWeightedSet).toArray(new Object[pWeightedSet.size()]);
        WeightComparator comparator = new WeightComparator(pWeightedSet, pbiggestFirst, pResortToNaturalOrder);
        Arrays.sort(sortedObjects, comparator);
        return sortedObjects;
    }                                          
    
    /** Makes a comparator that uses the weight ordering inherent in pWeightedSet */
    public static Comparator getSmallestFirstWeightComparator(WeightedSet pWeightedSet, boolean pResortToNaturalOrder) {
        return new WeightComparator(pWeightedSet, false, pResortToNaturalOrder);
    }
    
    /** Makes a comparator that uses the weight ordering inherent in pWeightedSet */
    public static Comparator getBiggestFirstWeightComparator(WeightedSet pWeightedSet, boolean pResortToNaturalOrder) {
        return new WeightComparator(pWeightedSet, true, pResortToNaturalOrder);
    } 
                           
    /** 
     * Compares objects according to their weight in the multiset.
     * If the frequencies are equal, uses the natural ordering of the objects.
     * 
     * Note: this assumes the objects implement Comparable, so that ties are broken consistently!
     */
    private static class WeightComparator implements Comparator {
        
        private boolean mBiggestFirst;
        private WeightedSet mWeightedSet;
        private boolean mUseNaturalOrdering;
        
        private WeightComparator(WeightedSet pWeightedSet,
                                 boolean pBiggestFirst,
                                 boolean pUseNaturalOrdering) {
            mWeightedSet = pWeightedSet;                                        
            mBiggestFirst = pBiggestFirst;
            mUseNaturalOrdering = pUseNaturalOrdering;
        }
        
        public int compare(Object pObj1, Object pObj2) {
            double val = mWeightedSet.getWeight(pObj1)-mWeightedSet.getWeight(pObj2);                        
            if (val == 0) {
                if (mUseNaturalOrdering) {
                    // try to resort to natural ordering...                
                    if (pObj1 instanceof Comparable) {
                        // object implements Comparable, and thus has natural ordering
                        val = ((Comparable)pObj1).compareTo(pObj2);
                    }
                    else {
                        // last hope: resort to string representation based ordering
                        val= pObj1.toString().compareTo(pObj2.toString());                    
                    }
                }                                             
            }
            if (mBiggestFirst) {
                val = -val;    
            }            
            if (val < 0.d) {
                return -1;
            }
            else if (val > 0.d) {
                return 1;
            }
            else {
                return 0;
            }
            
        }                               
    }            

}
