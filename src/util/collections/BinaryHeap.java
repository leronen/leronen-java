package util.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import util.IOUtils;
import util.converter.StringToIntegerConverter;
import util.dbg.Logger;


/**
 * This is an array-based queue where we have at least the following operations: 
 *    - add: log n
 *    - removemin: log n
 *    - get(Object): constant
 *    - getmin(): constant.
 * 
 * Each object in the heap shall have a key of type K, which will define
 * the heap order (K must be comparable). 
 * 
 * The heap is actually also a set, which means that it only stores each object
 * at most once (it is not possible to store the same object with 2 different
 * weights; even less twice with the same weight).
 *  
 * Note that the stored objects MUST sensibly implement equals() and hashcode()!
 * 
 * keywords: priority queue, array heap, array queue
 */
public class BinaryHeap<T,K extends Comparable<? super K>> {
        
    /**
     * This array may contain an arbitrary number of null elements in the
     * end. There may be overwritten. The number of business elements in the
     * array is kept track in a separate member variable; the array size is
     * incremented when needed by adding nulls to the array. This somewhat
     * unintuitive logic is because we want to copy the impl from Sevon as
     * unchanged as possible, to avoid introducing new bugs.
     */
    private ArrayList<Entry> mArr;

    /** Key: Stored object. Val: position in mArr */
    private Map<T, Integer> mPosByObject;
          
    private int mSize;
        
    private Mode mMode;
    
    public BinaryHeap(Mode pMode, int pInitialCapacity) {
        mSize = 0;
        mArr = new ArrayList(pInitialCapacity);
        mPosByObject = new HashMap();
        mMode = pMode;
    }
    
    public BinaryHeap(Mode pMode) {        
        this (pMode, 10);
    }

    public void clear() {
        mSize = 0;
        mPosByObject.clear();
    }
    
  /**
    * Insert pEntry to arr[pPos] and move it "up" in the heap until a object with
    * a smaller (or bigger, in the case of a max heap) key is found.
    * (naturally move existing, larger (smaller) objects downwards  
    *  in the process)
    *  
    * Note that pPos must not contain any entry before calling this!
    */ 
    private void drop(Entry pEntry, int pPos) {
        int j = pPos;

        // insert pEntry to pos j
        mArr.set(j, pEntry);
        mPosByObject.put(pEntry.mObj, j);

        // find a suitable pos for pEntry
        while (j > 0) {
            int j2 = (j - 1) / 2;
            Entry entry2 = mArr.get(j2);
            int cmp = pEntry.mKey.compareTo(entry2.mKey);
            if (mMode == Mode.MAX) cmp = -cmp;
            if (cmp < 0) {
                // the parent position holds a smaller [larger] object 
                // => perform a swap
                mArr.set(j, entry2);
                mPosByObject.put(entry2.mObj, j);
                mArr.set(j2, pEntry);
                mPosByObject.put(pEntry.mObj, j2);                
                // (heh, the next line was missing...)
                j = j2;
            } else {
                // we have reached a suitable position
                j = 0;
            }
        }
    }

    /**
     * Please note that trying to add the same object twice is a grave error 
     * (set semantics are enforced). 
     * 
     * Also, null objects of keys are not allowed. 
     */ 
    public void add(T pObject, K pKey) {
               
        if (mPosByObject.containsKey(pObject)) {
            throw new RuntimeException("Cannot add: heap already contains object: "+pObject); 
        }

        int j = mSize;

        if (j == mArr.size()) {
            // make space in the array
            mArr.add(null);
        }

        Entry entry = new Entry(pObject, pKey);
        
        drop(entry, j);
        mSize += 1;

        // ensureConsistentState();
    }
    
   /**
    * Remove and return the top element, which may be the minimum or maximum, 
    * depending on whether this is a min or max heap.
    * 
    * If the heap is empty, throw a NoSuchElementException.
    */ 
    public T pop() {

        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return internalRemove(0).mObj;
        }
    }

   /** 
    * Return the top element, which may be the minimum or maximum, 
    * depending on whether this is a min or max heap.
    * 
    * If the heap is empty, throw a NoSuchElementException.
    */
    public T peek() {
        
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            return mArr.get(0).mObj;
        }
    }
    
    /** Check (by hashing), if the heap contains pObj */
    public boolean contains(T pObj) {
        return mPosByObject.containsKey(pObj);
    }
    
    /**
     * Retrieve the heap key for pObj. Return null, if pObj is not in the heap.
     */
    public K key(T pObj) {
        if (isEmpty()) {
            throw new NoSuchElementException();
        } else {
            Integer i = mPosByObject.get(pObj);
            if (i == null) {
                return null;
            }
            else {
                return mArr.get(i).mKey;
            }
        }
    }
    
    /** 
     * Remove the entry at arr[pPos], moving smaller [bigger] entries
     * up to fill the space.  
     * 
     * Will also remove from map  mPosByObject. 
     */
    private Entry internalRemove(int pPos) {
        // Timer.startTiming("internalRemove");
        int j = pPos;

        Entry entryToBeRemoved = mArr.get(j);

        if (entryToBeRemoved == null) {
            throw new RuntimeException("Heap inconsistency: null entry at position "+pPos);
        }

        // remove from pos map
        mPosByObject.remove(entryToBeRemoved.mObj);

        int j2 = 2 * j + 1;
        while (j2 < mSize) {
            // get the 2 objects below j: arr[j2] and arr[j2]            
            Entry entry1 = mArr.get(j2);
            if (j2 + 1 < mSize) {
                Entry entry2 = mArr.get(j2 + 1);
                int cmp = entry2.mKey.compareTo(entry1.mKey);
                if (mMode == Mode.MAX) cmp = -cmp;
                if (cmp < 0) {
                    entry1 = entry2;
                    j2 = j2 + 1;
                }
                // now entry1 is the smallest of the two child entries,
                // and j2 is the pos of that entry
            }
            
            // move entry1 (in pos j2) upwards in the heap (overwriting the entry in pos j)
            mArr.set(j, entry1);
            mPosByObject.put(entry1.mObj, j);
            j = j2;
            j2 = 2 * j + 1;
        }

        mSize -= 1;
        
        if (mSize > j) {
            // now j is the position on the lowest level whose entry
            // was moved upwards, making position j empty; thus we have to
            // fill pos j by moving the entry to the last position of the
            // heap to pos j; unfortunately, it may then also be necessary to
            // move it up in the heap.
            // This whole thing is of course only required if the
            // the entry did not already end up in the last pos of the heap
            // (a nice condition, which seems, however, unlikely; in that
            // case, we can just leave the entry in the last pos, to be shortly
            // overwritten by a convenient null at hand).
            drop(mArr.get(mSize), j);
        }
        // now clear the last pos of array (it's element is now in pos j)
        mArr.set(mSize, null);

        // ensureConsistentState();

        // Timer.endTiming("internalRemove");

        return entryToBeRemoved;
    }

    /**
     * It's crucial that a log(n) implementation be provided for this.
     * Needless to say, trying to remove a non-existent object is an ERROR.
     */
    public T remove(T pObj) {
        Integer posToRemove = mPosByObject.remove(pObj);
        if (posToRemove == null) {
            throw new NoSuchElementException();
        } else {
            return internalRemove(posToRemove).mObj;
        }
    }

    public int size() {
        return mSize;
    }

    /** @return the min or max key in the heap. If the heap is empty, return null */
    public K topKey() {
        if (isEmpty()) {
            return null;
        } else {
            return mArr.get(0).mKey;
        }
    }

    /**
     * Just a convenience method, same as: remove(pObj), add(pObj,pKey).
     * 
     * TODO: As the usual usage is to just decrease the distance (?), we could
     * maybe implement this more efficiently? If this turns out to be a
     * bottleneck, then that shall be considered...
     * 
     * TODO: an even easier optimization would be to re-cycle the Entry
     * for this object; now we dispose of the old Entry and create a new one,
     * a practice which may not be very rigorous memory management-wise. 
     */
    public void updateKey(T pObj, K pNewKey) {
        // Timer.startTiming("updateDistance");
        remove(pObj);        
        add(pObj, pNewKey);
        // Timer.endTiming("updateDistance");
    }
        
    
    public boolean isEmpty() {
        return mSize == 0;
    }

    //////////////////////////////
    ///////////////////////////
    ///
    //  Only dbg stuff follows (?)
    // 
    private void checkHeapOrder() {
        for (int i = 0; i < mSize; i++) {
            Entry parent = mArr.get(i);
            if (parent == null) {
                throw new RuntimeException();
            }
            int j1 = i * 2 + 1;
            int j2 = j1 + 1;
            if (j1 < mSize) {
                Entry child1 = mArr.get(j1);
                int cmp = parent.mKey.compareTo(child1.mKey);
                if (mMode == Mode.MAX) cmp = -1;
                if (cmp > 0) {
                    throw new RuntimeException();
                }
            }
            if (j2 < mSize) {
                Entry child2 = mArr.get(j2);
                int cmp = parent.mKey.compareTo(child2.mKey); 
                if (mMode == Mode.MAX) cmp = -1;
                if (cmp > 0) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private void checkMap() {
        for (T objInMap : mPosByObject.keySet()) {
            Integer pos = mPosByObject.get(objInMap);
            Entry entry = mArr.get(pos);
            T objInArray = entry.mObj;
            if (!(objInMap.equals(objInArray))) {
                throw new RuntimeException();
            }
        }
    }

    private void ensureConsistentState() {
        checkHeapOrder();
        checkMap();
        // TODO: remove costly sanity check!
        // Set<Integer> mapIds = mMap.keySet();
        // HashSet<Integer> queueIds = new HashSet<Integer>();
        // for (CrawledNode node: mQueue) {
        // queueIds.add(node.mNode.getId());
        // }
        // if (!(mapIds.equals(queueIds))) {
        // Set idsNotInQueue = CollectionUtils.minus(mapIds, queueIds);
        // throw new RuntimeException(this+": noo! Ids in map, but not in
        // queue: "+idsNotInQueue);
        // }

        if (mPosByObject.size() != mSize) {
//            Set<Integer> idsInMap = mPosById.keySet();
//            Set<Integer> idsInArray = new HashSet<Integer>();
//            for (int i = 0; i < mSize; i++) {
//                idsInArray.add(mArr.get(i).mNode.getId());
//            }
//            if (!(idsInMap.equals(idsInArray))) {
//                Set idsNotInQueue = CollectionUtils.minus(idsInMap,
//                        idsInArray);
//                throw new RuntimeException(this
//                        + ": noo! Ids in map, but not in queue: "
//                        + idsNotInQueue);
//            }
//
//            Set<Integer> indicesInMap = new HashSet<Integer>(mPosById
//                    .values());
//            Set<Integer> indicesThatShouldBeInMap = new HashSet<Integer>();
//            for (int i = 0; i < mSize; i++) {
//                indicesThatShouldBeInMap.add(i);
//            }
//            if (!(indicesInMap.equals(indicesThatShouldBeInMap))) {
//                throw new RuntimeException(
//                        "indices in map and queue do not match!");
//            }

            throw new RuntimeException("Alles ist lost in Heap; map size is " +
                                       " different from queue size: "
                                       + mPosByObject.size() + "!=" + mArr.size() + "\n");
        }
        
        Logger.info("The heap indeed is in consistent state!");
    }


    public static void main(String[] args) throws Exception {
        List<Pair<String, Integer>> data = IOUtils.readPairs(System.in, null, new StringToIntegerConverter());
        BinaryHeap<String, Integer> heap = new BinaryHeap(Mode.MAX);
        for (Pair<String, Integer> pair: data) {
            heap.add(pair.getObj1(), pair.getObj2());
        }

        heap.ensureConsistentState();
        
        heap.remove("15");
        heap.remove("10");
        heap.remove("5");
        
        heap.ensureConsistentState();
        
        while (heap.size() > 0) {
                                   
            int minKey = heap.topKey();
            String minObj = heap.pop();            
            System.out.println(minObj+","+minKey);                        
        }
        
        heap.ensureConsistentState();

    }

    /** Wrap the objects into entries (sic) */
    private class Entry {
        T mObj;
        K mKey;
        
        private Entry(T pObj,
                      K pKey) {
            mObj = pObj;
            mKey = pKey;
        }
        
        public String toString() {
            return "("+mObj+","+mKey+")";
        }
    }
    
    public enum Mode {
        MAX, 
        MIN;
    }
    
}

