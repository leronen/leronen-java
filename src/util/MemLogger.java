package util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.Notification;
import javax.management.NotificationListener;

import util.dbg.Logger;

public class MemLogger implements NotificationListener {
    
    private static MemLogger sInstance;
    
    private MemoryMXBean mMMXBean;
    
    public MemLogger() {
        mMMXBean = ManagementFactory.getMemoryMXBean();
        Logger.info("The mmmx bean: "+mMMXBean);        
//        mMMXBean.setVerbose(true);

        
        
        //        NotificationEmitter ne = (NotificationEmitter)mMMXBean;
//        ne.addNotificationListener(this, null, null);
        
//        for (MemoryManagerMXBean b: ManagementFactory.getMemoryManagerMXBeans()) {
//            Logger.info("A mmmx bean: "+b.getName());
//            Logger.info("Memory pools for bean: "+StringUtils.arrayToString(b.getMemoryPoolNames(), " "));
//        }
        
        
    }
    
    public void handleNotification(Notification notification, Object handback) {
        Logger.info("Got nofification: "+notification);
        
    }
    
    public static boolean inUse() {
        return sInstance != null;
    }
    
    /** unless this is called first, any other calls shall have no effect */
    public static void initialize(boolean pVerbose) {
        Logger.info("Initializing memory usage logging...");
        if (sInstance == null) {
            sInstance = new MemLogger();
        }
        else {
            Logger.warning("MemLogger was already initialized, just setting verbosity flag...");
        }
        sInstance.mMMXBean.setVerbose(pVerbose) ;
    }
    
    public static void disable() {
        if (sInstance != null) {
            Logger.info("Disabling mem logging");
            sInstance.mMMXBean.setVerbose(false);
            sInstance = null;
        }
    }
    
                
    /** no-op if initialize() has not been called first */
    public static void logUsage() {
        if (sInstance != null) {
            sInstance.internalLogUsage();
        }
    }
    
    public static long memUsage() {
        sInstance.mMMXBean.gc();
        return sInstance.mMMXBean.getHeapMemoryUsage().getUsed();
    }
    
    private void internalLogUsage() {
        mMMXBean.gc();
        Logger.info("mem usage: "+mMMXBean.getHeapMemoryUsage());
    }
    
}
