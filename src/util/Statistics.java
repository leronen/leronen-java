package util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import util.converter.NumberBeautifyingConverter;
import util.dbg.Logger;

/** keywords: statisticmanager, statistic manager */
public class Statistics extends LinkedHashMap<String, Object> {        
        
    /**
	 * 
	 */
	private static final long serialVersionUID = -2741946785926163066L;

	public Statistics() {
        super();
    }

    /** read from file (only supports (long) integer values at the moment) */
    public Statistics(File f) throws IOException {
        super();
        List<String> lines = IOUtils.readLines(f);
        for (String line: lines) {
            String[] tok = line.split("=");
            String key = tok[0];
            long val = Long.parseLong(tok[1]);
            setOrIncreaseLongValue(key,val);
        }
    }
    
    public void put(String pKey, Object pVal, boolean pLog) {
        put(pKey, pVal);
        if (pLog) {
            Logger.info(pKey+"="+pVal);
        }
    }
    
    public void setOrIncreaseLongValue(String pKey, long pValue) {
        if (containsKey(pKey)) {
            Long oldVal = (Long)get(pKey);
            long newVal = oldVal + pValue;
            put(pKey, newVal);
        }
        else {
            put(pKey, pValue);
        }
    }
    
    public void setLongValue(String pKey, long pValue) {        
        put(pKey, pValue);        
    }
    
    public long getLong(String key) {
        return (Long)get(key);
    }    
    
    public void setDoubleValue(String pKey, double pValue) {        
        put(pKey, pValue);        
    }              
    
    public void setInteger(String pKey, int pValue) {        
        put(pKey, pValue);
    }

    
    public void setString(String pKey, String pValue) {
        put(pKey, pValue);
    }
            
    public void logToLogger(int pLogLevel) {
        if (pLogLevel == 0) {
            // no action
        }
        else if (pLogLevel == 1) {
            Logger.dbg(toString());
        }
        else if (pLogLevel == 2) {
            Logger.info(toString());
        }
        else if (pLogLevel == 3) {
            Logger.importantInfo(toString());
        }
    }
    
    public void logToLogger() {
        Logger.info(toString());
    }
    
    public void logToStdErr() {
        System.err.println(toString());
    }
    
    public String toString() {
        return StringUtils.mapToString(new TreeMap(this), "=", 
                                       System.getProperty("line.separator"), null,
                                       new NumberBeautifyingConverter(5));
                                       
    }
    
    /** Overwrite. */
    public void writeToFile(File pFile) throws IOException {
        IOUtils.clearFile(pFile);                   
        IOUtils.appendToFile(pFile, 
                             toString());        
    }
    
    
    /** sum statistics from a number of individual statistics files */
    public static Statistics sumStatistics(String... inputFiles) throws IOException {
        Statistics totalStats = new Statistics();
        for (String file: inputFiles) {
            Statistics stats = new Statistics(new File(file));
            for(String key: stats.keySet()) {
                long val = stats.getLong(key);
                totalStats.setOrIncreaseLongValue(key, val);
            }
        }
        return totalStats;
    }
    
    public static void main(String[] args) throws Exception {
        Statistics sums = sumStatistics(args);
        System.out.println(sums.toString());
    }

}
