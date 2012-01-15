package util.dbg;

import util.*;
import util.collections.*;
import util.collections.iterator.ConditionalIterator;
import util.condition.*;

import java.util.*;
import java.io.*;


/** Guru loggeri, kirjoittaa hierarkkisia debuggifaileja */
public final class Logger {
    
    ////////////////////////////////////////////////
    // private implementation starts
    ////////////////////////////////////////////////
    
    /** 
     * This flag is vulgarly public, to enable callers to poll it quickly (so that they do not have 
     * To construct lengthy log strings to be thrown into void!)
     *
     * So, indeed, my friend, if this is false, then even the most pathetic 
     * panic messages are abandoned without mercy.
     **/
    public static boolean loggingEnabled = true;
        
    private static Logger sSingletonInstance;
            
    // private ArrayList mStreamsUsingGlobalLoglevel; // streams with the default log level (mSharedLogLevel)
    private MultiMap mStreamsByLogLevel; // streams for each log level...
    private Map mLogLevelByStream;        
    
    private ArrayList<OutputStream> mStreamsCreatedByUs;
    private ArrayList<File> mFilesCreatedByUs;
    
    private int mCurrentHierarchyLevel;
    
    private static String sProgramName; 
                
    public static final int LOGLEVEL_DBG = 1;
    public static final int LOGLEVEL_INFO = 2;
    public static final int LOGLEVEL_IMPORTANT_INFO = 3;
    public static final int LOGLEVEL_WARNING = 4;
    public static final int LOGLEVEL_ERROR = 5;

    /** Use this when want to use the value in mSharedLogLevel */
    public static final int LOGLEVEL_SHARED = -1;        
    
    public static final int DEFAULT_LOG_LEVEL = LOGLEVEL_INFO;
    
    public static final String DBG_PREFIX = "DBG:";
    // public static final String INFO_PREFIX = "INFO:";
    public static final String WARNING_PREFIX = "WARNING:";
    public static final String ERROR_PREFIX = "ERROR:";
    
    private static List<String> PREFIX_BY_LOG_LEVEL = CollectionUtils.makeList(
            "not_used",
            DBG_PREFIX,
            "",
            WARNING_PREFIX,
            ERROR_PREFIX);
                        
    public static final int[] ALL_LOG_LEVELS = {
        LOGLEVEL_DBG,
        LOGLEVEL_INFO,
        LOGLEVEL_IMPORTANT_INFO,
        LOGLEVEL_WARNING,
        LOGLEVEL_ERROR,
    };

    public static final String PARAM_NAME_HIERARCHICAL_LOGGING = "hierarchical_logging";
    public static final String PARAM_NAME_LOG_LEVEL = "loglevel";                            
    
    private int mSharedLogLevel = DEFAULT_LOG_LEVEL;
    
    private static boolean sUseHierarchicalLogging = false;

    /** keys: warnings, values="does this warning need to be output to the end of the log" */
	private HashMap<String, Boolean> mAlreadyIssuedWarnings;
    private static HashSet<String> sAlreadyIssuedDbgs;
    
    private static Logger getInstance() {
        if (sSingletonInstance == null) {
            sSingletonInstance = new Logger();
        }
        return sSingletonInstance;
    }                                                                    
            
    
    
    public static int getLogLevel() {
        return getInstance().mSharedLogLevel;
    }
    
    public static boolean isDbg() {
        return getInstance().mSharedLogLevel <= Logger.LOGLEVEL_DBG;
    }
    
    private Logger() {
        // mStreamsUsingGlobalLoglevel = new ArrayList();
        mStreamsByLogLevel = new MultiMap();
        mLogLevelByStream = new HashMap();
        internalAddStream(System.err, LOGLEVEL_SHARED);                
        mStreamsCreatedByUs = new ArrayList<OutputStream>();
        mFilesCreatedByUs = new ArrayList<File>();
        String dateString = DateUtils.formatDate();
        mAlreadyIssuedWarnings = new HashMap();
        sAlreadyIssuedDbgs =  new HashSet<String>();
        if (sProgramName == null) {
            if (sUseHierarchicalLogging) {        
                internalStartSubSection("Starting at "+dateString+"...", LOGLEVEL_IMPORTANT_INFO);
            }
        }
        else {            
            internalStartSubSection("Starting "+sProgramName+" at "+dateString+"...", LOGLEVEL_IMPORTANT_INFO);
        }
    }                
    
    private Iterator getStreamsForLogLevel(int pMsgLevel) {
        Condition c = new StreamForLogLevelCondition(pMsgLevel);
        return new ConditionalIterator(mStreamsByLogLevel.values().iterator(), c);             
    }
    
    /** Print to all streams??? */
    private void internalPrintStackTrace(Exception e) {
        Iterator allStreams = mStreamsByLogLevel.values().iterator();
        while(allStreams.hasNext()) {
            PrintStream ps = (PrintStream)allStreams.next();
            e.printStackTrace(ps);
         }                
    }
    
    public static void setLogLevel(int pLevel) {        
        getInstance().mSharedLogLevel=pLevel;
        System.err.println("Log level set to: "+pLevel);
    }
    
    private void internalEndLog()  {
        // first, output all delayed warnings...
        
        boolean firstDelayedWarningOutputted = false;
        
        for (String warning: getInstance().mAlreadyIssuedWarnings.keySet()) {
            if (mAlreadyIssuedWarnings.get(warning)) {
                // delayed, need to output now
                if (!firstDelayedWarningOutputted) {
                    // write separator
                    Logger.importantInfo("");
                }
                Logger.warning(warning);
                firstDelayedWarningOutputted = true;
            }
        }
        
        if (sUseHierarchicalLogging) {
            internalEndSection(1, LOGLEVEL_IMPORTANT_INFO);
        }
        
        for (OutputStream os: mStreamsCreatedByUs) {
        	try {
        		os.close();
        	}
        	catch (Exception e) {
        		System.err.println("Failed closing stream: "+e);
        	}
        }
        for (File f: mFilesCreatedByUs) {
        	// System.err.println("File: "+f+", length:"+f.length());
        	if (f.length() == 0) {        		
        		f.delete();
        	}
        }
    }
    
    public static void endLog() {
        if (sSingletonInstance != null) {
            sSingletonInstance.internalEndLog();
        }
    }
        
    private void internalAddStream(PrintStream pStream, int pLogLevel) {        
        mStreamsByLogLevel.put(new Integer(pLogLevel), pStream);                    
        mLogLevelByStream.put(pStream, new Integer(pLogLevel));
    }
    
    private void internalAddStream(String pFileName, int pLogLevel) throws IOException {        
        FileOutputStream fileStream = new FileOutputStream(pFileName);
        PrintStream printStream = new PrintStream(fileStream);
        internalAddStream(printStream, pLogLevel);        
        mStreamsCreatedByUs.add(printStream);
        mStreamsCreatedByUs.add(fileStream);
        mFilesCreatedByUs.add(new File(pFileName));
    }
                    
    private void internalEndSection(int pLevel, int pLogLevel) {
        internalEndSection(pLevel, "", pLogLevel);        
    }
    
    public static void setHierarchicalLogging(boolean pVal) {        
        if (pVal == true) {
            System.err.println("Enabling hierarchical logging...");
        }
        else {
            System.err.println("Disabling hierarchical logging...");
        }
        sUseHierarchicalLogging = pVal;        
    }
    
    private void internalStartSection(int pHierarchyLevel, String pMsg, int pLogLevel) {
        if (sUseHierarchicalLogging) {
            internalPrintMsg(Log.SECTION_START+pHierarchyLevel+Log.SEPARATOR+pMsg, pLogLevel);
            if (mSharedLogLevel <= pLogLevel) {
                mCurrentHierarchyLevel = pHierarchyLevel;
            }
        }
        else {
            // OK, no hierarchical logging... lets display the message anyway...
            internalPrintMsg(pMsg, pLogLevel);
        }
    }
    
    /** start new section on current level (terminating the old section) */
    private void internalStartSection(String pMsg, int pLogLevel) {
        if (sUseHierarchicalLogging) {        
            internalPrintMsg(Log.SECTION_START+mCurrentHierarchyLevel+Log.SEPARATOR+pMsg, pLogLevel);
        }
        else {
            // OK, no hierarchical logging... lets display the message anyway...
            internalPrintMsg(pMsg, pLogLevel);   
        }
    }
    
    /** start new section on current level (terminating the old section) */
    private void internalStartSubSection(String pMsg, int pLogLevel) {
        if (sUseHierarchicalLogging) {
            if (mSharedLogLevel <= pLogLevel) {
                mCurrentHierarchyLevel = mCurrentHierarchyLevel+1;
            }
            internalPrintMsg(Log.SECTION_START+mCurrentHierarchyLevel+Log.SEPARATOR+pMsg, pLogLevel);
        }
        else {
            internalPrintMsg(pMsg, pLogLevel);
        }
    }        
    
    /** start new section on current level (terminating the old section) */
    private void internalEndSubSection(String pMsg, int pLogLevel) {
        if (sUseHierarchicalLogging) {
            internalPrintMsg(Log.SECTION_END+mCurrentHierarchyLevel+Log.SEPARATOR+pMsg, pLogLevel);
            if (mSharedLogLevel <= pLogLevel) {
                mCurrentHierarchyLevel--;
            }
        }
        else {
            // internalPrintMsg(pMsg, pLogLevel);
        }                 
    }
    
    /** start new section on current level (terminating the old section) */
    private void internalEndSubSection(int pLogLevel) {
        if (sUseHierarchicalLogging) {
            internalPrintMsg(Log.SECTION_END+mCurrentHierarchyLevel+Log.SEPARATOR, pLogLevel);
            if (mSharedLogLevel <= pLogLevel) {
                mCurrentHierarchyLevel--;
            }                
        }
        else {
            // no action needed
        }
    }    
            
    private void internalEndSection(int pHierarchyLevel, String pMsg, int pLogLevel) {
        if (sUseHierarchicalLogging) {
            internalPrintMsg(Log.SECTION_END+pHierarchyLevel+Log.SEPARATOR+pMsg, pLogLevel);
            if (mSharedLogLevel <= pLogLevel) {
                mCurrentHierarchyLevel = pHierarchyLevel-1;
            }                
        }
        else {
            internalPrintMsg(pMsg, pLogLevel);    
        }
    }            
        
    private void internalPrintMsg(String pMsg, int pLogLevel) {
        // TODO: should have a log level for each stream...
        Iterator streams = getStreamsForLogLevel(pLogLevel);
        while(streams.hasNext()) {
            PrintStream ps = (PrintStream)streams.next();
            ps.println(pMsg);
         }                
    }

    private void internalPrintMsg_cr(String pMsg, int pLogLevel) {        
        Iterator streams = getStreamsForLogLevel(pLogLevel);
        while(streams.hasNext()) {
            PrintStream ps = (PrintStream)streams.next();
            ps.print(pMsg+"               \r");            
         }         
    }                
    //////////////////////////////////////////
    // private internal implementation ends
    //////////////////////////////////////////
            
                        
    //////////////////////////////////////////
    // public (static!) interface starts
    //////////////////////////////////////////
    public static void dbgInSubSection(String pSectionName, String pText) {
        startSubSection(pSectionName);
        dbg(pText);
        endSubSection(pSectionName);
    }        
    
    public static void enableLogging(boolean pEnabled) {
        loggingEnabled = pEnabled;
    }
    
    public static void enableLogging() {
        loggingEnabled = true;
    }
    
    public static void disableLogging() {
        loggingEnabled = false;
    }        
    
    public static void addStream(PrintStream pStream, int pLogLevel) {        
        getInstance().internalAddStream(pStream, pLogLevel);       
    }
            
    public static void addStream(String pFileName, int pLogLevel) throws IOException {        
        getInstance().internalAddStream(pFileName, pLogLevel);        
    }
                
    public static void startSection(int pLevel) {
        if (loggingEnabled) {            
            getInstance().internalStartSection(pLevel, "", LOGLEVEL_INFO);
        }
    }
    
    public static void startSection(int pLevel, int pLogLevel) {
        if (loggingEnabled) {            
            getInstance().internalStartSection(pLevel, "", pLogLevel);
        }
    }
            
    public static void endSection(int pLevel) {
        if (loggingEnabled) {
            getInstance().internalEndSection(pLevel, "", LOGLEVEL_INFO);
        }
    }
    
    public static void startSection(int pLevel, String pMsg) {
        if (loggingEnabled) {
            getInstance().internalStartSection(pLevel, pMsg, LOGLEVEL_INFO);
        }
    }
    
    /** start new section on current level (terminating the old section), with log level "dbg" **/
    public static void startSubSection(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalStartSubSection(pMsg, LOGLEVEL_INFO);
        }
    }
    
    /** start new section on current level (terminating the old section) */
    public static void startSubSection(String pMsg, int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalStartSubSection(pMsg, pLogLevel);
        }
    }
    
    
    /** start new section on current level (terminating the old section), with log level "dbg" **/
    public static void startSection(String pMsg) {
        if (loggingEnabled) {        
            getInstance().internalStartSection(pMsg, LOGLEVEL_INFO);
        }
    }
    
    /** start new section on current level (terminating the old section) */
    public static void startSection(String pMsg, int pLogLevel) {
        if (loggingEnabled) {        
            getInstance().internalStartSection(pMsg, pLogLevel);
        }
    }
    
    /** start new section on current level (terminating the old section) */
    public static void endSubSection(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalEndSubSection(pMsg, LOGLEVEL_INFO);
        }                
    }
    
    /** start new section on current level (terminating the old section) */
    public static void endSubSection(String pMsg, int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalEndSubSection(pMsg, pLogLevel);
        }                
    }    
        
    /** start new section on current level (terminating the old section) */
    public static void endSubSection(int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalEndSubSection(pLogLevel);
        }
    }
    
    public static void endSubSection() {
        if (loggingEnabled) {
            getInstance().internalEndSubSection("", LOGLEVEL_INFO);
        }
    }    
                    
            
    public static void endSection(int pLevel, String pMsg) {
        if (loggingEnabled) {
            getInstance().internalEndSection(pLevel, pMsg, LOGLEVEL_INFO);
        }
    }

    public static void endSection(int pLevel, String pMsg, int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalEndSection(pLevel, pMsg, pLogLevel);
        }
    }            
        
    public static void dbg(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(DBG_PREFIX+pMsg, LOGLEVEL_DBG);
        }
    }
    
    public static void log(String pMsg, int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(PREFIX_BY_LOG_LEVEL.get(pLogLevel)+pMsg, pLogLevel);
        }
    }
    
    public static void print(String pMsg, int pLogLevel) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(pMsg, pLogLevel);
        }
    }    
    
    
    public static void info(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(pMsg, LOGLEVEL_INFO);
        }
    }
    
    /** Write pObj.toString to a separate line */
    public static void info(String pMsg, Object pObj ) {
        info(pMsg+"\n"+pObj);        
    }
    
    public static void importantInfo(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(pMsg, LOGLEVEL_IMPORTANT_INFO);
        }
    }
    
    
    /** @param pCR carriage return instead of line feed */
    public static void info(String pMsg, boolean pCR) {
        if (loggingEnabled) {
            if (pCR) {
                getInstance().internalPrintMsg_cr(pMsg, LOGLEVEL_INFO);
            }
            else {
                getInstance().internalPrintMsg(pMsg, LOGLEVEL_INFO);
            }
        }
    }
    
    /** info with carriage return (WTF?!?) */
    public static void info_cr(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg_cr(pMsg, LOGLEVEL_INFO);
        }
    }    
    
    
    public static void loudWarning(String pMsg) {
        warning("*****************************************************************");
        warning(pMsg);
        warning("*****************************************************************");
    }
    
    public static void loudInfo(String pMsg) {
        info("*****************************************************************");
        info(pMsg);        
    }
    
    public static void warning(String pMsg) {
        if (loggingEnabled) {
            getInstance().internalPrintMsg(WARNING_PREFIX+pMsg, LOGLEVEL_WARNING);
        }
    }
    
    public static void uniqueWarning(String pMsg) {
        uniqueWarning(pMsg, false);
    }
    
    /** @param pDelayed if true, only output to the end of the log */
    public static void uniqueWarning(String pMsg, boolean pDelayed) {
        if (loggingEnabled) {
        	if (!(getInstance().mAlreadyIssuedWarnings.containsKey(pMsg))) {
                if (!pDelayed) {
                    getInstance().internalPrintMsg(WARNING_PREFIX+pMsg, LOGLEVEL_WARNING);
                }        		
        		getInstance().mAlreadyIssuedWarnings.put(pMsg, pDelayed);
        	}
        	else {
        		// let us not be repetitive
        	}        	
        }
    }
    
    public static void uniqueDbg(String pMsg) {
        if (loggingEnabled) {
            if (!(sAlreadyIssuedDbgs.contains(pMsg))) {
                getInstance().internalPrintMsg(DBG_PREFIX+pMsg, LOGLEVEL_DBG);
                sAlreadyIssuedDbgs.add(pMsg);
            }
            else {
                // let us not be repetitive
            }           
        }
    }

    public static void warning(String pMsg, Exception pEx) {
        getInstance().internalPrintMsg(WARNING_PREFIX+pMsg, LOGLEVEL_WARNING);
        getInstance().internalPrintMsg(WARNING_PREFIX+pEx.getMessage(), LOGLEVEL_WARNING);            
    }                
    
    public static void error(String pMsg) {
        getInstance().internalPrintMsg(ERROR_PREFIX+pMsg, LOGLEVEL_ERROR);            
    }
    
    /** Error is allowed to be null */
    public static void error(String pMsg, Exception pEx) {
        getInstance().internalPrintMsg(ERROR_PREFIX+pMsg, LOGLEVEL_ERROR);
        if (pEx != null) {
            String exMsg = ExceptionUtils.format(pEx, "; "); 
            getInstance().internalPrintMsg(ERROR_PREFIX+exMsg, LOGLEVEL_ERROR);
        }
    }
    
    public static void error(Exception pEx) {        
    	getInstance().internalPrintMsg(""+StringUtils.arrayToString(pEx.getStackTrace()), LOGLEVEL_ERROR);
    	String exMsg = ExceptionUtils.format(pEx, "; ");
        getInstance().internalPrintMsg(ERROR_PREFIX+exMsg, LOGLEVEL_ERROR);            
    }
    
    public static void setProgramName(String pName) {
        sProgramName = pName;        
    }
    
    public static String getProgramName() {
        return sProgramName;
    }
    
    public static void printStackTrace(Exception e) {
        getInstance().internalPrintStackTrace(e);
    }
    
    public static void main (String[] args) {
        dbg("testing");
        // Logger anotherLogger = new Logger();
    }

    
    
    public static String logLevelString(String pSeparator) {
        return "1 - DBG; all log messages"+pSeparator+
               "2 - INFO; all but dbg messages"+pSeparator+
               "3 - IMPORTANT_INFO; only important info, warnings and errors"+pSeparator+
               "4 - WARNING; only warnings and errors"+pSeparator+
               "5 - ERROR only errors";
    }
    
    /**
     * Some standard logging configuration done here. TODO: dedicated 
     * argdef for these params as well!
     */
    public static void configureLogging(CmdLineArgs args) throws IOException {
        
        if (!(args.isDefined("nowarningfile"))) {
            String warningFile = args.getOpt("warningfile");
            Logger.addStream(warningFile, Logger.LOGLEVEL_WARNING);
        }

        if (args.isDefined("logfile")) {
            String logFile = args.getOpt("logfile");
            int logLevel = args.getIntOpt(Logger.PARAM_NAME_LOG_LEVEL);
            Logger.addStream(logFile, logLevel);
        }
        
        if (args.isDefined("log_mem_usage")) {
            MemLogger.initialize(true);
        }
    }

    private class StreamForLogLevelCondition implements Condition {
        private int mMsgLogLevel;
        
        StreamForLogLevelCondition(int pMsgLogLevel) {
            mMsgLogLevel = pMsgLogLevel;
        }
        
        public boolean fulfills(Object p) {
            PrintStream ps = (PrintStream)p;
            int streamLogLevel = ((Integer)mLogLevelByStream.get(ps)).intValue();
            if (streamLogLevel == LOGLEVEL_SHARED) {
                streamLogLevel = mSharedLogLevel;
            }
            return streamLogLevel <= mMsgLogLevel; 
        }
    }    

    /** 
     * Wraps this trad. leronen Logger to clients using the more lightweight, 
     * if less fancy, ILogger interface.
     */
    public static class ILoggerAdapter implements ILogger {
                      
        private String prefix;
        private StringGenerator dateGenerator;
        private StringBuffer buf = new StringBuffer();
        
        public ILoggerAdapter() {
            this(null, null);
        }
        
        public ILoggerAdapter(String prefix) {
            this(prefix, null);
        }
        
        public ILoggerAdapter(String prefix, StringGenerator dateGenerator) {
            this.prefix = prefix;
            this.dateGenerator = dateGenerator;
        }
                
        private String generateMsg(String msg) {
            buf.setLength(0);            
            if (dateGenerator != null) {
                buf.append(dateGenerator.generate());
                buf.append(' ');
            }
            if (prefix != null) {
                buf.append(prefix);                
            }
            buf.append(msg);
            return buf.toString();
        }
        
        @Override
        public void info(String msg) {
            Logger.info(generateMsg(msg));        
        }
        
        @Override
        public void warning(String msg) {
            Logger.warning(generateMsg(msg));        
        }
        
        /** Report error and exit with exit code 1. TODO: proper logging */
        @Override
        public void error(String msg) {
            Logger.error(generateMsg(msg));        
        }
        
        @Override
        public void error(String msg, Exception e) {
            Logger.error(generateMsg(msg), e);        
        }

        @Override
        public void closeStreams() {
            Logger.endLog();
        }

        @Override
        public void error(Exception e) {
            String msg = generateMsg("");
            if (msg.length() > 0) {
                Logger.error(msg, e);
            }
            else {
                Logger.error(e);
            }
        }       
    }
    
    
}


