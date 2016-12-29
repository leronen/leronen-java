package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import util.comparator.ReverseComparator;
import util.dbg.Logger;
import util.process.ProcessUtils;

public final class Utils {

    public static String[] removeDuplicateRows(String[] pRows) {
        LinkedHashSet tmp = new LinkedHashSet(pRows.length/10); // arbitrary!
        for (int i=0; i<pRows.length; i++) {
            tmp.add(pRows[i]);
        }
        return (String[])tmp.toArray(new String[tmp.size()]);
    }



    // foo
    public static boolean nullDurableEquals(Object p1, Object p2) {

        if (p1 == null && p2 == null) {
            return true;
        }
        else if (p1 != null && p2 == null) {
           return false;
        }
        else if (p1 == null && p2 != null) {
           return false;
        }
        else {
            // neither is null
            return p1.equals(p2);
        }
    }

    public static void mailTo(String pAddress, String pSubject, List<String> pContents) throws IOException  {
	    Process process = Runtime.getRuntime().exec("mail -s \""+pSubject+"\" "+pAddress);
		PrintWriter writer = new PrintWriter(process.getOutputStream());
		for (String line: pContents) {
			writer.println(line);
		}
		writer.close();
		process.getOutputStream().close();
    }

    /**
     * Return the value that is n:th biggest in the given list of values.
     * List may contain duplicates. Sorts (a copy of) the list first.
     * pN == means the largest value and so on (giving pN<1 is an error!)
     *
     * keywords: nthvalue, n:th biggest value, n:th value.
     */
    public static double nthBiggestValue(List<Number> pValues, int pN) {
        if (pN < 1) {
            throw new RuntimeException("Too small value for n: "+pN);
        }
        ArrayList<Number> copy = new ArrayList(pValues);
        Collections.sort(copy, new ReverseComparator());
        return copy.get(pN-1).doubleValue();
    }


    private static void testSqrt() {
        double result = Math.sqrt(0);
        System.out.println(result);
    }

    public static String[] clone(String[] pArr) {
        String[] result = new String[pArr.length];
        System.arraycopy(pArr, 0, result, 0, pArr.length);
        return result;
    }

    /**
     * Deduce how long segments we get when we split a list of pNumElements
     * elements into segments with length pSegmentLen, such that the last
     * segment is the only one that may contain less elements.
     */
    public static int[] deduceSplitSegmentLengths(int pNumElements, int pSegmentLen) {

        int numSegments;
        int[] segmentLengths;
        if (pNumElements % pSegmentLen == 0) {
            // split is even
            numSegments = pNumElements/pSegmentLen;
            segmentLengths = new int[numSegments];
            for (int i=0; i<numSegments; i++) {
                segmentLengths[i]=pSegmentLen;
            }
        }
        else {
            // split is not even
            numSegments = pNumElements/pSegmentLen+1;
            segmentLengths = new int[numSegments];
            for (int i=0; i<numSegments-1; i++) {
                segmentLengths[i]=pSegmentLen;
            }
            // the excess elements:
            segmentLengths[numSegments-1] = pNumElements % pSegmentLen;
        }

        return segmentLengths;
    }

    public static String[] appendCols(String[] pCols1, String[] pCols2) {
        int numRows = pCols1.length;
        String[] result = new String[numRows];
        for (int i=0; i<numRows; i++) {
            result[i]=pCols1[i]+" "+pCols2[i];
        }
        return result;
    }

    public static void permutations() {
        for (int i=0; i<100000; i++) {
            HashSet<Integer> remaining = new HashSet<Integer>();
            for (int j=0; j<6; j++) {
                remaining.add(j);
            }
            int numCorrect = 0;
            for (int j=0; j<6; j++) {

                int numRemaining = 6-j;
                ArrayList<Integer> remainingList = new ArrayList<Integer>(remaining);
                int next = remainingList.get(RandUtils.randInt(1, numRemaining)-1);
                remaining.remove(next);
                if (next==j) {
                    numCorrect++;
                }
            }
            System.out.println(numCorrect);
        }
    }

    /** The base name of the local host (no domain part) */
    public static String getLocalHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();

            // Get IP Address
            // byte[] ipAddr = addr.getAddress();

            // Get hostname
            String hostname = addr.getHostName();

            String[] parts = hostname.split("\\.");

            String tmp = parts[0];

            if (tmp.startsWith("ukko")) {
                // don't ask...
                tmp = tmp.replace("ukko", "node");
            }

            return tmp;
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Get a "fully qualified" host name, given a host name. Handles "localhost"
     * as a special case.
     */
    public static String getCanonicalHostName(String pHostName) throws UnknownHostException  {

        if (pHostName.equals("localhost")) {
            InetAddress localMachine = java.net.InetAddress.getLocalHost();
            return localMachine.getCanonicalHostName();
        }
        else {
            InetAddress addr = InetAddress.getByName(pHostName);
            return addr.getCanonicalHostName();
        }
    }

    public static void main(String args[]) {
        CmdLineArgs argParser = new CmdLineArgs(args);
        args = argParser.getNonOptArgs();
        // dbgMsg("Testing: "+args[0]);

        if (args[0].equals("testchop")) {
            String orig = "orig";
            String chopped = StringUtils.chop(orig);
            dbgMsg("orig: "+orig+", chopped: "+chopped);
            orig = "orig";
            String chopped_1 = StringUtils.chop(orig, 'g');
            String chopped_2 = StringUtils.chop(orig, 'a');
            dbgMsg("orig: "+orig+", chopped1: "+chopped_1+", chopped2: "+chopped_2);
        }
        else if (args[0].equals("testsqrt")) {
            testSqrt();
        }
        else if (args[0].equals("testnthbiggest")) {
            List<Number> values = (List)CollectionUtils.makeList(1,1,2,3,4,5,5);
            double thirdBiggest = nthBiggestValue(values, 3);
            System.out.println("3rdBiggest: "+thirdBiggest);
        }
        else if (args[0].equals("testrandint")) {
            int start = Integer.parseInt(argParser.getOpt("start"));
            int end = Integer.parseInt(argParser.getOpt("end"));
            for (int i=0; i<20; i++) {
                System.out.print(""+RandUtils.randInt(start, end)+" ");
            }
            System.out.println();
        }
        else if (args[0].equals("mail")) {
        	try {
	        	ArrayList<String> msg = new ArrayList<String>();
	        	msg.add("foo");
	        	msg.add("bar");
	        	msg.add("baz");
	        	mailTo("leronen", "testi", msg);
        	}
        	catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        else if (args[0].equals("testlocale")) {
            Locale defaultLocale = Locale.getDefault();
            System.err.println("Deafult locale: "+defaultLocale);
            System.err.println("yks piste yks: "+StringUtils.formatFloat(1.1));
            System.err.println("yks piste yks: "+StringUtils.formatFloat(1.1, 3));
            System.err.println("Foobar");
        }
        else if (args[0].equals("kolatesti")) {
            permutations();
        }
        else if (args[0].equals("executeindir")) {
            try {
                String dir = args[1];
                List<String> arguments = CollectionUtils.tailList(Arrays.asList(args), 2);
                
                ProcessUtils.executor()
                    .command(arguments)
                    .dir(dir)
                    .exec();                
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (args[0].equals("countfirstlinecols")) {
            try {
                String filename = args[1];
                int numcols = IOUtils.countFirstLineCols(filename);
                System.out.println(""+numcols);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (args[0].equals("containspattern")) {
            try {
                String filename = args[1];
                String pattern = args[2];
                System.out.println(""+IOUtils.containsPattern(filename, pattern));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
        else if (args[0].equals("makeallbinarystrings")) {
            int len = Integer.parseInt(args[1]);
            String[] result = makeAllBinaryStrings('0', '1', len);
            dbgMsg("\n"+StringUtils.arrayToString(result, "\n"));
        }
        else if (args[0].equals("makecombinations")) {
            int n = Integer.parseInt(args[1]);
            int k = Integer.parseInt(args[2]);
            String[] result = makeCombinationStrings('0', '1', n, k);
            dbgMsg("\n"+StringUtils.arrayToString(result, "\n"));
        }
        else if (args[0].equals("choose")) {
            int k = Integer.parseInt(args[1]);
            try {
                List<String> items = IOUtils.readLines(System.in);
                List<List<String>> result = choose(items, k);
                for (List<String> l: result) {
                    System.out.println(StringUtils.listToString(l, " "));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (args[0].equals("mkdirs")) {
            boolean success = IOUtils.mkdirs(args[1]);
            int exitstatus = success ? 0 : -1;
            System.exit(exitstatus);
        }
        else if (args[0].equals("getlocalhostname")) {
            String hostname = getLocalHostName();
            System.out.println(hostname);
            System.exit(0);
        }
        else if(args[0].equals("removeduplicaterows")) {
            try {
                InputStream istream=null;
                OutputStream ostream=null;
                if (args.length==1) {
                    istream = System.in;
                    ostream = System.out;
                }
                else if (args.length==2) {
                    String infilename = args[1];
                    istream = new FileInputStream(infilename);
                    ostream = System.out;
                }
                else if (args.length==3) {
                    String infilename = args[1];
                    String outfilename = args[2];
                    istream = new FileInputStream(infilename);
                    ostream = new FileOutputStream(outfilename);
                }
                else {
                    Logger.error("invalid number of args!");
                    System.exit(-1);
                }
                String[] lines = IOUtils.readLineArray(istream);
                String[] uniqueLines = removeDuplicateRows(lines);
                IOUtils.printLines(ostream, uniqueLines);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Logger.error("illegal command: "+args[0]);
            System.exit(-1);
        }
    }

    public int[] duplicateElements(int[] pArr) {
        int[] result = new int[pArr.length*2];
        for (int i=0; i<pArr.length; i++){
            result[i*2]=pArr[i];
            result[i*2+1]=pArr[i];
        }
        return result;
    }

    public double[] duplicateElements(double[] pArr) {
        double[] result = new double[pArr.length*2];
        for (int i=0; i<pArr.length; i++){
            result[i*2]=pArr[i];
            result[i*2+1]=pArr[i];
        }
        return result;
    }

    public static String[] makeAllBinaryStrings(char pChar1, char pChar2, int pLen) {
        int numStrings = (int)Math.pow(2, pLen);

        String result[] = new String[numStrings];
        for (int i=0; i<numStrings; i++) {
            char[] buf = new char[pLen];
            for (int j=0; j<pLen; j++) {
                int remainder = (i/(int)(Math.pow(2, j))) %2;
                if (remainder==0) {
                    buf[pLen-1-j] = pChar1;
                }
                else {
                    buf[pLen-1-j] = pChar2;
                }
            }
            result[i] = new String(buf);
        }
        return result;
    }


    public static String[] makeCombinationStrings(char pChar1, char pChar2, int pN, int pK ) {
        return (String[])ConversionUtils.collectionToArray(internalMakeCombinationStrings(pChar1, pChar2, pN, pK), String.class);
    }

    /**
     * Make all possible choices of k items from a list.
     * keywords: permutation, combination, combinations, k over n, n over k, choose
     */
    public static <T> List<List<T>> choose(List<T> pList, int pK ) {
        List<String> stringReps = Arrays.asList(makeCombinationStrings('0', '1', pList.size(), pK));
        List<List<T>> result = new ArrayList();
        for (String s: stringReps) {
            List<T> l = new ArrayList();
            for (int i=0; i<pList.size(); i++) {
                if (s.charAt(i) == '1') {
                    l.add(pList.get(i));
                }
            }
            result.add(l);
        }

        return result;
    }


    public static boolean isNumeric(Object p) {
        if (p instanceof Number) {
            return true;
        }
        else if (p instanceof String) {
            String s = (String)p;
            try {
                Double.parseDouble(s);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public static boolean isBoolean(Object p) {
        if (p instanceof Boolean) {
            return true;
        }
        else if (p instanceof String) {
            if (p.equals("true") || p.equals("false")) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public static int possiblyNumericCompare(Object p1, Object p2) {
        try {
            double d1 = ConversionUtils.anyToDouble(p1);
            double d2 = ConversionUtils.anyToDouble(p2);
            // OK, succeeded in converting, so let's compare as numbers
            int result = MathUtils.sign(d1-d2);
            // dbgMsg("possiblyNumericCompare("+p1+","+p2+")="+result);
            return result;
        }
        catch (NumberFormatException e) {
            // resort to natural ordering
            int result = ((Comparable)p1).compareTo(p2);
            // dbgMsg("possiblyNumericCompare("+p1+","+p2+")="+result);
            return result;
        }

    }

    /** Make all strings representing all the k-combinations of a set of size n */
    public static ArrayList internalMakeCombinationStrings(char pChar1, char pChar2, int pN, int pK ) {
        // dbgMsg("Make combinations, n="+pN+", k="+pK);
        if (pK == 0) {
            ArrayList result = new ArrayList();
            result.add(StringUtils.charMultiply(pN, pChar1));
            // dbgMsg("Make combinations, n="+pN+", k="+pK+" returning:\n"+StringUtils.collectionToString(result));
            return result;
        }

        int numCombinations = MathUtils.numCombinations(pN, pK);
        ArrayList result = new ArrayList(numCombinations);
        for (int i=0; i<=pN-pK; i++) {
            ArrayList suffixes = internalMakeCombinationStrings(pChar1, pChar2, pN-i-1, pK-1);
            // for (int j=0; j<=i; j++) {
            // make prefix of length i+1 that has a pChar2 at position i, and pChar1 in other positions
            String zerosString = StringUtils.charMultiply(i, pChar1);
            String prefixString = zerosString + pChar2;
            // dbgMsg("Make combinations, n="+pN+", k="+pK+" made prefix string:\n"+prefixString);

            // append all suffices to the prefix
            for (int j=0; j<suffixes.size(); j++) {
                StringBuffer prefixBuf = new StringBuffer(prefixString);
                prefixBuf.append((String)suffixes.get(j));
                result.add(prefixBuf.toString());
            }
        }

        // dbgMsg("Make combinations, n="+pN+", k="+pK+" returning:\n"+StringUtils.collectionToString(result));
        return result;
    }

    /**
     * Die, printing the given error message as the famous last words of
     * the virtual machine.
     *
     * Also does Logger.endLog() and flushing of stderr and stdout.
     */
    public static void die(String pErrorMessage) {
        Logger.error(pErrorMessage);
        Logger.endLog();
        System.out.flush();
        System.err.flush();
        System.exit(1);
    }

    /**
     * Die, printing the given error message, and then the stack trace as
     * the famous last words of the virtual machine.
     *
     * Also does Logger.endLog() and flushing of stderr and stdout.
     */
    public static void die(String pErrorMessage, Exception pEx) {
        Logger.error(pErrorMessage);
        // Logger.error(""+pEx.getClass().getName());
        Logger.printStackTrace(pEx);
        Logger.endLog();
        System.out.flush();
        System.err.flush();
        System.exit(1);
    }

    /**
     * Die, printing the stack trace as the famous last words of the virtual machine
     *
     * Also does Logger.endLog() and flushing of stderr and stdout.
     */
    public static void die(Exception pEx) {
        Logger.printStackTrace(pEx);
        Logger.endLog();
        System.out.flush();
        System.err.flush();
        System.exit(1);
    }

    public static void logIterator(Iterator pIter) {
        while (pIter.hasNext()) {
            Logger.info(""+pIter.next());
        }
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg("Utils: "+pMsg);
    }




}
