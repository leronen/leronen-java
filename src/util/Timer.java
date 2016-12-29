package util;

import java.io.File;
import java.io.IOException;
import java.util.*;

import util.collections.ArrayStack;
import util.converter.Converter;
import util.converter.NanosToMillisConverter;
import util.dbg.*;

/** 
 * A simple timer, targeted for performance analysis.
 * 
 * Use KEY_TOTAL to time the total execution of a program. 
 * In this case, it is required that KEY_TOTAL is the "top-level" task;
 * doing otherwise is an error. If KEY_TOTAL is used, then all "unnamed" time 
 * is automatically timed, under the key KEY_UNNAMED.
 * It is not necesssary to use KEY_TOTAL; it this case, KEY_UNNAMED is not used.
 * 
 * KEY_UNNAMED is stored completely independently of the hierarchical timing
 * system.
 * 
 */
public class Timer {

    public static final String KEY_TOTAL = "Total";
    public static final String KEY_UNNAMED = "Rest";
    
    private static boolean sTimingEnabled = true;
    
    private static Map<String, Long> mTotalTimes = new LinkedHashMap<String, Long>();    
    private static Map<String, Long> mActiveTasks = new LinkedHashMap<String, Long>();
    
    /** Contains "atomic" task names (not the hierarchical ones) */
    private static ArrayStack<String> sTaskStack = new ArrayStack<String>();
    
    public static boolean outputSecondsInsteadOfMilliseconds;  
    
    public static void clear() {
        mTotalTimes.clear();    
        mActiveTasks.clear();    
        sTaskStack.clear();
    }    
    
    /** If this is called, any subsequent calls will not have any effect */
    public static void disableTiming() {
        sTimingEnabled = false;
    }
    
    private static boolean sHierarchicalTiming = false;
    
    /** 
     * We may want to exclude some classes from timing. Of course, 
     * those classes must then kindly identify themselves for this to work;
     * java does not (at least easily) provide the information about 
     * the caller of a method.
     */
    private static Set<Class> sExcludedClasses = new HashSet<Class>(); 
    
    public synchronized static void setHierarchicalTiming(boolean pFlag) {
        sHierarchicalTiming = pFlag;
    }
    
    public synchronized static void startTiming(String pKey, boolean pLog) {       
        if (!sTimingEnabled) return;        
        startTiming(pKey);
        
        if (pLog == true) {
            Logger.info("Started timing: "+pKey);
        }
        
    }
    
    /** make a composite key for hierarchical logging */
    private synchronized static String makeCompositeKey(String pKey) {
        if (sTaskStack.isEmpty()) {
            return pKey;
        }
        else {
            return StringUtils.collectionToString(sTaskStack, "_")+"_"+pKey;
        }
    }
    
    /**
     * pContext is prepended to pKey, unless hierarchical timing is in use,
     * in which case pContext is ignored.
     */
    public synchronized static void startTiming(String pContext, String pKey) {
        if (sHierarchicalTiming) {
            startTiming(pKey);
        }
        else {
            startTiming(pContext+":"+pKey);
        }
    }
    
   /**
    * pContext is prepended to pKey, unless hierarchical timing is in use,
    * in which case pContext is ignored.
    */
    public synchronized static void endTiming(String pContext, String pKey) {
        if (sHierarchicalTiming) {
            endTiming(pKey);
        }
        else {
            endTiming(pContext+":"+pKey);
        }
    }
    
    public synchronized static void endTiming(String pKey, boolean pLog) {       
        if (!sTimingEnabled) return;        
        endTiming(pKey);
        
        if (pLog == true) {
            Logger.info("Ended timing: "+pKey);
        }
        
    }

    
    public static void startTiming() {
        startTiming(KEY_TOTAL);
    }
    
    public static void endTiming() {
        endTiming(KEY_TOTAL);
    }
    
    public synchronized static void startTiming(String pKey) {        
        if (!sTimingEnabled) return; 
            
//        Logger.info("startTiming: "+pKey);
        
        if (sHierarchicalTiming) {
            String compositeKey = makeCompositeKey(pKey);
            sTaskStack.add(pKey);
            internalStartTiming(compositeKey);
        }
        else {
            // plain unstructured timing
            internalStartTiming(pKey);
        }
    }
    
    /** Does not know anything about hierarchical timing? */ 
    private static void internalStartTiming(String pKey) {
                
//        if (!(pKey == KEY_TOTAL || pKey == KEY_UNNAMED)) {
//            return;
//        }

//        Logger.info("internalStartTiming: "+pKey);
        
        if (mActiveTasks.containsKey(KEY_UNNAMED)) {
            // end timing "unnamed"
            internalEndTiming(KEY_UNNAMED);            
        }
        
        Long prevTotal = mTotalTimes.get(pKey);
        if (prevTotal == null) {
            mTotalTimes.put(pKey, new Long(0));
        }
                        
        if (mActiveTasks.containsKey(pKey)) {
            Logger.warning("Already started timing: "+pKey);
        }
        else {
            // Long now = new Long(System.currentTimeMillis());            
            Long now = new Long(System.nanoTime());
            mActiveTasks.put(pKey, now);
            if (pKey.equals(KEY_TOTAL)) {
                if (mActiveTasks.size() > 1) {
                    throw new RuntimeException("Cannot start timing for \""+KEY_TOTAL+"; there are already active tasks!");
                }
                // start timing "unnamed" as a by-product                
                internalStartTiming(KEY_UNNAMED);                
            }
        }
    }
    
    /** Do not end KEY_TOTAL or KEY_UNNAMED */
    public synchronized static void endTimingForAllTasks() {
        if (!sTimingEnabled) return;
                      
        ArrayList<String> activeTasks = new ArrayList(mActiveTasks.keySet());
        for (String task: activeTasks) {
            if (!(task.equals(KEY_UNNAMED) || task.equals(KEY_TOTAL))) {
                endTiming(task);
            }
        }
    }
    
    public synchronized static void endTiming(String pKey) {
        if (!sTimingEnabled) return;
        
//        Logger.info("endTiming: "+pKey);
                
        if (sHierarchicalTiming) {
            if (sTaskStack.isEmpty()) {
                throw new RuntimeException("Cannot stop timing: no tasks in stack; pKey=="+pKey);
            }
            String tmp = sTaskStack.pop();
            if (!(tmp.equals(pKey))) {
                Logger.warning("Timer.endTiming(): inconsistent keys: param="+pKey+" versus stack="+tmp+"; task stack: "+sTaskStack);
                                
                // try to recover...
                Logger.warning("Timer: Trying recovery...");
                if (sTaskStack.contains(pKey)) {
                    int numTasksToEnd = sTaskStack.size()-sTaskStack.indexOf(pKey);                    
                    Logger.info("Timer: Recovery indeed seems to be possible. We shall do it " +
                    		    "by terminating "+numTasksToEnd+" unterminated tasks");
                    do {
                        Logger.info("Timer: terminating task: "+tmp);
                        // String compositeKey = StringUtils.collectionToString(sTaskStack, "_")+"_"+tmp;
                        String compositeKey = makeCompositeKey(tmp);
                        internalEndTiming(compositeKey);
                        tmp = sTaskStack.pop();
                    } while (!(tmp.equals(pKey)));
                    // OK, now the desired key should be at the top of the task stack
                }
                else {
                    Logger.info("Timer: recovery seems to be impossible.");
                }
                
            }
            // TODO: try to clean the stack to get to a sensible state?
            // String compositeKey = StringUtils.collectionToString(sTaskStack, "_")+"_"+pKey;
            String compositeKey = makeCompositeKey(pKey);
//            Logger.info("endTiming: "+compositeKey);
            internalEndTiming(compositeKey);
        }
        else {
            // plain unstructured timing
            internalEndTiming(pKey);
        }
    }
    
    private static long internalEndTiming(String pKey) {
//        Logger.info("internalEndTiming: "+pKey);
        long now = System.nanoTime();
        long then;
        if (mActiveTasks.containsKey(pKey)) {
            then = mActiveTasks.get(pKey); 
        }
        else {
            // not even started timing for this key;
            // let's arbitratily decide that now is then.
            Logger.info("Not started timing for key: "+pKey);
            then = now;
        }
        
        long elapsed = now-then;
        mActiveTasks.remove(pKey);        

        long prevTotal;
        if (mTotalTimes.containsKey(pKey)) {
            prevTotal = mTotalTimes.get(pKey);            
        }
        else{
            prevTotal = 0;
        }
        long newTotal = prevTotal += elapsed;
        mTotalTimes.put(pKey, new Long(newTotal));
        
        if (mActiveTasks.size() == 1  
            && mActiveTasks.keySet().iterator().next().equals(KEY_TOTAL) 
            && !(pKey.equals(KEY_UNNAMED))            ) {
            // OK, ended a "top-level" task; start timing the unnamed task again...
            internalStartTiming(KEY_UNNAMED);
        }
        
        if (pKey.equals(KEY_TOTAL)) {
            // End the whole timing
            internalEndTiming(KEY_UNNAMED);
        }
        
        return elapsed;
    }
    
    /** Get elapsed time in milliseconds */
    public synchronized static long getTime_millis(String pKey) {
        if (!sTimingEnabled) return 0;
        
        Long val = mTotalTimes.get(pKey);
        if (val == null) {
            return 0;
        }
        else {
            return val/1000000;            
        }
    }
    
    /** Get time in seconds, with a precision of one millis */      
    public synchronized static double getTime_sec(String pKey) {
        if (!sTimingEnabled) return 0;
        
        Long val = mTotalTimes.get(pKey);
        if (val == null) {
            return 0;
        }
        else {
            return ((double)getTime_millis(pKey)) / 1000;            
        }
    }
    
    /** Get elapsed time in nanoseconds */
    public synchronized static long getTime_nanos(String pKey) {
        if (!sTimingEnabled) return 0;
        
        Long val = mTotalTimes.get(pKey);
        if (val == null) {
            return 0;
        }
        else {
            return val;
        }
    }
    
    public synchronized static long getTimeSum() {
        if (!sTimingEnabled) return 0;
    	long sum = 0;
    	for (long l: mTotalTimes.values()) {
    		sum+=l;
    	}
    	return sum;
    }
            
    /** @param pKeyPrefix add this to beginning of each key */
    public synchronized static void logToFile(File pFile, boolean pAppend, String pKeyPrefix) throws IOException {
//        Logger.info("Timer logging to file with ")
        if (!sTimingEnabled) return;
        
        if (!pAppend) {
            IOUtils.clearFile(pFile);
        }
        
        IOUtils.appendToFile(pFile, stringRep(pKeyPrefix)+System.getProperty("line.separator"));        
    }
    
    public synchronized static void logToStdErr() {
        if (!sTimingEnabled) return;
    	System.err.println(stringRep());
    }
    
    public synchronized static void logToLogger() {
        if (!sTimingEnabled) return;
        Logger.info(stringRep());
    }
    
    public synchronized static String stringRep() {
        return stringRep("");
    }
    
    /** 
     * The last line of the rep does not contain a newline. 
     * @param pKeyPrefix add this to beginning of each key */
    public synchronized static String stringRep(String pKeyPrefix) {
        if (!sTimingEnabled) return "Timing not enabled";
                
        if (sHierarchicalTiming) {
            return StringUtils.format(asMillisecondsMap(),
                                           "=",
                                           "\n",
                                           new HierarchicalOutputBeautifyer(pKeyPrefix),
                                           new TimeFormatter());
        }
        else {
            return StringUtils.format(asMillisecondsMap(),
                    "=",
                    "\n",
                    new RegularOutputBeautifyer(pKeyPrefix),
                    new TimeFormatter());
            
        }
                                       
    }   
    
    public static class HierarchicalOutputBeautifyer implements Converter<String,String> {
        
        private final String PREFIX = KEY_TOTAL+"_";
        /** invariant: always non null (null param gets converted to "") */
        private String prefix = null;
        
        private HierarchicalOutputBeautifyer() {
            prefix = "";
        }
        
        private HierarchicalOutputBeautifyer(String prefix) {
            this.prefix = prefix != null ? prefix : ""; 
        }
        
        public String convert(String p) {
            
            if (p.startsWith(PREFIX)) {
                return prefix+p.substring(PREFIX.length());
            }
            else {
                return prefix+p;
            }
           
        }
    }
    
    public static class RegularOutputBeautifyer implements Converter<String,String> {
        
        /** invariant: always non null (null param gets converted to "") */
        private String prefix = null;
        
        private RegularOutputBeautifyer() {
            prefix = "";
        }
        
        private RegularOutputBeautifyer(String prefix) {
            this.prefix = prefix != null ? prefix : ""; 
        }
        
        public String convert(String p) {                       
            return prefix+p;                       
        }
    }
        
    public static class TimeFormatter implements Converter<Long,String> {
        public String convert(Long p) {
            if (outputSecondsInsteadOfMilliseconds) {
                return ""+((double)p.longValue())/1000;
            }
            else {
                return p.toString();
            }                                   
        }
    }
    
    /** 
     * The result is an unmodifiable map. Note that calling this results
     * in a slight overhead, so repetitive calling is not recommended. 
     */
    public synchronized static Map<String, Long> asMillisecondsMap() {
        if (!sTimingEnabled) return Collections.EMPTY_MAP;
    	
    	Map<String, Long> result = new LinkedHashMap(mTotalTimes);
    	if (result.containsKey(KEY_TOTAL)) {
            long unnamedTime = result.get(KEY_UNNAMED);
    	    long totalTime = result.get(KEY_TOTAL);
    		// move unnamed  and total times to the end (presumably using a LinkedHashMap...)
            result.remove(KEY_UNNAMED);
            result.put(KEY_UNNAMED, unnamedTime);
    		result.remove(KEY_TOTAL);
    		result.put(KEY_TOTAL, totalTime);                            		
    	}
        
        // fill in incomplete tasks (log warnings as well)...
        for (String key: mActiveTasks.keySet()) {
            Logger.warning("Task incomplete when retrieving timing info: "+key);
            // long now = System.currentTimeMillis();
            long now = System.nanoTime();
            long then = mActiveTasks.get(key);
            long elapsed = now-then;            
            result.put("Incomplete("+key+")", elapsed);
        }        
                 
        result = ConversionUtils.convertValues(result, 
                                               new NanosToMillisConverter(), 
                                               new LinkedHashMap());
        
        return Collections.unmodifiableMap(result);
    }
   
    public synchronized static void removeKey(String pKey) {
        if (!sTimingEnabled) return;
        mTotalTimes.remove(pKey);
    }
        
    
   // ONLY "excluded class"-specific functionality below...
    public synchronized static void addExcludedClass(Class pClass) {
        if (!sTimingEnabled) return;
        sExcludedClasses.add(pClass);
    }

    public synchronized static void removeExcludedClass(Class pClass) {
        if (!sTimingEnabled) return;
        sExcludedClasses.remove(pClass);       
    }
        
    public synchronized static void startTiming(String pKey, Class pClass) {
        if (!sTimingEnabled) return;
        if (!(sExcludedClasses.contains(pClass))) {
            startTiming((pKey));
        }
    }
    
    public synchronized static void endTiming(String pKey, Class pClass) {
        if (!sTimingEnabled) return;
        if (!(sExcludedClasses.contains(pClass))) {
            endTiming((pKey));
        }
    }

         
    public static void main(String[] args) {
        try {
            // test1();        
//            test2();
            test3();
        }
        catch (InterruptedException e) {
            System.out.println("Interrupted");
            e.printStackTrace();
        }
    }
    
    public static void test1() throws InterruptedException {
        startTiming(KEY_TOTAL);
        startTiming("Foo");
            Thread.sleep(500);
            startTiming("Foo_baz");
                Thread.sleep(500);
                startTiming("Foo_baz_baz");
                    Thread.sleep(500);
                    endTiming("Foo_baz_baz");
                    Thread.sleep(500);
                endTiming("Foo_baz");
        endTiming("Foo");
        Thread.sleep(500);
        startTiming("Bar");
            Thread.sleep(500);
        endTiming("Bar");
        endTiming(KEY_TOTAL);
        logToStdErr();
    }    
    
    public static void test2() throws InterruptedException {
        startTiming(KEY_TOTAL);
        startTiming("Foo");
        Thread.sleep(50);
        endTiming("Foo");
        startTiming("Foo");
        Thread.sleep(50);
        endTiming("Foo");
        startTiming("Foo");
        Thread.sleep(50);
        endTiming("Foo");
        endTiming(KEY_TOTAL);
        
        logToStdErr();
    }
    
    public static void test3() throws InterruptedException {
        setHierarchicalTiming(true);
        startTiming("task1");        
        startTiming("task12");
        startTiming("task123");
        Thread.sleep(50);
        endTiming("task123");
        startTiming("task124");
        Thread.sleep(50);
        endTiming("task12");
        endTiming("task1");                      
        logToStdErr();
    }
    
    
    
}
