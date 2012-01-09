package util.pool;

import java.util.*;

/** 
 * A pool of Integer objects. 
 * 
 * Motivation: avoids creating multiple instances of the same integer
 * (as Integers are immutable, they may as well be shared! )
 * 
 * Currently does not free objects, so an once-used integer stays in memory for 
 * ever. An alternative could be LRU-caching, or FIFO caching.
 *
 * As this pool is motivated by the need to use small positive integers, 
 * the implementation is such that integers the integers in range 
 * [0, NUM_TO_STORE_IN_ARRAY-1] are stored effeciently in an array, and the rest are
 * stored in an Integer ->  Integer map.
 * 
 *  Hmm, is the same kind of thing already in use bu the java runtime?
 *  I vaguely remember reading somewhere that instances of small Integers are
 *  magically shared, a thing is managed beyond the curtains. OK, 
 *  a small test (implemented in the main method) shows that when ints are
 *  cast to Integers (as in autoboxing), the small numbers (-128 to 127) are 
 *  shared, others are not.
 *  
 */
public final class IntegerPool {
    private static final int NUM_TO_STORE_IN_ARRAY = 1000;    
                    
    private static IntegerPool gSingletonInstance;
    
    private HashMap mHashMap;
    private Integer[] mArray;
    
    private IntegerPool() {
        mHashMap = new HashMap();
        mArray = new Integer[NUM_TO_STORE_IN_ARRAY];
    }
    
    private static IntegerPool getInstance() {
        if (gSingletonInstance == null) {
            gSingletonInstance = new IntegerPool();
        }
        return gSingletonInstance;
    }
        
    private Integer internalGetInteger(int pInt) {
        Integer pooled = null;                
        
        if (pInt >= 0 && pInt < NUM_TO_STORE_IN_ARRAY) {
            // pooledr is in range [0, NUM_TO_STORE_IN_ARRAY]
            pooled = mArray[pInt]; 
            if (pooled==null) {
                pooled = new Integer(pInt);
                mArray[pInt] = pooled;                 
            }                             
        }
        else {
            pooled = (Integer)(mHashMap.get(new Integer(pInt)));
            if (pooled == null) {
                pooled = new Integer(pInt);                
                mHashMap.put(pooled, pooled);
            }
        }            
        return pooled;
    }
    
    public static Integer get(int pInt) {
        return getInstance().internalGetInteger(pInt);                
    }       
    
    public static void main(String[] args) {
        for (int i = -200; i<200; i++) {
            Integer a = i;
            Integer b = i;
            if (a == b) {
                System.out.println(a+"=="+b);
            }
            else {
                System.out.println(a+"!="+b);
            }
                        
            
        }
    }
}
