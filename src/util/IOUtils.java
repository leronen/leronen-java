package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.collections.Pair;
import util.collections.SymmetricPair;
import util.collections.SymmetricTriple;
import util.collections.Triple;
import util.collections.iterator.ConditionalIterator;
import util.collections.iterator.ConverterIterator;
import util.collections.iterator.NumberingIterator;
import util.condition.ConvertedObjectCondition;
import util.condition.WithinRangeCondition;
import util.converter.Converter;
import util.converter.IdentityConverter;
import util.converter.ListFieldExtractor;
import util.converter.StringToIntegerConverter;
import util.converter.StringToLongConverter;
import util.dbg.Logger;
import util.process.ProcessUtils;

public class IOUtils {

    private static int DEFAULT_BUFFER_SIZE = 4 * 1024;

    /** count cols in the first line of a file */
    public static int countFirstLineCols(String pFilename) throws IOException  {
        String line = readFirstLine(new File(pFilename));
        Pattern p = Pattern.compile("\\s+");
        return p.split(line).length;
    }

    /** read the first line of a file */
    public static String readFirstLine(File pFile) throws IOException  {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));
        String result = reader.readLine();
        reader.close();
        return result;

    }

    /** read the first line of a file */
    public static String readFirstLine(String pFile) throws IOException  {
        return readFirstLine(new File(pFile));
    }

    public static String readFirstLine(InputStream pStream) throws IOException  {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
        String result = reader.readLine();
        reader.close();
        return result;
    }

    /** Read the first n lines of a file. If there are less lines, just return all lines. */
    public static List<String> readFirstLines(File pFile, int pNumLines) throws IOException  {
        Iterator<String> lineIterator = new LineIterator(pFile);
        return CollectionUtils.extractFirst(pNumLines, lineIterator);
    }

    public static Iterable<String> lines(InputStream is) {
        return new LineIterable(is);
    }

    public static Iterable<String> lines() {
        return new LineIterable(System.in);
    }

    /**
     * Read bytes from an input stream until the end of the stream is reached. Note
     * that reading may block if no bytes are available at the time of call.
     * The stream is NOT closed by this method.
     */
    public static byte[] readBytes(InputStream is) throws IOException  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while ((n = is.read(buffer)) > 0) {
            baos.write(buffer, 0, n);
        }

        return baos.toByteArray();
    }

    /**
     * Read a specified number of bytes from an input stream. Note that
     * reading may block if not enough bytes are available at the time of call.
     *
     * @return array containing numToRead bytes, or less if end of stream is reached prematurely.
     */
    public static byte[] readBytes(InputStream is, int numToRead) throws IOException  {

        byte[] buffer = new byte[numToRead];
        int nRead = 0;
        int nLeft = numToRead;
        int nToRead = Math.min(DEFAULT_BUFFER_SIZE, nLeft);
        int n;
        while ((n = is.read(buffer,nRead,nToRead)) > 0) {
            nRead += n;
            nLeft = numToRead - nRead;
            nToRead = Math.min(DEFAULT_BUFFER_SIZE, nLeft);
        }

        if (nRead != numToRead) {
            // could not read as much bytes
            Logger.warning("Could not read enough bytes; requested "+numToRead+", read "+nRead);
            byte[] result = new byte[nRead];
            System.arraycopy(buffer, 0, result, 0, nRead);
            return result;
        }
        else {
            // got the desired number of bytes
            return buffer;
        }
    }
    
    /** 
     * Read bytes from an input stream until the first zero byte in encountered, or the given max bytes limit is exceeded. 
     * Read the zero byte as well, but do not include it in the result.
     * 
     * @return null, if end of stream has already been reached.
     * @throws UnexpectedEndOfStreamException if some bytes are read, and the end stream ends with a non-zero byte
     * before any zero bytes are read.  
     * @throws RuntimeException when maxBytes bytes have already been read and the next byte is not null */     
    public static byte[] readBytesUntilNull(InputStream is, int maxBytes) throws UnexpectedEndOfStreamException, IOException, TooManyNonNullBytesException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;        
        while ( (b=is.read()) > 0 ) {            
            baos.write(b);
            if (baos.size() > maxBytes) {
                throw new TooManyNonNullBytesException();
            }
        }        

        if (b == 0) {        
            return baos.toByteArray();
        }
        else if (b == -1) {
            if (baos.size() == 0) {
                // no more bytes available
                return null;
            }
            else {
                throw new UnexpectedEndOfStreamException();
            }
        }
        else {
            // should not be possible
            throw new RuntimeException("foo");
        }
    }
    
    /**
     * Read bytes from an input stream until the first zero byte in encountered.
     * Read the zero byte as well, but do not include it in the result.
     *
     * @return null, if end of stream has already been reached.
     * @throws UnexpectedEndOfStreamException if some bytes are read, and the end stream ends with a non-zero byte
     * before any zero bytes are read.
     */
    public static byte[] readBytesUntilNull(InputStream is) throws UnexpectedEndOfStreamException, IOException  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ( (b=is.read()) > 0) {
            baos.write(b);
        }

        if (b == 0) {
            return baos.toByteArray();
        }
        else if (b == -1) {
            if (baos.size() == 0) {
                // no more bytes available
                return null;
            }
            else {
                throw new UnexpectedEndOfStreamException();
            }
        }
        else {
            // should not be possible
            throw new RuntimeException("foo");
        }
    }


    /**
     * Potentially blocking write of bytes to a stream. Writing is done in blocks of {@link #DEFAULT_BUFFER_SIZE}
     */
    public static void writeBytes(OutputStream os, byte[] data) throws IOException {
        List<Range> segments = new Range(0,data.length).split(DEFAULT_BUFFER_SIZE);

        for (Range segment: segments) {
            os.write(data, segment.start, segment.length());
        }

    }


    /**
     * Replace stdout with a faster, buffered version. Not for interactive
     * programs!!! Remember to System.out.flush() at the end!!!
     */
    public static void setFastStdout() {
        FileOutputStream fdout = new FileOutputStream(FileDescriptor.out);
        BufferedOutputStream bos = new BufferedOutputStream(fdout, 65536);
        PrintStream ps = new PrintStream(bos, false); // the last parameter is "autoflush"
        System.setOut(ps);
    }

    /**
     * Replace stderr with a faster, buffered version. Not for interactive
     * programs!!! Remember to flush at the end!!!
     */
    public static void setFastStderr() {
        FileOutputStream fderr = new FileOutputStream(FileDescriptor.err);
        BufferedOutputStream bos = new BufferedOutputStream(fderr, 65536);
        PrintStream ps = new PrintStream(bos, false); // the last parameter is "autoflush"
        System.setErr(ps);
    }

    /**
     * Output a collection delimited with newlines. Also write a newline
     * after the last element.
     */
    public static void writeCollection(PrintStream pStream,
                                       Collection pCollection) {

        for (Object o: pCollection) {
            pStream.println(o);
        }
    }

    /**
     * Output a collection delimited with newlines to STDOUT. Also write a newline
     * after the last element. Do not close stdout, naturally.
     */
    public static void writeCollection(Collection pCollection) {

        for (Object o: pCollection) {
            System.out.println(o);
        }
    }

    public static <T> void writeCollection(String pFile,
                                           Collection<T> pCollection,
                                           Converter<T, String> pFormatter) throws IOException {
        PrintStream ps = new PrintStream(new FileOutputStream(pFile));

        writeCollection(ps, pCollection, pFormatter);

        ps.close();
    }


    /**
     * Output a collection delimited with newlines. Also write a newline
     * after the last element.
     *
     * Does not close the stream!
     */
    public static <T> void writeCollection(PrintStream pStream,
                                           Collection<T> pCollection,
                                           Converter<T, String> pFormatter) {

        for (T o: pCollection) {
            pStream.println(pFormatter.convert(o));
        }
    }


    /**
     * Output a collection delimited with newlines. Also write a newline
     * after the last element.
     */
    public static void writeIterator(PrintStream pStream,
                                     Iterator pIterator) {

        for (Object o: new IteratorIterable(pIterator)) {
            pStream.println(o);
        }
    }

    /**
     * Output a collection delimited with newlines. Also write a newline
     * after the last element.
     */
    public static void writeIteratorToStdOut(Iterator pIterator) {

        for (Object o: new IteratorIterable(pIterator)) {
            System.out.println(o);
        }
    }



    public static void writeCollection(File pFile,
                                       Collection pCollection) throws FileNotFoundException {

        PrintStream ps = new PrintStream(new FileOutputStream(pFile));

        for (Object o: pCollection) {
            ps.println(o);
        }

        ps.close();
    }

    /** column indexing starts from 0. No header assumed. */
    public static List<String> readColumn(String file, String delimRegex, int column) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
    	List<String> result = readColumn(fis,  delimRegex, column);
    	fis.close();
    	return result;
    }

    /** column indexing starts from 0. No header assumed. */
    public static List<String> readColumn(InputStream pInputStream, String delimRegex, int column) throws IOException {
        String[] lines = readLineArray(pInputStream);
        List<String> result = new ArrayList<String>();
        for (String line: lines) {
            String[] tokens = line.split(delimRegex, -1);
            result.add(tokens[column]);
        }
        return result;
    }

    public static void writeCollection(String pFile,
                                       Collection pCollection) throws FileNotFoundException {

        writeCollection(new File(pFile), pCollection);
    }

    public static void writeCollectionToStdOut(Collection pCollection) {

        for (Object o: pCollection) {
            System.out.println(o);
        }
    }


    public static List<String> readLines(File pFile, boolean countLinesFirst) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));

        ArrayList<String> lines;
        if (countLinesFirst) {
            int lineCount = countRows(pFile.getAbsolutePath());
            lines = new ArrayList(lineCount);
        }
        else {
            lines = new ArrayList();
        }
        String line = reader.readLine();
        while(line!=null) {
            lines.add(line);
            line = reader.readLine();
        }

        reader.close();
        return lines;
    }

    public static void setProperty(String pFile, String pName, String pVal) throws IOException {
    	Pattern p = Pattern.compile("^([^=]+)=(.+)$");
    	File f = new File(pFile);
    	List<String> lines;
    	if (f.exists()) {
    		lines = readLines(f);
    		f.delete();
    	}
    	else {
    		lines = Collections.EMPTY_LIST;
    	}
    	boolean found = false;
    	for (String l: lines) {
    		Matcher m = p.matcher(l);
    		if (m.matches()) {
    			String name = m.group(1);
    			// String val = m.group(2);
    			if (name.equals(pName)) {
    				if (found == true) {
    					Logger.warning("Duplicate property!");
    				}
    				found = true;
    				appendToFile(f, pName+"="+pVal+"\n");
    			}
    			else {
    				appendToFile(f, l+"\n");
    			}
    		}
			else {
				appendToFile(f, l+"\n");
			}
    	}
    	if (found == false) {
    		appendToFile(f, pName+"="+pVal+"\n");
    	}
    }

    /**
     * Reads a map in the format
     *
     * KEY1=VAL1
     * KEY2=VAL2
     * etc...
     *
     * keywords: propertyfile, property file, delimited.
     *
     * Note that there is no writeMap; instead, the current canonical way is:
     *   writeToFile(mapFile, StringUtils.mapToString(map)+"\n"),
     * (which may not be optimal for large maps...)
     */
    public static Map<String,String> readMap(File pFile) throws IOException{
        return readMap(pFile, "=");
    }

    public static Map<String,String> readMap(InputStream is) throws IOException{
        return readMap(is, "=");
    }

    /**
     * Reads a map in the format
     *
     * KEY1=VAL1
     * KEY2=VAL2
     * etc...
     *
     * keywords: propertyfile, property file, delimited
     */
    public static Map<String, String> readMap(InputStream is, String pDelimRegex) throws IOException{
        dbgMsg("readMap");
        String[] lines = readLineArray(is);
        LinkedHashMap<String, String> result = parseMap(lines, pDelimRegex);
        dbgMsg("read "+lines.length);
        return result;
    }

    private static LinkedHashMap<String,String> parseMap(String[] pLines, String pDelimRegex) {
        return parseMap(pLines, pDelimRegex, false);
    }

    private static LinkedHashMap<String,String> parseMap(String[] pLines, String pDelimRegex, boolean pAllowLinesWithNotEnoughTokens) {
        LinkedHashMap<String, String> result = new LinkedHashMap(pLines.length);
        for (int i=0; i<pLines.length; i++) {
            String line = pLines[i];
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            String[] tokens = line.split(pDelimRegex, -1);
            if (tokens.length >= 2) {
                String key = tokens[0];
                String val = tokens[1];
                result.put(key, val);
            }
            else {
                if (pAllowLinesWithNotEnoughTokens) {
                    // silently ignore line!
                    continue;
                }
                else {
                    throw new RuntimeException("Not enough columns on line "+(i+1)+" of map file: <"+line+">"+
                                               "DELIM: <"+pDelimRegex+">\n"+
                    						   "TOKENS: "+StringUtils.arrayToString(tokens, ","));
                }

            }
        }
        return result;
    }

    /**
     * Reads a map in the format
     *
     * KEY1=VAL1
     * KEY2=VAL2
     * etc...
     *
     * keywords: propertyfile, property file, delimited     *
     */
    public static Map<String, String> readMap(File pFile, String pDelimRegex) throws IOException {
        return readMap(pFile, pDelimRegex, false);
    }

    /**
     * Reads a map in the format
     *
     * KEY1=VAL1
     * KEY2=VAL2
     * etc...
     *
     * keywords: propertyfile, property file, delimited     *
     */
    public static Map<String, String> readMap(File pFile, String pDelimRegex, boolean pAllowLinesWithNotEnoughTokens) throws IOException{
        dbgMsg("readMap: "+pFile);
        String[] lines = readLineArray(pFile);
        LinkedHashMap<String, String> result = parseMap(lines, pDelimRegex, pAllowLinesWithNotEnoughTokens);
        dbgMsg("read "+lines.length);
        return result;
    }




    /**
     * From a file, find the first line like pKey=val and return the val. If
     * such a line is not found, return null.
     */
    public static String readValue(File pFile, String pKey, int pMaxNumRetries) throws IOException {
        Pattern p = Pattern.compile(pKey+"=(.*)$");
        String[] lines = readLineArray(pFile, pMaxNumRetries);
        for (String line: lines) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
        }

        // not found
        return null;
    }

    /** keywords: countlines */
    public static int countRows(String pFileName) throws IOException  {
        return countRows(new File(pFileName));
    }

    public static int countRows(InputStream pStream) throws IOException  {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
        String line = reader.readLine();
        int count = 0;
        while(line!=null) {
            count++;
            line = reader.readLine();
        }
        reader.close();
        return count;
    }

    public static int countRows(File pFile) throws IOException  {
        BufferedReader reader = new BufferedReader(new FileReader(pFile), 1024*1024);
        String line = reader.readLine();
        int count = 0;
        while(line!=null) {
            count++;
            line = reader.readLine();
        }
        reader.close();

        return count;
    }

    /** Uhh, epätoivoinen optiomointiyritys johti täydelliseen tehon romahdukseen (11s => 41s!) */
    public static int countRows2(File pFile) throws IOException  {
       FileInputStream fis =
           new FileInputStream(pFile);
       BufferedInputStream bis =
           new BufferedInputStream(fis);
       int cnt = 0;
       int b;
       while ((b = bis.read()) != -1) {
           if (b == '\n') {
               cnt++;
           }
       }
       bis.close();
       return cnt;
    }

    public static boolean mkdirs(String pPath) {
        return new File(pPath).mkdirs();
    }

    public static boolean doesFileExist(String pFileName) {
        return new File(pFileName).exists();
    }


    public static boolean containsPattern(String pFileName, String pPattern) throws IOException {
        FileInputStream istream = new FileInputStream(pFileName);
        String[] lines = readLineArray(istream);

        Pattern p = Pattern.compile("^.*?"+pPattern+".*?$");
        for (int i=0; i<lines.length; i++) {
            Matcher m = p.matcher(lines[i]);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    public static List<SymmetricPair<String>> readPairs(String pFileName) throws IOException {
        return readPairs(new FileInputStream(pFileName));
    }

    public static List<SymmetricTriple<String>> readTriples(String pFileName) throws IOException {
        return readTriples(new FileInputStream(pFileName));
    }

    public static <T1,T2> List<Pair<T1,T2>> readPairs(InputStream pInputStream,
                                                      Converter<String,T1> pConverter1,
                                                      Converter<String,T2> pConverter2) throws IOException {
        if (pConverter1 == null) pConverter1 = new IdentityConverter();
        if (pConverter2 == null) pConverter2 = new IdentityConverter();
        String[] lines = readLineArray(pInputStream);
        ArrayList<Pair<T1,T2>> result = new ArrayList();
        for (String line: lines) {
            String[] tokens = line.split("\\s+", -1);
            result.add(new Pair(pConverter1.convert(tokens[0]),
                                pConverter2.convert(tokens[1])));
        }
        return result;
    }

    public static <T1,T2,T3> List<Triple<T1,T2,T3>> readTriples(InputStream pInputStream,
                                                                Converter<String,T1> pConverter1,
                                                                Converter<String,T2> pConverter2,
                                                                Converter<String,T3> pConverter3) throws IOException {
        if (pConverter1 == null) pConverter1 = new IdentityConverter();
        if (pConverter2 == null) pConverter2 = new IdentityConverter();
        if (pConverter3 == null) pConverter3 = new IdentityConverter();
        String[] lines = readLineArray(pInputStream);
        ArrayList<Triple<T1,T2,T3>> result = new ArrayList();
        for (String line: lines) {
            String[] tokens = line.split("\\s+", -1);
            result.add(new Triple(pConverter1.convert(tokens[0]),
                                  pConverter2.convert(tokens[1]),
                                  pConverter3.convert(tokens[2])));
        }
        return result;
    }

    /** Read pairs with white-space delimited columns. */
    public static List<SymmetricPair<String>> readPairs(InputStream pInputStream) throws IOException {
        String[] lines = readLineArray(pInputStream);
        ArrayList<SymmetricPair<String>> result = new ArrayList();
        for (String line: lines) {
            String[] tokens = line.split("\\s+");
            result.add(new SymmetricPair(tokens[0],tokens[1]));
        }
        return result;
    }

    /** Read pairs with white-space delimited columns. */
    public static List<SymmetricTriple<String>> readTriples(InputStream pInputStream) throws IOException {
        String[] lines = readLineArray(pInputStream);
        ArrayList<SymmetricTriple<String>> result = new ArrayList();
        for (String line: lines) {
            String[] tokens = line.split("\\s+");
            result.add(new SymmetricTriple(Arrays.asList(tokens)));
        }
        return result;
    }

    /** Read a table with white-space delimited columns. */
    public static List<String[]> readTable(String pFileName) throws IOException {
        return readTable(new FileInputStream(pFileName));
    }

    public static List<String[]> readTable(InputStream pInputStream) throws IOException {
        String[] lines = readLineArray(pInputStream);
        ArrayList<String[]> result = new ArrayList<String[]>();
        for (String line: lines) {
            String[] tokens = line.split("\\s+");
            result.add(tokens);
        }
        return result;

    }

    public static List<List<String>> readTable2(InputStream pInputStream) throws IOException {
        return readTable2(pInputStream, "\\s+");

    }

    public static List<List<String>> readTable2(InputStream pInputStream, String delimRegex) throws IOException {
        String[] lines = readLineArray(pInputStream);
        List<List<String>> result = new ArrayList<List<String>>();
        for (String line: lines) {
            String[] tokens = line.split(delimRegex,-1);
            result.add(Arrays.asList(tokens));
        }
        return result;

    }

    public static boolean isEmpty(File pFile) {
        return pFile.length() == 0;
    }

    public static boolean hasExactlyOneLine(File pFile) throws IOException {
        return hasAtMostNLines(pFile, 1) && !isEmpty(pFile);
    }

    public static boolean hasAtMostNLines(File pFile, int pN) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));
        int count = 0;
        while (count <= pN) {
            String line = reader.readLine();
            if (line == null) {
                return true;
            }
            count++;
        }
        reader.close();
        // OK, count should be > n...
        return false;

    }

    public static List<String> readLines(File pFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));

        ArrayList<String> lines = new ArrayList();
        String line = reader.readLine();
        while(line!=null) {
            lines.add(line);
            line = reader.readLine();
        }

        reader.close();
        return lines;
    }

    public static void cat(String pFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));

        String line = reader.readLine();
        while(line!=null) {
            System.out.println(line);
            line = reader.readLine();
        }

        reader.close();
    }


    /**
     * Reads everything from a character stream into a string.
     * always closes the stream, even if reading fails.
     *
     * keywords: read large file, read file to string, read file into string.
     */
    public static String readStream(InputStream is) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer result = new StringBuffer();
            char buf[] = new char[4096];
            int nRead;
            while ((nRead = reader.read(buf)) != -1) {
                result.append(buf,0,nRead);
            }
            return result.toString();
        }
        finally {
            is.close();
        }
    }


    /**
     * @param pResult collection for storing the results. Naturally not
     * stuff is just appended to any existing contents.
     */
    public static void readLines(File pFile, Collection<String> pResult) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pFile));

        String line = reader.readLine();
        while(line!=null) {
            pResult.add(line);
            line = reader.readLine();
        }

        reader.close();
    }

    public static List<String> readLines(String pFileName) throws IOException {
    	return readLines(new File(pFileName));
    }

    /** Closes the stream after reading all lines */
    public static List<String> readLines(InputStream pIstream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pIstream));

        ArrayList<String> lines = new ArrayList();
        String line = reader.readLine();
        while(line!=null) {
            lines.add(line);
            line = reader.readLine();
        }

        reader.close();
        return lines;
    }

    public static void cat(InputStream pIstream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pIstream));

        String line = reader.readLine();
        while(line!=null) {
            System.out.println(line);
            line = reader.readLine();
        }

        reader.close();
    }

    public static List<String> readLines() throws IOException {
        return readLines(System.in);
    }

    public static String[] readLineArray(String pFileName) throws IOException {
        return readLineArray(pFileName, 0);
    }

    public static String[] readLineArray(String pFileName, int pMaxNumRetries) throws IOException {
        return readLineArray(new File(pFileName), pMaxNumRetries);
    }

    public static String[] readLineArray(File pFile) throws IOException {
        FileInputStream fileStream = new FileInputStream(pFile);
        String[] result = readLineArray(fileStream);
        fileStream.close();
        return result;
    }

    public static String[] readLineArray(File pFile, int pNumRetries) throws IOException {
//        FileInputStream fileStream = new FileInputStream(pFile);

        if (!pFile.exists()) {
            throw new FileNotFoundException(pFile.getPath());
        }

        int numRetries = 0;
        while (true) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(pFile));
                ArrayList lines = new ArrayList();
                String line = reader.readLine();
                while (line!=null) {
                    lines.add(line);
                    line = reader.readLine();
                }
                reader.close();
                if (numRetries > 0) {
                    Logger.warning("Reading lines required "+numRetries+" retries to succeed.");
                }

                return (String[])lines.toArray(new String[lines.size()]);
            }
            catch (IOException e) {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (Exception fooE) {
                        // foo
                    }
                }

                if (numRetries < pNumRetries) {
                    numRetries++;

                    // let's wait a bit...
                    try {
                        Logger.info("Retrying to read file "+pFile+" after one second of sleep...");
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException intrE) {
                        // boo!
                    }

                    // let's retry
                    continue;
                }
                else {
                    // no more of these hopeless retries
                    throw e;
                }
            }

        }

    }

    public static String[] readLineArray(InputStream pStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));

          ArrayList lines = new ArrayList();

          String line = reader.readLine();
          while (line!=null) {
              lines.add(line);
              line = reader.readLine();
          }
          reader.close();

          return (String[])lines.toArray(new String[lines.size()]);
    }

//    public static String[] readLineArray(InputStream pStream, int pMaxNumRetries) throws IOException {
//        int numRetries = 0;
//        while (true) {
//            try {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
//                LinkedList lines = new LinkedList();
//                String line = reader.readLine();
//                while (line!=null) {
//                    lines.add(line);
//                    line = reader.readLine();
//                }
//                reader.close();
//                if (numRetries > 0) {
//                    Logger.warning("Reading lines required "+numRetries+" retries to succeed.");
//                }
//                return (String[])lines.toArray(new String[lines.size()]);
//            }
//            catch (IOException e) {
//                if (numRetries < pMaxNumRetries) {
//                    numRetries++;
//
//                    // let's wait a bit...
//                    try {
//                        Logger.info("Retrying to read file after one second of sleep...");
//                        Thread.sleep(1000);
//                    }
//                    catch (InterruptedException intrE) {
//                        // boo!
//                    }
//
//                    // let's retry
//                    continue;
//                }
//                else {
//                    // no more of these hopeless retries
//                    throw e;
//                }
//            }
//        }
//    }

    public static void printLines(OutputStream pStream, Object[] pObjects) {
    	printLines(pStream, Arrays.asList(pObjects));
    }

    public static void printLines(OutputStream pStream, List pObjects) {
        PrintStream stream = new PrintStream(pStream);
        for (Object o:pObjects) {
            stream.println(o);
        }
    }


    /*
    public static ProcessOutput executeCommand(String pCommand,
                                              ProcessOwner pProcessOwner) throws IOException{
        return executeCommand(pCommand, null, null, pProcessOwner);
    }
    */

    /**
     * Executes process and waits for it's termination.
    *  Lines outputted by stdout and stderr of the process are returned.
    */
/*
    public static ProcessOutput executeCommand(String pCommand,
                                               StreamListener pOutputStreamListener,
                                               StreamListener pErrorStreamListener,
                                               ProcessOwner pProcessOwner) throws IOException {
        // dbgMsg("Executing command: "+pCommand);
        return executeCommand(pCommand, null, pProcessOwner);
    }
    */

    /** Open emacs and do not wait */
    /*
    public static void openWithEmacs(File pFile, ProcessOwner pProcessOwner) throws IOException {
        executeCommand_nowait("rxvt -e ue "+pFile.getPath(), null, pProcessOwner);
    }
    */

    public static void openPdfFile(File pFile) throws IOException {
    	ProcessUtils.executeCommand_nowait("acroread "+pFile, null, null);
    }

    public static void openBMGraphFile(File pFile) throws IOException {
        ProcessUtils.executeCommand_nowait("bmvis_lauri "+pFile, null, null);
    }

    /** Open emacs and do not wait */
    /*
    public static void openWithEditor(File pFile, String pEditor, ProcessOwner pProcessOwner) throws IOException {
        executeCommand_nowait("rxvt -e "+pEditor+" "+pFile.getPath(), null, pProcessOwner);
        // executeCommand_nowait(pEditor+" "+pFile.getPath(), null);
    }



*/
  /*
    public static Process executeCommand_nowait(String pCommand, ProcessOwner pProcessOwner) throws IOException {
        return executeCommand_nowait(pCommand, null, pProcessOwner);
    }

    public static Process executeCommand_nowait(String pCommand, String pDir, ProcessOwner pProcessOwner) throws IOException {
        File dir = null;
        if (pDir!=null) {
            dbgMsg("Executing command: "+pCommand+" in dir "+pDir);
            dir = new File(pDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }
        else {
            dbgMsg("Executing command: "+pCommand+" in current dir");
        }
        Process proc = Runtime.getRuntime().exec(pCommand, null, dir);
        if (pProcessOwner != null) {
            pProcessOwner.registerExternalProcess(proc);
        }
        dbgMsg("returning process: "+proc);
        return proc;
    }
*/
    /** search terms: "pwd", "getCurrendDir", "current directory" */
    public static String getWorkingDir() {
        return System.getProperty("user.dir");

    }

    /** see below for explanation */
/*
    public static ProcessOutput executeCommand(String pCommand,
                                                    String pDir,
                                                    ProcessOwner pProcessOwner) throws IOException {
        return executeCommand(pCommand, pDir, null, null, pProcessOwner);
    }
  */
    /**
     * Executes process in give directory and waits for it's termination.
     * Lines outputted by stdout and stderr of the process are returned.
     * the listeners may be null.
     */
     /*
    public static ProcessOutput executeCommand(String pCommand,
                                                    String pDir,
                                                    StreamListener pOutputStreamListener,
                                                    StreamListener pErrorStreamListener,
                                                    ProcessOwner pProcessOwner) throws IOException {
        return executeCommand(pCommand,
                                   pDir,
                                   pOutputStreamListener,
                                   pErrorStreamListener,
                                   pProcessOwner,
                                   false);
    }
    */

    /**
     * Executes process in give directory and waits for it's termination.
     *  Lines outputted by stdout and stderr of the process are returned.
     * the listeners may be null.
     */
     /*
    public static ProcessOutput executeCommand(String pCommand,
                                                    String pDir,
                                                    StreamListener pOutputStreamListener,
                                                    StreamListener pErrorStreamListener,
                                                    ProcessOwner pProcessOwner,
                                                    boolean pOutputInfoMessages) throws IOException {
        File dir = null;
        if (pDir!=null) {
            dbgMsg("Executing command: "+pCommand+" in dir "+pDir);
            dir = new File(pDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }
        else {
            dbgMsg("Executing command: "+pCommand+" in current dir");
        }
        // execute process
        Process proc = Runtime.getRuntime().exec(pCommand, null, dir);
        if (pProcessOwner != null) {
            pProcessOwner.registerExternalProcess(proc);
        }

        // store output and errors
        InputStream outStream = proc.getInputStream();
        InputStream errStream = proc.getErrorStream();

        String arg0 = pCommand.split("\\s+")[0];
        String commandName = new File(arg0).getName();
        RunnableStreamReader stdoutReader = new RunnableStreamReader(commandName, "stdout", outStream, pOutputStreamListener, pOutputInfoMessages);
        RunnableStreamReader stderrReader = new RunnableStreamReader(commandName, "stderr", errStream, pErrorStreamListener, pOutputInfoMessages);

        Thread stdOutReaderThread = new Thread(stdoutReader);
        Thread stdErrReaderThread = new Thread(stderrReader);

        stdOutReaderThread.start();
        stdErrReaderThread.start();

        try {
            stdOutReaderThread.join();
            stdErrReaderThread.join();
            String[] outlist = stdoutReader.getResult();
            String[] errlist = stderrReader.getResult();

            // print debug info
            // Logger.dbg("**************** stdout of the executed process: *********************");
            // dbgMsg(arrayToString(outlist, "\n"));
            // Logger.dbg("**************** strerr of the executed process: *********************");
            // dbgMsg(arrayToString(errlist, "\n"));

            // wait for process to terminate, just in case...
            proc.waitFor();
            // all seems to have went well, return output of process
            dbgMsg("finished executing command: "+pCommand);
            dbgMsg("Returning process output...");
            return new ProcessOutput(outlist, errlist);
        }
        catch (InterruptedException e) {
            dbgMsg("interrupted while executing command: "+pCommand);
            e.printStackTrace();
            dbgMsg("Destroying process: "+proc);
            proc.destroy();
            dbgMsg("Process should rest in peace now.");
            dbgMsg("Returning null, as we failed to complete the processing due to the irritating interruption.");
            return null;
        }
    }
    */


    /** Append a string to a file. Note that a newline is not automatically appended! */
    public static void appendToFile(File pFile, String pStringToAppend) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(pFile, true));
        pw.print(pStringToAppend);
        pw.close();
    }

    /** Append a string to a file. Note that a newline is not automatically appended! */
    public static void appendToFile(String pFilename, String pStringToAppend) throws IOException {
        appendToFile(new File(pFilename), pStringToAppend);
    }

    /** Write text to file(overwriting previous contents!). Does not generate a line feed */
    public static void writeToFile(String pFilename, String pText) throws IOException {
        FileWriter fileWriter = new FileWriter(pFilename);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(pText);
        printWriter.close();
        fileWriter.close();
    }

    /** Write text to file(overwriting previous contents!). Does not generate a line feed */
    public static void writeToFile(File pFile, String pText) throws IOException {
        FileWriter fileWriter = new FileWriter(pFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(pText);
        printWriter.close();
        fileWriter.close();
    }

    /** Write text to file(overwriting previous contents!). Does not generate a line feed */
    public static void clearFile(String pFilename) throws IOException {
        writeToFile(pFilename, "");
    }

    /** Write text to file(overwriting previous contents!). Does not generate a line feed */
    public static void clearFile(File pFile) throws IOException {
        writeToFile(pFile, "");
    }

    /** Write lines to file. (overwriting previous contents!) */
    public static void writeToFile(String pFilename, Object[] pObjects) throws IOException {
        writeToFile(new File(pFilename), pObjects);
    }

    /** Write lines to file. (overwriting previous contents!) */
    public static void writeToFile(File pFile, Object[] pObjects) throws IOException {
    	writeToFile(pFile, Arrays.asList(pObjects));
    }

    /** Write lines to file. (overwriting previous contents!) */
    public static void writeToFile(File pFile, List pObjects) throws IOException {
        FileWriter fileWriter = new FileWriter(pFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (Object o:pObjects) {
            printWriter.println(o);
        }
        printWriter.close();
        fileWriter.close();
    }

    /** Append lines to file. */
    public static void appendToFile(String pFilename, String[] pText) throws IOException {
        appendToFile(new File(pFilename), pText);
    }

    /** Append lines to file. */
    public static void appendToFile(File pFile, String[] pText) throws IOException {
        FileWriter fileWriter = new FileWriter(pFile, true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (int i=0; i<pText.length; i++) {
            printWriter.println(pText[i]);
        }
        printWriter.close();
        fileWriter.close();
    }

    /**
     * Write lines to file. (overwriting previous contents!)
     * Does not, of course, close the stream.
     * TODO: uuh, PrintStream does not throw IOExceptions, instead checkError should be used...
     */
    public static void writeToStream(PrintStream pStream, String[] pText)  {
        for (int i=0; i<pText.length; i++) {
            pStream.println(pText[i]);
        }
    }

    /**
     * Reads all the lines from a reader. Closes the reader after
     * reading!
     */
    public static List<String> readLines(Reader pReader) throws IOException {
        BufferedReader reader = null;
        if (pReader instanceof BufferedReader) {
            reader = (BufferedReader)pReader;
        }
        else {
            reader = new BufferedReader(pReader);
        }
        ArrayList<String> lines = new ArrayList();
        String line = reader.readLine();
        while(line!=null) {
            lines.add(line);
            line = reader.readLine();
        }

        reader.close();
        return lines;



//        return (String[])lines.toArray(new String[lines.size()]);
    }

    /** Read lines of input and convert each line into some object using pObjectFactory */
    public static <T> List<T> readObjects(InputStream pStream, Converter<String, T> pObjectFactory) throws IOException {
        List<String> lines = readLines(pStream);
        return ConversionUtils.convert(lines, pObjectFactory);
    }



    /** Read lines of a file and convert each line into some object using pObjectFactory */
    public static <T> List<T> readObjects(String pFile, Converter<String, T> pObjectFactory) throws IOException {
        return readObjects(new FileInputStream(pFile), pObjectFactory);
    }


    /**
     * Read Longs, one on each line of pFile. Discard empty lines.
     * Beware of stray RuntimeExceptions
     */
    public static List<Long> readLongs(File pFile) throws IOException {
        List<String> lines = readLines(pFile);
        return ConversionUtils.convert(lines, new StringToLongConverter(), true);
    }

    /** Read Integers, one on each line of pFile. Beware of stray RuntimeExceptions */
    public static List<Integer> readIntegers(File pFile) throws IOException {
        List<String> lines = readLines(pFile);
        return ConversionUtils.convert(lines, new StringToIntegerConverter());
    }

    /** Read Integers, one on each line of pFile. Beware of stray RuntimeExceptions */
    public static int[] readInts(File pFile) throws IOException {
        int numRows = countRows(pFile);
        int data[] = new int[numRows];
        Iterator<String> lineIter = lineIterator(pFile);
        int i=0;
        while (lineIter.hasNext()) {
            data[i++] = Integer.parseInt(lineIter.next());
        }
        return data;

    }

    public static IOUtils.LineIterator lineIterator(String pFileName) throws IOException {
        return new IOUtils.LineIterator(pFileName);
    }

    public static IOUtils.LineIterator lineIterator(File pFile) throws IOException {
        return new IOUtils.LineIterator(pFile);
    }

    public static IOUtils.LineIterator lineIterator(InputStream pStream) {
        return new IOUtils.LineIterator(pStream);
    }

    public static Iterator<String> lineIterator(InputStream pStream, Range pLineRange) {
        Iterator<Pair<String, Integer>> numberedLineIter =
                new ConditionalIterator(
                    new NumberingIterator(IOUtils.lineIterator(pStream)),
                    new ConvertedObjectCondition(new ListFieldExtractor(1), new WithinRangeCondition(pLineRange.start, pLineRange.end)));
        return new ConverterIterator(numberedLineIter, new ListFieldExtractor(0));
    }

    public static IOUtils.LineIterator lineIterator() {
        return new IOUtils.LineIterator(System.in);
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg("IOUtils: "+pMsg);
    }

    /**
     * Return a Iterable of lines in a file. Does not read the complete file
     * into RAM, recommenderas for störra filarna!
     */
    public static Iterable<String> lines(String pFile) {
    	try {
    		return new LineIterable(pFile);
    	}
    	catch (IOException e) {
    		throw new RuntimeException(e);
    	}
    }

    public static class LineIterable implements Iterable<String> {

    	boolean alreadyIterated = false;
    	private final LineIterator mLineIterator;

        public LineIterable(InputStream pStream)  {
            mLineIterator = new LineIterator(pStream);
        }

        public LineIterable(String pFilename) throws IOException {
        	mLineIterator = new LineIterator(pFilename);
        }

        public LineIterable(File pFile) throws IOException {
        	mLineIterator = new LineIterator(pFile);
        }

        @Override
        public Iterator<String> iterator() {
        	if (alreadyIterated) {
        		throw new RuntimeException("Unfortunately, can only iterate once...");
        	}
        	else {
        		alreadyIterated = true;
        		return mLineIterator;
        	}
        }
    }


    /**
     * Class for iterating lines of file.
     * Please note that error handling is non-existent;
     * if something goes wrong, an runtime exception is thrown!
     *
     * The iterating might be implemented with methods that
     * throw an exception.
     *
     * Even in that case, the Iterator interface would have to be implemented by methods that throw an
     * RuntimeException, as the Iterator interface cannot, of course, handle io errors.
     *
     * Unfortunately not very suitable for interactive reading/writing,
     * as uses a buffer of one line...
     *
     * Closes stream after reading last line.
     */
    public static class LineIterator implements Iterator<String> {
        private BufferedReader mReader;
        /** may be null, if we have not created the stream ourself */
        private InputStream mInputStream;
        private String mLine;

        public LineIterator(InputStream pStream)  {
            init(pStream);
        }

        public LineIterator(String pFilename) throws IOException {
            mInputStream = new FileInputStream(pFilename);
            init(mInputStream);
        }

        public LineIterator(File pFile) throws IOException {
            mInputStream = new FileInputStream(pFile);
            init(mInputStream);
        }

        private void init(InputStream pInputStream) {
            mReader = new BufferedReader(new InputStreamReader(pInputStream));
            try {
                mLine = mReader.readLine();
            }
            catch (Exception e) {
                mLine = null;
                e.printStackTrace();
                throw new RuntimeException("Failed reading");
            }
        }

        private void moveToNextLine() {
            if (mLine == null) {
                // no more elements
                throw new NoSuchElementException();
            }
            else {
                try {
                    mLine = mReader.readLine();
                    if (mLine == null) {
                        // no more lines, close the reader
                        mReader.close();
                        if (mInputStream!=null) {
                            // we have created the input stream ourself, so close it as well
                            mInputStream.close();
                        }
                    }
                }
                catch (Exception e) {
                    mLine = null;
                    e.printStackTrace();
                    throw new RuntimeException("Failed reading");
                }
            }
        }

        @Override
        public boolean hasNext() {
            return (mLine != null);
        }

        @Override
        public String next() {
            String line = mLine;
            moveToNextLine();
            return line;
        }

        public String nextLine() {
            String line = mLine;
            moveToNextLine();
            return line;
        }

        @Override
        public void remove() {
            throw new RuntimeException("Operation not implemented by iterator");
        }
    }

    public static class UnexpectedEndOfStreamException extends Exception {
        //
    }


    /*
    private static class RunnableStreamReader implements Runnable {
        private InputStream mInputStream;
        private String mProcessName;
        private String mStreamName;
        private String[] mResult;
        private StreamListener mListener;
        private Pattern[] mListenerPatterns;
        private boolean mOutputAsInfo;

        RunnableStreamReader(String pProcessName, String pStreamName,
                             InputStream pInStream,
                             StreamListener pListener,
                             boolean pOutputAsInfo) {
            mStreamName = pStreamName;
            mProcessName = pProcessName;
            mInputStream = pInStream;
            mOutputAsInfo = pOutputAsInfo;
            if (pListener != null) {
                mListener = pListener;
                String[] patternStrings = pListener.getRegularExpressions();
                mListenerPatterns = new Pattern[patternStrings.length];
                for (int i=0; i<patternStrings.length; i++) {
                    mListenerPatterns[i] = Pattern.compile(patternStrings[i]);
                }
            }
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream));
                LinkedList lines = new LinkedList();
                String line = reader.readLine();
                while (line!=null) {
                    lines.add(line);
                    output(line);
                    if (mListener != null) {
                        for (int i=0; i<mListenerPatterns.length; i++) {
                            Matcher m = mListenerPatterns[i].matcher(line);
                            if (m.matches()) {
                                mListener.notify(line, i);
                            }
                        }
                    }

                    line = reader.readLine();
                }
                mResult = (String[])lines.toArray(new String[lines.size()]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String[] getResult() {
            return mResult;
        }

        private void output(String pMsg) {
            if (mOutputAsInfo) {
                Logger.info(mProcessName+" output ("+mStreamName+"): "+pMsg);
            }
        }
    }
    */


    
    public static class TooManyNonNullBytesException extends Exception {
        // 
    }
            
    public static void main (String[] args) {
        String cmd = args[0];

        if (cmd.equals("readvalue")) {
            try {
                File file = new File(args[1]);
                String key = args[2];
                String value = readValue(file, key, 0);
                System.out.println(value);
            }
            catch (IOException e) {
                System.err.println("Failed reading value: "+e.getMessage());
                System.exit(-1);
            }
        }
        else if (cmd.equals("setproperty")) {
            try {
                String file = args[1];
                String key = args[2];
                String value = args[3];
                setProperty(file, key, value);
            }
            catch (IOException e) {
                System.err.println("Failed reading value: "+e.getMessage());
                System.exit(-1);
            }
        }
        else {
            System.err.println("Unknown command: "+cmd);
            System.exit(-1);
        }
    }
}
