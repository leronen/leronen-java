package util;

import util.dbg.Logger;

public class MemUtils {

    public static long usedMemory_bytes() { 
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    public static String usedMemory_MB() { 
        return StringUtils.formatFloat(((double)usedMemory_bytes() / 1000000));
    }
    
    public static void log(String key) {
        Logger.info("Used memory after "+key+": "+usedMemory_MB()+" MB");
    }

}
