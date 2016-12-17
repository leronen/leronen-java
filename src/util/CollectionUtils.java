package util;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;

import util.collections.IPair;
import util.collections.MapEntryOperation;
import util.collections.MultiMap;
import util.collections.Operation;
import util.collections.Pair;
import util.collections.SymmetricPair;
import util.collections.SymmetricTriple;
import util.collections.UnorderedPair;
import util.comparator.NaturalOrderComparator;
import util.condition.Condition;
import util.condition.EqualsCondition;
import util.condition.IsNullCondition;
import util.converter.ArrayToListConverter;
import util.converter.CollectionToHashSetConverter;
import util.converter.CollectionToSizeConverter;
import util.converter.Converter;
import util.converter.ConverterChain;
import util.converter.ListToPairConverter;
import util.converter.MapConverter;
import util.converter.ObjectToClassConverter;
import util.dbg.Logger;



/** Todo: rename to ColUtils */
public final class CollectionUtils {

    private static Iterator EMPTY_ITERATOR;


    /** A work-around for some type checking nuisances with empty sets */
    @SuppressWarnings("unchecked")
    public static final Set<String> EMPTY_STRING_SET = Collections.EMPTY_SET;

    private static class EmptyIterator implements Iterator {
        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public void remove() {
            throw new RuntimeException("Boo");
        }
    }

    public static Iterator emptyIterator() {
        if (EMPTY_ITERATOR == null) {
            EMPTY_ITERATOR = new EmptyIterator();
        }
        return EMPTY_ITERATOR;
    }

    public static String reasonForInequality(Set pSet1, Set pSet2) {
        StringBuffer result = new StringBuffer();
        Set uniqueInSet1 = minus(pSet1, pSet2);
        Set uniqueInSet2 = minus(pSet2, pSet1);
        if (uniqueInSet1.size() > 0) {
            result.append("Unique in set1: "+SU.toString(uniqueInSet1, ", "));
        }
        if (uniqueInSet2.size() > 0) {
            if (result.length()>0) {
                result.append("; ");
            }
            result.append("Unique in set2: "+SU.toString(uniqueInSet2, ", "));
        }
        if (result.length() > 0) {
            return result.toString();
        }
        else {
            return "Strangely enought, cannot find any difference!!";
        }
    }

    public static <T> double jaccardDistance(Set<T> set1, Set<T> set2) {
    	Set<T> union = union(set1, set2);
    	Set<T> intersection = intersection(set1, set2);
    	return ((double)intersection.size()) / union.size();
    }

    /** Make a list containing the elements iterated by pIter */
    public static <T> HashSet<T> makeHashSet(Iterator<T> pIter) {
        HashSet<T> set = new HashSet<T>();
        while(pIter.hasNext()) {
            set.add(pIter.next());
        }
        return set;
    }

    public static <T> Set<T> asSet(Collection<T> pCol) {
        if (pCol instanceof Set) {
            return (Set<T>)pCol;
        }
        else {
            return new HashSet(pCol);
        }
    }

    /** Make a list containing the elements iterated by pIter */
    public static <T> ArrayList<T> makeArrayList(Iterator<T> pIter) {
        ArrayList<T> list = new ArrayList<T>();
        while(pIter.hasNext()) {
            list.add(pIter.next());
        }
        return list;
    }


    public static boolean containsDuplicates(Collection pCol) {
        return pCol.size() != new HashSet(pCol).size();
    }

    /** Also extracts "triplicates" etc... */
    public static <T> Set<T> extractDuplicates(Collection<T> pCol) {
        Set<T> tmp = new HashSet();
        Set<T> result = new HashSet();
        for (T o: pCol) {
            if (tmp.contains(o)) {
                // already encountered
                if (!(result.contains(o))) {
                    result.add(o);
                }
            }
            else {
                // Seen now for the first time
                tmp.add(o);
            }
        }
        return result;
    }

    public static <T> LinkedHashSet<T> makeLinkedHashSet(T... pItems) {
        return new LinkedHashSet<T>(Arrays.asList(pItems));
    }

    public static <T> List<T> makeList(T... pItems) {
    	return Arrays.asList(pItems);
    }

    public static <T> List<T> makeArrayList(T... pItems) {
        return new ArrayList<T>(Arrays.asList(pItems));
    }

    /**
     * Extract the set of elements that are not ordered according to pComparator
     * The assumption is that most elements are indeed ordered, and by
     * removing the set of "unordered" ones, we obtain a ordered collection.
     */
    public static <T> List<T> extractUnorderedElements(Iterator<T> pIter,
                                                       Comparator<T> pComparator) {
        // the previous element of "the ordered list"
        T prev = null;
        List<T> result = new ArrayList();

        while (pIter.hasNext()) {
            T cur = pIter.next();

            if (prev != null && pComparator.compare(cur, prev) < 0) {
                result.add(cur);
            }
            else {
                prev = cur;
            }
        }

        return result;
    }

//    /**
//     * If no val (or val is null) for such key, do nothing.
//     * Recall that a map may contain mappings to null, so get(k) == null does
//     * not imply !containskey(k)
//     */
//    public static <K,V> void replaceKey(Map<K,V> pMap, K pOldKey, K pNewKey) {
//        V val = pMap.get(pOldKey);
//        if (val
//
//    }
  /**
   * Replace all values that equal pOldVal with pNewVal.
   */
    public static <K,V> void replaceVal(Map<K,V> pMap, V pOldVal, V pNewVal) {
        for (K key: new ArrayList<K>(pMap.keySet())) {
            V val = pMap.get(key);
            if (val.equals(pOldVal)) {
                pMap.put(key, pNewVal);
            }
        }

  }

    /** Keywords: numbering, makenumbering, generate numbering */
    public static <T> Map<T,Integer> numberElements(Set<T> pSet, int pStartInd) {
        int number = pStartInd;
        HashMap result = new LinkedHashMap();
        for (T o: pSet) {
            result.put(o, number++);
        }
        return result;
    }

    public static <T> Map<T,Integer> numberElements(T... pVals) {
        int number = 1;
        HashMap result = new LinkedHashMap();
        for (T o: pVals) {
            result.put(o, number++);
        }
        return result;
    }

    public static <T> HashSet<T> makeHashSet(T... pItems) {
        return new HashSet(Arrays.asList(pItems));
    }

    /** Add all objects in pIter to pCollection */
    public static void addAll(Collection pCollection, Iterator pIter) {
        while(pIter.hasNext()) {
            pCollection.add(pIter.next());
        }
    }



    /** Extract pMap:s values in the order specified by pKey:s iterator */
    public static <K,V> List<V> extractList(Map<K,V> pMap, Collection<K> pKeys) {
        List<V> result = new ArrayList();
        for (K key: pKeys) {
            result.add(pMap.get(key));
        }
        return result;
    }

    /** let's remove some mappings from a map. */
    public static void removeKeys(Map pMap, Collection pKeysToRemove) {
        Iterator keys = pKeysToRemove.iterator();
        while(keys.hasNext()) {
            pMap.remove(keys.next());
        }
    }

    /** let's remove some mappings from a map. */
    public static void removeValues(Map pMap, Set pValuesToRemove) {
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            Object val = pMap.get(key);
            if (pValuesToRemove.contains(val)) {
                keys.remove();
            }
        }
    }

    /** let's remove some mappings from a map. */
    public static void retainKeys(Map pMap, Collection pKeysToSelect) {
        Set keysToRemove = new LinkedHashSet(pMap.keySet());
        keysToRemove.removeAll(pKeysToSelect);
        removeKeys(pMap, keysToRemove);
    }

    public static List pickEvenIndices(List pList) {
        // round up, of course
        int resultSize = pList.size()/2;
        if (pList.size()%2 == 1) {
            resultSize++;
        }
        ArrayList result = new ArrayList(resultSize);

        for (int i = 0; i<resultSize; i++) {
            result.add(pList.get(i*2));
        }
        return result;
    }

    /**
     *  Checks, if pComtained contains items that are a sub-range of pContaining
     *
     *  If yes, returns the range. In not, returns null.
     *
     */
    public static Range getRangeOfContainedList(List pContained, List pContaining) {
        Object first = pContained.get(0);
        int indexOfFirst = pContaining.indexOf(first);
        if (indexOfFirst == -1) {
            // even first item not found
            Logger.info("even first item not found");
            return null;
        }
        if (indexOfFirst+pContained.size() > pContaining.size()) {
            // pContaining does not contain enough elements to hold pContained
            Logger.info("pContaining does not contain enough elements to hold pContained");
            return null;
        }
        for (int i=1; i<pContained.size(); i++) {
            // note that we already know that the first item matched
            if (!(pContained.get(i).equals(pContaining.get(i+indexOfFirst)))) {
                Logger.info("Mismatch at position: "+i);
                return null;
            }
        }
        // Ok, everything matched
        return new Range(indexOfFirst, indexOfFirst+pContained.size());
    }

    public static List pickOddIndices(List pList) {
        // round down, of course
        int resultSize = pList.size()/2;

        ArrayList result = new ArrayList(resultSize);

        for (int i = 0; i<resultSize; i++) {
            result.add(pList.get(i*2+1));
        }
        return result;
    }

    public static<T> T extractSingletonObject(Collection<T> pCol) {
        if (pCol.size() != 1) {
            throw new RuntimeException("Cannot extract singleton object from collection: "+pCol+
                                        "; collection does not have exactly 1 element!");
        }
        return pCol.iterator().next();
    }

    /** @return objects in pCollection, matching pCondition, putting results into a new ArrayList. */
    public static <T> ArrayList<T> extractMatchingObjects(Collection<T> pCollection, Condition<T> pCondition) {
        return (ArrayList)extractMatchingObjects(pCollection.iterator(), pCondition, new ArrayList());
    }


    /** @return objects in pIterator, matching pCondition, putting results into a new ArrayList. */
    public static <T> ArrayList<T> extractMatchingObjects(Iterator<T> pIterator, Condition<T> pCondition) {
        return (ArrayList)extractMatchingObjects(pIterator, pCondition, new ArrayList());
    }

    /** @return objects in pCollection, matching pCondition, putting results into pResult. */
    public static <T> Collection<T> extractMatchingObjects(Collection<T> pCollection, Condition<T> pCondition, Collection<T> pResult) {
        return extractMatchingObjects(pCollection.iterator(), pCondition, pResult);
    }

    /** @return objects in pIterator, matching pCondition, putting results into pResult. */
    public static <T> Collection<T> extractMatchingObjects(Iterator<T> pIterator, Condition<T> pCondition, Collection<T> pResult) {
        Collection<T> result = pResult != null ? pResult : new ArrayList<T>();
        while (pIterator.hasNext()) {
            T o = pIterator.next();
            if (pCondition.fulfills(o)) {
                result.add(o);
            }
        }
        return result;
    }

    // public static List extractMatchingObjects(Iterator pIterator, Condition pCondition) {
        // ArrayList result = new ArrayList();
        // while (pIterator.hasNext()) {
            // Object o = pIterator.next();
            // if (pCondition.fulfills(o)) {
                // result.add(o);
            // }
        // }
        // return result;
    // }


    public static Object findFirstMatchingObject(Iterator pIter, Condition pCondition) {
        while (pIter.hasNext()) {
            Object o = pIter.next();
            if (pCondition.fulfills(o)) {
                return o;
            }
        }
        // found no matching objects...
        return null;
    }

    /** @return index of first matching object, or -1 if not found */
    public static <T> int findFirstMatchingObject(List<T> pList, Condition<T> pCondition) {
        int i = 0;
        Iterator<T> iter = pList.iterator();
        while (iter.hasNext()) {
            T obj = iter.next();
            if (pCondition.fulfills(obj)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static boolean doAllObjectsMatch(Collection pCollection, Condition pCondition) {
        Iterator i = pCollection.iterator();
        while (i.hasNext()) {
            if (!(pCondition.fulfills(i.next()))) {
                return false;
            }
        }
        return true;
    }

    public static double getMinWeight(ObjectWithWeight[] pArr) {
        return getObjectWithMinWeight(pArr).getWeight();
    }

    /** Assumes that the objects implement ObjectWithWeight */
    public static double findSmallestWeight(Iterator pObjects) {
        double minWeight = Double.MAX_VALUE;

        while (pObjects.hasNext()) {
            ObjectWithWeight obj = (ObjectWithWeight)pObjects.next();
            double weight = obj.getWeight();
            if (weight < minWeight) {
                minWeight = weight;
            }
        }
        return minWeight;
    }

    /**
     * Finds the smallest element from the collection, according to the comparator.
     * If there are multiple smallest elements, return the first one encountered.
     */
    public static <T> T findSmallest(List<T> pObjects, Comparator<T> comparator) {
        T smallest = null;

        for (T o: pObjects) {
            if (smallest == null) {
                smallest = o;
            }
            else {
                if (comparator.compare(o, smallest) < 0) {
                    smallest = o;
                }
            }
        }
        return smallest;
    }

    /**
     * Finds the largest element from the collection, according to the comparator.
     * If there are multiple largest elements, return the first one encountered.
     */
    public static <T> T findLargest(List<T> pObjects, Comparator<T> comparator) {
        T largest = null;

        for (T o: pObjects) {
            if (largest == null) {
                largest = o;
            }
            else {
                if (comparator.compare(o, largest) > 0) {
                    largest = o;
                }
            }
        }
        return largest;
    }

    public static ObjectWithWeight getObjectWithMinWeight(ObjectWithWeight[] pArr) {
        double minWeight = Double.MAX_VALUE;
        ObjectWithWeight minWeightObject = null;

        for (int i=0; i<pArr.length; i++) {
            if (pArr[i].getWeight() < minWeight) {
                minWeightObject = pArr[i];
                minWeight = pArr[i].getWeight();
            }
        }
        return minWeightObject;
    }

    public static int countObjects(Iterator pIterator) {
        int count = 0;
        while (pIterator.hasNext()) {
            pIterator.next(); // hi and bye
            count++;
        }
        return count;
    }

    public static int countObjects(Iterator pIterator, int pLoggingInterval) {
        int count = 0;
        while (pIterator.hasNext()) {
            pIterator.next(); // hi and bye
            count++;
            if ((count-1) % pLoggingInterval == 0 ) {
                Logger.info("Object count: "+count);
            }
        }
        return count;
    }

    public static int countNulls(List pList) {
        return countMachingObjects(pList, new IsNullCondition());
    }

    /**
     * Add nulls to end of list until desired size has been achieved
     *
     * keywords: pad with nulls, padWithNulls.
     */
    public static void addNulls(ArrayList pList, int pDesiredSize) {
        while (pList.size() < pDesiredSize) {
            pList.add(null);
        }
    }

    public static int countNumberOfEqualsFields(Map pMap1, Map pMap2) {
        Set commonKeys = intersection(pMap1.keySet(), pMap2.keySet());
        Iterator commonKeysIter = commonKeys.iterator();
        int count = 0;
        while (commonKeysIter.hasNext()) {
            Object key = commonKeysIter.next();
            if (pMap1.get(key).equals(pMap2.get(key))) {
                count ++;
            }
        }
        return count;
    }

    /** Count number of objects of pCol contained in pSet */
    public static <T> int countContainedObjects(Set<T> pSet, Collection<T> pCol) {
        int count = 0;
        for (T o: pCol) {
            if (pSet.contains(o)) {
                count++;
            }
        }
        return count;
    }

    /** pObjs must contain objects implementing interface ObjectWithWeight */
    public static ObjectWithWeight getObjectWithMinWeight(Collection pObjs) {
        double minWeight = Double.MAX_VALUE;
        ObjectWithWeight minWeightObject = null;

        Iterator i = pObjs.iterator();
        while (i.hasNext()) {
            ObjectWithWeight obj = (ObjectWithWeight)i.next();
            if (obj.getWeight() < minWeight) {
                minWeightObject = obj;
                minWeight = obj.getWeight();
            }
        }
        return minWeightObject;
    }

    /** pObjs must contain objects implementing interface ObjectWithWeight */
    public static double getMinWeight(Collection pObjs) {
        double minWeight = Double.MAX_VALUE;

        Iterator i = pObjs.iterator();
        while (i.hasNext()) {
            ObjectWithWeight obj = (ObjectWithWeight)i.next();
            if (obj.getWeight() < minWeight) {
                minWeight = obj.getWeight();
            }
        }
        return minWeight;
    }

    /**
     * This should work in reasonable time, given that pSet supports fast
     * membership checking. If pSet is not a Set, a new HashSet is constructed
     * to enable the membership checking.
     */
    public static int[] getIndicesOfContainedObjects(List pList, Collection pSet) {

        Set set;
        if (pSet instanceof Set) {
            set = (Set)pSet;
        }
        else {
            set = new HashSet(pSet);
        }

        List containedIndicesList = new ArrayList();
        int numElems = pList.size();
        for (int i=0; i<numElems; i++) {
            Object val = pList.get(i);
            if (set.contains(val)) {
                containedIndicesList.add(new Integer(i));
            }
        }
        return ConversionUtils.integerCollectionToIntArray(containedIndicesList);
    }

    /** count objects matching given condition */
    public static int countMachingObjects(Collection pCollection, Condition pCondition) {
        Iterator i = pCollection.iterator();
        return countMachingObjects(i, pCondition);
    }

    public static int countOccurences(Iterable pIterable, Object pObj) {
    	int count = 0;
        for (Object o: pIterable) {
        	if (o.equals(pObj)){
        		count++;
        	}
        }
        return count;
    }



    /** count objects matching given condition */
    public static int countMachingObjects(Iterator pIterator, Condition pCondition) {
        int count = 0;
        while (pIterator.hasNext()) {
            if (pCondition.fulfills(pIterator.next())) {
                count++;
            }
        }
        return count;
    }



    /**
     * Väsää potenssijoukot.
     *
     * Osajoukot ovat samassa järjestyksessä kuin alkuperäisen joukon iteraattori ne palauttaa.
     */
    public static List[] makeAllCombinations(Set pSet) {
        return makeAllCombinations(new ArrayList(pSet));
    }

    /**
     * Väsää potenssijoukot.
     *
     * Osajoukot ovat samassa järjestyksessä kuin alkuperäisen listan alkiot.
     */
    public static List[] makeAllCombinations(List pList) {
        int numElements = pList.size();
        int numCombinations = (int)Math.pow(2, numElements);

        if (numElements > 20) {
            // let's honorably commit harakiri, as it would be vulgar to try a set as large as this
            throw new RuntimeException("Execution terminated, as the method is about to start making\n"+
                                       numElements+"^2 = "+numCombinations+", which is\n"+
                                       "> 20^2 > 1000 000 subsets, which we in turn have arbitrarily,\n"+
                                       "in our far-seeing-wisdom, set as an upper limit!)");
        }

        Object[] listAsArray = pList.toArray(new Object[numElements]);

        // ok, the size of the set is deemed feasible
        String[] combinationStrings = Utils.makeAllBinaryStrings('0', '1', numElements);

        List[] result = new List[numCombinations];
        for (int i=0; i<numCombinations; i++) {
            List subset = new ArrayList(numElements);
            for (int j=0; j < numElements; j++) {
                if (combinationStrings[i].charAt(j)=='1') {
                    subset.add(listAsArray[j]);
                }
            }
            result[i]=subset;
        }

        return result;
    }

    public static <T> List<List<T>> allSubsets(List<T> elements, int k) {
        List<List<T>> result = new ArrayList();
        List<BitSet> bitSets = CollectionUtils.allSubsets(elements.size(), k);
        for (BitSet bs: bitSets) {
            ArrayList l = new ArrayList(bs.cardinality());
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
                l.add(elements.get(i));
            }
            result.add(l);
        }
        return result;
    }

    /** enumerate all subsets of size k out of n */
    public static List<BitSet> allSubsets(int n, int k) {
        if (n < k) {
            throw new RuntimeException("n = "+n+" > "+k+" = k");
        }

        if (k == 0) {
            // a special case...
            return Collections.singletonList(new BitSet(n));
        }

        ArrayList result = new ArrayList();
        BitSet firstBitSet = new BitSet();
        BitSet firstBitUnset = new BitSet();
        firstBitSet.set(0);

        internalAllSubsets(n-1, k-1, n, firstBitSet, result);
        internalAllSubsets(n-1, k, n, firstBitUnset, result);

        return result;
    }

    /** @paramt prototype contains the numAlreadySetBits first bits. */
    private static void internalAllSubsets(int n, int k, int origN,
                                           BitSet alreadySetBits,
                                           ArrayList<BitSet> result) {
        if (n == 0 || k == 0) {
            // no more bits to select
            result.add(alreadySetBits);
            return;
        }

        int firstBit = origN - n;

        if (k == n) {
            // set all remaining bits
            BitSet bs = new BitSet();
            bs.or(alreadySetBits);
            for (int i=firstBit; i<origN; i++) {
                bs.set(i);
            }
            result.add(bs);
        }
        else {
            // recurse
            BitSet firstBitSet = new BitSet();
            BitSet firstBitUnset = new BitSet();
            firstBitSet.or(alreadySetBits);
            firstBitUnset.or(alreadySetBits);
            firstBitSet.set(firstBit);
            internalAllSubsets(n-1, k-1, origN, firstBitSet, result);
            internalAllSubsets(n-1, k, origN, firstBitUnset, result);
        }




    }

    /**
     * Make all 2-combinations, that is, pairs, of the elements of pList.
     *
     * The combinations will be ordered the same as the elements of the original list.
     *
     * The combinations are "unordered", that is the same thing shall not appear
     * twice in different directions...
     *
     * @see #makeUnorderedPairs(Collection)
     */
    public static <T> Pair<T,T>[] make2Combinations(List<T> pList) {
        // use generic combinations method to make combinations
        List[] lists = makeCombinations(pList, 2);

        // convert result to an array of Pair objects
        return (Pair[])ConversionUtils.convert(lists, new ListToPairConverter(), Pair.class);
    }

    /**
     * Make all k-combinations of the elements of pList.
     *
     * The combinations will be ordered the same as the elements of the original list.
     */
    public static List[] makeCombinations(List pList, int pK) {
        int n = pList.size();

        if (pK < 0 || pK > n) {
            throw new RuntimeException("Invalid k");
        }
        int numCombinations = MathUtils.numCombinations(n, pK);

        Object[] listAsArray = pList.toArray(new Object[n]);

        // ok, the size of the set is deemed feasible
        String[] combinationStrings = Utils.makeCombinationStrings('0', '1', n, pK);

        List[] result = new List[numCombinations];
        for (int i=0; i<numCombinations; i++) {
            List subset = new ArrayList(n);
            for (int j=0; j < n; j++) {
                if (combinationStrings[i].charAt(j)=='1') {
                    subset.add(listAsArray[j]);
                }
            }
            result[i]=subset;
        }

        return result;
    }

    /** Check whether all objects match a given condition */
    /*
    public static boolean doAllObjectsMatch(Collection pCollection, Condition pCondition) {
        return countMachingObjects(pCollection, pCondition) == pCollection.size();
    }
    */

    /** remove objects matching objects from a collection. Iterator must support remove! */
    public static void removeMatchingObjects(Collection pCollection, Condition pCondition) {
        removeMatchingObjects(pCollection.iterator(), pCondition);
    }

    /** remove objects matching objects from a collection. Iterator must support remove! */
    public static void removeMatchingObjects(Iterator pIter, Condition pCondition) {
        // dbgMsg("removing objects...");
        while (pIter.hasNext()) {
            Object o = pIter.next();
            if (pCondition.fulfills(o)) {
                // dbgMsg("removing object: "+o);
                pIter.remove();
            }
        }
    }

    /** as above, but return removed objects */
    public static List removeMatchingObjects_B(Collection pCollection, Condition pCondition) {
        Iterator i = pCollection.iterator();
        ArrayList removed = new ArrayList();
        // dbgMsg("removing objects...");
        while (i.hasNext()) {
            Object o = i.next();
            if (pCondition.fulfills(o)) {
                // dbgMsg("removing object: "+o);
                i.remove();
                removed.add(o);
            }
        }
        return removed;
    }


    /** remove null objects from a collection. Iterator must support remove! */
    public static void removeNullObjects(Collection pCollection) {
        removeMatchingObjects(pCollection, new IsNullCondition());

    }

    /** Pyh, Collection.remove only removes the first occurence! */
    public static void removeAllOccurences(Collection pCollection, Object pObjToRemove) {
        removeMatchingObjects(pCollection, new EqualsCondition(pObjToRemove));
    }

    public static List headList(List pList, int pFirstIndexToExclude) {
        return pList.subList(0, pFirstIndexToExclude);
    }

    public static <T> List<T> tailList(List<T> pList, int pFirstIndexToInclude) {
        return pList.subList(pFirstIndexToInclude, pList.size());
    }

    /** Return the objects at indices specified by pIndices */
    public static <T> List<T> selectObjects(List<T> pList, int[] pIndices) {
        ArrayList result = new ArrayList(pIndices.length);
        for (int i=0; i<pIndices.length; i++) {
            result.add(pList.get(pIndices[i]));
        }
        return result;
    }


   /**
    * Extract first pNumToExtract objects.  If there aren't enough objects, just return all the objects in the collection
    */
    public static <T> List<T> extractFirst(int pNumToExtract, Collection<T> pCol) {
        Iterator<T> objects = pCol.iterator();
        ArrayList<T> result = new ArrayList<T>(pNumToExtract);
        int numExtracted = 0;
        while(objects.hasNext() && numExtracted<pNumToExtract) {
            result.add(objects.next());
            numExtracted++;
        }
        return result;
    }

    /**
     * Extract first pNumToExtract objects.
     * If there are'nt enough objects, just return all the objects in the collection
     */
     public static <T> List<T> extractFirst(int pNumToExtract, Iterator<T> pIter) {
         ArrayList<T> result = new ArrayList<T>(pNumToExtract);
         int numExtracted = 0;
         while(pIter.hasNext() && numExtracted<pNumToExtract) {
             result.add(pIter.next());
             numExtracted++;
         }
         return result;
     }

    /**
    * Extract first pNumToExtract objects.
    * If there are'nt enough objects, just return all the objects in the collection
    */
    public static List extractFirst(int pNumToExtract, Object[] pArr) {
        ArrayList result = new ArrayList(Math.min(pNumToExtract, pArr.length));
        for (int i=0; i<pNumToExtract && i<pArr.length; i++) {
            result.add(pArr[i]);
       }
       return result;
    }

    public static void forEach(Collection pCollection, Operation pOperation) {
        Iterator i = pCollection.iterator();
        while (i.hasNext()) {
            pOperation.doOperation(i.next());
        }
    }


    public static void forEach(Iterator pIterator, Operation pOperation) {
        while (pIterator.hasNext()) {
            pOperation.doOperation(pIterator.next());
        }
    }


    public static void forEach(Object[] pArray, Operation pOperation) {
        for (int i=0; i<pArray.length; i++) {
            pOperation.doOperation(pArray[i]);
        }
    }

    public static void forEach(Map pMap, MapEntryOperation pOperation) {
        Iterator keys = pMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            Object val = pMap.get(key);
            pOperation.doOperation(key, val);
        }
    }

    /** Make pairs, each consisting of a element of pCol1 and an element of pCol2 */
    public static <T1, T2> Pair<T1, T2>[] makeCombinations(Collection<T1> pCol1, Collection<T2> pCol2) {
        ArrayList<Pair<T1,T2>> result = new ArrayList<Pair<T1,T2>>(pCol1.size() * pCol2.size());
        Iterator<T1> iter1 = pCol1.iterator();
        while(iter1.hasNext()) {
            T1 o1 = iter1.next();
            Iterator<T2> iter2 = pCol2.iterator();
            while(iter2.hasNext()) {
                T2 o2 = iter2.next();
                result.add(new Pair(o1,o2));
            }
        }
        return (Pair<T1,T2>[])ConversionUtils.collectionToArray(result, Pair.class);
    }

    /**
     * Make pairs, each pair p_i consisting of a pCol1[i] and pCol2[i].     *
     *
     * keywords: paired iterator, pairediterator (this makes a paired iterator
     * unneeded, unless one does not want to construct a new collection,
     * due to e.g. memory restrictions.
     *
     * @see #makeUnorderedPairs(Collection) for something completely different
     */
    public static <T1, T2> List<Pair<T1, T2>> makePairs(Collection<T1> pCol1, Collection<T2> pCol2) {
    	if (pCol1.size() != pCol2.size()) {
    		throw new RuntimeException("List sizes differ!");
    	}
    	int numPairs = pCol1.size();

        List<Pair<T1,T2>> result = new ArrayList<Pair<T1,T2>>(numPairs);
        Iterator<T1> iter1 = pCol1.iterator();
        Iterator<T2> iter2 = pCol2.iterator();
        while(iter1.hasNext()) {
            T1 o1 = iter1.next();
            T2 o2 = iter2.next();
            result.add(new Pair(o1,o2));
        }
        return result;
    }

    /**
     * Make pairs, each consisting of two fields, extracted by two separate
     * converters.
     *
     */
    public static <E,F1,F2> List<Pair<F1,F2>> makePairs(
            Collection<E> pCollection,
            Converter<E,F1> pFieldExtractor1,
            Converter<E,F2> pFieldExtractor2) {
        List<Pair<F1,F2>> result = new ArrayList();
        for (E obj: pCollection) {
            F1 f1 = pFieldExtractor1.convert(obj);
            F2 f2 = pFieldExtractor2.convert(obj);
            result.add(new Pair(f1,f2));
        }
        return result;

    }

    /**
     * Combine elements of pCol in all possible ways to make up UNORDERED pairs.
     * Do not form any "self-referential pairs" (x,x).
     *
     * @see #make2Combinations(List) */
    public static <T> List<UnorderedPair<T>> makeUnorderedPairs(Collection<T> pCol) {
        int nElems = pCol.size();
        int nPairs = nElems * (nElems-1) / 2;

        List<T> elemList = new ArrayList(pCol);
        List<UnorderedPair<T>> result = new ArrayList(nPairs);

        for (int i=0; i<nElems-1; i++) {
            for (int j=i+1; j<nElems; j++) {
                result.add(new UnorderedPair<T>(elemList.get(i), elemList.get(j)));
            }
        }

        return result;
    }

    /**
     * Make unordered pairs such that all pairs i,j will be formed
     * where i in pCol1 and j in pCol2. If there is overlap between
     * the collections, funny stuff might occur.
     */
    public static <T> List<UnorderedPair<T>> makeUnorderedPairs(
            Collection<T> col1,
            Collection<T> col2) {
        int n1 = col1.size();
        int n2 = col2.size();
        int nPairs = n1 * n2;

        List<T> list1 = col1 instanceof List && col1 instanceof RandomAccess
                        ? (List<T>)col1
                        : new ArrayList<T>(col1);

        List<T> list2 = col2 instanceof List && col2 instanceof RandomAccess
                        ? (List<T>)col2
                        : new ArrayList<T>(col2);

        List<UnorderedPair<T>> result = new ArrayList(nPairs);

        for (int i=0; i<n1; i++) {
            for (int j=0; j<n2; j++) {
                result.add(new UnorderedPair<T>(list1.get(i), list2.get(j)));
            }
        }

        return result;
    }


    public static <T> SymmetricPair<T> makePair(T p1, T p2) {
    	return new SymmetricPair(p1, p2);
    }

    public static Object[] clone(Object[] pArr, Class pClass) {
        return ConversionUtils.collectionToArray(Arrays.asList(pArr), pClass);
    }

    /**
     * Brutally flattens a collection such that no collections remains.
     * Use with utmost carefullness.
     */
    public static List flatten(Collection pCollection) {
        ArrayList result = new ArrayList();
        Iterator objs = pCollection.iterator();
        while(objs.hasNext()) {
            Object obj = objs.next();
            if (obj instanceof Collection) {
                // aha, object is a collection; recursive attack needed!
                result.addAll(flatten((Collection)obj));
            }
            else {
                // pheq, object is just some "atomic" object
                result.add(obj);
            }
        }
        return result;
    }

    /**
     * Brutally collapses a collection; collapse only one "level", in contrast to the
     * even more brutal "flatten"
     *
     * hakusanat: romauta, collapse.
     *
     * Use with utmost carefullness, still.
     */
    public static List flatten_nonrecursive(Collection pCollection) {
        ArrayList result = new ArrayList();
        Iterator objs = pCollection.iterator();
        while(objs.hasNext()) {
            Object obj = objs.next();
            if (obj instanceof Collection) {
                // aha, object is a collection; extract the elements...
                result.addAll((Collection)obj);
            }
            else {
                // phew, object is just some "atomic" object
                result.add(obj);
            }
        }
        return result;
    }

    /**
     * Check whether a map contains only mappings (pairs of keys and values)
     * that are present in another map.
     */
    public static <K,V> boolean isSubMap(Map<K,V> pMaybeSubMap, Map<K,V> pSuperMap) {
        for (K key: pMaybeSubMap.keySet()) {
            V subMapVal = pMaybeSubMap.get(key);
            V superMapVal = pSuperMap.get(key);
            if (superMapVal == null) {
                // no such key in supermap
                return false;
            }
            else if (!(subMapVal.equals(superMapVal))) {
                // different values for this key
                return false;
            }
        }

        // all mappings were found in the supermap
        return true;
    }

    /** Does not allow for duplicate entries; these will throw a runtimeexception! */
    public static <T> boolean isSubList(List<T> subListCandidate, List<T> superList) {

    	// first, check both lists for duplicates, and fail if ones found
    	if (containsDuplicates(subListCandidate)) {
    		throw new RuntimeException("Sublist candidate contains duplicate items");
    	}
    	if (containsDuplicates(superList)) {
    		throw new RuntimeException("Superlist contains duplicate items");
    	}

    	int j = 0;
        for (T subListItem : subListCandidate) {
        	while (j+1<superList.size() && !(subListItem.equals(superList.get(j)))) {
        		// current sublist element not found at superlist[j], and there are more elements in superlist
        		// => move to next element of superlist
        		j++;
        	}
        	if (j >= superList.size()) {
        		// exhausted superlist without finding the current sublist element
        		return false;
        	}

        	if (subListItem.equals(superList.get(j))) {
        		// current sublist element found in superlist[j], move to next element of superlist
        		j++;
        	}
        	else {
        		// current sublist element not found in superlist
        		return false;
        	}
        }

        // all entries of sublist candidate were found in the superlist
        return true;
    }

    /** Implemented by creating a new linkedhashmap (some kind of wrapper could be more efficient...) */
    public static <K,V> Map<K,V> subMap(Map<K,V> pMap, Collection<K> pKeys, boolean getNullValues) {
        Map<K,V> result = new LinkedHashMap<K,V>();
        Iterator<K> keysToSelect = pKeys.iterator();
        while(keysToSelect.hasNext()) {
            K key = keysToSelect.next();
            V val = pMap.get(key);
            if (val != null || getNullValues) {
                result.put(key, val);
            }
        }
        return result;
    }

    public static <K,V> List<V> select(Map<K,V> pMap, Collection<K> pKeys) {
        ArrayList<V> result = new ArrayList<V>();
        for (K key: pKeys) {
            V val = pMap.get(key);
            if (val != null) {
                result.add(val);
            }
        }
        return result;
    }


    /**
     * Makes a map out of a array of Object arrays (length of which must be 2!)
     * An example should clarify the situation.
     * Object[][] data = {
     *    {"Punainen", Color.red"},
     *    {"Sininen", Color.blue"}
     * };
     * LinkedHashMap map = makeMap(data);
     **/
    public static <T> LinkedHashMap<T,T> makeMap(T[][] pData) {
        Converter arrayToPairCnverter = new ConverterChain(new ArrayToListConverter(), new ListToPairConverter());
        Pair[] keyValuePairs = (Pair[])ConversionUtils.convert(pData, arrayToPairCnverter, Pair.class);
        return makeMapFromPairs(keyValuePairs);
    }

    public static <K,V> HashMap<K,V> makeMap(Collection<K> pKeys, Collection<V> pVals) {

        if (pKeys.size() != pVals.size()) {
            throw new RuntimeException("Different number of keys and vals!");
        }



        int n = pKeys.size();
        HashMap<K,V> result = new HashMap(n);
        Iterator<K> keyIter = pKeys.iterator();
        Iterator<V> valIter = pVals.iterator();

        for (int i=0; i<n; i++) {
            K key = keyIter.next();
            V val = valIter.next();
            result.put(key, val);
        }

        return result;

    }

    public static Map makeSimpleMap(Object pKey, Object pVal) {
        Map map = new HashMap();
        map.put(pKey, pVal);
        return map;
    }

    public static LinkedHashMap makeMapFromPairs(IPair[] pKeyValuePairs) {
        LinkedHashMap result = new LinkedHashMap();
        for (int i=0; i<pKeyValuePairs.length; i++) {
            result.put(pKeyValuePairs[i].getObj1(), pKeyValuePairs[i].getObj2());
        }
        return result;
    }

    public static MultiMap makeMultiMapFromPairs(IPair[] pPairs) {
        MultiMap result = new MultiMap();
        for (int i=0; i<pPairs.length; i++) {
            result.put(pPairs[i].getObj1(), pPairs[i].getObj2());
        }
        return result;
    }

    public static <T1,T2> MultiMap<T1,T2> makeMultiMapFromPairs(Collection<IPair<T1,T2>> pPairs) {
        MultiMap<T1,T2> result = new MultiMap();
        for (IPair<T1,T2> pair: pPairs) {
            result.put(pair.getObj1(), pair.getObj2());
        }
        return result;
    }

    public static <T> MultiMap<T,T> makeMultiMapFromSymmetricPairs(Collection<SymmetricPair<T>> pPairs) {
        MultiMap<T,T> result = new MultiMap<T,T>();
        for (SymmetricPair<T> pair: pPairs) {
            result.put(pair.getObj1(), pair.getObj2());
        }
        return result;
    }

    /**
     *Make a map: field -> object
     */
    public static <K,V> Map<K,V> makeMap(Collection<V> pCollection, Converter<V,K> pFieldExtractor) {
        Map<K,V> map = new LinkedHashMap<K,V>();
        for (V val: pCollection) {
            K key = pFieldExtractor.convert(val);
            map.put(key, val);
        }
        return map;
    }

    /**
     * Make a map: field1 -> field2
     */
    public static <E,K,V> Map<K,V> makeMap(Collection<E> pCollection,
                                           Converter<E,K> pKeyExtractor,
                                           Converter<E,V> pValExtractor) {

        Map<K,V> map = new LinkedHashMap<K,V>();
        for (E obj: pCollection) {
            K key = pKeyExtractor.convert(obj);
            V val = pValExtractor.convert(obj);
            map.put(key, val);
        }
        return map;
    }

    /**
     *Make a map: field -> object
     */
    public static MultiMap<Class, Object> makeByClassMultiMap(Collection pCollection) {
        return makeMultiMap(pCollection, new ObjectToClassConverter(), MultiMap.makeHashMapBasedMultiMap());
    }

    /**
     * Make a multi map: field -> object. Note that both the entries in the multi map as well as the entries in the
     * sets contained in the multi map are both in the insertion orde.
     *
     * @param pResult if non-null, store results here; else, store results
     * into a newly allocated multimap.
     */
    public static <K,V> MultiMap<K,V> makeMultiMap(Collection<V> pCollection,
                                                   Converter<V,K> pFieldExtractor,
                                                   MultiMap<K,V> pResult) {
        MultiMap<K,V> map = pResult != null ? pResult : new MultiMap<K,V>();
        Iterator<V> i = pCollection.iterator();
        while(i.hasNext()) {
            V o = i.next();
            K key = pFieldExtractor.convert(o);
            map.put(key, o);
        }
        return map;
    }

   /**
    * Combine several maps into a multimap. It is not required that the map have the same set of keys.
    */
    public static MultiMap makeMultiMap(Map[] pMaps) {
        MultiMap multiMap = new MultiMap();

        for (int i=0; i<pMaps.length; i++) {
            multiMap.putAll(pMaps[i]);
        }

        return multiMap;
    }

   /**
    * Combine several maps into a multimap. It is not required that the map have the same set of keys.
    * Only get keys in PKeySet
    *
    */
    public static MultiMap makeMultiMap(Map[] pMaps, Set pKeySet) {
        MultiMap multiMap = new MultiMap();

        for (int i=0; i<pMaps.length; i++) {
            Iterator permittedKeys = pKeySet.iterator();
            while(permittedKeys.hasNext()) {
                Object key = permittedKeys.next();
                Object val = pMaps[i].get(key);
                if (val != null) {
                    multiMap.put(key, val);
                }
             }
        }

        return multiMap;
    }


    /** Split a list to pairs. List must have even number of elements */
    public static <T> List<SymmetricPair<T>> splitToPairs(List<T> pList) {
        if (pList.size() % 2 != 0) {
            throw new RuntimeException("List has odd size; cannot split to pairs!");
        }
        int numPairs = pList.size()/2;
        List<SymmetricPair<T>> result = new ArrayList<SymmetricPair<T>>(numPairs);
        for (int i=0; i<numPairs; i++) {
            result.add(new SymmetricPair<T>(pList.get(i*2), pList.get(i*2+1)));
        }
        return result;
    }

    /** Split a list to pairs. List must have even number of elements */
    public static <T> SymmetricPair<T>[] splitToPairArray(List<T> pList) {
        if (pList.size() % 2 != 0) {
            throw new RuntimeException("List has odd size; cannot split to pairs!");
        }
        int numPairs = pList.size()/2;
        SymmetricPair[] result = new SymmetricPair[numPairs];
        for (int i=0; i<numPairs; i++) {
            result[i] = new SymmetricPair<T>(pList.get(i*2), pList.get(i*2+1));
        }
        return result;
    }

    /** Split a list to triples. List must have even number of elements */
    public static <T> List<SymmetricTriple<T>> splitToTriples(List<T> pList) {
        if (pList.size() % 3 != 0) {
            throw new RuntimeException("Cannot split to triples: number of items not divisible by 3!");
        }
        int numTriples = pList.size()/3;
        List<SymmetricTriple<T>> result = new ArrayList<SymmetricTriple<T>>(numTriples);
        for (int i=0; i<numTriples; i++) {
            result.add(new SymmetricTriple(pList.get(i*3), pList.get(i*3+1), pList.get(i*3+2)));
        }
        return result;
    }

    /** Duplicate to pairs so that both elements of the pair will be the same */
    public static Pair[] duplicateToPairs(List pList) {
        int numElements = pList.size();
        Iterator i = pList.iterator();
        ArrayList result = new ArrayList(numElements);
        while(i.hasNext()) {
            Object o = i.next();
            Pair pair = new Pair(o,o);
            result.add(pair);
        }
        return (Pair[])ConversionUtils.collectionToArray(result, Pair.class);
    }

    public static <T> Map<Integer, T> makeByIndexMap(List<T> pList) {
        LinkedHashMap<Integer,T> result = new LinkedHashMap();
        int size = pList.size();
        for (int i=0; i<size; i++) {
            result.put(new Integer(i), pList.get(i));
        }
        return result;
    }

    public static <T> Map<T,Integer> makeObjectToIndexMap(T[] pArr) {
    	return makeObjectToIndexMap(Arrays.asList(pArr));
    }

    public static <T> Map<T, Integer> makeObjectToIndexMap(List<T> pList) {
        LinkedHashMap<T, Integer> result = new LinkedHashMap();
        int size = pList.size();
        for (int i=0; i<size; i++) {
        	T o = pList.get(i);
        	if (result.containsKey(o)) {
        		throw new RuntimeException("List contains duplicate value: "+o);
        	}
        	else {
        		result.put(pList.get(i), new Integer(i));
        	}
        }
        return result;
    }

    public static <T> Map<Integer, T> makeIndexToObjectMap(T[] pArr) {
        return makeByIndexMap(Arrays.asList(pArr));
    }

    public static Map makeByClassNameMap(Collection pObjs) {
        Iterator i = pObjs.iterator();
        HashMap result = new HashMap();
        while(i.hasNext()) {
            Object o = i.next();
            String className = o.getClass().getName();
            if (result.containsKey(className)) {
                throw new RuntimeException("Multiple entries with same class, cannot make by class name map");
            }
            else {
                result.put(className, o);
            }
        }
        return result;

    }

    public static Object shift(List pList) {
        if (pList.size()==0) {
            return null;
        }
        Object obj = pList.get(0);
        pList.remove(0);
        return obj;
    }

    public static Object pop(List pList) {
        if (pList.size()==0) {
            return null;
        }
        else {
            return pList.remove(pList.size()-1);
        }
    }


    /** @todo effecient implementation in case RandomAccess is not supported */
    public static void sortPairwise(List pList, Comparator pComparator) {
        int numElems = pList.size();
        if (numElems % 2 != 0) {
            throw new RuntimeException("Must have even number of elements!");
        }
        int numPairs = numElems/2;
        for (int i=0; i<numPairs; i++) {
            int ind1 = i*2;
            int ind2 = i*2+1;
            Object o1 = pList.get(ind1);
            Object o2 = pList.get(ind2);
            if (pComparator.compare(o1, o2) > 0) {
                Collections.swap(pList, ind1, ind2);
            }
        }
    }

    /** Example: duplicateElements({"A","B"}) = {"A","A","B","B"} */
    public static List duplicateElements(List pList) {
        int numElements = pList.size();
        ArrayList result = new ArrayList(numElements);
        Iterator i = pList.iterator();
        while(i.hasNext()) {
            Object o = i.next();
            result.add(o);
            result.add(o);
        }
        return result;
    }

    public static LinkedHashMap keyOrderedMap(Map pMap) {
        return keyOrderedMap(pMap, new NaturalOrderComparator());
    }

    public static LinkedHashMap keyOrderedMap(Map pMap, Comparator pComparator) {
        List keys = new ArrayList(pMap.keySet());
        int numElems = keys.size();
        Collections.sort(keys, pComparator);
        LinkedHashMap result = new LinkedHashMap(numElems);
        for (int i=0; i<keys.size(); i++) {
            Object key = keys.get(i);
            Object val = pMap.get(key);
            result.put(key, val);
        }
        return result;
    }

    /**
     * Split a list to as equally sized segments as possible.
     * @param pAllocateNewlists allocate new ArrayList for each segment
     * instead of using List.subList().
     */
    public static <T> List<T>[] splitToSegments_numsegments(List<T> pList,
                                                            int pNumSegments,
                                                            boolean pAllocateNewLists) {
        int totNumElements = pList.size();
        int minSegmentSize = totNumElements / pNumSegments;
        int numExcessElems = totNumElements % pNumSegments;
        int numSegmentsWithExcessElem = numExcessElems;
        int numSegmentsWithoutExcessElem = pNumSegments-numSegmentsWithExcessElem;
        Integer segmentSize1;
        if (numExcessElems == 0) {
            segmentSize1 = new Integer(minSegmentSize);
        }
        else {
            segmentSize1 = new Integer(minSegmentSize+1);
        }
        Integer segmentSize2 = new Integer(minSegmentSize);
        List<Integer> segmentSizes1 = Collections.nCopies(numSegmentsWithExcessElem, segmentSize1);
        List<Integer> segmentSizes2 = Collections.nCopies(numSegmentsWithoutExcessElem, segmentSize2);
        ArrayList segmentSizes = new ArrayList(segmentSizes1.size()+segmentSizes2.size());
        segmentSizes.addAll(segmentSizes1);
        segmentSizes.addAll(segmentSizes2);
        return splitToSegments(pList, ConversionUtils.integerCollectionToIntArray(segmentSizes), pAllocateNewLists);
    }



    /**
     * Split list to segments of size pSegmentLen; if numElements % pSegmentLen != 0,
     * then the last segment shall contain a smaller number of elements.
     *
     * keywords: split to windows, split to ranges, divide to ranges, divide to windows
     *
     * @param pAllocateNewlists allocate new ArrayList for each segment
     * instead of using List.subList().
     */

    public static <T> List<T>[] splitToSegments_segmentlen(List<T> pList,
                                                           int pSegmentLen,
                                                           boolean pAllocateNewlists) {
//        int numElements = pList.size();
//        int numSegments;
//        int[] segmentLengths;
//        if (numElements % pSegmentLen == 0) {
//            // split is even
//            numSegments = numElements/pSegmentLen;
//            segmentLengths = new int[numSegments];
//            for (int i=0; i<numSegments; i++) {
//                segmentLengths[i]=pSegmentLen;
//            }
//        }
//        else {
//            // split is not even
//            numSegments = numElements/pSegmentLen+1;
//            segmentLengths = new int[numSegments];
//            for (int i=0; i<numSegments-1; i++) {
//                segmentLengths[i]=pSegmentLen;
//            }
//            // the excess elements:
//            segmentLengths[numSegments-1] = numElements % pSegmentLen;
//        }
        int[] segmentLengths = Utils.deduceSplitSegmentLengths(pList.size(), pSegmentLen);
        return splitToSegments(pList, segmentLengths, pAllocateNewlists);
    }

    /**
     * @param pAllocateNewlists allocate new ArrayList for each segment
     * instead of using List.subList().
     */
    public static <T> List<T>[] splitToSegments(List<T> pList,
                                               int[] pSegmentLengths,
                                               boolean pAllocateNewLists) {
        int segmentLenSum = MathUtils.sum(pSegmentLengths);
        if (segmentLenSum != pList.size()) {
            throw new RuntimeException("Cannot split to segments: sum of segment lengths <> lenght of list!");
        }
        // alles in ordnung, ja?
        int segmentStart = 0;
        int numSegments = pSegmentLengths.length;
        List<T>[] result = new List[numSegments];
        for (int i=0; i<numSegments; i++) {
            int segmentLen = pSegmentLengths[i];
            int segmentEnd = segmentStart+segmentLen;
            List segment = pList.subList(segmentStart, segmentStart+segmentLen);
            if (pAllocateNewLists) {
                segment = new ArrayList(segment);
            }
            result[i] = segment;
            segmentStart = segmentEnd;
        }
        return result;
    }

    /** Return true, iff:  pArr1.length == pArr2.length and for all i: pArr1[i].equals(pArr2[i]) == true */
    public static boolean areArraysEqual(Object[] pArr1, Object[] pArr2) {
        if (pArr1.length != pArr2.length) {
            return false;
        }
        else {
            for (int i=0; i<pArr1.length; i++ ) {
                if (!(pArr1[i].equals(pArr2[i]))) {
                    return false;
                }
            }
        }
        // passed the requirements for equality
        return true;
    }


    /**
     * -defined to be true for an empty collection
     * -defined to be true for a collection containing only nulls.
     */
    public static boolean areAllObjectsEqual(Collection pCol) {
        if (pCol.size()==0) {
            // OK, we define that this is true for an empty collection
            return true;
        }
        else {
            Iterator i =  pCol.iterator();
            // OK, we have a first object
            Object first = pCol.iterator().next();

            if (first == null) {
                // require that all the others are null as well...
                while (i.hasNext()) {
                    Object cur = i.next();
                    if (cur != null) {
                        return false;
                    }
                }
            }
            else {
                // So, first object is non-null
                // require that all objects are equal to the first object...'
                while (i.hasNext()) {
                    Object cur = i.next();
                    if (!(first.equals(cur))) {
                        return false;
                    }
                }
            }
            // tests passed, so all objects must be equal.
            return true;
        }
    }

    public static boolean areObjectsPairWiseEqual(List pList) {
        if (pList.size() % 2 != 0) {
            throw new RuntimeException("Cannot check pairwise equality; uneven number of objects!");
        }
        SymmetricPair[] pairs = splitToPairArray(pList);
        List valSets = ConversionUtils.convert(Arrays.asList(pairs), new CollectionToHashSetConverter());
        Set valSetSizeCounts = new HashSet(ConversionUtils.convert(valSets, new CollectionToSizeConverter()));
        if (valSetSizeCounts.equals(Collections.singleton(new Integer(1)))) {
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * Hmm, maybe this is not a real "closure", but let's use that term here
     * in abscence of a better term.
     *
     * Example input:
     *    A1->A2
     *    A2->A3
     *    A3->A4
     *    B1->B2
     *
     * Result:
     *    A1->A4
     *    A2->A4
     *    A3->A4
     *    B1->B2
     */
    public static <V> Map<V,V> closure(Map pMap) {
        Map<V, V>  map = pMap;
        Map<V, V> oldMap;

        do {
            oldMap = map;
            map = ConversionUtils.convertValues(map,
                                                new MapConverter<V,V>(map, MapConverter.NotFoundBehauvior.RETURN_ORIGINAL),
                                                new LinkedHashMap());
        } while (!(map.equals(oldMap)));
        return map;
    }

    /** represent a map by a set of key-value Pairs; the ordering is the order of keys as returned by the map's keyset's iterator. */
    public static Pair[] mapToKeyValuePairs(Map pMap) {
        int numObjects = pMap.size();
        Iterator keys = pMap.keySet().iterator();
        Pair[] result = new Pair[numObjects];
        for (int i=0; i<numObjects; i++) {
            Object key = keys.next();
            result[i] = new Pair(key, pMap.get(key));
        }
        return result;
    }

    /** represent a map by a set of key-value Pairs; the ordering is the natural ordering of the keys */
    public static Pair[] mapToKeyValuePairs_keysorted(Map pMap) {
        return mapToKeyValuePairs(asKeySortedMap(pMap, new NaturalOrderComparator()));
    }

    public static LinkedHashMap asKeySortedMap(Map pOriginalMap, Comparator pComparator) {
        int numKeys = pOriginalMap.size();
        ArrayList sortedKeys = new ArrayList(pOriginalMap.keySet());
        Collections.sort(sortedKeys, pComparator);
        LinkedHashMap result = new LinkedHashMap(numKeys);
        for (int i=0; i<numKeys; i++) {
            Object key = sortedKeys.get(i);
            result.put(key, pOriginalMap.get(key));
        }
        return result;
    }

    /**
     * Assert injectivity!
     *
     * Keywords: reverseMap, reverse map
     */
    public static <T1, T2> Map<T2,T1> inverseMap(Map<T1,T2> pMap) {
        HashMap<T2,T1> result = new HashMap<T2,T1>();
        Iterator<T1> originalKeys = pMap.keySet().iterator();
        while(originalKeys.hasNext()) {
            T1 originalKey = originalKeys.next();
            T2 originalVal = pMap.get(originalKey);
            if (result.containsKey(originalVal)) {
                throw new RuntimeException("Cannot make inverse map; the map is not injective. Original values:\n"+
                                           StringUtils.mapToString(pMap));
            }
            result.put(originalVal, originalKey);
        }
        return result;
    }

    /** Keywords: reverseMap reverse map */
    public static <T1, T2> Map<T2,T1> inverseMap(MultiMap<T1,T2> pMultiMap) {
        HashMap<T2,T1> result = new HashMap<T2,T1>();
        for (T1 originalKey: pMultiMap.keySet()) {
            for (T2 originalVal: pMultiMap.get(originalKey)) {
            	if (result.containsKey(originalVal)) {
            		throw new RuntimeException("Cannot make inverse map; the multimap given as parameter is not injective. Original values:\n"+
                                           		StringUtils.multiMapToString(pMultiMap));
            	}
            	else {
            		result.put(originalVal, originalKey);
            	}
            }
        }
        return result;
    }

    /**
     *  Of course, when the map is not injective, we need a MultiMap
     *  for the inverse mapping.
     */
    public static<V, K> MultiMap<V, K> inverseMultiMap(Map<K, V> pMap, MultiMap pResult) {
        MultiMap<V, K> result = pResult != null ? pResult : new MultiMap<V,K>();

        for (K originalKey: pMap.keySet()) {
            V originalVal = pMap.get(originalKey);
            result.put(originalVal, originalKey);
        }

        return result;
    }


    /**
     * Reverse a multimap. Use the (presumably originally empty) multimap pResult
     * for storing the result.
     */
    public static<V, K> MultiMap<V, K> inverseMultiMap(MultiMap<K, V> pMap, MultiMap pResult) {
        MultiMap<V, K> result = pResult != null ? pResult : new MultiMap<V,K>();

        for (K originalKey: pMap.keySet()) {
            for (V originalVal: pMap.get(originalKey)) {
                result.put(originalVal, originalKey);
            }
        }

        return result;
    }


    /** A kludge to enable Sets of type Set<? extends T> */
    public static <T> Set<? extends T> intersection2(Set<? extends T> p1, Set<? extends T> p2) {
    	// Logger.info("Running old intersection impl");
        LinkedHashSet<T> result = new LinkedHashSet<T>(p1);
        result.retainAll(p2);
        return result;
    }

    /** Does p1 contain any of the elements of p2? */
    public static <T> boolean containsAny(Set<T> p1, Collection<T> p2) {
        Iterator<T> i = p2.iterator();
        while (i.hasNext()) {
            T o = i.next();
            if (p1.contains(o)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Set<T> intersection_old(Set<T> p1, Set<T> p2) {
        LinkedHashSet<T> result = new LinkedHashSet<T>(p1);
        result.retainAll(p2);
        return result;
    }

    /**
     * Perform intersection by iterating each element e of pCollection, and
     * checking pSet for the existence of each e; if exists, add to result.
     *
     * Return the result in a new HashSet.
     */
    public static <T> Set<T> intersection(Set<T> pSet, Collection<T> pCollection) {

        int maxSize = Math.min(pSet.size(), pCollection.size());
        ArrayList tmp = new ArrayList(Math.min(maxSize, 10));

        for (T e: pCollection) {
            if (pSet.contains(e)) {
                tmp.add(e);
            }
        }

        return new HashSet(tmp);
    }

    /**
     * Perform intersection by iterating the smaller of the sets
     * and checking the larger set for existence of each item of the smaller set.
     * Return the results as a HashSet.
     */
    public static <T> Set<T> intersection(Set<T> p1, Set<T> p2) {
//    	Logger.info("Running new intersection impl");
//    	Timer.startTiming("CollectionUtils.intersection");

    	int maxSize = Math.min(p1.size(), p2.size());
    	ArrayList tmp = new ArrayList(Math.min(maxSize, 10));

    	Set<T> smallSet;
    	Set<T> largeSet;

    	if (p1.size() < p2.size()) {
    		smallSet = p1;
    		largeSet = p2;
    	}
    	else {
    		smallSet = p2;
    		largeSet = p1;
    	}

    	for (T o: smallSet) {
    		if (largeSet.contains(o)) {
    			tmp.add(o);
    		}
    	}

//    	Timer.endTiming("CollectionUtils.intersection");

    	return new HashSet(tmp);
	}


    /** keywords: "do sets intersect", "do sets have common elements" */
    public static <T> boolean intersect(Set<T> p1, Set<T> p2) {

        Set<T> smallSet;
        Set<T> largeSet;

        if (p1.size() < p2.size()) {
            smallSet = p1;
            largeSet = p2;
        }
        else {
            smallSet = p2;
            largeSet = p1;
        }

        for (T o: smallSet) {
            if (largeSet.contains(o)) {
                return true;
            }
        }

        // no common elements
        return false;

    }



    /** A hopefully fast implementation for checking whether two sets intersect */
    public static boolean intersects(Set p1, Set p2) {
        Set smaller;
        Set larger;
        if (p1.size() <= p2.size()) {
            smaller = p1;
            larger = p2;
        }
        else {
            smaller = p2;
            larger = p1;
        }

        for (Object o: smaller) {
            if (larger.contains(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Perform intersection by adding elements of first set to pResult and
     * calling retainAll.
     *
     * Put results to pResult. Assume pResult is never null! Clear pResult before use.
     * Might possibly be optimized further...
     */
    public static <T> void intersection(Set<T> p1, Set<T> p2, Set<T> pResult) {
        pResult.clear();
        pResult.addAll(p1);
        pResult.retainAll(p2);
    }


    public static <T> Set<T> asUnmodifiableSet(Collection<T> pCollection) {
        if (pCollection instanceof Set) {
            return Collections.unmodifiableSet((Set)pCollection);
        }
        else {
            return Collections.unmodifiableSet(new HashSet<T>(pCollection));
        }
    }

   /**
    * Perform intersection by adding elements of first set to a new LinkedHashSet
    * and calling retainAll on that set.
    */
    public static <T> Set<T> intersection(Collection<T> p1, Collection<T> p2) {
        LinkedHashSet<T> result = new LinkedHashSet<T>(p1);
        result.retainAll(p2);
        return result;
    }

    public static <T> Set<T> union(Set<T> p1, Set<T> p2) {
        LinkedHashSet<T> result = new LinkedHashSet<T>(p1);
        result.addAll(p2);
        return result;
    }

    public static Number findMin(Iterable<? extends Number> p) {
        Number min = null;
        for (Number n: p) {
            if (min == null || min.doubleValue() > n.doubleValue()) {
                min = n;
            }
        }

        return min;
    }

    public static Number findMax(Iterable<? extends Number> p) {
        Number max = null;
        for (Number n: p) {
            if (max == null || max.doubleValue() < n.doubleValue()) {
                max = n;
            }
        }

        return max;
    }

    /**
     * A set-minus implemented by going through all entries of p1 and
     * adding each entry that is not in p2 to the result.
     * Efficient when p2 is expected to be "large", that is, the result
     * is expected to be "small".
     *
     * Consider also using {@link #minus(Collection, Collection, Set).
     *
     * Invariant: p1 <> pResult && p2 <> pResult.
     * @param pResult store results here. If null, create a new LinkedHashSet
     * for storing the results.
     */
    public static <T> Set<T> minus2(Collection<T> pSet1, Set<T> pSet2, Set<T> pResult) {
        if (pSet1 == pResult || pSet2 == pResult) {
            throw new RuntimeException("Invariant violated");
        }

        Set<T> result = pResult;
        if (result == null) {

            int resultSize;
            int sizeDifference = pSet1.size()-pSet2.size();
            if (sizeDifference > 10) {
                resultSize = sizeDifference;
            }
            else {
                resultSize = 10;
            }
            result = new LinkedHashSet(resultSize);
        }

        for (T o: pSet1) {
            if (!(pSet2.contains(o))) {
                result.add(o);
            }
        }

        return result;

    }


    /**
     * Set-minus. Store result into pResult, or into a new LinkedHashSet, if
     * pResult is null. Implemented by creating a set with all elements of p1,
     * and then removing all elements of p2 from that set.
     * Invariant: p1 <> pResult && p2 <> pResult.
     */
    public static <T> Set<T> minus(Collection<T> p1, Collection<T> p2, Set<T> pResult) {

        if (p1 == pResult || p2 == pResult) {
            throw new RuntimeException("Invariant violated");
        }

        Set<T> result = pResult != null ? pResult : new LinkedHashSet<T>(p1.size());
        result.addAll(p1);
        result.removeAll(p2);
        return result;
    }

    /**
     * Symmetric difference of sets. "XOR". "Union-intersection". Store result
     * into pResult, or into a new LinkedHashSet, if * pResult is null.
     *
     * Compute by removing intersection from union (maybe not maximally efficient...)
     */
    public static <T> Set<T> symmetricDifference(Set<T> p1, Set<T> p2, Set<T> pResult) {
        Set<T> result = pResult != null ? pResult : new LinkedHashSet<T>();
        if (result.size() > 0) {
            throw new RuntimeException("Result set not empty!");
        }

        result.addAll(p1);
        result.addAll(p2);
        Set<T> intersection = intersection(p1, p2);
        result.removeAll(intersection);

        return result;
    }

    /**
     * Set-minus. Return result in a new LinkedHashSet.
     * Implemented by creating a LinkedHashSet with all elements of p1,
     * and then removing all elements of p2 from that set.     *
     */
    public static <T> Set<T> minus(Collection<T> p1, Collection<T> p2) {
        return minus(p1, p2, null);
    }

    public static <T> Set<T> minus3(Collection<T> p1,T p2) {
        return minus(p1, Collections.singleton(p2));
    }

    public static <T> Set<T> union(Set<T>... pSets) {
        LinkedHashSet<T> result = new LinkedHashSet<T>();
        for (Set<T> set: pSets) {
            result.addAll(set);
        }
        return result;
    }

    public static <T> Set<T> union(Collection<Set<T>> pSets) {
        LinkedHashSet<T> result = new LinkedHashSet<T>();
        for (Set<T> set: pSets) {
            result.addAll(set);
        }

        return result;
    }


    public static Set intersection(Set[] pSets) {
        if (pSets.length == 0) {
            return new LinkedHashSet();
        }
        else {
            LinkedHashSet result = new LinkedHashSet(pSets[0]);
            for (int i=1; i<pSets.length; i++) {
                result.retainAll(pSets[i]);
            }
            return result;
        }
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg("CollectionUtils: "+pMsg);
    }

    public static void main(String[] args) throws Exception {
    	// test1();
    	// test2();
        // closureTest();
        // containmentTest();
        // numberingTest();
//        unorderedPairsTest();
        minusTest(args);
    }

    private static void minusTest(String[] args) throws IOException {
        Set<String> set1= new LinkedHashSet(IOUtils.readLines(args[0]));
        Set<String> set2= new LinkedHashSet(IOUtils.readLines(args[1]));
        Set<String> result = minus(set1, set2, null);
        IOUtils.writeCollectionToStdOut(result);

    }

    public static String[] join(String[] pArr1, String[] pArr2) {
        List<String> result = new ArrayList<String>(pArr1.length+pArr2.length);
        result.addAll(Arrays.asList(pArr1));
        result.addAll(Arrays.asList(pArr2));
        return ConversionUtils.stringCollectionToArray(result);
    }

    public static String[] join(String[] pArr, String pString) {
        List<String> result = new ArrayList<String>(pArr.length+1);
        result.addAll(Arrays.asList(pArr));
        result.add(pString);
        return ConversionUtils.stringCollectionToArray(result);
    }

    @SuppressWarnings("unused")
    private static void test1() {
    	ArrayList miscObjects = new ArrayList();
        /*
        miscObjects.add(new javax.swing.JPanel());
        miscObjects.add(new java.awt.Container());
        miscObjects.add(new java.awt.Component() { });
        miscObjects.add("foobar");
        */
        miscObjects.add("foobar");
        miscObjects.add("barfoo");
        dbgMsg("Most common super class of objects: "+ReflectionUtils.getMostSpecificCommonSuperClass(miscObjects));
        String[] objects = (String[])ConversionUtils.collectionToArray(miscObjects);
        dbgMsg("objects:\n"+StringUtils.arrayToString(objects, "\n"));
    }

    public static void numberingTest() throws Exception {
        Set<String> objects = new LinkedHashSet(IOUtils.readLines(System.in));
        Map<String, Integer> numbering = numberElements(objects, 1);
        System.out.println(StringUtils.mapToString(numbering));
    }

    public static <T> Iterable<T> asIterable(Iterator<T> pIterator) {
        return new IteratorIterable<T>(pIterator);
    }



}
