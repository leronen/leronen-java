package util.memorytester;

import java.util.*;

public final class MemoryTester {                                       
                                        
    private LinkedList mList = new LinkedList();
    private ObjectFactory mObjectFactory;  
    // private int mNumObjectsAllocated = 0;
    private long mInitialFreeMemory;
    private int mNumIters;
    
    // Unfortunate kludge to prevent totally running out of memory!
    public static final int MEMORY_RESERVE_SIZE = 1024*1024*3;
    public static final int SIZE_OF_NULL = 23; // a magic constant (obtained by storing nulls into a linked list)
    Object mMemoryReserve;              
            
    public MemoryTester() {
        mObjectFactory = new DefaultObjectFactory();
        mNumIters = 3;
    }

    public MemoryTester(ObjectFactory pFactory, int pNumIters) {
        mObjectFactory = pFactory;
        mNumIters = pNumIters;
    }
    
    public void allocateMemoryReserve() {
        mMemoryReserve = new byte[MEMORY_RESERVE_SIZE];
    }
    
    
                            
                           
    public static void main (String[] args) {        
        MemoryTester test = new MemoryTester();
        test.run();
    }
    
    public void run() {
        System.err.println("Initial memory status:");
        reportMemoryStatus();                        
        allocateMemoryReserve();         
        System.err.println("Initial memory status (after allocating a memory reserve of "+MEMORY_RESERVE_SIZE+" bytes:");                           
        reportMemoryStatus();                        
        
        // int numMegsAllocated = 0;
        for (int i=1; i<=mNumIters; i++) {
            System.err.println("\nStarting iteration "+i);
            try {
                allocateMemoryReserve();
                mInitialFreeMemory = Runtime.getRuntime().freeMemory();
                wasteAsMuchMemoryAsPossible();
            }
            catch (OutOfMemoryError e) {
                // this should provide us with some memory to manouver with!
                mMemoryReserve = null;                
                long numCreatedObjects = mList.size(); 
                // System.err.println("Memory exhausted!");                
                System.err.println("Memory exhausted after allocating "+numCreatedObjects+" objects.");                
                System.err.println(""+numCreatedObjects+" objects fit into the memory of "+mInitialFreeMemory);
                System.err.println("Thus an object took on average "+(mInitialFreeMemory/numCreatedObjects)+" memory.");
                System.err.println("After eliminating the space taken by the linked list, an object took on average "+(mInitialFreeMemory/numCreatedObjects-SIZE_OF_NULL)+" memory.");
                // System.err.println("Memory status before freeing list:");                                
                // reportMemoryStatus();                                
                mList = null;
                
                // System.err.println("Memory status after freeing list:");
                // reportMemoryStatus();
                System.gc();
                // System.err.println("Memory status after calling gc():");
                // reportMemoryStatus();            
            }
        }
        System.err.println("\nDone all iterations.");
    }    
    
    private void wasteAsMuchMemoryAsPossible() {     
        System.err.println("Wasting as much memory as possible...");        
        mList = new LinkedList();
        while (true) {            
            mList.add(mObjectFactory.makeObject());            
            // System.err.println("Now we have allocated "+numMegsAllocated+" megabytes.");
        }       
    }
    
    private void reportMemoryStatus() {
        long totalMem = Runtime.getRuntime().totalMemory();
        // long maxMem = Runtime.getRuntime().maxMemory();
        long freeMem = Runtime.getRuntime().freeMemory();        
        long usedMem = totalMem-freeMem;
        // System.err.println("Total memory: "+totalMem);
        // System.err.println("Max memory:   "+maxMem);
        // System.err.println("Free memory:  "+freeMem);
        System.err.println("Used memory:  "+usedMem);                    
    }

    private class DefaultObjectFactory implements ObjectFactory {
        public Object makeObject() {
            return null;
            // return new byte[1000];
            // return new byte[1000000];
        }
    }                            
         
}
