package util;



import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.collections.Distribution;
import util.collections.IMultiMapReadOps;
import util.collections.MultiMap_old;
import util.collections.MultiSet;
import util.collections.StringMultiMap;
import util.collections.tree.NodeAdapter;
import util.collections.tree.TreeNodeAdapter;
import util.commandline.CommandLineTests;
import util.condition.Condition;
import util.condition.IsEmptyStringCondition;
import util.condition.NotCondition;
import util.converter.AnyToIntegerConverter;
import util.converter.Converter;
import util.converter.ObjectToStringConverter;
import util.dbg.Logger;



public final class StringUtils extends CommandLineTests {


    public static final String CMD_H = "h";
    public static final String CMD_TESTALLSTRINGSITERATOR = "testallstringsiterator";
    public static final String CMD_TESTFORMATTERASETC = "testformatterasetc";
    public static final String CMD_TESTDOUBLEQUOTEDSTRINGS = "testdoublequotedstrings";
    public static final String CMD_TESTFASTSPLIT = "testfastsplit";
    public static final String CMD_EXPANDBASHLIST = "expandBashList";
    public static final String CMD_REMOVETRAILINGZEROS = "removetrailingzeros";
    public static final String CMD_FORMATINT = "formatint";
    public static final String CMD_DEC2HEX = "dec2hex";
    public static final String CMD_TESTEXTRACTCOL = "testextractcol";
    public static final String CMD_TESTESCAPEBACKLASHES = "testescapebacklashes";
    public static final String CMD_TESTREMOVESUBSTRING = "testremovesubstring";
    public static final String CMD_TESTUNIQUENAME = "testuniquename";
    public static final String CMD_PARSELINES = "parselines";


    private static DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS;
    private static final DecimalFormat LONGEST_DECIMAL_FORMAT;

    private static final int DEFAULT_NUM_DECIMALS = 4;

    public static final String DASH_LINE = "--------------------------------------------------------------------------------";

    private static final DecimalFormat[] DECIMAL_FORMATS;

    private static final int DECIMAL_FORMAT_MAX_PRECISION = 20;

    static {
        DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();
        DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator('.');

        DECIMAL_FORMATS = new DecimalFormat[DECIMAL_FORMAT_MAX_PRECISION];
        for (int i=0; i<DECIMAL_FORMAT_MAX_PRECISION; i++) {
            int numDecimals = i+1;
            String suffix = stringMultiply(numDecimals, "#");
            DECIMAL_FORMATS[i] = new DecimalFormat("###."+suffix, DECIMAL_FORMAT_SYMBOLS);
        }

        LONGEST_DECIMAL_FORMAT = DECIMAL_FORMATS[DECIMAL_FORMAT_MAX_PRECISION-1];

    }

    public StringUtils(String[] args) {
        super(args);
    }



    @Override
    public void run(String cmd) throws Exception {
        if (cmd.equals(CMD_H)) {
            if (args.getNonOptArgs().size() == 0) {
                try {
                    List<String> lines = IOUtils.readLines(System.in);
                    for (String line: lines) {
                        System.out.println(h(Long.parseLong(line)));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(h(Integer.parseInt(args.shift())));
            }
        }
        else if (cmd.equals("testallstringsiterator")) {
            testAllStringsIterator();
        }
        else if (cmd.equals("testformatterasetc")) {
            for (String s: args.posargs) {
                Long l = Long.parseLong(s);
                System.out.println(formatSize(l));
            }
        }
        else if (cmd.equals("testdoublequotedstrings")) {
            testDoubleQuotedStrings(args.posargs);
        }
        else if (cmd.equals(CMD_TESTFASTSPLIT)) {
            testFastSplit();
        }
        else if (cmd.equals(CMD_REMOVETRAILINGZEROS)) {
            for (String line: IOUtils.readLines()) {
            	System.out.println(removeTrailingZeros(line));
            }
        }
        else if (cmd.equals(CMD_EXPANDBASHLIST)) {
            expandBashListCmdLIne();
        }
        else if (cmd.equals(CMD_FORMATINT)) {
            long val = Long.parseLong(args.shift());
            System.out.println(formatLargeInteger(val));
        }
        else if (cmd.equals(CMD_DEC2HEX)) {
            long i = Long.parseLong(args.shift());
            System.out.println(Long.toHexString(i));
        }
        else if (cmd.equals(CMD_TESTEXTRACTCOL)) {
            int col = Integer.parseInt(args.shift());
            Iterator<String> lineIter = IOUtils.lineIterator(System.in);
            while(lineIter.hasNext()) {
                System.out.println(extractCol(lineIter.next(), '\t', col));
            }
        }
        else if (cmd.equals(CMD_TESTESCAPEBACKLASHES)) {
            List<String> data = CollectionUtils.makeList("foobar", "foobar\\", "\"quoted\"", "\\\"complex\\\"");
            Logger.info("Escape backlashes:");
            for (String s: data) {
                String escaped = escapeBacklashes(s);
                Logger.info("Original: "+s);
                Logger.info("Escaped: "+escaped);
            }
            Logger.info("");
            Logger.info("Escape double quotes:");
            for (String s: data) {
                String escaped = escapeDoubleQuotes(s);
                Logger.info("Original: "+s);
                Logger.info("Escaped: "+escaped);
            }
        }
        else if (cmd.equals("testremovesubstring")) {
            String s = args.shift();
            int start = Integer.parseInt(args.shift());
            int end = Integer.parseInt(args.shift());
            String result = removeSubString(s, start, end);
            System.out.println(result);
        }
        else if (cmd.equals("testuniquename")) {
            // Set<String> usedNames = new HashSet(CollectionUtils.makeList("foo", "fooA", "fooB", "fooC", "fooF"));
            String prefix = "foo_";
            Set<String> usedSuffixes = new LinkedHashSet(CollectionUtils.extractFirst(100, new AllStringsIterator(new ABAlphabet())));
            Set<String> usedNames = new LinkedHashSet();
            usedNames.add(prefix);
            for (String usedSuffix: usedSuffixes) {
                usedNames.add(prefix+usedSuffix);
            }
            Logger.info("Used names:\n"+StringUtils.collectionToString(usedNames));
            String uniqueName = createUniqueName(prefix, usedNames, new AllStringsIterator(new ABAlphabet()));
            Logger.info("generated unique name: "+uniqueName);
        }
        else if (cmd.equals("parselines")) {
            int i=0;
            for (String line: IOUtils.readLines()) {
                i++;
                Logger.info("Parsed map "+i+":");
                Logger.info(mapToString(parseMapLine(line)));
                Logger.info("");
            }
        }
        else {
            System.err.println("Illegal command: "+cmd);
        }
    }

    /**
     * "Monopoly-style" formatting of big bucks etc. e.g. 1000 => 1.000,
     * 1000000=>1.000.000, and so on.
     */
    public static String formatLargeInteger(long pVal) {
        return formatLargeInteger(""+pVal);
    }


    public static int[] findOccurences(String s, char c) {
    	List<Integer> result = new ArrayList<Integer>();
    	int i = s.indexOf(c);
    	while (i != -1) {
    		result.add(i);
    		i = s.indexOf(c, i+1);
    	}
    	return ConversionUtils.toIntArray(result);

    }



    public static void expandBashListCmdLIne() throws IOException {
        String[] lines = IOUtils.readLineArray(System.in);
        // Logger.info("Read lines:\n"+StringUtils.arrayToString(lines));
        for (String in: lines) {
        	for (String out: expandBashLists(in)) {
        		System.out.println(out);
        	}
        }
    }



    /** Expand all lists of style "FOO{foo,bar,baz}BAR" to (FOOfooBAR, FOObarBAR, FOObazBAR). Nested lists not supported! */
    public static List<String> expandBashLists(String p) {
    	int [] openBracketInds = findOccurences(p, '{');
    	int [] closeBracketInds = findOccurences(p, '}');
    	if (openBracketInds.length != closeBracketInds.length) {
    		System.err.println("Warning: unbalanced curly brackets in string: "+p);
    		return Collections.singletonList(p);
    	}
    	else if (openBracketInds.length == 0) {
    		return Collections.singletonList(p);
    	}
    	int n = openBracketInds.length;


    	if (n == 1) {
    	    // one pair of brackets
    	    int i1 = openBracketInds[0];
            int i2 = closeBracketInds[0];
    		String prefix = p.substring(0, i1);
    		String listString = p.substring(i1+1,i2);
    		String suffix = p.substring(i2+1);
    		String[] tok = listString.split(",", -1);
    		ArrayList<String> result = new ArrayList<String>(tok.length);
    		for (String t: tok) {
    			result.add(prefix+t+suffix);
    		}
    		return result;
    	}
    	if (n == 2) {
    		// 2 pairs of brackets
    	    //    case 1: {} {}
    	    //    case 2: { { } }

    	    if (closeBracketInds[0] < openBracketInds[1]) {
    	         // case 1: 2 consecutive pairs of brackets
    	        int i1 = openBracketInds[0];
    	        int i2 = closeBracketInds[0];
        		int i1B = openBracketInds[1];
            	int i2B = closeBracketInds[1];
        		String prefix = p.substring(0, i1);
        		String listString1 = p.substring(i1+1,i2);
        		String middle = p.substring(i2+1, i1B);
        		String listString2 = p.substring(i1B+1,i2B);
        		String suffix = p.substring(i2B+1);
        		String[] tok1 = listString1.split(",", -1);
        		String[] tok2 = listString2.split(",", -1);
        		ArrayList<String> result = new ArrayList<String>(tok1.length*tok2.length);
        		for (String t1: tok2) {
        			for (String t2: tok2) {
        				result.add(prefix+t1+middle+t2+suffix);
        			}
        		}
        		return result;
    	    }
    	    else {
    	        // assume case 2
    	        // expend outer brackets here, expand inner brackets recursively
    	        int i1 = openBracketInds[1];
    	        int i2 = closeBracketInds[0];
                String prefix = p.substring(0, i1);
                String listString = p.substring(i1+1,i2);
                String suffix = p.substring(i2+1);
//                Logger.info("prefix: "+prefix);
//                Logger.info("listString: "+listString);
//                Logger.info("suffix: "+suffix);
                String[] tok = listString.split(",", -1);
                ArrayList<String> result = new ArrayList<String>(tok.length);
                for (String t: tok) {
                    String inner = prefix+t+suffix;
//                    Logger.info("inner: "+inner);
                    result.addAll(expandBashLists(inner));
                }
                return result;
    	    }
    	}
    	if (n == 3) {
        	if (n > 4) {
        		System.err.println("Warning: expanding "+n+" > 3 brackets currently unsupported (line: "+p+"); only considering 2 first");
        	}
        	int i1 = openBracketInds[0];
            int i2 = closeBracketInds[0];
    		int i1B = openBracketInds[1];
        	int i2B = closeBracketInds[1];
        	int i1C = openBracketInds[2];
        	int i2C = closeBracketInds[2];
    		String prefix = p.substring(0, i1);
    		String listString1 = p.substring(i1+1,i2);
    		String middle = p.substring(i2+1, i1B);
    		String listString2 = p.substring(i1B+1,i2B);
    		String middle2 = p.substring(i2B+1, i1C);
    		String listString3 = p.substring(i1C+1,i2C);
    		String suffix = p.substring(i2C+1);
    		String[] tok1 = listString1.split(",", -1);
    		String[] tok2 = listString2.split(",", -1);
    		String[] tok3 = listString3.split(",", -1);
    		ArrayList<String> result = new ArrayList<String>(tok1.length*tok2.length*tok3.length);
    		for (String t1: tok2) {
    			for (String t2: tok2) {
    				for (String t3: tok3) {
        				result.add(prefix+t1+middle+t2+middle2+t3+suffix);
        			}
    			}
    		}
    		return result;
    	}
    	else { // n >= 4
        	if (n > 4) {
        		System.err.println("Warning: expanding "+n+" > 3 brackets currently unsupported (line: "+p+"); only considering 2 first");
        	}
        	int i1 = openBracketInds[0];
            int i2 = closeBracketInds[0];
    		int i1B = openBracketInds[1];
        	int i2B = closeBracketInds[1];
        	int i1C = openBracketInds[2];
        	int i2C = closeBracketInds[2];
        	int i1D = openBracketInds[3];
        	int i2D = closeBracketInds[3];
    		String prefix = p.substring(0, i1);
    		String listString1 = p.substring(i1+1,i2);
    		String middle = p.substring(i2+1, i1B);
    		String listString2 = p.substring(i1B+1,i2B);
    		String middle2 = p.substring(i2B+1, i1C);
    		String listString3 = p.substring(i1C+1,i2C);
    		String middle3 = p.substring(i2C+1, i1D);
    		String listString4 = p.substring(i1D+1,i2D);
    		String suffix = p.substring(i2D+1);
    		String[] tok1 = listString1.split(",", -1);
    		String[] tok2 = listString2.split(",", -1);
    		String[] tok3 = listString3.split(",", -1);
    		String[] tok4 = listString4.split(",", -1);
    		ArrayList<String> result = new ArrayList<String>(tok1.length*tok2.length*tok3.length*tok4.length);
    		for (String t1: tok2) {
    			for (String t2: tok2) {
    				for (String t3: tok3) {
    					for (String t4: tok4) {
            				result.add(prefix+t1+middle+t2+middle2+t3+middle3+t4+suffix);
            			}
        			}
    			}
    		}
    		return result;
    	}

    }

    public static String removeSuffixAndPrefix(String pStr, int pPrefixLen, int pSuffixLen) {

        if (pStr == null) {
            return null;
        }

        return pStr.substring(pPrefixLen, pStr.length()-pSuffixLen);
    }

    /**
     * "Monopoly-style" formatting of big bucks etc. e.g. 1000 => 1.000,
     * 1000000=>1.000.000, and so on.
     */
    public static String formatLargeInteger(String pVal) {
        LinkedList<Character> charList = new LinkedList(ConversionUtils.asList(pVal));
        Collections.reverse(charList);
        ListIterator<Character> listIter = charList.listIterator();
        int counter = 0;
        while (listIter.hasNext()) {
            if (counter < 3) {
                counter++;
            }
            else {
                listIter.add(',');
                counter = 1;
            }
            listIter.next();
        }
        Collections.reverse(charList);
        return collectionToString(charList, "");
    }



    /**
     * Actually compute the sum as a side-effect.
     *
     * @return "x1 + x2 + ... + xn = sum".
     */
    public static String formatSum (double... x) {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        double sum = 0;
        for (double xi: x) {
            sum += xi;

            if (first) {
                first = false;
                result.append(formatFloat(xi));
            }
            else {
                result.append(" + ");
                result.append(formatFloat(xi));
            }
        }

        result.append(" = ");
        result.append(sum);
        return result.toString();
    }

/*
    private static final DecimalFormat DECIMAL_FORMAT_1 = new DecimalFormat("###.#", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_2 = new DecimalFormat("###.##", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_3 = new DecimalFormat("###.###", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_4 = new DecimalFormat("###.####", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_5 = new DecimalFormat("###.#####", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_6 = new DecimalFormat("###.######", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_7 = new DecimalFormat("###.#######", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_8 = new DecimalFormat("###.########", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_9 = new DecimalFormat("###.#########", decimalFormatSymbols);
    private static final DecimalFormat DECIMAL_FORMAT_10 = new DecimalFormat("###.#########", decimalFormatSymbols);
    private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = DECIMAL_FORMAT_4;
    */

    private static final Condition EMPTY_STRING_CONDITION = new IsEmptyStringCondition();
    private static final Condition NON_EMPTY_STRING_CONDITION = new NotCondition(EMPTY_STRING_CONDITION);

    public static final String EMPTY_PATTERN = "^\\s*$";

    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    public static boolean isEmpty(String pString) {
        return pString.matches(EMPTY_PATTERN);
    }

    public static boolean isEmptyOrNull(String pString) {
        return pString == null || isEmpty(pString);
    }

    public static String[] removeEmptyStrings(String pStrings[]) {
        List result = CollectionUtils.extractMatchingObjects(Arrays.asList(pStrings), NON_EMPTY_STRING_CONDITION);
        return ConversionUtils.stringCollectionToArray(result);
    }

    public static String extractFirstCol(String pString, char pSeparator) {
        int sepInd = pString.indexOf(pSeparator);
        if (sepInd != -1) {
            return pString.substring(0, sepInd);
        }
        else {
            return pString;
        }
    }

    /** java-style indexing of cols, of course */
    public static String extractCol(String pString, char pSeparator, int pInd) {
        int left = -1;
        int right = pString.indexOf(pSeparator, left);
        if (right == -1) {
            // just one column
            return pString;
        }

        for (int i=1; i<=pInd; i++) {
            // Logger.info("i="+i);
            left = right;
            if (left == -1) {
                throw new RuntimeException("Not enough columns!");
            }
            right = pString.indexOf(pSeparator, left+1);
        }

        //  Logger.info("Finishing, left="+left+", right="+right);

        if (right != -1) {
            return pString.substring(left+1, right);
        }
        else {
            // the rightmost column
            return pString.substring(left+1);
        }
    }

    /** @see DateUtils#formatSeconds(int) */
    public static String formatSeconds(int pSec) {
        return DateUtils.formatSeconds(pSec);
    }

    /**
     * Parse a white space-delimited "command line" into tokens,
     * such that parts within double quotes are interpreted as a single token.
     *
     * Example input 'foo "bar baz quux"'
     * Example output: ['foo', 'bar baz quux']
     *
     * @return a list of tokens, all of which are guaranteed to be non-empty strings
     */
    public static String[] reconstructDoubleQuotedTokens(String pLine) throws ParseException {
        String[] tokens = pLine.split(" ", -1);
        ArrayList<String> result = new ArrayList(tokens.length);
        // boolean inDoubleQuotedString = false;
        StringBuffer dqString = null;
        for (String tok: tokens) {

            if (dqString != null) {
                // in a double quoted string
                if (tok.length() > 0 && tok.charAt(tok.length()-1) == '"') {
                    // last tok of dq string
                    dqString.append(" "+tok.substring(0, tok.length()-1));
                    result.add(dqString.toString());
                    dqString = null;
                }
                else {
                    // not yet at the end of the dq string
                    dqString.append(" "+tok);
                }
            }
            else {
                // 0 or an even number of double quotes encountered so far
                if (tok.length() > 0  && tok.charAt(0) == '"') {
                    // begin a new dq string
                    if (tok.length() > 1 && tok.charAt(tok.length()-1) == '"') {
                        // token contains the complete dq string
                        result.add(tok.substring(1, tok.length()-1));
                    }
                    else{
                        // token starts a new dq string, to be continued in the next tok
                        dqString = new StringBuffer(tok.substring(1));
                    }
                }
                else {
                    // the easy case: a token without double quotes
                    result.add(tok);
                }
            }
        }

        if (dqString != null) {
            throw new ParseException("Unterminated double quoted string: "+dqString.toString(), 0);
        }

        // last, remove any empty strings from request
        Iterator<String> iter = result.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if (isEmpty(s)) {
                iter.remove();
            }
        }

        return ConversionUtils.stringCollectionToStringArray(result);
    }

    public static String escapeBacklashes_old(String pString) {
        int len = pString.length();
        boolean escapingNecessary = false;
        for (int i=0; i<len; i++) {
            if (pString.charAt(i) == '\\') {
                escapingNecessary = true;
                break;
            }
        }

        if (escapingNecessary) {
            StringBuffer buf = new StringBuffer();
            for (int i=0; i<len; i++) {
                char c = pString.charAt(i);
                if (c == '\\') {
                    buf.append("\\\\");
                }
                else {
                    buf.append(c);
                }
            }
            return buf.toString();
        }
        else {
            // no backlashes
            return pString;
        }
    }

    public static String escapeBacklashes(String pString) {
        return escape('\\', "\\\\", pString);
    }

    public static String escapeDoubleQuotes(String pString) {
        return escape('"', "\\\"", pString);
    }

    public static String escape(char pNonEscaped, String pEscaped, String pString) {
        int len = pString.length();
        boolean escapingNecessary = false;
        for (int i=0; i<len; i++) {
            if (pString.charAt(i) == pNonEscaped) {
                escapingNecessary = true;
                break;
            }
        }

        if (escapingNecessary) {
            StringBuffer buf = new StringBuffer();
            for (int i=0; i<len; i++) {
                char c = pString.charAt(i);
                if (c == pNonEscaped) {
                    buf.append(pEscaped);
                }
                else {
                    buf.append(c);
                }
            }
            return buf.toString();
        }
        else {
            // no backlashes
            return pString;
        }
    }

    /**
     * Method capable of mystically beautifying any string... OK,
     * actually just formats floats a bit... */
    public static String beautifyString(String pString) {
        try {
            int i = Integer.parseInt(pString);
            return ""+i;
        }
        catch (NumberFormatException e) {
            // no int
        }
        try {
            double d = Double.parseDouble(pString);
            return formatFloat(d);
        }
        catch (NumberFormatException e) {
            // no float
        }

        // cannot beautify, just return the ugly old pString
        return pString;
    }

    public static final String formatFloat(double pVal) {
        return formatFloat(pVal, DEFAULT_NUM_DECIMALS);
    }

    public static final String format(double[][] pVals) {
        Matrix m = new Matrix(0, pVals[0].length, false);
        // List rows = new ArrayList();
        for (int i=0; i<pVals.length; i++) {
            // Logger.info("pVals["+i+"].length=="+pVals[i].length);
            List row = ConversionUtils.asList(pVals[i]);
            // Logger.info("row: "+StringUtils.listToString(row));

            m.appendRow(row);
            // for (int j=0; j<pVals.length; j++) {

            // }
        }
        return m.toString();
        // return new Matrix(rows).toString();
    }

    public static final String formatFloat(double pVal, int pDecimals) {
        int numDecimals = pDecimals;
        double smallestDisplayableValue = Math.pow(0.1d, numDecimals);
        double absVal = Math.abs(pVal);
        if (absVal != 0.d) {
            while (absVal < smallestDisplayableValue) {
                numDecimals++;
                smallestDisplayableValue = smallestDisplayableValue*0.1d;
            }
        }
        if (numDecimals <= 0) {
            throw new RuntimeException("Number of defimals cannot be <=0!");
        }
        else if (numDecimals > DECIMAL_FORMAT_MAX_PRECISION) {
            return LONGEST_DECIMAL_FORMAT.format(pVal);
        }
        else {
            return DECIMAL_FORMATS[numDecimals-1].format(pVal);
        }

    }

    public static String formatLong(long pLong) {
        List components = new ArrayList();
        long tmp = pLong;
        while (tmp>0) {
            long component = tmp%1000;
            tmp = tmp/1000;
            if (tmp>0) {
                components.add(formatFixedWidthLong(component, 3));
            }
            else {
                components.add(""+component);
            }
        }
        Collections.reverse(components);
        return listToString(components, ".");
    }

    public static String formatAngle_rad(double pVal) {
        double asDegrees = Math.toDegrees(pVal);
        // dbgMsg("Converted rad to deg: "+pVal+"->"+asDegrees);
        String result = formatFloat(asDegrees, 3);
        // dbgMsg("Returning result: "+result);
        return result;
    }


    public static String arrToStr(StringBuffer[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(pArr[i]);
        }
        return buf.toString();
    }

    public static String arrayToString(int[] pArr) {
        return arrayToString(pArr, " ");
    }

    public static String arrayToString(boolean[] pArr) {
        return arrayToString(pArr, " ");
    }

    public static String arrayToString(char[] pArr) {
        return arrayToString(pArr, " ");
    }


    public static String possiblyNullObjectToString(Object pObj) {
        if (pObj == null) {
            return "null";
        }
        else if (pObj instanceof String) {
            return (String)pObj;
        }
        else {
            return pObj.toString();
        }
    }

    public static double[] toDoubleArray(int[] pArr) {
        double[] result = new double[pArr.length];
        for (int i=0; i<pArr.length; i++) {
            result[i]=pArr[i];
        }
        return result;
    }

    public static String[] toStringArr(int[] pArr) {
        String[] result = new String[pArr.length];
        for (int i=0; i<pArr.length; i++) {
            result[i]=""+pArr[i];
        }
        return result;
    }

    public static String[] toStringArr(double[] pArr) {
        String[] result = new String[pArr.length];
        for (int i=0; i<pArr.length; i++) {
            result[i]=""+pArr[i];
        }
        return result;
    }

    public static String arrayToString(int[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(pArr[i]);
        }
        return buf.toString();
    }

    public static String arrayToString(boolean[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(pArr[i]);
        }
        return buf.toString();
    }

    public static String removeFirstCharacter(String pString) {
    	return pString.substring(1);
    }




    /** "STRING" -> "S T R I N G" */
    public static String divideWithWhiteSpaces(String pString) {
        List<Character> charList = ConversionUtils.asList(pString);
        return StringUtils.collectionToString(charList, " ");
    }

    public static String removeLeadingWhiteSpaces(String pString) {
        Pattern p = Pattern.compile("^\\s*(.*)$");
        if (isEmpty(pString)) {
            return "";
        }
        Matcher m = p.matcher(pString);
        if (m.matches()) {
            return m.group(1);
        }
        else {
            throw new RuntimeException("Tuhannen turjakkeen bazzibazuukkia!");
        }
    }

    public static String removeTrailingWhiteSpaces(String pString) {
        Pattern p = Pattern.compile("^(.*?\\S)\\s*$");
        Matcher m = p.matcher(pString);
        if (m.matches()) {
            return m.group(1);
        }
        else {
            // OK, only white space, it seems; just return a empty string
            return "";
            // throw new RuntimeException("Cannot remove trailing white spaces!");
        }
    }

    /** if a string has only zeros, return an empty string */
    public static String removeTrailingZeros(String pString) {
        int i = pString.length()-1;
        while (i >= 0 && pString.charAt(i) == '0') {
        	i--;
        }
        return pString.substring(0, i+1);
    }

    public static String removeLastComponent(String pString, String pDelim) {
        List components = new ArrayList(Arrays.asList(pString.split(pDelim, -1)));
        CollectionUtils.pop(components);
        if (pDelim.equals("\\.")) {
            // args, kludge forced by the evil nature of regexps...
            pDelim = ".";
        }
        return listToString(components, pDelim);
    }

    /** pStart inclusive, pEnd exclusive, as usual */
    public static String removeSubString(String p, int pStart, int pEnd) {
        return p.substring(0, pStart)+
               p.substring(pEnd);
    }

    public static String removeLastComponents(String pString, String pDelim, int pNumComponentsToRemove) {
        List components = new ArrayList(Arrays.asList(pString.split(pDelim, -1)));
        for (int i=0; i<pNumComponentsToRemove; i++) {
            CollectionUtils.pop(components);
        }
        if (pDelim.equals("\\.")) {
            // args, kludge forced by the evil nature of regexps...
            pDelim = ".";
        }
        return listToString(components, pDelim);
    }

    public static String arrayToString(char[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(pArr[i]);
        }
        return buf.toString();
    }

    public static String arrayToString(double[] pArr) {
        return arrayToString(pArr, "\n");
    }

    public static String removeWhiteSpaces(String pString) {
        String[] components = pString.split("\\s+", -1);
        return arrayToString(components, "");
    }

    public static String arrayToString(double[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(pArr[i]);
        }
        return buf.toString();
    }

    public static String arrayToString(Object[] pArr) {
        return arrayToString(pArr, "\n");
   }

    public static String arrayToString(Object[] pArr, String pDelim) {
        if (pArr.length==0) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        buf.append(pArr[0]);
        for (int i=1; i<pArr.length; i++) {
            buf.append(pDelim);
            buf.append(possiblyNullObjectToString(pArr[i]));
        }
        return buf.toString();
    }


    public static String mapToString(Map pMap, String pKeyValDelim, String pEntryDelim) {
        StringBuffer buf = new StringBuffer();
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            buf.append(possiblyNullObjectToString(key));
            Object val = pMap.get(key);
            buf.append(pKeyValDelim);
            buf.append(possiblyNullObjectToString(val));
            if (keys.hasNext()) {
                buf.append(pEntryDelim);
            }
        }
        return buf.toString();
    }

    public static String mapToString(Map pMap, String pKeyValDelim, String pEntryDelim, Converter pKeyConverter, Converter pValConverter) {
        StringBuffer buf = new StringBuffer();
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            if (pKeyConverter != null) {
                buf.append(pKeyConverter.convert(key));
            }
            else {
                buf.append(possiblyNullObjectToString(key));
            }
            Object val = pMap.get(key);
            buf.append(pKeyValDelim);
            if (pValConverter != null) {
                buf.append(pValConverter.convert(val));
            }
            else {
                buf.append(possiblyNullObjectToString(val));
            }
            if (keys.hasNext()) {
                buf.append(pEntryDelim);
            }
        }
        return buf.toString();
    }

    /** Keywords: isDouble, isFloat, parseDouble */
    public static boolean isNumeric(String p) {
        try {
            Double.parseDouble(p);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /** Keywords: isInteger, is integer */
    public static boolean isIntegral(String p) {
        try {
            Integer.parseInt(p);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /** Even more professional string beautifyer */
    public static String beautifyString_professional(String pString, int pMaxDecimals) {
        if (pString == null) {
            return null;
        }
        if (StringUtils.isNumeric(pString)) {
            Matcher intMatcher = INT_PATTERN.matcher(pString);
            if (intMatcher.matches()) {
                // an integer; no need for formatting...
                return pString;
            }
            else {
                // a float, we presume...
                double val = Double.parseDouble(pString);
                return StringUtils.formatFloat(val, pMaxDecimals);
            }
        }
        else {
            return pString;
            // not numeric, does not overlap our field of business...
        }
    }

    /**
     * Parse a "map line" with the form key1=val1 key2=val2, ... Vals (but not keys)
     * can contain white spaces (but not in the end or beginning). No val or key
     * can contain a '='-
     *
     */
    public static Map<String, String> parseMapLine(String pLine) throws IOException {
        HashMap<String, StringBuffer> tmp = new HashMap();
        String[] tokens = fastSplit(pLine, ' ');
//        String prevkey = null;
        StringBuffer prevVal = null;
        for (int i=0; i<tokens.length; i++) {
            String tok = tokens[i];
//            Logger.info("Handling token: "+tok);
            int j = tok.indexOf('=');
            if (j != -1) {
                // a new key-value pair
                String key = tok.substring(0, j);
                StringBuffer val = new StringBuffer(tok.substring(j+1));
                tmp.put(key, val);
                prevVal = val;
            }
            else {
                // a continuation of the prev val
                if (prevVal != null) {
                    prevVal.append(' ');
                    prevVal.append(tok);
                }
                else {
                    throw new RuntimeParseException("Not parsable as map: "+pLine);
                }
            }
        }

        HashMap<String, String> result = new HashMap(tmp);
        for (String key: tmp.keySet()) {
            result.put(key, tmp.get(key).toString());
        }

        return result;
    }

    /*
    public static boolean isFloat(String p) {
        try {
            double val = Double.parseDouble(p);
            int trunctated = (int)val;
            if (val == (double)trunctated) {
                return trunctated
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
    */

    /**
     * (Approximately) ls -lh type formatting of numbers, with applications including but not limited
     * to expressing file sizes (the unlimited applications naturally having to do with formatting
     * of other quantities having similar numeric magnitude ranges).
     */
    public static String formatSize(long value) {
        long TERA = 1000000000000l;
        long GIGA = 1000000000l;
        long MEGA = 1000000l;
        long KILO = 1000l;
        if (value >= TERA) {
            long teras = value / TERA;
            long bytes = value - teras * TERA;
            long gigas= bytes / GIGA;
            return ""+teras+"."+(gigas/100)+"T";
        }
        else if (value >= GIGA) {
            long gigas = value / GIGA;
            long bytes = value - gigas * GIGA;
            long megas = bytes / MEGA;
            return ""+gigas+"."+(megas/100)+"G";
        }
        else if (value >= MEGA) {
            long megas = value / MEGA;
            long bytes = value - megas * MEGA;
            long kilos = bytes / KILO;
            return ""+megas+"."+(kilos/100)+"M";
        }
        else if (value >= KILO) {
            long kilos = value / KILO;
            long bytes = value - kilos * 1000;
            return ""+kilos+"."+(bytes/ 100)+"K";
        }
        else {
            return ""+value;
        }
    }

    /** keywords: järjestysluku, järjestysluvun */
    public static String formatOrdinal(int pOrdinal) {
    	String asString = ""+pOrdinal;
    	char lastNumber = asString.charAt(asString.length()-1);

    	if (pOrdinal == 11) {
            return "11th";
        }
    	else if (pOrdinal == 12) {
            return "12th";
        }
    	else if (pOrdinal == 13) {
            return "13th";
        }
    	else if (lastNumber == '1') {
            return asString+"st";
        }
        else if (lastNumber == '2') {
            return asString+"nd";
        }
        else if (lastNumber == '3') {
            return asString+"rd";
        }
        else {
            return ""+pOrdinal+"th";
        }
    }

    public static String multiSetToString(MultiSet pSet, String pValCountDelim, String pEntryDelim) {
        Map asMap = new LinkedHashMap(pSet.asObjToWeightMap());
        ConversionUtils.convertVals_inplace(asMap, new AnyToIntegerConverter());
        return mapToString(asMap, pValCountDelim, pEntryDelim);
    }

    public static String distributionToString(Distribution pDistribution, String pValCountDelim, String pEntryDelim) {
        return mapToString(pDistribution.asObjToWeightMap(), pValCountDelim, pEntryDelim);
    }

    /* count occurences of char in string */
    public static int countOccurences(String pString, char pChar) {

        int count = 0;
        int len = pString.length();

        for (int i=0; i<len; i++) {
            if (pString.charAt(i) == pChar) {
                count++;
            }
        }
        return count;
    }


    /**
     * Param pChars a set of characters.
     * Return null, if all characters are in pChars.
     */
    public static Character findFirstCharacterNotInSet(String pString, Set pChars) {
        for (int i=0; i<pString.length(); i++) {
            Character c = new Character(pString.charAt(i));
            if (!(pChars.contains(c))) {
                return c;
            }
        }
        return null;
    }

    /**
    * @param pDelim delimeter between each key-val-pair
    * key and val are separated by an "="-character
    **/
    public static String mapToString(Map pMap, String pDelim) {
        return mapToString(pMap, "=", pDelim);
    }

    public static String mapToString(Map pMap) {
        return mapToString(pMap, "=", "\n");
    }

    /** keywords: startsWith */
    public static boolean hasPrefix(String pString, String pPrefix, boolean pAllowEquality) {
        int prefixLen = pPrefix.length();
        int minStringLen = pAllowEquality ? prefixLen : prefixLen+1;

        if (pString.length() < minStringLen) {
            return false;
        }

        for (int i=0; i<prefixLen; i++) {
            if (pString.charAt(i) != pPrefix.charAt(i)) {
                return false;
            }
        }

        // all chars matched
        return true;
    }

    public static String longestCommonPrefix(String[] pStrings) {
        return longestCommonPrefix(Arrays.asList(pStrings));
    }

    public static String longestCommonPrefix(Collection<String> pStrings) {
        int indexOfLastCommonChar = indexOfLastCommonChar(pStrings);
        if (indexOfLastCommonChar == -1) {
            // no common prefix
            return "";
        }
        else {
            return pStrings.iterator().next().substring(0, indexOfLastCommonChar+1);
        }
    }

    public static String longestCommonSuffix(Collection<String> pStrings) {

        int maxlen = Integer.MAX_VALUE;

        for (String s: pStrings) {
            int len = s.length();
            if (len < maxlen) {
                maxlen = len;
            }
        }

        String longestFoundSuffix = "";

        for (int i=1; i<=maxlen; i++) {
            String s1 = pStrings.iterator().next();
            String candidate = s1.substring(s1.length()-i);
//            Logger.info("Checking: "+candidate);
            boolean accept = true;
            for (String s: pStrings) {
                if (!(s.endsWith(candidate))) {
                    accept = false;
                    break;
                }
            }
            // all had same suffix
            if (accept) {
                longestFoundSuffix = candidate;
            }
            else {
                break;
            }
        }

        return longestFoundSuffix;
    }


    public static String[] removeLongestCommonPrefix(String[] pStrings) {
        String longestCommonPrefix = longestCommonPrefix(pStrings);
        Pattern p = Pattern.compile("^"+longestCommonPrefix+"(.*)$");
        String result[] = new String[pStrings.length];
        for (int i=0; i<result.length; i++) {
            Matcher m = p.matcher(pStrings[i]);
            if (m.matches()) {
                result[i]=m.group(1);
            }
            else {
                result[i]=pStrings[i];
            }
        }
        return result;
    }

    public static List<String> removeLongestCommonSuffix(Collection<String> pStrings) {
        String longestCommonSuffix= longestCommonSuffix(pStrings);
        ArrayList<String> result = new ArrayList(pStrings.size());
        for (String s: pStrings) {
            result.add(s.substring(0, s.length()-longestCommonSuffix.length()));
        }
        return result;
    }

    public static List<String> removeLongestCommonPrefix(Collection<String> pStrings) {
        String longestCommonPrefix= longestCommonPrefix(pStrings);
        ArrayList<String> result = new ArrayList(pStrings.size());
        for (String s: pStrings) {
            result.add(s.substring(longestCommonPrefix.length()));
        }
        return result;
    }


//    public static int indexOfLastCommonChar(Collection<String> pStrings) {
//        List stringLengths = ConversionUtils.convert(pStrings, new StringToStringLenConverter());
//        int minLength = MathUtils.minInt(ConversionUtils.integerCollectionToIntArray(stringLengths));
//
//        for (int i=0; i<minLength; i++) {
//            char theChar = pStrings.iterator().next().charAt(i);
//            for (String s: pStrings) {
//                if (s.charAt(i) != theChar) {
//                    return i-1;
//                }
//            }
//        }
//        // OK, all prefixes of len minLength are equal...
//        return minLength-1;
//    }

    /**
     * In other words, length of longest common prefix-1.
     *   <ul>
     *     <li> -1 if no common characters
     *     <li> -1 if collection is empty
     *     <li> -1 if empty string is included
     *   </ul>
     */
    public static int indexOfLastCommonChar(Collection<String> pStrings) {
        int minLen = Integer.MAX_VALUE;

        for (String s: pStrings) {
            int len = s.length();
            if (len < minLen) {
                minLen = len;
            }
        }

        if (minLen == Integer.MAX_VALUE) {
            // no strings given as input, no common characters by definition
            return -1;
        }

        if (minLen == 0) {
            // the empty string is included, no common characters by definition
            return -1;
        }

        for (int i=0; i<minLen; i++) {
            char c = pStrings.iterator().next().charAt(i);
            for (String s: pStrings) {
                if (s.charAt(i) != c) {
                    return i-1;
                }
            }
        }
        // OK, all prefixes of len minLength are equal...
        return minLen-1;
    }

    public static <K,V> String toString(StringMultiMap pMap) {
        return toString(pMap, "\n\t", "\n\t", "\n");
    }

    public static <K,V> String toString(StringMultiMap pMap,
                                        String pKeyValDelim,
                                        String pValDelim,
                                        String pEntryDelim) {
        StringBuffer buf = new StringBuffer();
        Iterator<String> keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            buf.append(possiblyNullObjectToString(key));
            buf.append(pKeyValDelim);
            Set<String> vals = pMap.getSet(key);
            buf.append(colToStr(vals,pValDelim));
            if (keys.hasNext()) {
                buf.append(pEntryDelim);
            }
        }

        return buf.toString();
    }

    public static String multiMapToString(IMultiMapReadOps pMap,
                                          String pKeyValDelim,
                                          String pValDelim,
                                          String pEntryDelim) {
        StringBuffer buf = new StringBuffer();
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            buf.append(possiblyNullObjectToString(key));
            buf.append(pKeyValDelim);
            Set vals = pMap.get(key);
            buf.append(collectionToString(vals,pValDelim));
            if (keys.hasNext()) {
                buf.append(pEntryDelim);
            }
        }
        return buf.toString();
    }


    public static String multiMapToString(IMultiMapReadOps pMap,
                                          String pKeyValDelim,
                                          String pValDelim,
                                          String pEntryDelim,
                                          Converter pKeyFormatter,
                                          Converter pValFormatter) {
        StringBuffer buf = new StringBuffer();

        if (pKeyFormatter == null) {
            pKeyFormatter = new ObjectToStringConverter();
        }
        if (pValFormatter == null) {
            pValFormatter = new ObjectToStringConverter();
        }

        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            buf.append(pKeyFormatter.convert(key));
            buf.append(pKeyValDelim);
            Set vals = pMap.get(key);
            buf.append(collectionToString(vals,pValDelim,pValFormatter));
            if (keys.hasNext()) {
                buf.append(pEntryDelim);
            }
        }

        return buf.toString();
    }

    public static String multiMapToString(IMultiMapReadOps pMap) {
        return multiMapToString(pMap, "\n\t", "\n\t", "\n");
    }

    public static String multiMapToString(IMultiMapReadOps pMap,
                                          Converter pKeyFormatter,
                                          Converter pValFormatter) {
        return multiMapToString(pMap, "\n\t", "\n\t", "\n", pKeyFormatter, pValFormatter);
    }

    public static String multiMapToString(MultiMap_old pMap, String pKeyValDelim, String pValDelim, String pEntryDelim) {
        StringBuffer buf = new StringBuffer();
        Iterator keys = pMap.keySet().iterator();
        while(keys.hasNext()) {
            Object key = keys.next();
            buf.append(possiblyNullObjectToString(key));
            buf.append(pKeyValDelim);
            Set vals = pMap.get(key);
            Iterator valIter = vals.iterator();
            while(valIter.hasNext()) {
                Object val = valIter.next();
                buf.append(pValDelim);
                buf.append(possiblyNullObjectToString(val));
                buf.append(pEntryDelim);
            }
        }
        return buf.toString();
    }

    public static String multiMapToString(MultiMap_old pMap) {
        return multiMapToString(pMap, "\n", "\t", "\n");
    }


    public static String iteratorToString(Iterator pIter) {
        return iteratorToString(pIter, "\n");
    }

    public static String iteratorToString(Iterator pIter, String pDelim) {
        if (!pIter.hasNext()) {
            return "";
        }
        else {
            StringBuffer buf = new StringBuffer();
            Object first = pIter.next();
            buf.append(first);
            while(pIter.hasNext()) {
                Object o = pIter.next();
                buf.append(pDelim);
                buf.append(o);
            }
            return buf.toString();
        }
    }

    /** A shorthand wrapper to collectionToString */
    public static String colToStr(Collection pCol, String pDelim) {
        return collectionToString(pCol, pDelim);
    }


    public static String collectionToString(Collection pCol, String pDelim) {
        if (pCol.size()==0) {
            return "";
        }
        else {
            StringBuffer buf = new StringBuffer();
            Iterator i = pCol.iterator();
            Object first = i.next();
            buf.append(first);
            while(i.hasNext()) {
                Object o = i.next();
                buf.append(pDelim);
                buf.append(o);
            }
            return buf.toString();
        }
    }

    public static <T> String collectionToString(Collection pCol, String pDelim, Converter<T, String> pFormatter) {
        if (pCol.size()==0) {
            return "";
        }
        else {
            StringBuffer buf = new StringBuffer();
            Iterator<T> i = pCol.iterator();
            T first = i.next();
            buf.append(pFormatter.convert(first));
            while(i.hasNext()) {
                T o = i.next();
                buf.append(pDelim);
                buf.append(pFormatter.convert(o));
            }
            return buf.toString();
        }
    }

    public static void writeCollection(Collection pCol, PrintStream pStream, String pDelim) {
        Iterator i = pCol.iterator();
        while(i.hasNext()) {
            Object o = i.next();
            pStream.print(possiblyNullObjectToString(o));
            if (i.hasNext()) {
                pStream.print(pDelim);
            }
        }
    }

    public static String collectionToString(Collection pCol) {
        Object[] arr = pCol.toArray(new Object[pCol.size()]);
        return arrayToString(arr);
    }

    public static String listToString(List pList) {
        return listToString(pList, "\n");
    }

    public static void listToString_fast(List pList, String pDelim, StringBuffer pBuf) {
        int size = pList.size();
        pBuf.setLength(0);
        if (size==0) {
            return;
        }
        if (pList instanceof RandomAccess) {
            pBuf.append((pList.get(0)));
            for (int i=1; i<size; i++) {
                pBuf.append(pDelim);
                pBuf.append(pList.get(i));
            }
            return;
        }
        else {
            Iterator i = pList.iterator();
            Object first = i.next();
            pBuf.append(first);
            while(i.hasNext()) {
                Object o = i.next();
                pBuf.append(pDelim);
                pBuf.append(o);
            }
            return;
        }
    }

    public static String listToString(List pList, String pDelim) {
        int size = pList.size();
        if (size==0) {
            return "";
        }
        if (pList instanceof RandomAccess) {
            StringBuffer buf = new StringBuffer();
            buf.append(possiblyNullObjectToString(pList.get(0)));
            for (int i=1; i<size; i++) {
                buf.append(pDelim);
                buf.append(possiblyNullObjectToString(pList.get(i)));
            }
            return buf.toString();
        }
        else {
            // list does not support random access
            return listToString(new ArrayList(pList), pDelim);
        }
    }

    public static int hamming(String p1, String p2) {
        int len = p1.length();
        int dist = 0;
        for (int i=0; i<len; i++) {
            if (p1.charAt(i)!=p2.charAt(i)) {
                dist++;
            }
        }
        return dist;
    }

    public static String charMultiply(int pMultiplier, char pChar) {
        if (pMultiplier<=0) {
            return "";
        }
        char[] buf = new char[pMultiplier];
        for (int i=0; i<pMultiplier; i++) {
            buf[i] = pChar;
        }
        return new String(buf);
    }

    public static String stringMultiply(int pMultiplier, String pString) {
        if (pMultiplier<=0) {
            return "";
        }
        StringBuffer buf = new StringBuffer(pMultiplier*pString.length());
        for (int i=0; i<pMultiplier; i++) {
            buf.append(pString);
        }
        return buf.toString();
    }

    public static String formatFixedWidthField(String pString, int pMinLen) {
        String tmp;
        if (pString == null) {
            tmp = "null";
        }
        else {
            tmp = pString;
        }
        int len = tmp.length();
        if (len<pMinLen) {
            return tmp+makeSpaceString(pMinLen-len);
        }
        else return tmp;
    }

    public static String formatFixedWidthLong(long pLong, int pLen) {
        String tmp = ""+pLong;
        int len = tmp.length();
        if (len>=pLen) {
            return tmp;
        }
        int lendiff = pLen-len;
        return stringMultiply(lendiff, "0")+tmp;
    }



   /**
    * Formats a set of fields, each with a minimum widht
    * If some of the strings is longer than it's respective col width,
    * fails pathetically.
    */
    public static String formatStrings(String[] pStrings, int[] pColWidths) {
        if (pStrings.length != pColWidths.length) {
            throw new RuntimeException("Kehnot parametrit.");
        }

        StringBuffer result = new StringBuffer(MathUtils.sum(pColWidths));
        for (int i=0; i<pStrings.length; i++) {
            result.append(formatFixedWidthField(pStrings[i], pColWidths[i]));
        }
        return result.toString();
    }

    /**
    * Formats a set of fields, each with a minimum widht
    * If some of the strings is longer than it's respective col width,
    * fails pathetically.
    */
    public static String formatStrings(List pObjects, int[] pColWidths) {
        int numObjects = pObjects.size();

        if (numObjects != pColWidths.length) {
            throw new RuntimeException("Kehnot parametrit.");
        }

        StringBuffer result = new StringBuffer(MathUtils.sum(pColWidths));

        for (int i=0; i<numObjects; i++) {
            result.append(formatFixedWidthField(possiblyNullObjectToString(pObjects.get(i)), pColWidths[i]));
        }
        return result.toString();
    }


    /**
    * Formats a set of fields, each with a minimum widht
    * If some of the strings is longer than it's respective col width,
    * fails pathetically.
    */
    public static String formatList(List pObjs, int[] pColWidths) {

        if (pObjs.size() != pColWidths.length) {
            throw new RuntimeException("Kehnot parametrit.");
        }

        StringBuffer result = new StringBuffer(MathUtils.sum(pColWidths));
        for (int j=0; j<pObjs.size(); j++) {
            result.append(formatFixedWidthField(possiblyNullObjectToString(pObjs.get(j)), pColWidths[j]));
        }
        return result.toString();
    }

    /**
     * Formats a set of fields, each with a minimum widht
     * If some of the strings is longer than it's respective col width,
     * fails pathetically.
     */
     public static <T> String formatList(List<T> pList,
                                         int[] pColWidths,
                                         Converter<T, String> pFormatter,
                                         String pNullRep) {
         if (pList.size() != pColWidths.length) {
             throw new RuntimeException("Kehnot parametrit. list size: "+pList.size()+" vs. colWidhts len: "+pColWidths.length);
         }

         StringBuffer result = new StringBuffer(MathUtils.sum(pColWidths));
         for (int j=0; j<pList.size(); j++) {
             Object o = pList.get(j);
             String rep;
             if (o != null) {
                 rep = pFormatter.convert(pList.get(j));
             }
             else {
                 rep = pNullRep;
             }
             result.append(formatFixedWidthField(rep, pColWidths[j]));
         }
         return result.toString();
     }

    /**
     * Formats a table, represented by a list (rows) of lists (columns of a row)
     *
     * If all rows are not equally long, "missing" cols are interpreted as nulls.
     *
     * Nulls are formatted as pNullRep.
     */
     public static <T> String formatTable(List<List<T>> pRows,
                                          Converter<T, String> pFormatter,
                                          String pNullRep) {
         int numCols = 0;

         // first pass: count max number of cols
         for (List<T> row: pRows) {
             numCols = Math.max(numCols, row.size());
         }

         // second pass: count string len in each col
         int[] colWidths = new int[numCols];
         for (List<T> row: pRows) {
             for (int j=0; j<row.size(); j++) {
                String formatted = pFormatter.convert(row.get(j));
                int len = formatted.length();
                colWidths[j] = MathUtils.max(colWidths[j], len+1);
             }
         }

         // third pass: format

         StringBuffer buf = new StringBuffer();
         for (List<T> row: pRows) {
             buf.append(formatList(row, colWidths, pFormatter, pNullRep));
             buf.append("\n");
         }

         return buf.toString();
     }

     /**
      * Formats a table, represented by a list (rows) of lists (columns of a row)
      *
      * If all rows are not equally long, "missing" cols are interpreted as nulls.
      *
      * Nulls are formatted as pNullRep.
      */
      public static <T> String formatTable(List<List<T>> pRows,
                                           String pNullRep) {
          return formatTable(pRows, new ObjectToStringConverter(), pNullRep);
      }



    public static String makePrefixPaddedString(String pString, int pMinLen, char pPadCharacter) {
        int len = pString.length();
        if (len<pMinLen) {
            return stringMultiply((pMinLen-len), ""+pPadCharacter)+pString;
        }
        else return pString;

    }

    /** Keywords: replaceSuffix, replaceType, replace suffix, replace type*/
    public static String replaceExtension(String pOriginalPath, String pNewExtension) {
        String[] tokens = split_with_whitespace_removal(pOriginalPath, "\\.");
        ArrayList tokenList = new ArrayList(Arrays.asList(tokens));
        String prefix = collectionToString(tokenList.subList(0, tokenList.size()-1), ".");
        return prefix+"."+pNewExtension;
    }

    public static File replaceExtension(File pOriginalPath, String pNewExtension) {
        return new File(replaceExtension(pOriginalPath.getPath(), pNewExtension));
    }


    public static String removeExtension(String pOriginalPath) {
        String[] tokens = split_with_whitespace_removal(pOriginalPath, "\\.");
        ArrayList tokenList = new ArrayList(Arrays.asList(tokens));
        return collectionToString(tokenList.subList(0, tokenList.size()-1), ".");
    }

    /** @return null if no extension (i.e., no '.' in filename). */
    public static String getExtension(File file) {
         String name = file.getName();
         if (name.contains(".")) {
             String[] tokens = split_with_whitespace_removal(name, "\\.");
             return tokens[tokens.length-1];
         }
         else {
             return null;
         }

    }

    public static String getExtension(String path) {
        return getExtension(new File(path));
    }

    public static boolean containsUpperCaseLetters(String pString) {
        for (int i=0; i<pString.length(); i++) {
            if (Character.isUpperCase(pString.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsWhiteSpace(String pString) {
        for (int i=0; i<pString.length(); i++) {
            if (Character.isWhitespace(pString.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /** fiksumpi kuin Stringtokenizer, sillä käyttää regexpejä */
    public static String[] split(String pString) {
        return split_with_whitespace_removal(pString, "\\s+");
    }

    /** fiksumpi kuin Stringtokenizer, sillä käyttää regexpejä */
    public static String[] splitToSegmentsOfLen(String pString, int pMaxLen) {
        ArrayList result = new ArrayList();
        int len = pString.length();
        int segmentStart = 0;
        int segmentEnd;
        while(segmentStart<len) {
            segmentEnd = MathUtils.min(segmentStart+pMaxLen, len);
            String segment = pString.substring(segmentStart, segmentEnd);
            result.add(segment);
            segmentStart = segmentEnd;
        }
        return ConversionUtils.stringCollectionToArray(result);
    }




     /**
      * Splits a string according to pDelim.
      * Let n = pGroups.length.
      * The string is split into n parts; For all 0 <= i <= n, the i:th part shall contains pGroups[i] tokens.
      *
      * Example:
      *   pString = "A B C D E F G"
      *   pDelim = " "
      *   pGroups = {1,2,3,1}
      *
      * -> result = {"A", "B C", "D E F", "G"}
      *
      * Note that this enforces the invariant: sum(pGroups) = number of tokens in pString
      * If the invariant is not fulfilled, throws a ParseException.
      *
      * Note that this previously removed leading and trailing white space; this is no longer the case!
      */
    public static String[] split(String pString, String pDelim, int[] pGroups) throws RuntimeParseException {
        String[] tokens = pString.split(pDelim, -1);

        int numTokensExpected = MathUtils.sum(pGroups);
        if (tokens.length != numTokensExpected) {
        	// Logger.importantInfo("LINE: "+pString+"ENDLINE");
        	// Logger.importantInfo("TOKENS:\n"+StringUtils.arrayToString(tokens));
            throw new RuntimeParseException("The number of tokens on line("+tokens.length+") does not match the number sum of groups ("+numTokensExpected+")");
        }

        ArrayList result = new ArrayList(pGroups.length);
        int tokenInd = 0;
        for (int i=0; i<pGroups.length; i++) {
            StringBuffer buf = new StringBuffer();
            for (int j=0; j<pGroups[i]; j++) {
                if(j!=0) {
                    buf.append(" ");
                }
                buf.append(tokens[tokenInd++]);
            }
            result.add(buf.toString());
        }
        return (String[])ConversionUtils.collectionToArray(result, String.class);
    }


    /**
     * Interprets string as an array of tokens delimeted by pDelim.
     * "Replaces" the n:th element by the given new text; of course, as string
     * are immutable, we have to produce a new string.
     * Indexing starts from 0.
     */
    public static String replaceNthElement(String pString, String pReplacementText, String pDelim, int pInd) {
        String [] strings = pString.split(pDelim, -1);
        strings[pInd] = pReplacementText;
        return arrayToString(strings, pDelim);
    }


    /** String.split so that leading and trailing white space is removed first */
    public static String[] split_with_whitespace_removal(String pString, String pDelim) {
        String tmp = removeLeadingWhiteSpaces(pString);
        tmp = removeTrailingWhiteSpaces(tmp);
        return tmp.split(pDelim);
    }

    /**
     * See also other version of fastSplit(). The version presented here
     * does not require knowing the number of cols in advance.
     */
    public static String[] fastSplit(String pString,
                                     char pSeparator) {
        int numCols = countOccurences(pString, pSeparator) + 1;
        String[] result = new String[numCols];
        fastSplit(pString, pSeparator, result);
        return result;

    }

    /** Deduce numcols from pResult */
    public static void fastSplit(String pString,
                                 char pSeparator,
                                 String[] pResult) {
        fastSplit(pString,
                  pSeparator,
                  pResult,
                  pResult.length);
    }



    /**
     * More (much so) efficient alternative for String.split() (with some restrictions):
     *
     * The number of cols must be known in advance, and a suitable
     * array for storing the results must be provided; also separator is just a
     * single char. Otherwise results should be as with ordinary split,
     * with 10-fold improvement in efficiency!!
     *
     * - If there are more columns in data than in pResult, excess columns
     *  shall be discarded.
     *
     * - If there are less columns in data than in pResult, a RuntimeException
     * shall be thrown.
     *
     */
    public static void fastSplit(String pString,
                                 char pSeparator,
                                 String[] pResult,
                                 int pNumCols) {
        // int numCols = pResult.length;

        if (pNumCols == 0) {
            // Special case 1: nothing needs to be done,
            // as there is no space to store the results into
        }
        else if (pNumCols == 1) {
            // Special case 2: a only one column
            pResult[0] = pString;
        }
        else {
            // more than one column

            // find token 0
            int left = -1;
            // String[] pResult = new String[numCols];

            int right = pString.indexOf(pSeparator, left);
            if (right == -1) {
                throw new RuntimeException("Only one column in pString: "+pString);
            }

            if (right != -1) {
                pResult[0] = pString.substring(left+1, right);
            }
            else {
                // the rightmost column
                pResult[0] = pString.substring(left+1);
            }

            // find tokens 1..(pNumCols-1)
            for (int i=1; i<pNumCols; i++) {
                // Logger.info("i="+i);
                left = right;
                if (left == -1) {
                    throw new RuntimeException("Only "+i+"/"+pNumCols+" columns in pString: "+pString);
                }
                right = pString.indexOf(pSeparator, left+1);
                // result[i] = left+1;
                if (right != -1) {
                    pResult[i] = pString.substring(left+1, right);
                }
                else {
                    // the rightmost column
                    pResult[i] = pString.substring(left+1);
                }
            }
        }
    }

    /**
     * More (much so) efficient alternative for String.split() (with some restrictions):
     *
     * The number of cols must be known in advance, and a suitable
     * array for storing the results must be provided; also separator is just a
     * single char. Otherwise results should be as with ordinary split,
     * with 10-fold improvement in efficiency!!
     *
     * Columns are stored to the existing StringBuffers in pResult.
     *
     * It is possible to only extract selected columns, by setting some of buffers
     * in the result array to null.
     *
     *
     * - If there are more or less columns in data than in pResult, an UnexpectedNumColumnsException
     * shall be thrown.
     *
     */
    public static void fastSplit(String pString,
                                 char pSeparator,
                                 StringBuffer[] pResult) throws UnexpectedNumColumnsException {
        int numColsExpected = pResult.length;

        if (numColsExpected== 0) {
            // Special case 1: nothing needs to be done,
            // as there is no space to store the results into
        }
        else if (numColsExpected== 1) {
            // Special case 2: a only one column
            if (pResult[0] != null) {
                pResult[0].setLength(0);
                pResult[0].append(pString);
            }
        }
        else {
            // more than one column

            // find token 0
            int left = -1;
            int right = pString.indexOf(pSeparator, left);
            if (right == -1) {
                throw new UnexpectedNumColumnsException(pString, 1, numColsExpected);
            }

            // set token 0 if needed
            if (pResult[0] != null) {
                pResult[0].setLength(0);
                if (right != -1) {
                    pResult[0].append(pString.substring(left+1, right));
                }
                else {
                    // the rightmost column
                    pResult[0].append(pString.substring(left+1));
                }
            }


            // find tokens 1..(numCols-1)
            for (int i=1; i<numColsExpected; i++) {
                // Logger.info("i="+i);
                left = right;
                if (left == -1) {
                    throw new UnexpectedNumColumnsException(pString, i, numColsExpected);
                }
                right = pString.indexOf(pSeparator, left+1);

                if (pResult[i] != null) {
                    pResult[i].setLength(0);

                    if (right != -1) {
                        pResult[i].append(pString.substring(left+1, right));
                    }
                    else {
                        // the rightmost column
                        pResult[i].append(pString.substring(left+1));
                    }
                }
            }

            if (right != -1) {
                throw new UnexpectedNumColumnsException(pString, -1, numColsExpected);
            }
        }
    }


    public static String lastComponent(String pString, String pDelim) {
        Pattern p = Pattern.compile(pDelim);
        String[] tokens = p.split(pString, -1);
        return tokens[tokens.length-1];
    }

    /**
     * If pExistingNames does not contain pName, returns pName. Else
     * creates a unique name from pName by appending the first possible suffix
     * from pSuffixCandidates. A suffix is "possible" if appending it does not
     * produce any name in pExistingNames.
     *
     * If pSuffixCandidates does not contain any more elements, and no possible
     * name has been found, sadly resort to throwing a RuntimeException.
     *
     */
    public static String createUniqueName(String pName,
                                          Set<String> pExistingNames,
                                          Iterator<String> pSuffixCandidates) {
        String nameCandidate = pName;
        boolean nameOK = false;
        while(!nameOK) {
            // Logger.info("Testing name: "+nameCandidate);
            if (!pExistingNames.contains(nameCandidate)) {
                nameOK = true;
            }
            else {
                // try again
                nameCandidate = pName+pSuffixCandidates.next();
            }
        }
        return nameCandidate;
    }

    /** chop last character away */
    public static String chop(String pString) {
        return pString.substring(0, pString.length()-1);
    }

    /** chop last character away, if it is pChar */
    public static String chop(String pString, char pChar) {
        if (pString.charAt(pString.length()-1) == pChar) {
            return pString.substring(0, pString.length()-1);
        }
        else {
            return pString;
        }
    }

    public static String toMoreInformativeString(Object pObj) {
        if (pObj == null) {
            return "null";
        }
        else {
            return "class: "+pObj.getClass() + ", toString(): "+pObj.toString();
        }
    }

    /*
    public static String removeLastComponent(String pString, String pDelim) {
        Pattern p = Pattern.compile(pDelim);
        String[] tokens = p.split(pString);
        String result = tokens[0];
        for (int i=1; i<tokens.length-1; i++) {
            result+=pDelim;
            result+=tokens[i];
        }
        return result;
    }
    */

    public static String firstComponent(String pString, String pDelim) {
        Pattern p = Pattern.compile(pDelim);
        String[] tokens = p.split(pString);
        return tokens[0];
    }

    public static String makeSpaceString(int pLen) {
        return stringMultiply(pLen, " ");
    }

    public static void main(String[] args) throws Exception {
        StringUtils su = new StringUtils(args);
        su.run();
    }

    private static void testAllStringsIterator() {
        // AllStringsIterator iter = new AllStringsIterator(new AToZAlphabet());
        AllStringsIterator iter = new AllStringsIterator(new ABAlphabet());

        List<String> first1000 = CollectionUtils.extractFirst(1000, iter);

        System.out.println(StringUtils.collectionToString(first1000));
    }

    private static void testDoubleQuotedStrings(List<String> pArgs) throws Exception {
        StringBuffer buf = new StringBuffer();

        for (String arg: pArgs) {
            buf.append(" ");
            buf.append("\"");
            buf.append(arg);
            buf.append("\"");
        }

        // get rid of the first, unnecessarily added, space:
        String line = buf.toString().substring(1);

        String[] tokens = reconstructDoubleQuotedTokens(line);

        System.out.println("Tokens:\n"+StringUtils.arrayToString(tokens,"\n"));



    }

    private static void testFastSplit() throws IOException {
        String data = IOUtils.readFirstLine(System.in);
        String[] result = fastSplit(data, ' ');
        System.out.println(arrayToString(result));
    }

    public static String h(long p) {
    	try (Formatter formatter = new Formatter(Locale.US)) {
	    	if (p >= 1000000000) {
	    		return formatter.format("%.1fG", (double)p/1000000000).toString();
	    	}
	    	else if (p >= 1000000) {
	    		return formatter.format("%.1fM", (double)p/1000000).toString();
	    	}
	    	else if (p >= 1000) {
	    		return formatter.format("%.1fk", (double)p/1000).toString();
	    	}
	    	else {
	    		return ""+p;
	    	}    	
    	}
    }

    /**
     * Note that there is always an endline at the end of the resulting
     * string (unless it is empty).
     */
    public static String reflectiveToString(Object p) {
        try {
            StringBuffer result = new StringBuffer();
            Class c = p.getClass();
            for (Field f : c.getDeclaredFields()) {
                result.append(f.getName()+":\n");
                f.setAccessible(true);
                Object val = f.get(p)+"\n";

                result.append(""+val+"\n");
            }
            return result.toString();
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     *  Itarates all strings of the given alphabet. Shorter strings are
     *  iterated first. No limit on the number of strings iterated!
     *  So iteration must be terminated by some other method than
     *  calling next()!!!
     */
    public static class AllStringsIterator implements Iterator<String> {

        List<Character> mAlphabet;
        char mLastCharOfAlphabet;
        private String mCurrentString;

        public AllStringsIterator (List<Character> pAlphabet) {
            mAlphabet = pAlphabet;
            mLastCharOfAlphabet = pAlphabet.get(pAlphabet.size()-1);
            mCurrentString = ""+mAlphabet.get(0);

        }

        @Override
        public boolean hasNext() {
            return true; // sic
        }

        @Override
        public String next() {
            String result = mCurrentString;
            generateNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }


        private void generateNext() {
            // Logger.info("-------------------");
            // Logger.info("generateNext, curString: "+mCurrentString);
            int numSuffixingLastChars = numSuffixingLastChars();
            int curLen = mCurrentString.length();
            // Logger.info("numSuffixingLastChars: "+numSuffixingLastChars);
            // Logger.info("curLen: "+curLen);

            if (numSuffixingLastChars == 0) {
                // the ordinary case; last character not a "last char" (sic)
                char oldLastChar = mCurrentString.charAt(curLen-1);
                char newLastChar = (char)(oldLastChar+1);
                String prefix;
                if (curLen == 1) {
                    prefix = "";
                }
                else {
                    prefix = mCurrentString.substring(0, curLen-1);
                }
                mCurrentString = prefix+newLastChar;
            }
            else if (numSuffixingLastChars == curLen) {
                // generate a new, longer string!
                mCurrentString = stringMultiply(curLen+1, ""+mAlphabet.get(0));
            }
            else {
                // Ok, the current string has a suffix consisting of
                // numSuffixingLastChars "last characters"
                // updateInd is the index of the char which gets incremented
                // (the suffix is reset to the first char)
                int updateInd = mCurrentString.length()-1-numSuffixingLastChars;
                String prefix = mCurrentString.substring(0, updateInd);
                char updated = (char)(mCurrentString.charAt(updateInd)+1);
                String suffix = stringMultiply(numSuffixingLastChars, ""+mAlphabet.get(0));
                mCurrentString = prefix+updated+suffix;
            }
            // Logger.info("curString is now: "+mCurrentString);
        }

        /** In a A-Z alphabet, the number of suffixing Z:s */
        private int numSuffixingLastChars() {
            int i = mCurrentString.length()-1;
            while (i >= 0 && mCurrentString.charAt(i) ==  mLastCharOfAlphabet) {
                i--;
            }
            // if all chars were last chars, i is now -1
            // if none were last chars, i is now mCurrentString.length()-1
            return mCurrentString.length()-1-i;
        }

    }

    public static class ABAlphabet extends ArrayList<Character> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -2664917303330764320L;

		public ABAlphabet() {
            super();
            add('A');
            add('B');
        }
    }

    private static String indentString(int pLevel, int pIndent) {
        return StringUtils.stringMultiply(pLevel*pIndent, " ");
    }


    /**
     * keywords: printtree, outputtree, print tree, output tree
     */
    public static <T> String formatTree(T pRoot,
                                        TreeNodeAdapter<T> pNodeAdapter,
                                        int pIndent,
                                        boolean pIncludeRoot) {
        return formatTree(pRoot,
                          pNodeAdapter,
                          pIndent,
                          pIncludeRoot,
                          new ObjectToStringConverter<T>());
    }

    /** Element formatting for method @link{formatTree} */
    private static <T> void formatElement(T elem,
            							  TreeNodeAdapter<T> nodeAdapter,
            							  int level,
            							  int indent,
            							  StringBuffer buf,
            							  Converter<T, String> nodeFormatter,
            							  Set<T> visitedNodes) {

    	if (visitedNodes.contains(elem)) {
    		throw new RuntimeException("Not a tree: trying to revisit node: "+nodeFormatter.convert(elem));
    	}

    	visitedNodes.add(elem);

        // print element
        buf.append(indentString(level, indent)+nodeFormatter.convert(elem)+"\n");

        // print children
        for (T child: nodeAdapter.children(elem)) {
            formatElement(child, nodeAdapter, level+1, indent, buf, nodeFormatter, visitedNodes);
        }
    }

    public static <T> String formatTree(T root,
                                        TreeNodeAdapter<T> nodeAdapter,
                                        int indent,
                                        boolean includeRoot,
                                        Converter<T, String> nodeFormatter) {
        StringBuffer result = new StringBuffer();
        Set<T> visitedNodes = new HashSet<T>();

        if (includeRoot) {
            formatElement(root,  nodeAdapter, 0, indent, result, nodeFormatter, visitedNodes);
        }
        else {
            // exclude root
            for (T e: nodeAdapter.children(root)) {
                formatElement(e,  nodeAdapter, 0, indent, result, nodeFormatter, visitedNodes);
            }
        }

        return result.toString();
    }


    /**
     * keywords: printtree, outputtree, print tree, output tree
     */
    public static <T> String formatTree(T pRoot,
                                        NodeAdapter pNodeAdapter,
                                        int pIndent,
                                        boolean pIncludeRoot) {
        return formatTree(pRoot,
                          pNodeAdapter,
                          pIndent,
                          pIncludeRoot,
                          new ObjectToStringConverter());
    }

    public static <T> String formatTree(T pRoot,
                                        NodeAdapter<T> pNodeAdapter,
                                        int pIndent,
                                        boolean pIncludeRoot,
                                        Converter<T, String> pNodeFormatter) {
        StringBuffer result = new StringBuffer();

        if (pIncludeRoot) {
            formatElement(pRoot,  pNodeAdapter, 0, pIndent, result, pNodeFormatter);
        }
        else {
            // exclude root
            for (T e: pNodeAdapter.children(pRoot)) {
                formatElement(e,  pNodeAdapter, 0, pIndent, result, pNodeFormatter);
            }
        }

        return result.toString();
    }

    public static <T> String formatTree_newick(T pRoot,
            NodeAdapter<T> pNodeAdapter,
            Converter<T, String> pNodeFormatter) {
        StringBuffer result = new StringBuffer();

        formatElement_newick(pRoot,  pNodeAdapter, result, pNodeFormatter);

        result.append(";");

        return result.toString();

    }

	public static <T> String formatTreeJSON(T pRoot, NodeAdapter<T> pNodeAdapter,
			int pIndent, 			Converter<T, String> pNodeFormatter) {
		StringBuffer result = new StringBuffer();

		formatElementJSON(pRoot, pNodeAdapter, pIndent, result, pNodeFormatter, false);

		return result.toString();
	}

    private static <T> void formatElement(T pElem,
                                  NodeAdapter<T> pNodeAdapter,
                                  int pLevel,
                                  int pIndent,
                                  StringBuffer pBuf,
                                  Converter<T, String> pNodeFormatter) {
        // print element
        pBuf.append(indentString(pLevel, pIndent)+pNodeFormatter.convert(pElem)+"\n");

        // print children
        for (T child: pNodeAdapter.children(pElem)) {
            formatElement(child, pNodeAdapter, pLevel+1, pIndent, pBuf, pNodeFormatter);
        }
    }

    private static <T> void formatElement_newick(T pElem,
            NodeAdapter<T> pNodeAdapter,
            StringBuffer pBuf,
            Converter<T, String> pNodeFormatter) {

        // print children, if any
        List children = pNodeAdapter.children(pElem);
        if (children != null && children.size() >0) {
            pBuf.append("(");
            boolean first = true;
            for (T child: pNodeAdapter.children(pElem)) {
                if (!first) {
                    pBuf.append(",");
                }
                else {
                    first=false;
                }
                formatElement_newick(child, pNodeAdapter, pBuf, pNodeFormatter);
            }
            pBuf.append(")");
        }

        // print node name
        pBuf.append(pNodeFormatter.convert(pElem));
    }

    private static <T> void formatElementJSON(T pElem,
            NodeAdapter<T> pNodeAdapter,
            int indentLevel,
            StringBuffer pBuf,
            Converter<T, String> pNodeFormatter, boolean pPrintComma) {

    	pBuf.append(indentString(indentLevel, 2));
    	pBuf.append("{");
    	pBuf.append(pNodeFormatter.convert(pElem));

        List children = pNodeAdapter.children(pElem);
        if (children != null && children.size() > 0) {
	    	pBuf.append(", \"children\": [\n");

	    	int iter = 1;
            for (T child: pNodeAdapter.children(pElem)) {
            	boolean printComma = true;
            	if (iter == children.size())
            		printComma = false;
                formatElementJSON(child, pNodeAdapter, indentLevel + 1, pBuf, pNodeFormatter, printComma);
                iter++;
            }
            pBuf.append(indentString(indentLevel + 1, 2));
            pBuf.append("]");
        }
        pBuf.append("}");
        if (pPrintComma)
        	pBuf.append(",");
        pBuf.append("\n");
    }


    public static class AToZAlphabet extends ArrayList<Character> {

        /**
		 * 
		 */
		private static final long serialVersionUID = -7804017294279911147L;

		public AToZAlphabet() {
            super();
            for (char c = 'A'; c <= 'Z'; c++) {
                add(c);
            }
        }
    }



    public static List<String> wrapLines(List<String> lines, int maxLen) {

        StringBuffer buf = new StringBuffer();
        // List<String> buffer = new ArrayList<String>();

        ArrayList<String> result = new ArrayList<String>();


        for (String line: lines) {
            if (isEmpty(line)) {
                // empty line interpreted as paragraph break
                if (buf.length() > 0) {
                    // flush any accumulated words
                    result.add(buf.toString());
                    buf.setLength(0);
                }
                result.add("");

            }
            else {
                // non-empty line
                List<String> wordsOnLine = Arrays.asList(line.split("\\s+"));
                for (String word: wordsOnLine) {
                    if (buf.length() + (buf.length() == 0 ? 0 : 1) + word.length() <= maxLen) {
                        // word fits to buf
                        if (buf.length() != 0) {
                            buf.append(" ");
                        }
                        buf.append(word);
                    }
                    else {
                        // word would make current line too long; flush buf and start new buf from current word
                        if (buf.length() != 0) {
                            result.add(buf.toString());
                        }

                        buf.setLength(0);
                        buf.append(word);
                    }
                }
            }


        }

        // no more words; write last line from buf, if any
        if (buf.length() != 0) {
            result.add(buf.toString());
        }

        return result;
    }

    public static class UnexpectedNumColumnsException extends RuntimeException {

        public static final long serialVersionUID = 123123122345345L;

        public String line;
        public int numColumnsOnLine;
        public int numColumnsExpected;

        /** -1 if num columns is not counted, but is known to be more than expected */
        public UnexpectedNumColumnsException(String line, int numColumnsOnLine, int numColumnsExpected) {
            super((numColumnsOnLine != -1 ? "Unexpected number of columns: "+numColumnsOnLine : "More than the expected number of columns") +
                  " on line: "+line+"; expected: "+numColumnsExpected);
            this.line = line;
            this.numColumnsOnLine = numColumnsOnLine;
            this.numColumnsExpected = numColumnsExpected;
        }
    }


}
