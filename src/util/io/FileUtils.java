package util.io;

import util.*;
import util.IOUtils.LineIterator;
import util.dbg.*;
import util.collections.*;
import util.collections.tree.*;
import util.condition.*;
import util.converter.*;
import util.math.*;
import util.matrix.*;


import java.util.*;
import java.util.regex.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.channels.*;


/**
 * Instead of representing sensible file utils, this has begun to resemble something of a bulvane 
 * to Matrixes stored in a file...
 */
public final class FileUtils {
    
    public static final String CMD_SYNC = "sync";
    public static final String CMD_REALPATH = "realpath";
    public static final String CMD_MAX = "max";    
    public static final String CMD_REARRANGECOLS = "rearrangecols";
    public static final String CMD_JACCARDDISTANCE = "jaccarddistance";
    public static final String CMD_REMOVEFIRSTROWS = "removefirstrows";
    public static final String CMD_REMOVELASTROWS = "removelastrows";
    public static final String CMD_REMOVEDUPLICATEROWS = "removeduplicaterows";
    public static final String CMD_COMBINE_WEIGHTED_GOODNESSES = "combine_weighted_goodnesses";
    public static final String CMD_REMOVE_LONGEST_COMMON_PREFIX = "removelongestcommonprefix";
    public static final String CMD_REMOVE_LONGEST_COMMON_SUFFIX = "removelongestcommonsuffix";
    public static final String CMD_LONGEST_COMMON_SUFFIX = "longestcommonsuffix";
    public static final String CMD_COUNTROWS = "countrows";
    public static final String CMD_CAT = "cat";
    public static final String CMD_COMPARE_SETS = "compare-sets";
    public static final String CMD_DB2_POSTPROCESS = "db2-postprocess";
    /** copy stdin to stdout, log count of lines in the process */      
    public static final String CMD_PIPE_AND_COUNT = "pipe_and_count";
//    public static final String CMD_COUNTROWS2 = "countrows2";
    public static final String CMD_REMOVEROWS = "removerows";
    public static final String CMD_REMOVEFIRSTCOLS = "removefirstcols";
    public static final String CMD_REMOVELASTCOLS = "removelastcols";
    public static final String CMD_PREFIXES = "prefixes";
    /** Sort cols of of each row separately */
    public static final String CMD_SORT_ROWS_ROWWISE = "sortcols_rowwise";
    public static final String CMD_COUNTCOLS = "countcols";
    public static final String CMD_REMOVECOLS = "removecols";
    public static final String CMD_SET_CLIPBOARD_CONTENTS = "set_clipboard_contents";
    public static final String CMD_ENSURECONTAINSLINE = "ensurecontainsline";
    public static final String CMD_REMOVEROWSWITHZEROVALUEINCOLUMN = "removerowswithzerovalueincolumn";
    public static final String CMD_INSERT_LINE_INTO_FILE = "insert_line_into_file";
    public static final String CMD_CONVERTCOL = "convertcol";
    public static final String CMD_MAKE_PLOT = "makeplot"; // plot 2 values from a set of "summary" files ($1 as x, $2 as y)
    public static final String FORMAT_TABDELIMITED = "format_csv_file"; // tääl
    
    
    /**
     * Convert a col $2 of a file $1 in-place, according to a mapping specified 
     * in file $3, as <key> <value> pairs (separated by white space). 
     */
    public static final String CMD_CONVERTCOL_MAPPINGFILE = "convertcol_mappingfile";
    public static final String CMD_CONVERT_ALL_TOKENS_MAPPINGFILE = "convert_all_tokens";
    /** only preserve certain keys of a map file (with lines like "foo=bar"), given in another file. Edit happens inplace. */
    public static final String CMD_PRUNE_MAP = "prunemap";
    public static final String CMD_IS_SUBMAP = "is-submap";
    /** if FILE1 a sublist of FILE2 */
    public static final String CMD_IS_SUBLIST = "is-sublist";
    /** only preserve the smallest p val for each key. input: file with 2 cols: $1=key, $2=p-val */
    public static final String CMD_PRUNE_P_VALS = "prune_p_vals";
    public static final String CMD_APPEND_COLS = "appendcols";
    public static final String CMD_APPEND_COL = "appendcol";
    public static final String CMD_FIRST_COL = "firstcol";
    public static final String CMD_DUPLICATE_ROWS = "duplicaterows";
    public static final String CMD_ONES = "ones";
    public static final String CMD_PASTE = "paste";
    public static final String CMD_SELECT_MAX_ROWS = "selectmaxrows";
    // check whether numbers in input (single col) are ordered 
    public static final String CMD_IS_ORDERED = "is_ordered";
    public static final String CMD_FIND_GAPS = "find_gaps";
    public static final String CMD_FIND = "find";
    public static final String CMD_SELECT = "select";
    // select a subset of rows where value of a given col is in a subset specified in another file
    public static final String CMD_SELECT_BY_ID_LIST = "select_by_id_list";
    public static final String CMD_SELECT_BY_ID_LIST_2KEY = "select_by_id_list_2key";
    public static final String CMD_REPLACE_IN_FILE = "replaceinfile";
    public static final String CMD_REPLACE_LINE = "replaceline";
    public static final String CMD_REPLACE_IN_FILES = "replaceinfiles";
    public static final String CMD_REPLACE_IN_DIR = "replaceindir";
    public static final String CMD_UNION = "union";
    public static final String CMD_MULTIDIFF = "multidiff";
    public static final String CMD_UNIQ = "uniq";   
    public static final String CMD_FAST_UNIQ = "fastuniq"; // only works for "tight" integers
    public static final String CMD_SIZE = "size";
    public static final String CMD_INTERSECTION = "intersection";
    public static final String CMD_INTERSECTION_NEW = "intersection_new";
    public static final String CMD_MAKE_ORDERED_PAIRS = "makeorderedpairs";
    public static final String CMD_MAKE_UNORDERED_PAIRS = "makeunorderedpairs";
    public static final String CMD_JOIN = "join";
    public static final String CMD_JOIN_TABDELIMITED = "join_tabdelimited";
    public static final String CMD_JOIN_PAIRS = "join_pairs";
    public static final String CMD_SPLIT_TO_BATCHES = "split_to_batches";
    public static final String CMD_ENSURE_INTERSECTION_IS_CANONICALLY_ORDERED = "ensureintersectioniscanonicallyordered";
    public static final String CMD_MINUS = "minus";
    public static final String CMD_PICK_BALANCED_SET = "pick_balanced_set";
    public static final String CMD_MINUS2 = "minus2";
    public static final String CMD_SYM_DIFF = "symdiff";
    public static final String CMD_NO_OP = "no_op";
    public static final String CMD_NO_OP2 = "no_op2";
    public static final String CMD_DIFF = "diff";
    public static final String CMD_COLSUMS = "colsums";
    public static final String CMD_COLAVERAGES = "colaverages";
    public static final String CMD_RELATIVE_PATH = "relativepath";
    public static final String CMD_PRETTIFYCOLS = "prettifycols";
    public static final String CMD_LAST_MODIFIED = "lastmodified";    
           
    // these are tasteless:
    public static final String CMD_BINARY_OPERATOR = "binaryoperator";
    public static final String CMD_UNARY_OPERATOR = "unaryoperator";
    
    
//    public static final String[] ALL_FILEUTILS_COMMANDS = {    
//        CMD_REARRANGECOLS,
//        CMD_REMOVEFIRSTROWS,
//        CMD_REMOVELASTROWS,
//        CMD_REMOVEDUPLICATEROWS,        
//        CMD_COUNTROWS,
//        CMD_REMOVEROWS,
//        CMD_REMOVEFIRSTCOLS,
//        CMD_REMOVELASTCOLS,
//        CMD_COUNTCOLS,
//        CMD_REMOVECOLS,
//        CMD_ENSURECONTAINSLINE,
//        CMD_REMOVEROWSWITHZEROVALUEINCOLUMN,
//        CMD_INSERT_LINE_INTO_FILE,
//        CMD_CONVERTCOL,
//        CMD_APPEND_COLS,
//        CMD_APPEND_COL,
//        CMD_DUPLICATE_ROWS,
//        CMD_ONES,
//        CMD_PASTE,
//        CMD_SELECT_MAX_ROWS,
//        CMD_REPLACE_IN_FILE,
//        CMD_REPLACE_IN_FILES,
//        CMD_REPLACE_LINE,            
//        CMD_BINARY_OPERATOR,
//        CMD_UNARY_OPERATOR,
//        CMD_SELECT,
//        CMD_NO_OP,
//        CMD_NO_OP2,
//        CMD_COLSUMS,
//        CMD_RELATIVE_PATH
//    };
    
    public static boolean isDescendant(File pPotentialDescendant, File pAncestorDir) throws IOException {
        // TODO: more efficient impl.
        return TreeUtils.isDescendant(pPotentialDescendant.getCanonicalFile(), pAncestorDir.getCanonicalFile(), new FileNodeAdapter());          
    }

    /** Gets the path of pFile, relative to pAncestorDir. Return empty string if not a descendant. */
    public static String getPathRelativeTo(File pFile, File pAncestorDir) throws IOException {
        if (!isDescendant(pFile, pAncestorDir)) {
            // sanity check
            return "";
            // throw new RuntimeException("File "+pFile+" is not a descendant of dir "+pAncestorDir);
        }
        
        File canonicalFile = pFile.getCanonicalFile();
        File canonicalDir = pAncestorDir.getCanonicalFile();
        
        // dbgMsg("canonicalFile: "+canonicalFile);
        // dbgMsg("canonicalDir: "+canonicalDir);
        
        List pathList = TreeUtils.getPathToAncestor(canonicalFile, canonicalDir, new FileNodeAdapter());
        
        StringBuffer result = new StringBuffer();
        for (int i=pathList.size()-1; i>=0; i--) {            
            File component = (File)pathList.get(i);
            result.append(component.getName());
            if (i>0) {
                result.append(File.separator);
            }
        }
                
        return result.toString();                               
    }
    
    public static File[] filesInDir(File pDir, boolean pRecursive) {
        return filesInDir(pDir, pRecursive, null);    
    }
    
    /** Get the list of files under a directory. */
    public static File[] filesInDir(File pDir, boolean pRecursive, String pFilenNameRegex) {
        List result = new ArrayList();
        
        if (pRecursive) {
            // files in this dir and all sub dirs
            result.addAll(internalFilesInDir(pDir));
            File[] allDirs = subDirs(pDir, true);            
            for(int i=0; i<allDirs.length; i++) {
                result.addAll(internalFilesInDir(allDirs[i]));
            }
        }
        else {
            // just files in this dir
            result.addAll(internalFilesInDir(pDir));
        }
        
        if (pFilenNameRegex != null) {
            // require that the file names match a regex
            ConvertedObjectCondition condition =
                new ConvertedObjectCondition(new FileToFileNameConverter(), new MatchesRegexCondition(pFilenNameRegex));
            result = (List)CollectionUtils.extractMatchingObjects(result, condition);
        }
                
        return (File[])ConversionUtils.collectionToArray(result, File.class);
    }
    
    /**
     * Copy with a number of retries to avoid failed copies resulting 
     * from file system (read: NFS) synchronization problems or whatever java
     * horrors...
     * 
     * Success is in practice checked by comparing the sizes of the src and dst
     * files.
     * 
     * Uses channels, which presumably amounts to an efficient implementation...
     * 
     */
    public static void copy(File pSrcFile, File pDstFile) throws IOException {
        if (Logger.getLogLevel() <= 1) {
            Logger.dbg("Starting FileUtils.copy("+pSrcFile+","+pDstFile);
        }
        
        int numRetries = 0;
        int maxNumRetries = 5;        
        
        if (!pSrcFile.exists()) {
            throw new FileNotFoundException(pSrcFile.getPath());
        }
        
        // jihuu, we have a brand new implementation!
        File dstFile;
                        
        if (pDstFile.isDirectory()) {
            String dstFileName = pSrcFile.getName();
            dstFile = new File(pDstFile+File.separator+dstFileName);
        }
        else {
            dstFile = pDstFile;
        }                
            
        boolean retry = true;
                          
        IOException lastException = null;                          
                          
        while (retry)  {
            FileChannel srcChannel = null;            
            FileChannel dstChannel = null;
            
            try {
                dstFile.delete();
                        
                srcChannel = new FileInputStream(pSrcFile).getChannel();            
                dstChannel = new FileOutputStream(dstFile).getChannel();
            
                // Copy file contents from source to destination
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            
                // Close the channels
                srcChannel.close();
                dstChannel.close();

                long srcSize = pSrcFile.length();
                long dstSize = dstFile.length();
                    
                if (srcSize == dstSize) {
                    // we probably succeeded
                    retry = false;                                                                                   
                }
                else {
                    // let's keep the retry flag...   
                }
            }
            catch (IOException e) {
                // something went wrong; do not clear the retry flag
                lastException = e;
                
                // make sure channels are closed                                
                if (srcChannel != null) {
                    try {
                        srcChannel.close();
                    }
                    catch (Exception fe) {
                        // foo
                    }
                }
                if (dstChannel != null) {
                    try {
                        dstChannel.close();
                    }
                    catch (Exception fe) {
                        // foo
                    }
                }
            }
                                            
            if (retry) {                
                if (numRetries < maxNumRetries) {
                    // let's retry...
                    numRetries++;
                    
                    // let's wait a bit...
                    try {
                        Logger.dbg("Sleeping before retry in FileUtils.copy())");
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException intrE) {
                        // boo!
                        Logger.warning("Interrupted in FileUtils.copy()");
                    }                                                            
                }
                else {
                    // no more hopeless retries
                    if (lastException != null) {
                        throw lastException;
                    }
                    else {
                        throw new IOException("File sized differ even after "+maxNumRetries);
                    }
                }
            }
        }
        
        if (numRetries > 0) {
            Logger.warning("Copying "+pSrcFile+"->"+pDstFile+" required "+numRetries+" retries.");                                            
        }
        
        if (Logger.getLogLevel() <= 1) {
            Logger.dbg("Finished FileUtils.copy("+pSrcFile+","+pDstFile);
        }
    }
    
    
    public static void cut(String pInFile, String pOutFile, int pFirstCol, int pLastCol) throws IOException {
        cut(new File(pInFile), new File(pOutFile), pFirstCol, pLastCol);
    }
    
    /**
     * Emulates the standard unix command "cut", with arbitrary sequence of 
     * white space as the separator string.
     * 
     * Note that indexing is "awk-style", starting from one, and inclusive end-index.
     */ 
    public static void cut(File pInFile, File pOutFile, int pFirstCol, int pLastCol) throws IOException {        
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(pOutFile))); 
        BufferedReader reader = new BufferedReader(new FileReader(pInFile));                                
        String line = reader.readLine();
        while (line!=null) {                                    
            String[] cols = line.split("\\s+");                       
            StringBuffer outputLine = new StringBuffer();            
            if (cols.length > 0) {
                outputLine.append(cols[pFirstCol-1]);
                for (int i=pFirstCol+1; i<=pLastCol; i++) {
                    outputLine.append(" ");
                    outputLine.append(cols[i-1]);
                }                
            }
            pw.println(outputLine);
            line = reader.readLine();
        }
        reader.close();
        pw.close();
                                                  
    }
    
    
    public static void paste(String pInFile1, String pInFile2, String pOutFile) throws IOException {
        paste(new File(pInFile1), new File(pInFile2), new File(pOutFile));  
    }
    
    /** 
     * Emulates the standard unix command "paste", with arbitrary sequence of 
     * white space as the separator string and two input files.
     *
     * Note that indexing is "awk-style", starting from one, and inclusive end-index--- 
     */
    public static void paste(File pInFile1, File pInFile2, File pOutFile) throws IOException {
        BufferedReader reader1 = new BufferedReader(new FileReader(pInFile1));
        BufferedReader reader2 = new BufferedReader(new FileReader(pInFile2));
        PrintStream ps = new PrintStream(new FileOutputStream(pOutFile));
        
        String line1 = reader1.readLine();
        String line2 = reader2.readLine();
                        
        while (line1 != null && line2 != null) {                                                                                  
            ps.println(line1+" "+line2);
            line1 = reader1.readLine();
            line2 = reader2.readLine();
        }
        
        reader1.close();
        reader2.close();        
        ps.close();
        
        if (line1 != null || line2 != null) {
            Utils.die("Different number of lines in input files!");            
        }               
                                                                 
    }
    
    /** A robust move with some retries (@see #copy()) */
    public static void move(File pSrcFile, File pDstFile) throws IOException {
        // new wonderful impl:
        copy(pSrcFile, pDstFile);
        pSrcFile.delete();                    
    }

    /** A drop-in replacement for unix find */
    public static List<File> find(File dir) {
        return find(dir, null);
    }
    
    /** Condition is allowed to be null */
    public static List<File> find(File dir, Condition<File> condition) {
        List<File> result = new ArrayList<File>();
        internalFind(dir, result, condition);
        return result;
    }
    
    /** Condition is allowed to be null */
    private static void internalFind(File pDir, Collection<File> result, Condition<File> condition) {
        for (File f: pDir.listFiles()) {
            if (condition == null || condition.fulfills(f)) {
                result.add(f);
            }
            
            if (f.isDirectory()) {
                internalFind(f, result, condition);
            }
        }
    }
                            
    /** Non-recursively get the list of files in directory. The result will not include directories. */                         
    private static List internalFilesInDir(File pDir) {
        ArrayList result = new ArrayList();
        String[] files = pDir.list();
        for (int i = 0; i<files.length; i++) {
            String dirName = StringUtils.chop(pDir.getAbsolutePath(), '/')+'/';            
            String filename = dirName+files[i];
            File file = new File(filename);
            if (file.exists() && !(file.isDirectory())) {                                  
                result.add(file);                
            }    
        }    
        return result;
    }
       
    public static void convertCol_inplace(File pFile, int pColInd, Converter pConverter, 
    		                      		  boolean pIncludesHeader, String pOutputSeparator) throws IOException {
        Logger.startSubSection("convertCol");                
        Matrix m = new Matrix(pIncludesHeader);        
        m.readFromFile(pFile);
        // dbgMsg("original matrix\n:"+m);
        m.convertCol(pColInd, pConverter);
        // dbgMsg("converted matrix:\n"+m);
        m.setOutputColumnSeparator(pOutputSeparator);
        m.writeToFile(pFile);
        Logger.endSubSection("convertCol");            
    }
    
    public static void convertCol_streaming(int pColInd, Converter pConverter, 
    		  								boolean pIncludesHeader, boolean tabbedinput, String pOutputSeparator) throws IOException {    	   
    	Matrix m = new Matrix(pIncludesHeader);
    	if (tabbedinput) {
    		m.setRowFormatFactory(RowFormatFactory.DEFAULT_TABBED_FACTORY);
    	}    	
    	m.readFromStream(System.in);
    	m.convertCol(pColInd, pConverter);// 	
    	m.setOutputColumnSeparator(pOutputSeparator);
    	m.writeToStream(System.out);    	            
    }
    
    /** Get the subdirectories of a given dir */
    public static File[] subDirs(File pDir, boolean pRecursive) {
        ArrayList result = new ArrayList();
                
        String[] files = pDir.list();         
        
        for (int i = 0; i<files.length; i++) {
            String dirName = StringUtils.chop(pDir.getAbsolutePath(), '/')+'/';            
            String filename = dirName+files[i];
            File file = new File(filename);
            if (file.exists() && file.isDirectory()) {                                  
                result.add(file);
                if (pRecursive) {
                    // recursively get subdirs of sub dir                    
                    File[] subSubDirs = subDirs(file, true);
                    result.addAll(Arrays.asList(subSubDirs));
                }
            }    
        }
        return (File[])ConversionUtils.collectionToArray(result, File.class);                                  
    }
    
    /** Replace the pLineInd:th line of pFile with pNewLine. The indexing starts from 0. */
    public static void replaceLine(String pNewLine, int pLineInd, File pFile) throws IOException {
        String[] lineArr = IOUtils.readLineArray(pFile);
        lineArr[pLineInd] = pNewLine;
        IOUtils.writeToFile(pFile, lineArr);                
    }
        
    /** Remove columns of a matrix form file */
    public static void removeCols(int[] pCols, File pFile, boolean pHasHeader) throws IOException {
        removeCols(ConversionUtils.asList(pCols), pFile, pHasHeader);        
    }
    
    /** Synchronize a set of files such that the newest one is copied to all others */
    public static void sync(Set<File> pFiles) {
        Set<File> largest = new HashSet();
        Set<File> newest = new HashSet();
        
        for (File f: pFiles) {
            
            Logger.info("Handling file: "+f);
         
            if (! f.exists()) {
                // only valid as a dst file
                continue;
            }
            
            if (newest.isEmpty()) {
                newest.add(f);
            }
            else {
                if (newest.iterator().next().lastModified() < f.lastModified()) {
                    newest.clear();
                    newest.add(f);
                }
                else if (newest.iterator().next().lastModified() == f.lastModified()) {
                    // as new as the previous one(s) in the set...
                    newest.add(f);
                }
                else {
                    // older than ones in set, no action
                }
            }
            
            if (largest.isEmpty()) {
                largest.add(f);
            }
            else {
                if (largest.iterator().next().length() < f.length()) {
                    Logger.info("Initially largest file: "+f);
                    largest.clear();
                    largest.add(f);
                }
                else if (largest.iterator().next().length() == f.length()) {
                    // as new as the previous one(s) in the set...
                    Logger.info("Setting as new largest file: "+f);
                    largest.add(f);
                }
                else {
                    // older than ones in set, no action
                }
            }                              

        }
        
        Logger.info("Newest:\n\t"+SU.toString(newest, "\n\t"));
        Logger.info("");
        Logger.info("Largest:\n\t"+SU.toString(largest,"\n\t"));
                       
        
        if (newest.size() > 1) {
            Logger.warning("Too many newest files, no action!");
            System.exit(0);
        }
        
        File n = newest.iterator().next();
        
        Logger.info("Length of newest file: "+n.length());
        
        if (! largest.contains(n)) {
            Logger.warning("Newest file "+n+" not contained in the set of largest files: "+SU.toString(largest, " "));
            System.exit(0);
        }
        
        // OK, proceed to sync
        Logger.info("Copying newest file: "+n+" to all other files...");
        for (File dst: pFiles) {
            if (! dst.equals(n)) {
                try {
                    Logger.info(""+n+" => "+dst);
                    FileUtils.copy(n, dst);
                }
                catch (IOException e) {
                    Logger.warning("Failed copying "+n+" to "+dst, e);
                }
            }
        }
    }
    
    /** Remove columns of a matrix form file */
    public static void removeCols(Collection pCols, File pFile, boolean pHasHeader) throws IOException {
        // create matrix with no header        
        Matrix m = new Matrix(pHasHeader);
        m.readFromFile(pFile);
        HashSet colSet = new HashSet(pCols);
        m.removeCols(colSet);
        m.writeToFile(pFile);               
    }
    
    /** Remove columns of a matrix form file */
    public static void removeLastCols(int pNumCols, File pFile, boolean pHasHeader) throws IOException {
        // create matrix with no header        
        Matrix m = new Matrix(pHasHeader);
        m.readFromFile(pFile);
        int oldNumCols = m.getNumCols();
        int firstColToRemove = oldNumCols-pNumCols;
        int[] colsToRemove = new Range(firstColToRemove, oldNumCols).asIntArr();        
        removeCols(colsToRemove, pFile, pHasHeader);                       
    }
    
    /** Remove columns of a matrix form file */
    public static void removeFirstCols(int pNumCols, File pFile, boolean pHasHeader) throws IOException {        
        int[] colsToRemove = new Range(0, pNumCols).asIntArr();        
        removeCols(colsToRemove, pFile, pHasHeader);        
    }
    
    public static List<String> readHeader(File pFile) throws IOException {
        String line = IOUtils.readFirstLine(pFile); 
        return Arrays.asList(line.split("\\s")); 
    }
    
            
    /** Remove columns of a matrix from file */
    public static void removeRows(int[] pRows, File pFile) throws IOException {
        removeRows(ConversionUtils.asList(pRows), pFile);        
    }


    
    /** Remove rows of a matrix from file */
    public static void removeRows(Collection pRows, File pFile) throws IOException {
        // create matrix with no header        
        Matrix m = new Matrix();
        m.readFromFile(pFile);
        HashSet rowSet = new HashSet(pRows);
        m.removeRows(rowSet);
        m.writeToFile(pFile);               
    }
    
    /** Remove rows of a matrix from file */
    public static void removeLastRows(int pNumRows, File pFile) throws IOException {
        // create matrix with no header        
        Matrix m = new Matrix();
        m.readFromFile(pFile);
        int oldNumRows = m.getNumRows();
        int firstRowToRemove = oldNumRows-pNumRows;
        int[] rowsToRemove = new Range(firstRowToRemove, oldNumRows).asIntArr();        
        removeRows(rowsToRemove, pFile);                       
    }
    
    /** Remove rowumns of a matrix from file */
    public static void removeFirstRows(int pNumRows, File pFile) throws IOException {        
        int[] rowsToRemove = new Range(0, pNumRows).asIntArr();        
        removeRows(rowsToRemove, pFile);        
    }
               
    
    public static int countRows(File pFile) throws IOException {
        // create matrix with no header        
        // Matrix m = new Matrix();
        // m.readFromFile(pFile);
        // return m.getNumRows();
        return IOUtils.countRows(pFile.getPath());
        
    }
        
    public static void db2Postprocess(String [] args) throws IOException {
        
        int maxLen = 0;
        
        IOUtils.setFastStdout();
        List<String> lines = IOUtils.readLines();
        for (String line: lines) {
            int len = line.length();
            if (maxLen < len) {
                maxLen = len;
            }
        }
        
        boolean[] emptyPositions = new boolean[maxLen];
        for (int i=0; i<maxLen; i++) {
            // each position is empty until proven otherwise
            emptyPositions[i] = true;
        }
        
        for (String line: lines) {            
            for (int i=0; i<line.length(); i++) {
                if (line.charAt(i) != ' ') {
                    emptyPositions[i] = false;
                }                   
            }
        }

        for (int i=3; i<maxLen; i++) {
            if (!emptyPositions[i]) {
                // mark previous ones non-empty as well...
                emptyPositions[i-1] = false;
                emptyPositions[i-2] = false;
                emptyPositions[i-3] = false;
            }
        }
        
        
        for (String line: lines) {
            StringBuffer buf = new StringBuffer();
            for (int i=0; i<line.length(); i++) {
                if (!(emptyPositions[i])) {
                    buf.append(line.charAt(i));
                }
            }
            System.out.println(buf.toString());
        }
        
        System.out.flush();
    }
    
    public static void cat(String [] args) throws IOException {
                
        IOUtils.setFastStdout();
        if (args.length == 0) {
            IOUtils.cat(System.in);
        }
        else {
            for (String f: args) {
                IOUtils.cat(f);
            }
        }
        System.out.flush();
    }
                
    public static int countCols(File pFile) throws IOException {
        // create matrix with no header        
        Matrix m = new Matrix();
        m.readFromFile(pFile);
        return m.getNumCols();                       
    }
    
    /** 
     * Replaces all occurences or pRegexToReplace in  pFile with pReplacementText 
     * @return number of modified lines.
     */
    public static int replaceInFile(String pRegexToReplace, String pReplacementText, File pFile) throws IOException {        
        dbgMsg("replaceInFile("+pRegexToReplace+","+pReplacementText+","+pFile);                             
        String[] lines = IOUtils.readLineArray(pFile);
        Pattern p = Pattern.compile(pRegexToReplace);
        int numLinesModified = 0;
        for (int i=0; i<lines.length; i++) {
            Matcher m = p.matcher(lines[i]);
            String old = lines[i];            
            lines[i]=m.replaceAll(pReplacementText);
            if (!(old.equals(lines[i]))) {
                numLinesModified++;
            }
        }                                               
                 
        IOUtils.writeToFile(pFile, lines);
        return numLinesModified;                
    }        
    
    public static void forEachFileDo(FileOperation pFileOperation, File[] pFiles) throws IOException {
        for (int i=0; i<pFiles.length; i++) {
            pFileOperation.doOperation(pFiles[i]);
        }        
    }
    
    /** Wrapper for #suffix(String) */ 
    public static String suffix(File pFile) {
        return suffix(pFile.getName());
    }
    
    /** 
     * Return null, if the file name is not of the form *.foo
     * 
     * Keywords: extractSuffix, extract suffix, extension.
     * 
     * Actually, just delegates to {@link StringUtils#getExtension(String)}
     */
    public static String suffix(String pFileName) {
        return StringUtils.getExtension(pFileName);
//        Pattern p = Pattern.compile("^.+?\\.([^.]+)$");
//        Matcher m = p.matcher(pFileName);
//        if (m.matches()) {          
//            String suffix = m.group(1);
//            return suffix;             
//        }
//        else {
//            // Logger.warning("Suffix not recognized: "+pFileName);
//            return false;
        
    }
            
    
    /**
     * is file name of the form <foo>.<bar>, where <foo> is a non-empty string
     * and <bar> is one of the strings in pPossibleSuffixes. Note thus, in other
     * words, that in  particular '.' should not be included in the possible 
     * suffix strings, and it is required that a non-empty string precedes the '.'.
     */
    public static boolean hasSuffix(String pFileName, Set<String> pPossibleSuffixes) {    
        Pattern p = Pattern.compile("^.+?\\.([^.]+)$");
        Matcher m = p.matcher(pFileName);
        if (m.matches()) {        	
            String suffix = m.group(1);
            // Logger.warning("Suffix: "+suffix);
            boolean result = pPossibleSuffixes.contains(suffix);
            // Logger.info("hasSuffix returning: "+result);
            return result;                
        }
        else {
        	// Logger.warning("Suffix not recognized: "+pFileName);
        	return false;
        }
    }
    
    /**
     * is file name of the form <foo>.<bar>, where <foo> is a non-empty string
     * and <bar> is one of the strings in pPossibleSuffixes. Note thus, in other
     * words, that in  particular '.' should not be included in the possible 
     * suffix strings, and it is required that a non-empty string precedes the '.'.
     */
    public static boolean hasSuffix(File pFile, Set<String> pPossibleSuffixes) {        
        return hasSuffix(pFile.getName(), pPossibleSuffixes);
    }
    
    
    /** 
     * Replace all occurences of pRegexToReplace with pReplacementText in all pFiles 
     * @return a ReplaceFileOperation, which can be queried for number of replaced lines and files.
     */
    public static ReplaceFileOperation replaceInFiles(String pRegexToReplace, String pReplacementText, File[] pFiles) throws IOException {
        dbgMsg("replaceInFiles: "+pRegexToReplace+","+pReplacementText+","+StringUtils.arrayToString(pFiles));
        ReplaceFileOperation operation = new ReplaceFileOperation(pRegexToReplace, pReplacementText);
        forEachFileDo(operation, pFiles);
        return operation;                
    }
    
    /** 
     * Replace all occurences of pRegexToReplace with pReplacementText in all pFiles 
     * @return a ReplaceFileOperation, which can be queried for number of replaced lines and files.
     */
    public static ReplaceFileOperation replaceInDir(String pRegexToReplace, String pReplacementText, String pDir) throws IOException {
        File[] allFiles = filesInDir(new File(pDir), true);                
        // find files containing pExpr
        ContainsPatternFileCondition containsExprCondition = new ContainsPatternFileCondition("^.*"+pRegexToReplace+".*$");
        File[] filesContainingExpr = findFilesMatchingCondition(allFiles, containsExprCondition);   
        ReplaceFileOperation operation = new ReplaceFileOperation(pRegexToReplace, pReplacementText);
        forEachFileDo(operation, filesContainingExpr);
        return operation;                
    }
    
    
    
    /** Inserts lines into a file, such that pLinesToInsert[0] shall be the pWhereToInsert:th line of the file 
     * Note: line numbering starts from 1!!!! 
     */
    public static void insertLinesIntoFile(List pLinesToInsert, int pWhereToInsert, File pFile) throws IOException {
        Logger.startSubSection("insertLinesIntoFile");
        // dbgMsg("pLinesToInsert=\n"+StringUtils.listToString(pLinesToInsert)+"\npWhereToInsert="+pWhereToInsert);
        String[] originalLinesArr = IOUtils.readLineArray(pFile);
        List originalLinesList = Arrays.asList(originalLinesArr);
        List prefixLines = originalLinesList.subList(0, pWhereToInsert-1);
        List postfixLines = originalLinesList.subList(pWhereToInsert-1, originalLinesList.size());        
        ArrayList allLines = new ArrayList();
        allLines.addAll(prefixLines);
        allLines.addAll(pLinesToInsert);
        allLines.addAll(postfixLines);
        String[] linesToWrite = (String[])ConversionUtils.collectionToArray(allLines, String.class);
        IOUtils.writeToFile(pFile, linesToWrite);
        Logger.endSubSection("insertLinesIntoFile");        
    }
    
    /** Inserts lines into a file, such that pLinesToInsert[0] shall be the pWhereToInsert:th line of the file */
    public static void insertLinesIntoFile(String[] pLinesToInsert, int pWhereToInsert, File pFile) throws IOException {
        insertLinesIntoFile(Arrays.asList(pLinesToInsert), pWhereToInsert, pFile);
    }
        
    /** Inserts a line into a file, such that pLineToInsert be the pWhereToInsert:th line of the file */
    public static void insertLineIntoFile(String pLineToInsert, int pWhereToInsert, File pFile) throws IOException {           
        insertLinesIntoFile(Collections.singletonList(pLineToInsert), pWhereToInsert, pFile);
    }    
    
    public static File[] findFilesMatchingCondition(File[] pFiles,
                                                    FileCondition pCondition) throws IOException {
        ArrayList result = new ArrayList();                                                        
        for (int i=0; i<pFiles.length; i++) {
            File file = pFiles[i];
            if (pCondition.fulfills(pFiles[i])) {
                result.add(file);                     
            }
        }
        return (File[])ConversionUtils.collectionToArray(result, File.class);                                                                                                                          
    }
    
    private static boolean differs(File pFile1, File pFile2) throws IOException {
        if (!pFile1.exists()) {
            throw new RuntimeException(pFile1.getAbsolutePath()+" does not exist");
        }
        if (!pFile2.exists()) {
            throw new RuntimeException(pFile1.getAbsolutePath()+" does not exist");
        }
        
        if (pFile1.length() != pFile2.length()) {
            return true;
        }
                        
        // OK, same length
        
        if (pFile1.length() == 0) {
            // both empty
            return false;
        }
        
        InputStream is1 = new FileInputStream(pFile1);
        InputStream is2 = new FileInputStream(pFile2);
        is1 = new BufferedInputStream(is1);
        is2 = new BufferedInputStream(is2);
                
        int byte1 = is1.read();
        int byte2 = is2.read();
        
        while (byte1 != -1) {
            if (byte1 != byte2) {
                // differing byte
                is1.close();
                is2.close();
                return true;
            }
            byte1 = is1.read();
            byte2 = is2.read();
        }
                
        // reached end of files, no differences detected
        is1.close();
        is2.close();
        return false;
        
    }
    
   /** 
     * Finds the lines in the given set of files that match the given condition 
     * Return a MultiMap, with Files as keys and the lines in file (Integer objects!) as values.
     * Line indexing starts from 0!
     */
    public static MultiMap findLinesMatchingCondition(File[] pFiles,
                                                    LineCondition pCondition) throws IOException {
        MultiMap result = new MultiMap();                                                        
        for (int i=0; i<pFiles.length; i++) {
            File file = pFiles[i];
            String[] lines = IOUtils.readLineArray(file.getAbsolutePath());
            for (int j=0; j<lines.length; j++) {
                if (pCondition.fulfills(lines[j])) {
                    result.put(file, new Integer(j+1));
                }             
            }
        }                
        return result;                                                                                                                          
    }
    
    /** 
     * Insert the specified line into lines in files specified by pLinesByFile.
     * Note that this enables inserting the same line into multiple places into a file;
     * This is just an implementation issue.
     */ 
    public static void insertLineIntoFiles(MultiMap pLinesByFile, String pLineToInsert) throws IOException {
        Iterator files = pLinesByFile.keySet().iterator();
        while(files.hasNext()) {            
            File file = (File)files.next();                        
            Iterator lines = pLinesByFile.get(file).iterator();            
            while(lines.hasNext()) {
                Integer line = (Integer)lines.next();
                insertLineIntoFile(pLineToInsert, line.intValue(), file);
            }            
        }        
    }
    
    public static void appendLineIntoFiles(File[] pFiles, String pLineToAppend) throws IOException {
        String[] lineList = new String[]{pLineToAppend};        
        for (int i=0; i<pFiles.length; i++) {                                                                    
            IOUtils.appendToFile(pFiles[i], lineList);                                                
        }        
    }
    
    /*
    public static void join(String pFile1, String pFile2) {
        List<String[]> table1 = readTable(pFile1);
        List<String[]> table2 = readTable(pFile2);
                                
    }
    */
    
    /** This is heavy stuff, use with utmost discretion, or file system in it's entirety will be ruined. */
    public static void ensureEachFileContainsLine(String pDir,
                                           String pExpr,
                                           String pExprToContain,
                                           String pLineToInsert,                
                                           String pLineToInsertAfter) throws IOException {
        /*                                               
        dbgMsg("starting ensureEachFileContainsLine:\n"+
               "dir: "+pDir+"\n"+
               "expr: "+pExpr+"\n"+
               "exprToContain: "+pExprToContain+"\n"+
               "lineToInsert: "+pLineToInsert+"\n"+
               "lineToInsertAfter: "+pLineToInsertAfter);
        */               
        
        // all files 
        File[] allFiles = filesInDir(new File(pDir), true);
        // dbgMsg("*****************************************************");
        // dbgMsg("all files:\n"+StringUtils.arrayToString(allFiles));
        
        // find files containing pExpr
        ContainsPatternFileCondition containsExprCondition = new ContainsPatternFileCondition("^.*"+pExpr+".*$");
        File[] filesContainingExpr = findFilesMatchingCondition(allFiles, containsExprCondition);
        // dbgMsg("*****************************************************");
        // dbgMsg("all files containing expr:\n"+StringUtils.arrayToString(filesContainingExpr));
        
        // find files not containing pExprToContain
        ContainsPatternFileCondition containsLineCondition = new ContainsPatternFileCondition("^.*"+pExprToContain+".*$");
        FileCondition doesNotContainLineCondition = new NotFileCondition(containsLineCondition);
        File[] filesNotContainingRequiredLine = findFilesMatchingCondition(filesContainingExpr, doesNotContainLineCondition);
        // dbgMsg("*****************************************************");
        // dbgMsg("files not containing line:\n"+StringUtils.arrayToString(filesNotContainingRequiredLine));
        
        // find the place
        LineCondition lineToInsertAfterCondition = new ContainsPatternLineCondition("^.*"+pLineToInsertAfter+".*$");
        MultiMap linesByFile = findLinesMatchingCondition(filesNotContainingRequiredLine, lineToInsertAfterCondition);
        // dbgMsg("*****************************************************");
        // dbgMsg("lines to insert before addition:\n"+linesByFile);
        
        // silentyly prune the lines, so that only the first one remains...
        linesByFile.prune();
        
        // add 1 to line numbers to insert after the corresponding line!
        linesByFile = ConversionUtils.convert(linesByFile, new IntAdderConverter(1));
        // dbgMsg("*****************************************************");
        // dbgMsg("lines to insert after addition:\n"+linesByFile);
        
        
        // dbgMsg("Inserting line: \""+pLineToInsert+"\" into "+filesNotContainingRequiredLine.length+" files.");
        insertLineIntoFiles(linesByFile, pLineToInsert);                                                                                                                  
    }
        
    
    public static void main(String[] args) {
        try {
            CmdLineArgs argParser = new CmdLineArgs(args);
            if (args.length == 0) {
    			usageAndExit("First argument must be a command.");
    		}
            
            if (!argParser.isDefined("loglevel")) {
            	Logger.setLogLevel(Logger.LOGLEVEL_IMPORTANT_INFO);
            }
            // Logger.setProgramName("FileUtils "+args[0]);            
            
            String cmd = argParser.shift("Command");
            args = argParser.getNonOptArgs();
            // checks recursively for all files under the given dir(arg1) that:
            // each file that contains expression args2 also contains expression (arg3); 
            // for those files, for which this condition does not hold,  we insert the
            // line(arg4) after the line specified by arg[5] 
            if (cmd.equals(CMD_ENSURECONTAINSLINE)) {
                if (args.length != 5) {
                    throw new RuntimeException("Usage: java util.io.FileUtils ensurecontainsline <dir> <expr> <exprtocontain> <linetoinsert> <linetoinsertafter>");
                }
                String dir = argParser.shift("dir");
                String expr = argParser.shift("expr");
                String exprToContain = argParser.shift("exprToContain");
                String lineToInsert = argParser.shift("lineToInsert"); 
                String lineToInsertAfter = argParser.shift("lineToInsertAfter");
                
                ensureEachFileContainsLine(dir, expr, exprToContain, lineToInsert, lineToInsertAfter);                
            }
            else if (cmd.equals(CMD_SYNC)) {
                Set<File> files = new HashSet();
                for (String name: argParser.getNonOptArgsAsList()) {                    
                    files.add(new File(name));
                }
                sync(files);
            }
            else if (cmd.equals(CMD_LAST_MODIFIED)) {
                String fileName = argParser.shift("filename");
                File file = new File(fileName);
                if (file.exists()) {
                    System.out.println(file.lastModified());
                }
                else {
                    Logger.error("File does not exist: "+file);
                    System.exit(-1);
                }
                
            }
            else if (cmd.equals(CMD_MAKE_PLOT)) {
                String xKey = argParser.shift("xkey");
                String yKey = argParser.shift("ykey");
                String[] files = argParser.getNonOptArgs();
                TreeMap<Double,Double> result = new TreeMap<Double, Double>();
                for (String file: files) {
                    Map<String,String> map = IOUtils.readMap(new File(file));
                    double x = Double.parseDouble(map.get(xKey));
                    double y = Double.parseDouble(map.get(yKey));
                    result.put(x, y);
                }
                for (Double x: result.keySet()) {
                    double y = result.get(x);
                    System.out.println(x+" "+y);
                }
                
                
                
            }            
            else if (cmd.equals(CMD_SET_CLIPBOARD_CONTENTS)) {
                String[] argStrings = argParser.getNonOptArgs();
                String val = StringUtils.arrayToString(argStrings, " ");
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();        
                StringSelection data = new StringSelection(val);
                Logger.info("Setting clipboard to: "+val);
                clipboard.setContents(data, null);                
            }                        
            else if (cmd.equals(CMD_REMOVECOLS)) {                
                // usage: java blahblah removecols <filename> collist
                Logger.startSubSection("removeCols");                                                 
                String fileName = argParser.shift("filename");
                String[] cols = argParser.getNonOptArgs();
                List colList = ConversionUtils.convert(Arrays.asList(cols), new StringToIntegerConverter());
                boolean includesHeader = argParser.isDefined("includesheader");                
                removeCols(colList, new File(fileName), includesHeader);
                Logger.endSubSection("removeCols");                                 
            }                                    
            else if (cmd.equals(CMD_CONVERTCOL)) {
                // usage: java blahblah convertcol <filename> <colind> <converterclassname>                                                 
                String fileName = argParser.shift("filename");
                int colInd = argParser.shiftInt("colind");
                String converterClassName = argParser.shift("converterclassname");                                
                boolean includesHeader = argParser.getBooleanOpt("includesheader", false);
                boolean tabbed = argParser.getBooleanOpt("tabbed", false);
                String outputSeparator;
                if (tabbed) {                	
                	outputSeparator = "\t";
                }
                else {                	
                	outputSeparator = " ";
                }
                convertCol_inplace(new File(fileName), 
                        		   colInd,
                        		   (Converter)ReflectionUtils.createInstance(converterClassName),
                        		   includesHeader,
                        		   outputSeparator);                                             
            }
            else if (cmd.equals(CMD_CONVERTCOL_MAPPINGFILE)) {
                // usage: java util.io.FileUtils convertcol FILENAME COLIND MAPPINGFILE [-tabbed=BOOL] [-includesheader=BOOL]
            	//    or: java util.io.FileUtils convertcol COLIND MAPPINGFILE [-tabbed=BOOL] [-includesheader=BOOL]
            	// converts in-place if file given!!
            	String fileName = null;
            	File mappingFile = null;
                int colInd;                                
            	
            	if (argParser.getNumNonOptArgs() == 2) {
            		// read stdin, write stdout
            		colInd = argParser.shiftInt("colind");                    
                    mappingFile = new File(argParser.shift("mappingfile"));            		
            	}
            	else if (argParser.getNumNonOptArgs() == 3) {
            		// in-place!!
            		colInd = argParser.shiftInt("colind");
                    fileName = argParser.shift("filename");
                    mappingFile = new File(argParser.shift("mappingfile"));            		
            	}
            	else {
            		throw new RuntimeException(
            				"Illegal number of arguments: "+argParser.getNumNonOptArgs()+".\n"+
            	            "Usage: java util.io.FileUtils convertcol FILENAME COLIND MAPPINGFILE [-tabbed=BOOL] [-includesheader=BOOL]\n"+
            	            "   or: java util.io.FileUtils convertcol COLIND MAPPINGFILE [-tabbed=BOOL] [-includesheader=BOOL]\n");
            	}
            	
            	boolean tabbed = argParser.getBooleanOpt("tabbed", false);
            	boolean includesHeader = argParser.getBooleanOpt("includesheader", false);
                
                Map<String,String> mapping;
                String outputSeparator = null;
                if (tabbed) {
                	mapping = IOUtils.readMap(mappingFile, "\t");
                	outputSeparator = "\t";
                }
                else {
                	mapping = IOUtils.readMap(mappingFile, "\\s+");
                	outputSeparator = " ";
                }
                                
                Converter converter = new MapConverter(mapping, MapConverter.NotFoundBehauvior.RETURN_ORIGINAL_AND_WARN);                                                                
                
                if (fileName != null) {
                	convertCol_inplace(new File(fileName), 
                			   		   colInd,
                			   		   converter,
                			   		   includesHeader, 
                			   		   outputSeparator);
                }
                else {
                	convertCol_streaming(colInd,
     			   		   				 converter,
     			   		   				 includesHeader,
     			   		   				 tabbed,
     			   		   				 outputSeparator);
                }
                                                            
            }                                                            
            else if (cmd.equals(CMD_CONVERT_ALL_TOKENS_MAPPINGFILE)) {
                // usage: java blahblah convertcol <filename> <colind> <converterclassname>                                                                                 
                File mappingFile = new File(argParser.shift("mappingfile"));
                Map mapping = IOUtils.readMap(mappingFile, "\\s+", true); 
                Converter converter = new MapConverter(mapping, MapConverter.NotFoundBehauvior.RETURN_ORIGINAL);                
                
                for (String line: IOUtils.readLines()) {
                    String[] tokens = line.split("\\s+");
                    boolean first = true;
                    for (String token: tokens) {
                        if (!first) {
                            System.out.print(" ");
                        }                        
                        System.out.print(converter.convert(token));
                        first = false;
                    }
                    System.out.println();
                }
            }
            else if (cmd.equals(CMD_NO_OP)) {
                Matrix m = new Matrix(false);
                m.readFromStream(System.in);
                m.setPrettyPrinting(true);
                m.writeToStream(System.out);                
            }
            else if (cmd.equals(CMD_SPLIT_TO_BATCHES)) {
                List<String> lines = IOUtils.readLines(System.in);
                int nBatches = argParser.shiftInt();
                String prefix = argParser.shift();
                String suffix = argParser.shift();
                List<String>[] segments = 
                    CollectionUtils.splitToSegments_numsegments(
                            lines, nBatches, false);
                for (int i=0; i<segments.length; i++) {
                    String file = prefix+(i+1)+suffix;
                    IOUtils.writeToFile(new File(file), segments[i]);
                }
                                
            }            
            else if (cmd.equals(CMD_PRETTIFYCOLS)) {
//                Matrix m = new Matrix(false);
//                m.readFromStream(System.in);
//                m.setPrettyPrinting(true);
//                m.writeToStream(System.out);
            	boolean tabbed = argParser.getBooleanOpt("tabbed", false);
            	String sep;
            	if (tabbed) {
            		sep = "\t";
            	}
            	else {
            		sep = "\\s+";
            	}
                List<List<String>> rows = IOUtils.readTable2(System.in, sep);
                String formatted = StringUtils.formatTable(rows, "");
                System.out.println(formatted);
            }
            else if (cmd.equals(CMD_NO_OP2)) {
                Matrix original = new Matrix(true);
                original.readFromStream(System.in);
                Matrix clone = original.createClone();
                Logger.info("Format factory of original: "+original.getRowFormatFactory());
                Logger.info("Format factory of clone: "+clone.getRowFormatFactory());
                Logger.info("Row Format of original: "+clone.getHeader());
                Logger.info("Row Format of clone: "+clone.getHeader());
                Logger.info("Row Format of first row of clone: "+((Row)clone.getRow(0)).getFormat());                
                clone.writeToStream(System.out);                
            }
            else if (cmd.equals(CMD_COUNTCOLS)) {
                // usage: java blahblah removecols <filename> collist                                                 
                String fileName = argParser.shift("filename");                
                System.out.println(""+countCols(new File(fileName)));                                   
            }
            else if (cmd.equals(CMD_REMOVELASTCOLS)) {
                // usage: java blahblah removelastcols <filename> <numcols>                      
                String fileName = argParser.shift("fileName");
                int numcols = argParser.shiftInt("numcols");
                boolean includesHeader = argParser.isDefined("includesheader");
                removeLastCols(numcols, new File(fileName), includesHeader);                                                                 
            }
            else if (cmd.equals(CMD_REMOVEFIRSTCOLS)) {
                // usage: java blahblah removecols <filename> <numcolstoremove>                                
                String fileName = argParser.shift("filename");
                int numcols = argParser.shiftInt("numcols");
                boolean includesHeader = argParser.isDefined("includesheader");
                removeFirstCols(numcols, new File(fileName), includesHeader);                                                                 
            }            
            else if (cmd.equals(CMD_REMOVEROWS)) {
                // usage: java blahblah removerows <filename> collist                                                 
                String fileName = argParser.shift("filename");
                String[] rows = argParser.getNonOptArgs();
                List colList = ConversionUtils.convert(Arrays.asList(rows), new StringToIntegerConverter());
                removeRows(colList, new File(fileName));                                 
            }
            else if (cmd.equals(CMD_PRUNE_MAP)) {
                // only preserve certain keys of a map. edit inplace.
                // usage: java util.io.FileUtils prunemap <keyfile> <mapfile?> 
                String keyFile = null;
                String mapFile = null;
                
//                Logger.info("num args: "+argParser.getNumNonOptArgs());
                if (argParser.getNumNonOptArgs() == 1) {
                    keyFile = argParser.shift("keyfile");
                    mapFile = null;
                }
                else if (argParser.getNumNonOptArgs() == 2) {
                    keyFile = argParser.shift("keyfile");
                    mapFile = argParser.shift("mapfile");                    
                }
                else {
                    Utils.die("Illegal number of args: "+argParser.getNumNonOptArgs());
                }

                Map<String,String> map;
                if (mapFile != null) {
                    map = IOUtils.readMap(new File(mapFile));
                }
                else {
                    map = IOUtils.readMap(System.in);
                }
                Set<String> keysToPreserve = new LinkedHashSet(IOUtils.readLines(keyFile));
                map.keySet().retainAll(keysToPreserve);
                if (mapFile != null) {
                    IOUtils.writeToFile(mapFile, StringUtils.mapToString(map)+"\n");
                }
                else {
                    System.out.println(StringUtils.mapToString(map));
                }
//                List colList = ConversionUtils.convert(Arrays.asList(rows), new StringToIntegerConverter());
//                removeRows(colList, new File(fileName));                                 
            }
            else if (cmd.equals(CMD_PRUNE_P_VALS)) {
                // only preserve certain keys of a map. edit inplace.
                // usage: java util.io.FileUtils prunemap < foo.dat 
                Map<String, Double> data = new HashMap();
                
                for (String line: IOUtils.readLines()) {
                    String[] tok = line.split("\\s+");
                    String key = tok[0];
                    double val = Double.parseDouble(tok[1]);
                    if (data.containsKey(key)) {
                        double oldVal = data.get(key);
                        if (val < oldVal) {
                            data.put(key, val);
                        }
                    }
                    else {
                        data.put(key, val);
                    }
                }
                
                System.out.println(StringUtils.mapToString(data, " ", "\n"));
                                                 
            }
            else if (cmd.equals(CMD_REMOVEDUPLICATEROWS)) {
                // usage: java blahblah removeduplicaterows <filename>                                                 
                File file = new File(argParser.shift("filename"));
                boolean includesHeader = argParser.isDefined("includesheader");
                Matrix m = new Matrix(includesHeader);
                m.readFromFile(file);
                m.removeDuplicateRows();
                m.writeToFile(file);                                                 
            }                                    
            else if (cmd.equals(CMD_COUNTROWS)) {
                // usage: java blahblah countrows <filename>
                
                // Note: it seems to be a bit slower to read stdin,
                // compared to reading a file (cat versus redirect file
                // does not make a difference)
                
                if (argParser.getNumNonOptArgs() == 0) {
                    System.out.println(""+IOUtils.countRows(System.in));                    
                }
                else {
                    String fileName = argParser.shift("filename");                
                    System.out.println(""+countRows(new File(fileName)));   
                }
            }
            else if (cmd.equals(CMD_CAT)) {
                cat(args);
            }
            else if (cmd.equals(CMD_DB2_POSTPROCESS)) {
                db2Postprocess(args);
            }
            else if (cmd.equals(CMD_LONGEST_COMMON_SUFFIX)) {
                // usage: java blahblah countrows <filename>
                
                // Note: it seems to be a bit slower to read stdin,
                // compared to reading a file (cat versus redirect file
                // does not make a difference)
                
                if (argParser.getNumNonOptArgs() != 0) {
                    Utils.die("No args accepted (read from stdin)");
                }
                List<String> lines = IOUtils.readLines();
                String result = StringUtils.longestCommonSuffix(lines);
                System.out.println(result);
            }            
            else if (cmd.equals(CMD_REMOVE_LONGEST_COMMON_SUFFIX)) {
                // usage: java blahblah countrows <filename>
                
                // Note: it seems to be a bit slower to read stdin,
                // compared to reading a file (cat versus redirect file
                // does not make a difference)
                
                if (argParser.getNumNonOptArgs() != 0) {
                    Utils.die("No args accepted (read from stdin)");
                }
                List<String> lines = IOUtils.readLines();
                List<String> result = StringUtils.removeLongestCommonSuffix(lines);
                for (String s: result) {
                    System.out.println(s);
                }
            }
            else if (cmd.equals(CMD_REMOVE_LONGEST_COMMON_PREFIX)) {
                // usage: java blahblah countrows <filename>
                
                // Note: it seems to be a bit slower to read stdin,
                // compared to reading a file (cat versus redirect file
                // does not make a difference)
                
                if (argParser.getNumNonOptArgs() != 0) {
                    Utils.die("No args accepted (read from stdin)");
                }
                List<String> lines = IOUtils.readLines();
                List<String> result = StringUtils.removeLongestCommonPrefix(lines);
                for (String s: result) {
                    System.out.println(s);
                }
            }
            else if (cmd.equals(CMD_PIPE_AND_COUNT)) {
                // usage: java blahblah countrows <filename>
                
                // Note: it seems to be a bit slower to read stdin,
                // compared to reading a file (cat versus redirect file
                // does not make a difference)
                                
                List<String> lines = IOUtils.readLines();                
                Logger.info("There are "+lines.size()+" lines");
                int countOutputted = 0; 
                for (String line: lines) {                   
                    System.out.println(line);
                    countOutputted++;
                    if (countOutputted % 1000 == 0) {
                        Logger.info(countOutputted + "/" + lines.size());
                    }
                }                
            }
//            else if (cmd.equals(CMD_COUNTROWS2)) {
//                // usage: java blahblah countrows2 <filename>                                                  
//                String fileName = argParser.shift("filename");                
//                System.out.println(""+IOUtils.countRows2(new File(fileName)));                                   
//            }
            else if (cmd.equals(CMD_PREFIXES)) {
                // usage: java blahblah prefixed <filename> <prefixlen>
                
                String file = argParser.shift();
                int prefixLen = argParser.shiftInt();
                
                IOUtils.setFastStdout();
                BufferedReader reader = new BufferedReader(new FileReader(file), 1024*1024);
                String line = reader.readLine();                
                while(line!=null) {                    
                    System.out.println(line.substring(0, prefixLen));
                    line = reader.readLine();
                }
                reader.close();
                System.out.flush();
                
//                BufferedReader reader = new BufferedReader(new FileReader(pFile), 1024*1024);                                   
            }
            else if (cmd.equals(CMD_FIRST_COL)) {
                // usage: java blahblah firstcol <filename> <separatorchar>
                
                String file = argParser.shift();
                char separator = argParser.shiftChar();
                
                IOUtils.setFastStdout();
                BufferedReader reader = new BufferedReader(new FileReader(file), 1024*1024);

                String line = reader.readLine();                
                while(line!=null) {                   

                    int i = line.indexOf(separator);
                    if (i == -1) {
                        System.out.println(line);    
                    }
                    else {
                        System.out.println(line.substring(0, i));
                    }
                    line = reader.readLine();
                }
                reader.close();
                System.out.flush();
                
//                BufferedReader reader = new BufferedReader(new FileReader(pFile), 1024*1024);                                   
            }            
            else if (cmd.equals(CMD_REMOVELASTROWS)) {
                // usage: java blahblah removelastrows <filename> <firstcoltoremove>                      
                String fileName = argParser.shift("filename");
                int numrows = argParser.shiftInt("numrows");
                removeLastRows(numrows, new File(fileName));                                                                 
            }
            else if (cmd.equals(CMD_REMOVEFIRSTROWS)) {
                // usage: java blahblah removerows <filename> <numrowstoremove>                                
                String fileName = argParser.shift("filename");
                int numrows = argParser.shiftInt("numrows");
                removeFirstRows(numrows, new File(fileName));                                                                 
            }          
            else if (cmd.equals(CMD_REARRANGECOLS)) {
                // usage: java util.io.FileUtils rearrangecols <cols> (read from stdin, write to stdout)                                   
                List colOrderAsStrings = Arrays.asList(argParser.getNonOptArgs());                
                List colOrderAsIntegers = ConversionUtils.convert(colOrderAsStrings, new StringToIntegerConverter());
                boolean includesHeader = argParser.isDefined("includesheader");                                                                                                   
                Matrix m = new Matrix(includesHeader);                        
                m.readFromStream(System.in);                                                                                                  
                m.rearrangeCols_by_index(colOrderAsIntegers);        
                m.writeToStream(System.out);                                                                             
            }            
            else if (cmd.equals(CMD_REALPATH)) {                
                String path = argParser.getNonOptArgs()[0];
                File file = new File(path);
                String realPath = file.getAbsolutePath();
                System.out.println(realPath);
            }
            else if (cmd.equals(CMD_REMOVEROWSWITHZEROVALUEINCOLUMN)) {
                // usage: java util.io.FileUtils removerowswithzerovalueincolumn <col> (read from stdin, write to stdout)
                boolean includesHeader = argParser.isDefined("includesheader");                                                                                                                                    
                Matrix m = new Matrix(includesHeader);                        
                m.readFromStream(System.in);                                                                                                  
                m.removeRowsWithZeroValueInColumn(argParser.shiftInt("colind"));        
                m.writeToStream(System.out);                                                                             
            }
            else if (cmd.equals(CMD_INSERT_LINE_INTO_FILE)) {
                // line numbering starts from 1!!!!
                // usage: java util.io.FileUtils insert_line_into_file <line> <linenumber> <filename>
                String line = argParser.shift("line");
                int lineNumber = argParser.shiftInt("lineNumber");                                
                String filename = argParser.shift("fileName");                
                File file = new File(filename);                                                                                                
                insertLineIntoFile(line, lineNumber, file);                                                                                                                               
            }
            else if (cmd.equals(CMD_REPLACE_IN_FILE)) {                
                // usage: java util.io.FileUtils replaceinfile <filename> <regextoreplace> <replacementtext>
                File file = new File(argParser.shift("filename"));
                String regexToReplace = argParser.shift("regextoreplace");
                String replacementText = argParser.shift("replacementtext");
                int numModifiedLines = replaceInFile(regexToReplace, replacementText, file);
                System.out.println(""+numModifiedLines+" lines modified");                                                                                                                                                               
            }
            else if (cmd.equals(CMD_REPLACE_LINE)) {                
                // usage: java util.io.FileUtils replaceinfile <filename> <regextoreplace> <replacementtext>
                File file = new File(argParser.shift("filename"));
                int lineToReplace = argParser.shiftInt("line to replace");
                String newContents = argParser.shift("new contents");
                replaceLine(newContents, lineToReplace-1, file);
                System.out.println("1 lines modified");                                                                                                                                                               
            }
            else if (cmd.equals(CMD_REPLACE_IN_FILES)) {                
                // usage: java util.io.FileUtils replaceinfiles <filename1> <filename2> ... <filenameN> <regextoreplace> <replacementtext>                
                int numFiles = args.length-2;
                List fileNames = Arrays.asList(args).subList(0, numFiles);
                File[] files = new File[numFiles];
                for (int i=0; i<files.length; i++) {
                    files[i] = new File((String)fileNames.get(i));
                }                                
                String regexToReplace = args[numFiles];
                String replacementText = args[numFiles+1];
                ReplaceFileOperation result = replaceInFiles(regexToReplace, replacementText, files);
                System.out.println(result);                                                                                                                                                                               
            }
            else if (cmd.equals(CMD_REPLACE_IN_DIR)) {                
                // usage: java util.io.FileUtils replaceindir <dirname> <regextoreplace> <replacementtext>
                String dir = argParser.shift("dirname");
                String regexToReplace = argParser.shift("regextoreplace");
                String replacementText = argParser.shift("replacementtext");
                ReplaceFileOperation oper = replaceInDir(regexToReplace, replacementText, dir);
                System.out.println(""+oper);                                                                                                                                                               
            }                                    
            else if (cmd.equals(CMD_APPEND_COLS)) {
                // line numbering starts from 1!!!!
                // usage: java util.io.FileUtils appendcols <file1> <file2> (write to stdout)
                File file1 = new File(argParser.shift("file1"));
                File file2 = new File(argParser.shift("file2"));
                boolean includesHeader = argParser.isDefined("includesheader");                                                                                                                                                                                                
                Matrix m1 = new Matrix(includesHeader);
                Matrix m2 = new Matrix(includesHeader);
                m1.readFromFile(file1);
                m2.readFromFile(file2);
                m1.append(m2);
                m1.writeToStream(System.out);                                                                                           
            }
            else if (cmd.equals(CMD_APPEND_COL)) {
                // line numbering starts from 1!!!!
                // append col to matrix file (in-place!)
                // only works for matrices with header
                // usage: java util.io.FileUtils appendcol <matrixfile> <colfile> <colname=x>
                File matrixfile = new File(argParser.shift("matrixfile"));
                File colfile = new File(argParser.shift("colfile"));
                String colname = argParser.getOpt("colname");               
                if (colname == null) {
                    throw new RuntimeException("Must specify option colname!");
                }                                                                                                                                                                                                
                Matrix m = new Matrix(true);
                String[] col = IOUtils.readLineArray(colfile);                 
                m.readFromFile(matrixfile);                
                m.appendCol(Arrays.asList(col), colname);
                m.writeToFile(matrixfile);                                                                                           
            }
            else if (cmd.equals(CMD_SORT_ROWS_ROWWISE)) {
                for (String[] cols: IOUtils.readTable(System.in)) {
                    Arrays.sort(cols);
                    System.out.println(StringUtils.arrayToString(cols, " "));
                }
            }
            else if (cmd.equals(CMD_DUPLICATE_ROWS)) {
                // line numbering starts from 1!!!!
                // usage: java util.io.FileUtils duplicaterows <filename> (write to stdout)
                File file = new File(argParser.shift("filename"));                
                boolean includesHeader = argParser.isDefined("includesheader");                                                                                                                                                                                                                
                String[] lines = IOUtils.readLineArray(file);
                List dataLines;
                if (includesHeader) {
                    dataLines = CollectionUtils.tailList(Arrays.asList(lines), 1);
                    String header = lines[0];
                    System.out.println(header);
                }
                else {
                    dataLines = Arrays.asList(lines);
                }
                List duplicatedLines = CollectionUtils.duplicateElements(dataLines);
                String[] linesToPrint = (String[])ConversionUtils.collectionToArray(duplicatedLines, String.class);
                IOUtils.printLines(System.out, linesToPrint);
            }
            else if (cmd.equals(CMD_ONES)) {
                // line numbering starts from 1!!!!
                // usage: java util.io.FileUtils ones <numrows> (write to stdout)                                
                int numRows = argParser.shiftInt("numrows");                                                                                                                                                                                                                
                for (int i=0; i<numRows; i++) {
                    System.out.println("1");
                }
            }
            else if (cmd.equals(CMD_PASTE)) {
                // append files linewise
                // usage: java util.io.FileUtils paste <file1> <file2> (write to stdout)
                String file1 = argParser.shift("file1");
                String file2 = argParser.shift("file2");                                                                
                String[] lines1 = IOUtils.readLineArray(file1);
                String[] lines2 = IOUtils.readLineArray(file2);
                if (lines1.length != lines2.length) {
                    throw new RuntimeException("Different number of lines; cannot paste!");
                }               
                int numRows = lines1.length;
                for (int i=0; i<numRows; i++) {
                    dbgMsg("line1:<"+lines1[i]+">");
                    dbgMsg("line2:<"+lines2[i]+">");
                    String tmp = lines1[i] + lines2[i];
                    dbgMsg("pasted line:<"+tmp+">");                    
                    System.out.println(tmp);
                }
            }
            else if (cmd.equals(CMD_SELECT_MAX_ROWS)) {
                // select rows which have max value among rows having same id
                // usage: java util.io.FileUtils selectmaxrows <idcolname> <valcolname> (read stdin, write to stdout)
                String idCol = argParser.getOpt("idcolname");
                String valCol = argParser.getOpt("valcolname");
                boolean twoRowsAsOne =  argParser.isDefined("tworowsasone");
                Matrix m = new Matrix(true);
                m.readFromStream(System.in);
                Matrix result = m.selectMaxRows(idCol, valCol, twoRowsAsOne);
                result.writeToStream(System.out);                                                                                               
            }
            // now this is tasteless:
            else if (cmd.equals(CMD_BINARY_OPERATOR)) {
                // calculate logical greater than operation between two columns of matrix
                // usage: java util.io.FileUtils binaryoperator <colname1> <colname2> (read stdin, write to stdout)
                String col1 = argParser.shift("col1");
                String col2 = argParser.shift("col2");
                String operClassName = argParser.getOpt("operator");
                String header = argParser.getOpt("header");
                BinaryOperator operator = (BinaryOperator)ReflectionUtils.createInstance(operClassName);                
                Matrix m = new Matrix(true);
                m.readFromStream(System.in);
                double[] result = m.calculate(col1, col2, operator);
                if (header != null) {
                    System.out.println(header);
                }
                System.out.println(StringUtils.arrayToString(result, "\n"));                
            }
            else if (cmd.equals(CMD_UNARY_OPERATOR)) {
                // calculate logical greater than operation between two columns of matrix
                // usage: java util.io.FileUtils unaryoperator <colname> (read stdin, write to stdout)
                String col = argParser.shift("col");                
                String operClassName = argParser.getOpt("operator");
                UnaryOperator operator = (UnaryOperator)ReflectionUtils.createInstance(operClassName);
                String header = argParser.getOpt("header");                
                Matrix m = new Matrix(true);
                m.readFromStream(System.in);
                double[] result = m.calculate(col, operator);
                if (header != null) {
                    System.out.println(header);
                }
                System.out.println(StringUtils.arrayToString(result, "\n"));                
            }
            else if (cmd.equals(CMD_SELECT_BY_ID_LIST)) {
            	IOUtils.setFastStdout();
                String subsetidfile = argParser.shift();
                int col = argParser.shiftInt(); 
                Logger.info("subsetidfile: "+subsetidfile);
                Logger.info("col: "+col);
                HashSet<String> ids = new HashSet<String>(IOUtils.readLines(subsetidfile));
                Iterator<String> iter = IOUtils.lineIterator();
                while (iter.hasNext()) {
                	String line = iter.next();
                	String key = StringUtils.extractCol(line, ' ', col);
                	if (ids.contains(key)) {
                		System.out.println(line);
                	}
                }
                System.out.flush();
                System.out.close();
                
            }            
            else if (cmd.equals(CMD_SELECT_BY_ID_LIST_2KEY)) {
            	// ad hoc, read code plz
            	IOUtils.setFastStdout();                
                String subsetidfile = argParser.shift();
                int col1 = argParser.shiftInt();
                int col2 = argParser.shiftInt();
                Logger.info("subsetidfile: "+subsetidfile);
                Logger.info("col1: "+col1);
                Logger.info("col2: "+col2);
                HashSet<String> ids = new HashSet<String>(IOUtils.readLines(subsetidfile));
                Iterator<String> iter = IOUtils.lineIterator();
                while (iter.hasNext()) {
                	String line = iter.next();
                	String key1 = StringUtils.extractCol(line, ' ', col1);
                	String key2 = StringUtils.extractCol(line, ' ', col2);
                	String key = key1+" "+key2;
                	if (ids.contains(key)) {
                		System.out.println(line);
                	}
                }
                System.out.flush();
                System.out.close();
                
            }
            else if (cmd.equals(CMD_SELECT)) {
                Matrix inputMatrix = new Matrix(true);
                inputMatrix.readFromStream(System.in);                 
                Matrix resultMatrix = inputMatrix.select(args);
                System.out.println(resultMatrix);                
            }
            else if (cmd.equals(CMD_FIND)) {
                System.out.println("args: "+StringUtils.arrayToString(args));
                String pattern = argParser.getOpt("name");
                Condition<File> condition;
                if (pattern != null) {
                    condition = new FileNameCondition(pattern);
                }
                else {
                    condition = null;
                }
                List<File> files;
                if (args.length == 0) {
                    files = Collections.singletonList(new File("."));
                }
                else {
                    files = new ArrayList<File>();
                    for (String name: args) {                        
                        files.add(new File(name));
                    }
                }
                
                for (File d: files) {
                    if (!d.exists()) {
                        System.err.println("No such file: "+d);
                    }
                    else if (d.isDirectory()) {                     
                        List<File> foundFiles = find(d, condition);
                        for (File f: foundFiles) {
                            System.out.println(f.getPath());
                        }
                    }
                    else {
                        // actually, only a pesky file
                        if (condition == null || condition.fulfills(d)) {
                            System.out.println(d.getPath());
                        }
                    }
                }
            }            
            else if (cmd.equals(CMD_COLSUMS)) {
                Matrix inputMatrix = new Matrix(true);
                inputMatrix.readFromStream(System.in);                 
                double result[] = inputMatrix.colSums();
                System.out.println(StringUtils.arrayToString(result, " "));                
            }
            else if (cmd.equals(CMD_IS_SUBMAP)) {
                // usage: java util.io.FileUtils is-submap <submapfile> <supermapfile>
                String subMapFile = argParser.shift("submapfile");
                String superMapFile = argParser.shift("supermapfile");
                Map<String,String> subMap = IOUtils.readMap(new File(subMapFile));
                Map<String,String> superMap = IOUtils.readMap(new File(superMapFile));
                boolean result = CollectionUtils.isSubMap(subMap, superMap);
                System.out.println(result);                
            }            
            else if (cmd.equals(CMD_IS_SUBLIST)) {
                // usage: java util.io.FileUtils is-submap <submapfile> <supermapfile>
                String subListFile = argParser.shift("sublistfile");
                String superListFile = argParser.shift("superlistfile");
                List<String> subList = IOUtils.readLines(subListFile);
                List<String> superList = IOUtils.readLines(superListFile);
                boolean result = CollectionUtils.isSubList(subList, superList);
                System.out.println(result);                
            }
            else if (cmd.equals(CMD_COLAVERAGES)) {
                Matrix inputMatrix = new Matrix(false);
                inputMatrix.readFromStream(System.in);                 
                double result[] = inputMatrix.colAvgs();
                System.out.println(StringUtils.arrayToString(result, " "));                
            }            
            else if (cmd.equals(CMD_COLAVERAGES)) {
                Matrix inputMatrix = new Matrix(false);
                inputMatrix.readFromStream(System.in);                 
                double result[] = inputMatrix.colAvgs();
                System.out.println(StringUtils.arrayToString(result, " "));                
            }
            else if (cmd.equals(CMD_UNION)) {
                String file0 = args[0];                
                Set<String> result = new LinkedHashSet<String>(IOUtils.readLines(file0));
                Logger.info("First file has "+result.size()+" lines");
                for (int i=1; i<args.length; i++) {
                    Set<String> lines = new LinkedHashSet(IOUtils.readLines(args[i]));                  
                    Logger.info(StringUtils.formatOrdinal(i+1)+" file has "+lines.size()+" lines");
                    result.addAll(lines);
                }
                Logger.info("There are "+result.size()+" lines in the result.");                
                for (String line: result) { 
                    System.out.println(line);
                }                                                  
            }
            else if (cmd.equals(CMD_MULTIDIFF)) {                
                int maxReportedCount = argParser.getIntOpt("maxcount", Integer.MAX_VALUE);
                // Logger.info("maxReportedCount: "+maxReportedCount);
                List<String> fileNames = Arrays.asList(args);               
                List<Set<String>> fileSets = new ArrayList();
                for (String file: fileNames) {
                    boolean addedToExistingSet = false;
                    // check if file belongs to some existing set
                    for (Set<String> fileSet: fileSets) {
                        String fileInSet = fileSet.iterator().next();
                        boolean differs = differs(new File(file), new File(fileInSet));
                        if (!differs) {
                            // add file to set
                            fileSet.add(file);
                            addedToExistingSet = true;
                            // break out of inner loop
                            break;
                        }
                    }
                    if (!addedToExistingSet) {
                        // create new set for this file
                        Set<String> newSet = new HashSet();
                        newSet.add(file);
                        fileSets.add(newSet);
                    }
                }
                
                // output sets
                System.out.println("Number of sets: "+fileSets.size());
                System.out.println("The files within each of the following sets are identical:");
                
//                boolean first = true;
                for (Set<String> fileSet: fileSets) {
                    // if (!first) {
                        System.out.println("--------------------------------------------------------------------------------");                        
//                    }
//                    else {
//                        first = false;
//                    }
                    if (fileSet.size() <= maxReportedCount) {
                        System.out.println(StringUtils.collectionToString(fileSet, "\n"));
                    }
                    else {
                        // Logger.info("Not reporting!");
                    }
                }
            }
            else if (cmd.equals(CMD_COMPARE_SETS)) {                // 
                boolean outputFiles = argParser.isDefined("o"); 
                                
                ArrayList<Set<String>> sets = new ArrayList<Set<String>>();
                for (String f: args) {
                    Set<String> set = new HashSet();
                    set.addAll(IOUtils.readLines(f));
                    sets.add(set);
                }
                List<String> setNames = Arrays.asList(args);
                setNames = StringUtils.removeLongestCommonPrefix(setNames);
                setNames = StringUtils.removeLongestCommonSuffix(setNames);
                compareSets(sets, setNames, outputFiles);
            }
            else if (cmd.equals(CMD_JACCARDDISTANCE)) {
            	String f1 = argParser.shift();
            	String f2 = argParser.shift();
            	Set<String> set1 = new HashSet<String>(IOUtils.readLines(f1));
            	Set<String> set2 = new HashSet<String>(IOUtils.readLines(f2));
            	System.out.println(CollectionUtils.jaccardDistance(set1, set2));
            }
            else if (cmd.equals(CMD_DIFF)) {
                String file1 = args[0];
                String file2 = args[1];
                boolean differs = differs(new File(file1), new File(file2));
                if (differs) {
                    System.out.println("Files differ");
                }
            }
            else if (cmd.equals(CMD_INTERSECTION)) {            	                
            	String file0 = args[0];
                Set<String> result = new LinkedHashSet<String>(IOUtils.readLines(file0));
            	Logger.info("First file has "+result.size()+" lines");
            	for (int i=1; i<args.length; i++) {
            		Set<String> lines = new LinkedHashSet(IOUtils.readLines(args[i]));            		
            		Logger.info(StringUtils.formatOrdinal(i+1)+" file has "+lines.size()+" lines");
            		result.retainAll(lines);
            	}
            	Logger.info("There are "+result.size()+" lines in the result.");
            	for (String line: result) { 
            		System.out.println(line);
            	}
            }
            else if (cmd.equals(CMD_INTERSECTION_NEW)) {
            	if (!(args.length == 2)) {
            		Utils.die("Invalid number of args: "+args.length);
            	}            		            
            
            	String file0 = args[0];
            	String file1 = args[1];
                Set<String> set0 = new LinkedHashSet<String>(IOUtils.readLines(file0));
                Set<String> set1 = new LinkedHashSet<String>(IOUtils.readLines(file1));
            	Logger.info("First set has "+set0.size()+" objects");
            	Logger.info("First set has "+set1.size()+" objects");
            	Set<String> result = CollectionUtils.intersection(set0, set1);
            	Logger.info("Result has "+result.size()+" objects.");
            	for (String line: result) { 
            		System.out.println(line);
            	}
            }    
            else if (cmd.equals(CMD_MAKE_ORDERED_PAIRS)) {                                
                String file0 = args[0];
                List<String> objects = IOUtils.readLines(file0);
                Logger.info("There are "+objects.size()+" objects in input");
                Pair[] result = CollectionUtils.make2Combinations(objects);                
                Logger.info("There are "+result.length+" unordered pairs in the result, which makes for "+result.length*2+" ordered pairs.");
                for (Pair pair: result) { 
                    System.out.println(pair.getObj1()+" "+pair.getObj2());
                    System.out.println(pair.getObj2()+" "+pair.getObj1());
                }
            }            
            else if (cmd.equals(CMD_MAKE_UNORDERED_PAIRS)) {                               
                String file0 = args[0];
                List<String> objects = IOUtils.readLines(file0);
                Logger.info("There are "+objects.size()+" objects in input");
                Pair[] result = CollectionUtils.make2Combinations(objects);                
                Logger.info("There are "+result.length+" unordered pairs in the result");
                for (Pair pair: result) {
                    String s1 = (String)pair.getObj1();
                    String s2 = (String)pair.getObj2();
                    if (s1.compareTo(s2) < 0) {
                        System.out.println(s1+" "+s2);
                    }
                    else if (s1.compareTo(s2) > 0) {
                        System.out.println(s1+" "+s2);
                    }
                    else {
                        throw new RuntimeException();
                    }
                    
                }
            }
            else if (cmd.equals(CMD_COMBINE_WEIGHTED_GOODNESSES)) {
                // join "weighted goodnesses" in two files $1 and $2                
                //
                // input:
                //   -col1 = key
                //   -col2 = weight
                //   -col3 = goodness
                
                // result will have:
                //   -col1 = key
                //   -col2 = weighted average of the two keys in the two files,
                //           according to the two corresponding weights

                String file1 = args[0];
                String file2 = args[1];

                Set<String> lines1 = new LinkedHashSet<String>(IOUtils.readLines(file1));
                Set<String> lines2 = new LinkedHashSet<String>(IOUtils.readLines(file2)); 
                Logger.info("First file has "+lines1.size()+" lines");
                Logger.info("Second file has "+lines2.size()+" lines");
                
                Map<String, Pair<Double, Double>> map1 = new HashMap();
                Map<String, Pair<Double, Double>> map2 = new HashMap();
                
                for (String line: lines1) {
                    String[] tok = line.split("\\s+");
                    String key = tok[0];
                    double w = Double.parseDouble(tok[1]);
                    double g = Double.parseDouble(tok[2]);
                    map1.put(key, new Pair(w,g));                   
                }
                
                for (String line: lines2) {
                    String[] tok = line.split("\\s+");
                    String key = tok[0];
                    double w = Double.parseDouble(tok[1]);
                    double g = Double.parseDouble(tok[2]);
                    map2.put(key, new Pair(w,g));                   
                }
                
                LinkedHashSet<String> allKeys = new LinkedHashSet();
                allKeys.addAll(map1.keySet());
                allKeys.addAll(map2.keySet());
                Map<String, Double> result = new LinkedHashMap(); 
                
                for (String key: allKeys) {
                    Pair<Double,Double> data1 = map1.get(key);
                    Pair<Double,Double> data2 = map2.get(key);
                    if (data1 == null) {
                        // use goodness from data2 as such
                        result.put(key, data2.getObj2());   
                    }
                    else if (data2 == null) {
                        // use goodness from data1 as such
                        result.put(key, data1.getObj2());
                    }
                    else {
                        // actually perform a weighted average
                        double w1 = data1.getObj1();
                        double g1 = data1.getObj2();
                        double w2 = data2.getObj1();
                        double g2 = data2.getObj2();
                        double wAvg = MathUtils.weightedAvg(g1, w1, g2, w2);
                        result.put(key, wAvg);
                    }
                }
                
                for (String key: allKeys) {
                    double val = result.get(key);
                    System.out.println(key+" "+SU.format(val));
                }
            }                        
            else if (cmd.equals(CMD_JOIN)) {
                // a simple join based on the first col
                // works by reading second file into a hashmap
                IOUtils.setFastStdout();
                
                if (args.length == 2) {
                    // read 2 input files
                    String file1 = args[0];
                    String file2 = args[1];
                    
//                    Logger.info("Args: "+cla);
                    
                    boolean fillInZeroValues = argParser.isDefined("fill_in_zero_vals");
                    Logger.info("Filling in zero values: "+fillInZeroValues);
                    
                    Set<String> lines1 = new LinkedHashSet<String>(IOUtils.readLines(file1));
                    Set<String> lines2 = new LinkedHashSet<String>(IOUtils.readLines(file2)); 
                    Logger.info("First file has "+lines1.size()+" lines");
                    Logger.info("Second file has "+lines2.size()+" lines");                
                    // put lines of second file into map
                    Map<String, String> map = new HashMap();                
                    // Logger.info("There are "+result.size()+" lines in the result.");
                    for (String line: lines2) {
                        String[] tokens = line.split("\\s+");
                        String key = tokens[0];
                        String data = line.substring(key.length()); 
                        map.put(key, data);
                    }
                    
                    Set<String> keysMissingFromData1 = null;
                    if (fillInZeroValues) {
                        // initially, consider all keys in data 2 as potentially missing from data 1
                        keysMissingFromData1 = new LinkedHashSet(map.keySet());
                    }
                    
                    for (String line: lines1) {
                        String[] tokens = line.split("\\s+");
                        String key = tokens[0];
                        String data1 = line.substring(key.length());
                        String data2 = map.get(key);
                        if (data2 != null) {                            
                            System.out.println(key+data1+data2);
                        }
                        else {
                            if (fillInZeroValues) {
                                // output a value missing from data file 2 as "0"
                                System.out.println(key+data1+" 0");
                            }
                        }
                        
                        if (fillInZeroValues) {
                            keysMissingFromData1.remove(key);
                        }
                    }
                    
                    if (fillInZeroValues) {
                        // output values that were missing from data file 1 as "0":s
                        for (String key: keysMissingFromData1) {
                            String data2 = map.get(key);
                            System.out.println(key+" 0"+data2);
                        }
                    }
                    
                }
                else {
                    // read everything from stdin
                    // assume each key comes exactly twice!
                    
                    List<String> lines = IOUtils.readLines();
                                  
                    Map<String, String> map1 = new HashMap();
                    Map<String, String> map2 = new HashMap();
                    
                    // Logger.info("There are "+result.size()+" lines in the result.");
                    for (String line: lines) {
                        String[] tokens = line.split("\\s+");
                        String key = tokens[0];
                        String data = line.substring(key.length());
                        if (!(map1.containsKey(key))) {                            
                            map1.put(key, data);
                        }
                        else if (!(map2.containsKey(key))) {
                            map2.put(key, data);
                        }
                        else {
                            throw new RuntimeException("Triplicate key: "+key);
                        }
                    }
                    
                    for (String key: map1.keySet()) {
                        String data1 = map1.get(key);
                        String data2 = map2.get(key);
                        if (data2 != null) {
                            System.out.println(key+data1+data2);
                        }
                    }
                }
                
                System.out.flush();
                
            }                
            else if (cmd.equals(CMD_JOIN_TABDELIMITED)) {
                // a simple join based on the first col
                // works by reading second file into a hashmap
                IOUtils.setFastStdout();
                
                if (args.length == 2) {
                    // read 2 input files
                    String file1 = args[0];
                    String file2 = args[1];
                    
//                    Logger.info("Args: "+cla);
                    
                    boolean fillInZeroValues = argParser.isDefined("fill_in_zero_vals");
                    Logger.info("Filling in zero values: "+fillInZeroValues);
                    
                    Set<String> lines1 = new LinkedHashSet<String>(IOUtils.readLines(file1));
                    Set<String> lines2 = new LinkedHashSet<String>(IOUtils.readLines(file2)); 
                    Logger.info("First file has "+lines1.size()+" lines");
                    Logger.info("Second file has "+lines2.size()+" lines");                
                    // put lines of second file into map
                    Map<String, String> map = new HashMap();                
                    // Logger.info("There are "+result.size()+" lines in the result.");
                    for (String line: lines2) {
                        String[] tokens = line.split("\t");
                        String key = tokens[0];
                        String data = line.substring(key.length()); 
                        map.put(key, data);
                    }                                                           
                    
                    for (String line: lines1) {
                        String[] tokens = line.split("\t");
                        String key = tokens[0];
                        String data1 = line.substring(key.length());
                        String data2 = map.get(key);
                        if (data2 != null) {                            
                            System.out.println(key+data1+data2);
                        }                                                                       
                    }
                }
                else {
                	System.err.println("Illegal number of args");
                	System.exit(1);
                }
                
                System.out.flush();
                
            }
            else if (cmd.equals(CMD_JOIN_PAIRS)) {
                // a simple join based on the UNORDERED pairs defined by first two cols
                String file1 = args[0];
                String file2 = args[1];
                
                Set<String> lines1 = new LinkedHashSet<String>(IOUtils.readLines(file1));
                Set<String> lines2 = new LinkedHashSet<String>(IOUtils.readLines(file2)); 
                Logger.info("First file has "+lines1.size()+" lines");
                Logger.info("Second file has "+lines2.size()+" lines");                
                // put lines of second file into map
                Map<UnorderedPair<String>, String> map = new HashMap();                
                // Logger.info("There are "+result.size()+" lines in the result.");
                for (String line: lines2) {
                    List<String> tokens = Arrays.asList(line.split("\\s+"));
                    UnorderedPair<String> key = new UnorderedPair(tokens.get(0), tokens.get(1));
                    String data = SU.toString(CollectionUtils.tailList(tokens, 2), " ");
                    map.put(key, data);
                }
                
                for (String line: lines1) {
                    List<String> tokens = Arrays.asList(line.split("\\s+"));
                    UnorderedPair<String> key = new UnorderedPair(tokens.get(0), tokens.get(1));
                    String data1 = SU.toString(CollectionUtils.tailList(tokens, 2), " ");
                    String data2 = map.get(key);
                    if (data2 != null) {
                        System.out.println(key+" "+data1+" "+data2);
                    }
                }
            }
            else if (cmd.equals(CMD_FAST_UNIQ)) {                
                Iterator<String> lineIterator = IOUtils.lineIterator(System.in);
                ArrayList<Boolean> found = new ArrayList(1000);
                // Set<String> uniqueLines = new HashSet();
                while(lineIterator.hasNext()) {
                    int val = Integer.parseInt(lineIterator.next());
                    if (found.size() < val+1) {
                        found.ensureCapacity(val+1);
                        int diff = val+1-found.size();
                        for (int i=0; i<diff; i++) {
                            found.add(null);
                        }
                    }
                    found.set(val, true);
                    //int val = 
//                    uniqueLines.add(lineIterator.next());
                }
                int n = found.size();
                for (int i=0; i<n; i++) {
                    if (found.get(i) != null) {
                        System.out.println(i);
                    }
                    
                }
//                for (String line: uniqueLines) { 
//                    System.out.println(line);
//                }
            }
            else if (cmd.equals(CMD_FIND_GAPS)) {
                int[] data = IOUtils.readInts(new File(args[0]));
                int thresh = Integer.parseInt(args[1]);
                List<Range> gaps = findGaps(data, thresh);
                for (Range gap: gaps) {
                    System.out.println(""+gap.start+"-"+gap.end);
                }
            }            
            else if (cmd.equals(CMD_IS_ORDERED)) {
                Iterator<String> lineIterator;                           
                                                
                if (args.length > 0) {
                    for (String f: args) {
                        lineIterator = IOUtils.lineIterator(f);
                        checkOrdered(lineIterator, "in file "+f+" ");
                    }
                }
                else {
                    lineIterator = IOUtils.lineIterator(System.in);
                    checkOrdered(lineIterator, "");
                }
//                
//                Double prev = null;
//                while(lineIterator.hasNext()) {
//                    double val = Double.parseDouble(lineIterator.next());
//                    if (prev != null && val < prev) {
//                        System.out.println("Values are not ordered ("+prev+" > "+val+")");
//                        System.exit(0);
//                    }                    
//                    prev = val;
//                }
//                System.out.println("Values are ordered.");
            }
            else if (cmd.equals(CMD_UNIQ)) {
                IOUtils.setFastStdout();
                if (argParser.isDefined("c")) {
                    // counts also
                    Iterator<String> lineIterator = IOUtils.lineIterator(System.in);
                    MultiSet<String> counts = new HashMultiSet();                    
                    while(lineIterator.hasNext()) {                        
                        counts.add(lineIterator.next());
                    }
                    for (String line: counts) { 
                        System.out.println(line+" "+counts.getCount(line));
                    }
                }
                else if (argParser.isDefined("C")) {
                    // only output count of distinct elements and count total count
                    // of elements
                    Iterator<String> lineIterator = IOUtils.lineIterator(System.in);                    
                    // HashSet<String> uniqueLines = new HashSet();
                    MultiSet<String> uniqueLines = new HashMultiSet();
                    MultiSet<Integer> distribution = new HashMultiSet();
                    int lineCount = 0;                    
                    while(lineIterator.hasNext()) {
                        lineCount++;
                        uniqueLines.add(lineIterator.next());
                    }
                    for (String line: uniqueLines) {
                        int count = uniqueLines.getCount(line);
                        distribution.add(count);
                    }                    
                    System.out.println("overall_count: "+lineCount);
                    for (Integer count: new TreeSet<Integer>(distribution)) {
                        int countCount = distribution.getCount(count);
                        System.out.println(""+count+" "+countCount);
                    }                   
                    // System.out.println("count_of_unique_lines: "+uniqueLines.size());
                }
                else {
                    // no counts        
                    // A hash-based uniq (hopefully more efficient than standard unix
                    // sort in some cases where the number of different entries is small)
                    // line order in result is arbitrary
                	
                	// Logger.enableLogging();
                	// Logger.setLogLevel(1);
                	
                	// dbgMsg("no counts!");
                	
                	// if -col defined, only consider column col. column indexing begins from 0
                	Integer col = argParser.getIntOpt("col");
                	// dbgMsg("col: "+col);
                	
                    Iterator<String> lineIterator = IOUtils.lineIterator(System.in);
                    Set<String> seen = new HashSet();
                    while (lineIterator.hasNext()) {
                    	String line = lineIterator.next();
                    	String val;
                    	if (col != null) {
                    		val = line.split("\\s+")[col];
                    		// dbgMsg("val: "+val);
                    	}
                    	else {
                    		val = line;
                    	}
                    	if (!(seen.contains(val))) {
                    		// new line                    		
                    		System.out.println(line);
                    		seen.add(val);
                    	}                       
                    }                    
                }                
                System.out.flush();
            }
            else if (cmd.equals(CMD_MAX)) {
                double max = -Double.MAX_VALUE;
                Iterator<String> lineIterator = IOUtils.lineIterator(new BufferedInputStream(System.in));                
                while(lineIterator.hasNext()) {
                    String line = lineIterator.next();
                    double val = Double.parseDouble(line);
                    if (val > max) {
                        max = val;                      
                    }
                }
                if ((int)max == max) {
                    System.out.println((int)max);
                }
                else {
                    System.out.println(max);
                }
            }
            else if (cmd.equals(CMD_SIZE)) {
                File file = new File(args[0]);
                System.out.println(file.length());
                           
            }
            else if (cmd.equals(CMD_ENSURE_INTERSECTION_IS_CANONICALLY_ORDERED)) {
            	List<List<String>> lists = new ArrayList<List<String>>();
            	for (int i=0; i<args.length; i++) {
            		List<String> list = new LinkedList(IOUtils.readLines(args[i]));
            		Logger.info(StringUtils.formatOrdinal(i+1)+" file has "+list.size()+" lines");
            		lists.add(list);
            	}
            	Set<String> common = new LinkedHashSet(lists.get(0));
            	
            	for (int i=1; i<lists.size(); i++) {
            		Logger.info("Removing entries not in "+StringUtils.formatOrdinal(i+1)+" file from set of common entries");
            		common.retainAll(new HashSet(lists.get(i)));
            	}
            	
            	Logger.info("There are "+common.size()+" common entries.");
            	            	            	
            	for (int i=0; i<lists.size(); i++) {
            		Logger.info("Removing non-common entries from "+StringUtils.formatOrdinal(i+1)+" list");
            		lists.get(i).retainAll(common);
            	}
            	
            	// boolean result = true;
            	for (int i=1; i<lists.size(); i++) {
            		if (!(lists.get(0).equals(lists.get(i)))) {            			
            			System.out.println("Differing orderings in files "+args[0]+" and "+args[i]);
            			System.exit(-1);
            		}
            	}
            	System.out.println("All files have same ordering of common entries.");            	
            }
            else if (cmd.equals(CMD_MINUS)) {
                IOUtils.setFastStdout();
                String file1 = args[0];
                String file2 = args[1];
                Set<String> set = new HashSet(Arrays.asList(IOUtils.readLineArray(file1)));
                LineIterator i = new LineIterator(file2);
                while (i.hasNext()) {
                	String line = i.next();
                	set.remove(line);
                }
                
                for (String line: set) {
                    System.out.println(line);
                }
//                for (String line: IO)
//                Set<String> set2 = new HashSet(Arrays.asList(IOUtils.readLineArray(file2)));                
//                Set<String> result = CollectionUtils.minus(set1, set2);
                
//                if (result.size() > 0) {
//                    System.out.println(StringUtils.collectionToString(result));
//                    for (String line: result) {
//                        System.out.println(line);
//                    }
//                }
                System.out.flush();
            }
            else if (cmd.equals(CMD_PICK_BALANCED_SET)) {                
                // given num of objects to pick, pick an equally large number
                // of objects with each key (differing by at most 1),
                // in the order that the objects appear in the input
                // key is col1
                // allow only one occurence of each key2 in col2
                
                // so, there are 2 sets to pair (col1 and col2), and
                // col1 has a limited supply of keys, which means
                // we have to pick multiple copies of each
                // col2 has an "unlimited" supply of keys,
                // which means we pick at most one of each key2
                IOUtils.setFastStdout();                
                String file1 = args[0];
                int totalNumToPick = Integer.parseInt(args[1]);
                Set<String> keys= new HashSet();
                MultiSet<String> keyCounts = new HashMultiSet();
                List<String> lines = IOUtils.readLines(file1);
                for (String line: lines) {
                    String[] tok = line.split("\\s+");
                    String key = tok[0];
//                    Logger.info("Adding key: "+key);
                    keys.add(key);
                    keyCounts.add(key);
                }
                Logger.info("There are "+keys.size() + " keys");
//                Logger.info("Key counts: "+keyCounts);
                int numToPickOfEach = totalNumToPick / keys.size();
                int remainder = totalNumToPick % keys.size();
                Logger.info("Pick at least "+numToPickOfEach+" of each key");
                Logger.info("Pick 1 more of "+remainder+" keys");
                MultiSet<String> picksLeft = new HashMultiSet();
                Set<String> oneMorePick = new HashSet(RandUtils.sampleWithoutReplacement(new ArrayList(keys), remainder)); 
                for (String key: keys) {
                    int n = numToPickOfEach;
                    if (oneMorePick.contains(key)) {
                        n += 1;
                    }
                    picksLeft.add(key, n);
                }
                
                // just a small assert now:
                for (String key: picksLeft) {
                    int numToPick = picksLeft.getCount(key);
                    int count = keyCounts.getCount(key);
                    
                    if (numToPick > count) {
//                        Logger.info("Key: "+key);
//                        Logger.info("num to pick: "+numToPick);
//                        Logger.info("count: "+count);
                        Utils.die("Not enough occurences of key: "+key);
                    }
                }
                
                Set<String> pickedKeys2 = new HashSet();
                int totalPicksLeft = totalNumToPick;
                for (String line: lines) {
                    String[] tok = line.split("\\s+");
                    String key = tok[0];
                    if (picksLeft.getCount(key) > 0) {
                        String key2 = tok[1];
                        if (!(pickedKeys2.contains(key2))) {
                            // pick this
                            System.out.println(line);
                            picksLeft.decrease(key);
                            totalPicksLeft--;
                            pickedKeys2.add(key2);
                            if (totalPicksLeft == 0) {
                                break;
                            }
                        }
                    }                    
                }
                
                for (String key: picksLeft) {
                    if (picksLeft.getCount(key) > 0) {
                        Utils.die("Could not pick enough of: "+key);
                    }
                }
                                                                   
                System.out.flush();
            }            
            else if (cmd.equals(CMD_MINUS2)) {
                // If set2 is larger than set1, this should be at least
                // more memory-efficient
                IOUtils.setFastStdout();
                String file1 = args[0];
                String file2 = args[1];
                // Logger.info("Reading file2 ("+file2+") into RAM...");
                Set set2 = new HashSet();
                for (String line2: IOUtils.lines(file2)) {
                    set2.add(line2);
                }        
                
                // Logger.info("Scanning file1 ("+file1+")...");
                for (String line1: IOUtils.lines(file1)) {
                    if (!(set2.contains(line1))) {
                        System.out.println(line1);
                    }
                }
                
                System.out.flush();
            }
            else if (cmd.equals(CMD_SYM_DIFF)) {
                String file1 = args[0];
                String file2 = args[1];
                Set set1 = new LinkedHashSet(Arrays.asList(IOUtils.readLineArray(file1)));
                Set set2 = new LinkedHashSet(Arrays.asList(IOUtils.readLineArray(file2)));
                Set result = CollectionUtils.symmetricDifference(set1, set2, new HashSet());                
                
                if (result.size() > 0) {
                    System.out.println(StringUtils.collectionToString(result));
                }
            }
            else if (cmd.equals(CMD_RELATIVE_PATH)) {                               
                File child = new File(args[0]);
                File parent = new File(args[1]);
                String relativePath = getPathRelativeTo(child, parent);
                System.out.println(relativePath);                         
            }
            else {
            	usageAndExit("Illegal command: "+cmd);
//            	Utils.die("Unknown command: "+cmd+".\n"+
//                "List of possible commands:\n"+
//                StringUtils.arrayToString(ALL_FILEUTILS_COMMANDS));    
            }                                
        }        
        catch (Exception e) {
            Utils.die(e);
        }        
        // always do this nuisance:
        Logger.endLog();
    }            
    
    private static boolean isOrdered(int[] data) {
        int prev = -Integer.MAX_VALUE;
        for (int i=0; i<data.length; i++) {            
            if (data[i] < prev) {
                return false;
            }                    
            prev = data[i];
        }
        return true;
    }
    
    private static List<Range> findGaps(int[] data, int gapThresh) {
        if (!isOrdered(data)) {
            Arrays.sort(data);
        }
        List<Range> gaps = new ArrayList<Range>();
        int prev = data[0];
        for (int i=1; i<data.length; i++) {
            if (data[i] - prev >= gapThresh ) {
                gaps.add(new Range(prev, data[i]));
            }
            prev = data[i];
        }
        
        return gaps;
    }
    
    private static void compareSets(List<Set<String>> sets, List<String> pSetNames, boolean outputFiles) {

        Map<String, Integer> setIndices= new HashMap();
        
        int nSets = sets.size();

        List<String> setNames;
        
        if (pSetNames != null) {
        	// set names given
        	setNames = pSetNames;
        }
        else {
        	// set names not given, generate names based on the order of the list
        	setNames = new ArrayList();
        	for (int i=0; i<sets.size(); i++) {
	            String name = "set"+i;
	            setIndices.put(name, i);
	            setNames.add(name);
	        }
        }
        
        Set<String> union = CollectionUtils.union(sets);
        MultiMap<BitSet, String> itemsByBitset = new MultiMap();
//        Map<String, BitSet> bitsetByItem = new HashMap(); 
        
        for (String item: union) {
            BitSet bs = new BitSet(nSets);
            for (int i=0; i<nSets; i++) {
                Set<String> set = sets.get(i);
                if (set.contains(item)) {
                    bs.set(i);
                }
            }
            itemsByBitset.put(bs, item);            
        }
        
        Map<BitSet, String> bitsetReps = new HashMap();
        for (BitSet bs: itemsByBitset.keySet()) {
            StringBuffer rep = new StringBuffer();            
        
            for (int i=0; i<nSets; i++) {
                if (bs.get(i)) {
                    String setName = setNames.get(i);
                    if (rep.length() == 0) {
                        rep.append(setName);
                    }
                    else {
                        rep.append(" ∩ "+setName);
                    }
                }                
            }
            
            bitsetReps.put(bs, rep.toString());
        }
        
        for (BitSet bs: itemsByBitset.keySet()) {
            String rep = bitsetReps.get(bs);
            Set<String> items = itemsByBitset.get(bs);
            if (outputFiles) {
                System.out.println("Outputting "+items.size()+" items to "+rep+".items");
                try {
                    FileOutputStream fos = new FileOutputStream(rep+".items");
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 65536);
                    PrintStream ps = new PrintStream(bos, false); // the last parameter is "autoflush"
                    for (String item: items) {
                        ps.println(item);                    
                    }
                    ps.flush();
                    ps.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(rep+": "+items.size()+" items");
            }
        }

    }
    
    private static void usageAndExit(String pErrMsg) {				
		Logger.error(pErrMsg);
		TreeSet<String> availableCommands = new TreeSet(ReflectionUtils.getPublicStaticStringFieldsWithPrefix(FileUtils.class, "CMD_"));
		Logger.info("List of available commands:\n"+StringUtils.collectionToString(availableCommands));
		System.exit(-1);		
	}
        

    
    public static void recursivelyRemoveDir(File pDir) throws IOException {
        if (!(pDir.isDirectory())) {
            throw new RuntimeException("Not a directory!");
        }
        DeleteFileOperation deleteOper = new DeleteFileOperation(true, true);
        deleteOper.doOperation(pDir);
        dbgMsg("Dir "+pDir+" deleted.");
    }
    
    public static TreeSet allCommands() {
    	return new TreeSet(ReflectionUtils.getPublicStaticFieldsWithPrefix(FileUtils.class, "CMD_", true).values());
    }
    
    /** 
     * Get exactly the desired lines of a text file. 
     * @param pLines must contain Integer objects.
     */
    public static String[] getLines(File pFile, Collection pLines) throws IOException {
        String[] lines = IOUtils.readLineArray(pFile);
        ArrayList result = new ArrayList();
        Iterator i = pLines.iterator();        
        while(i.hasNext()) {
            int line = ((Integer)i.next()).intValue();
            result.add(lines[line]);
        }
        return (String[]) ConversionUtils.collectionToArray(result, String.class);
    }
    
    private static void checkOrdered(Iterator<String> lineIterator, String title) {
        Double prev = null;
        while(lineIterator.hasNext()) {
            double val = Double.parseDouble(lineIterator.next());
            if (prev != null && val < prev) {
                System.out.println("Values "+title+"are not ordered ("+prev+" > "+val+")");
                return;
            }                    
            prev = val;
        }
        System.out.println("Values "+title + "are ordered.");
    }
    
    
    /** Not needed, as this is already done by File.getName() 
    
    public static String getLastPathComponent(File pFile) {
        String[] components = StringUtils.split(pFile.getName());
        return components[components.length-1];
    }
    */
               
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("FileUtils: "+pMsg);
    }
    

}
