package util;

import util.converter.*;
import util.math.*;
import util.dbg.*;

import java.util.*;


/**
 *  TODO: maybe, just maybe, in this kind of static mathematic util class the parameter naming convention need not be
 *  func(pSomething, pSomethingElse), maybe something like func(a, b) is more appropriate under these
 *  extreme circumstances...
 */ 
public class MathUtils {
               
    public static int sign(double p) {
        if (p < 0) {
            return -1;
        }
        else if (p > 0) {
            return 1;
        }
        else {
            return 0;
        }
    }                 
    
    public static double avg(double p1, double p2) {
        return (p1+p2)/2;
    }
    
    /** if a = log p and b = log q, computes the logarithmitm of p+q, given a and b */
    public static double logSum(double pA, double pB) {
        double a = pA;
        double b = pB;
        // ensure that a is the bigger value...
        if (a < b) {
            double tmp = a;
            a = b;
            b = tmp;
        }
            
        return a+Math.log(1+Math.exp(b-a));                                    
    }
    
    public static double min(double... pArr) {
        double min = Double.MAX_VALUE;        
        for (double val: pArr) {
            if (min > val) min = val;            
        }                            
        return min;
    }        
    
    public static double min(Collection<? extends Number> pValues) {
        double min = Double.MAX_VALUE;        
        for (Number n: pValues) {
            if (min > n.doubleValue()) min = n.doubleValue();            
        }                            
        return min;
    }
    
//    public static double min(int... pValues) {
//        int min = Integer.MAX_VALUE;        
//        for (int n: pValues) {
//            if (min > n) min = n;            
//        }                            
//        return min;
//    }
    
    public static double max(Collection<? extends Number> pValues) {
        double max = -Double.MAX_VALUE;        
        for (Number n: pValues) {
            if (max < n.doubleValue()) max = n.doubleValue();            
        }                            
        return max;
    }
                  
    public static double[] nCopies(double pVal, int pNumCopies) {        
        double[] result = new double[pNumCopies];
        for (int i=0; i<pNumCopies; i++) {
            result[i]=pVal;
        }
        return result;
        
    }
    
    public static <T> Map<T, Integer> rank(Collection<T> pObjects) {
        List sortedObjects = new ArrayList(pObjects);
        Collections.sort(sortedObjects);
        HashMap result = new HashMap();
        int rank = 0;
        for (Object o: sortedObjects) {
            result.put(o, ++rank);
        }
        return result;
        
    }
    
    public static double max(double... pArr) {
        double max = -Double.MAX_VALUE;        
        for (double val: pArr) {
            if (val > max) max = val;            
        }
        return max;
    }
    
    
    public static int max(int... pArr) {
        int max = Integer.MIN_VALUE;        
        for (int val: pArr) {
            if (val > max) max = val;            
        }
        return max;
    }
           
    
    public static long max(long pArr[]) {
        long max = Long.MIN_VALUE;        
        for (int i=0; i<pArr.length; i++) {
            if (max < pArr[i]) max = pArr[i];            
        }                            
        return max;
    }    
    
    public static int minInt(int... pVals) {
        int min = Integer.MAX_VALUE;        
//        for (int i=0; i<pArr.length; i++) {
//            if (pArr[i] < min) min = pArr[i];            
//        }
        for (int val: pVals) {
            if (val < min) min = val;            
        }
        return min;
    }
    
    public static int min(int p1, int p2) {
        if (p1 <= p2) {
            return p1;
        }
        else {
            return p2;
        }
    }

        
    public static int max(int p1, int p2) {
        if (p1 >= p2) {
            return p1;
        }
        else {
            return p2;
        }
    }
                
    
    public static double[] minAndMax(double pArr[]) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i=0; i<pArr.length; i++) {
            if (min > pArr[i]) min = pArr[i];
            if (max < pArr[i]) max = pArr[i];
        }            
        double[] result = new double[2];
        result[0]=min;
        result[1]=max;
        return result;
    }       
    
    public static double sum(double[]pArr) {
        double sum = 0;
        for (int i=0; i<pArr.length; i++) {
            sum+=pArr[i];
        }
        return sum;        
    }
    
    public static double sum(double[]pArr, boolean pDropNaNs) {
        double sum = 0;
        for (int i=0; i<pArr.length; i++) {
            if (!pDropNaNs || !(Double.isNaN(pArr[i]))) {
                sum+=pArr[i];
            }
        }
        return sum;        
    }
    
    
    /** calculates sum of two vectors */
    public static double[] sum(double[]pVec1, double[] pVec2) {
        if (pVec1.length != pVec2.length) {
            throw new RuntimeException("All is lost!");
        }
        int len = pVec1.length;
        double[] result = new double[len];        
        for (int i=0; i<len; i++) {
            result[i]=pVec1[i]+pVec2[i];
        }
        return result;        
    }

    /** calculates sum of two vectors */
    public static int[] sum(int[]pVec1, int[] pVec2) {
        if (pVec1.length != pVec2.length) {
            throw new RuntimeException("pVec1.len == "+pVec1.length+" != "+pVec2.length +" == pVec2.len "+
                                       " => All is lost!");
        }
        int len = pVec1.length;
        int[] result = new int[len];        
        for (int i=0; i<len; i++) {
            result[i]=pVec1[i]+pVec2[i];
        }
        return result;        
    }    
    
    
    public static int sum(int[]pArr) {
        int sum = 0;
        for (int i=0; i<pArr.length; i++) {
            sum+=pArr[i];
        }
        return sum;        
    }
    
//    public static int sum(List<Integer> pList) {
//        int sum = 0;
//        for (int i: pList) {
//            sum+=i;
//        }
//        return sum;        
//    }
    
    public static double sum(Collection<? extends Number> pList) {
        double sum = 0;
        for (Number n: pList) {
            sum += n.doubleValue();
        }
        return sum;        
    }

    
    public static double avg(double[] pArr) {
        // by default, do not permit NaNs
        return avg(pArr, false);   
    }
    
    public static double avg(Collection<? extends Number> pVals) {
        double sum = sum(pVals);        
        double avg = sum / pVals.size();
        return avg;           
    }
    
    public static double weightedAvg(double pVal1, double pWeight1, 
                                     double pVal2, double pWeight2) {
        return (pVal1*pWeight1+pVal2*pWeight2) / (pWeight1 + pWeight2);  
    }
    
    public static double weightedAvg(double pVal1, double pWeight1, 
                                     double pVal2, double pWeight2,
                                     double pVal3, double pWeight3) {
        return (pVal1*pWeight1+pVal2*pWeight2+pVal3*pWeight3) / (pWeight1 + pWeight2 + pWeight3);  
    }

    
    public static double avg(double[] pArr, boolean pPermitNaNs) {
        if (!pPermitNaNs) {
            return sum(pArr)/pArr.length;
        }
        else {
            // NaNs shall be forgotten about...        
            double sum = 0;
            int numVals = 0;
            for (int i=0; i<pArr.length; i++) {
                if (!(Double.isNaN(pArr[i]))) {
                    sum+=pArr[i];
                    numVals++;
                }
            }
            return sum/numVals;
        }
    }        
            
    public static double[] zeros(int pLen) {
        double[] result = new double[pLen];
        for (int i=0; i<pLen; i++) {
            result[i] = 0;
        }
        return result;
    }
    
    public static double[] ones(int pLen) {
        double[] result = new double[pLen];
        for (int i=0; i<pLen; i++) {
            result[i] = 1;
        }
        return result;
    }        
    
    
    public static double[] normalize(double[] pArr) {           
        double sum = sum(pArr);
        double result[] = new double[pArr.length];
        for (int i=0; i<pArr.length; i++) {            
            result[i]=pArr[i]/sum;            
        }
        return result;                 
    }
    
    public static double sum(double[][] pArr) {
        double sum = 0;
        for (int i=0; i<pArr.length; i++) {
            for (int j=0; j<pArr[i].length; j++) {
                sum += pArr[i][j];
            }
        }  
        return sum;              
    }
    
    /** normalizes (inplace) a matrix such that the sum of all elements is 1 */
    public static void normalize(double[][] pArr) {           
        double sum = sum(pArr);        
        for (int i=0; i<pArr.length; i++) {
            for (int j=0; j<pArr[i].length; j++) {            
                pArr[i][j] = pArr[i][j]/sum;
            }                    
        }                         
    }

    /*
    public static int[] sum(int[] pArr1, int[] pArr2) {
        if (pArr1.length != pArr2.length) {
            throw new RuntimeException("array lengths must match");
        }                 
        int len = pArr2.length;
        int result[] = new int[len];
        for (int i=0; i<len; i++) {            
            result[i]=pArr1[i]+pArr2[i];            
        }
        return result;                 
    }
    */
    
    public static boolean containsOnlyVals(int[] pArr, int pScalar) {
        int len = pArr.length;        
        for (int i=0; i<len; i++) {
            if (pArr[i]!=pScalar) {
                return false;
            }
        }
        return true;
    }        

    public static double[] calculate(List pVals1,
                                     List pVals2,
                                     BinaryOperator pOper) {
        return calculate(ConversionUtils.DoubleCollectionTodoubleArray(pVals1),
                         ConversionUtils.DoubleCollectionTodoubleArray(pVals2),
                         pOper);                                         
    }

    public static double[] calculate(List pVals,                                     
                                     UnaryOperator pOper) {
        return calculate(ConversionUtils.DoubleCollectionTodoubleArray(pVals),                         
                         pOper);                                         
    }                         
                         

    public static double[] calculate(double[] pVals1,
                                     double[] pVals2,
                                     BinaryOperator pOper) {
        if (pVals1.length != pVals2.length) {
            throw new RuntimeException("Alles ist lost.");
        }
        // alles in ordnung
        int len = pVals1.length;        
        double[] result = new double[len];
        for (int i=0; i<len; i++) {
            result[i] = pOper.calculate(pVals1[i], pVals2[i]);                                  
        }  
        return result;                                          
    }       

    public static double[] calculate(double[] pVals,                                     
                                     UnaryOperator pOper) {                
        int len = pVals.length;        
        double[] result = new double[len];
        for (int i=0; i<len; i++) {
            result[i] = pOper.calculate(pVals[i]);                                  
        }  
        return result;                                          
    }                                        
                                 
    
    public static boolean isPowerOf2(double pNumber) {
        double tmp = pNumber;
        while(tmp>1) {
            tmp /= 2.d;    
        }
        return tmp==1;            
    }
        
    public static int numPermutations(int pN) {
        // Logger.info("numPermutations: "+pN);        
        if (pN == 1 || pN == 0) {
            return 1;
        }
        else {
            return pN * numPermutations(pN-1);
        }
    }
            
    /* "kertoma" = "kuinka monella tavalla voidaan valita k alkiota n:stä" */
    public static int numCombinations(int n, int k) {
        // Logger.info("numCombinations: "+n+", "+k);
        long tmp = 1;
        for (int i=n; i>=n-k+1; i--) {
            tmp*=i;    
        }
        for (int i=k; i>=2; i--) {
            tmp = tmp/i;    
        }
        if (tmp > Integer.MAX_VALUE) {
            throw new RuntimeException("Aargh, precision kosahti!");
        }
        return (int)tmp;
        // ineffective:
        // return numPermutations(pN) / (numPermutations(pK) * numPermutations(pN-pK));
    }
    
    public static boolean or(boolean[] pArr) {
        int len = pArr.length;        
        for (int i=0; i<len; i++) {
            if (pArr[i]==true) {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Kuinka todennäköistä on saada n:n toiston kokeessa täsmälleen k "onnistumista",
     * kun "onnistumisen" todennäköisyys on p.
     */ 
    public static double binomialProbability(int n, int k, double p) {
        if (n<0 || k<0 || k > n || p < 0 || p > 1) {
            throw new RuntimeException("Illegal params for method binomialProbability:\n"+
                                       "n="+n+"\n"+
                                       "k="+k+"\n"+
                                       "p="+p+"\n");
        }
        return numCombinations(n,k) * Math.pow(p,k) * Math.pow(1-p, n-k);                                     
    }
    
    /** 
     * Kuinka todennäköistä on saada n:n toiston kokeessa korkeintaan k "onnistumista",
     * kun "onnistumisen" todennäköisyys on p.
     */ 
    public static double cumulativeBinomialProbability(int n, int k, double p) {
        double sum = 0;

        for (int i=0; i<=k; i++) {
            sum += binomialProbability(n, i, p);
        }                                     
        
        return sum;
    }
    
        
    /** Return true, iff |pVal1-pVal| < pEpsilon */
    public static boolean approximatelyEquals(double pVal1, double pVal2, double pEpsilon) {
        return Math.abs(pVal1-pVal2)<pEpsilon;        
    }
    
    public static boolean and(boolean[] pArr) {
        int len = pArr.length;        
        for (int i=0; i<len; i++) {
            if (pArr[i]==false) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean[] invert(boolean[] pArr) {
        int len = pArr.length;
        boolean[] result = new boolean[len];
        for (int i=0; i<len; i++) {
            result[i] = !pArr[i];    
        }
        return result;
    }
    
    public static double[] multiply(double[] pArr, double pScalar) {                   
        double result[] = new double[pArr.length];
        for (int i=0; i<pArr.length; i++) {            
            result[i]=pArr[i]*pScalar;            
        }
        return result;                 
    }
    
    public static double[] log(double[] pArr) {                   
        double result[] = new double[pArr.length];
        for (int i=0; i<pArr.length; i++) {            
            result[i]=Math.log(pArr[i]);            
        }
        return result;                 
    }
    
    
    /** @return the following sequence of ints: (pStart, pStart+1, pStart+2, ... , pEnd-2, pEnd-1) */ 
    public static int[] intSequence(int pStart, int pEnd) {
        int[] result = new int[pEnd-pStart];
        for (int i=pStart; i<pEnd; i++) {
            result[i-pStart] = i;                   
        }
        return result;
    }
        
    /** 
     * @param pMaps array of maps with numeric values (Strings are permitted for convenience) 
     * @param pNullsAndNansAllowed if true, the average is calculated only among the non-null ones...
     * @return a map holding the avaraged values. (represented by Double objects)
     *  ARGH: it seems that for some cursed reason, we return strings in the map,
     *  instead of floats!!! 
     *      
     */ 
    public static Map averages(Map[] pMaps, boolean pNullsAndNansAllowed) {                
        List keySets = ConversionUtils.convert(Arrays.asList(pMaps), new MapToKeySetConverter());        
        
        // require that all summaries have the same keys...
        if (!(CollectionUtils.areAllObjectsEqual(keySets))) {
            // OK, unfortunately this is  not the case; let's give up
            throw new RuntimeException("All keysets are not equal");
        }
        else {
            // All summaries have the same keys; let's proceed to count the averages
            // Notice that we do not check that all the values are numeric;
            // we prefer instead to just calmly wait for doom.
            Map firstMap = pMaps[0];                         
            Set allFields = firstMap.keySet();                                     
            Map result = new LinkedHashMap();
            Iterator keys = allFields.iterator();
            while(keys.hasNext()) {
                Object key = keys.next();
                MapFieldExtractor fieldExtractor = new MapFieldExtractor(key);
                List valList = ConversionUtils.convert(Arrays.asList(pMaps), fieldExtractor);
                if (valList.contains("")) {                
                    throw new RuntimeException("empty string for key: "+key);
                }
                
                if (!pNullsAndNansAllowed) {
                	if (valList.contains(null)) {                
                		throw new RuntimeException("null for key: "+key);
                	}                   
                	for (Object val: valList) {
                		if (val instanceof String) { 
                			if (val.equals("NaN")) {
                				throw new RuntimeException("NaN:s not allowed, lets throw showel to the grovel.");
                			}
                		}
                		else if (val instanceof Number) {
                			if (Double.isNaN(((Number)val).doubleValue())) {
                				throw new RuntimeException("NaN:s not allowed, lets throw showel to the grovel.");
                			}
                		}
                		else {
                			throw new RuntimeException("Unrecognized object: "+val);
                		}
                	}
                }
                
                CollectionUtils.removeNullObjects(valList);
                CollectionUtils.removeAllOccurences(valList, "BROKEN");                
                Iterator i = valList.iterator();
                while(i.hasNext()) {
                	Object val = i.next();
            		if (val instanceof String) {
            			if (val.equals("NaN")) {            		
            				i.remove();
            			}
            		}
            		else if (val instanceof Number) {
            			if (Double.isNaN(((Number)val).doubleValue())) {                		            		
            				i.remove();
            			}
            		}
            		else {
            			throw new RuntimeException("Unrecognized object: "+val);
            		}
                }
                
                // OK, a bit of a kludge again, for courtesy of our redeemed caller, TestExecution...                
                valList = ConversionUtils.convert(valList, new AnyToDoubleConverter());
                if (valList.contains(Double.NaN)) {
                	throw new RuntimeException("What, there is still a NaN!!!");
                }
                double[] vals = ConversionUtils.DoubleCollectionTodoubleArray(valList);
                double avg = avg(vals);
                // result.put(key, new Double(avg));
                result.put(key, StringUtils.formatFloat(avg, 5));                                                                              
            }                                   
            return result;
        }
    }
    
    /** @param pEnd exclusive */
    public static List<Integer> range(int pStart, int pEnd) {
        return range(pStart, pEnd, 1);
    }
    
    /** @param pEnd exclusive. @param pStep must be positive. */
    public static List<Integer> range(int pStart, int pEnd, int pStep) {
        if (pStep <= 0) {
            throw new RuntimeException("Only supports positive step.");
        }        
        
        List<Integer> result = new ArrayList();        
        for (int i=pStart; i<pEnd; i+=pStep) {
            result.add(i);
        }
        return result;
    }
    
    /**
     * Normalizes a value to lie in the range [0,1] 
     * keywords: scale
     */
    public static double normalize(double pVal, double pMin, double pMax) {
        return (pVal-pMin)/(pMax-pMin);            
    }

    /** "unnormalizes" a that has been normalized by method normalize */
    public static double unnormalize(double pNormalizedVal, double pMin, double pMax) {
        return (1-pNormalizedVal)*pMin + pNormalizedVal*pMax;            
    }

    /** return the int nearest to the argument */
    public static int rint(double pDouble) {
        return (int)Math.rint(pDouble);
    }                
    
     
    public static boolean isInteger(double pDouble) {
        return Math.rint(pDouble) == pDouble;    
    }
    
    /** calculate the base-pBase logarithm of pVal */ 
    public static double log(double pVal, double pBase) {
        return Math.log(pVal) / Math.log(pBase);
        
    }
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("MathUtils: "+pMsg);
    }
    
    public static void test1 (String[] args) {
        double p = 0.000000001;
        double q = 0.000000001;
        double a = Math.log(p);
        double b = Math.log(q);
        double logSum = logSum(a,b);
        double sum = Math.exp(logSum);
        System.out.println(
            "p="+p+"\n"+
            "q="+q+"\n"+
            "a="+a+"\n"+
            "b="+b+"\n"+
            "sum="+sum+"\n"+"logSum="+logSum+"\n");
    }
    
    public static void main (String[] args) {
        try {
            Logger.setProgramName("MathUtils");                
            CmdLineArgs argParser = new CmdLineArgs(args);
            String cmd = argParser.shift();        
            if (cmd.equals("sum")) {
                String[] lines = IOUtils.readLineArray(System.in);
                double[] vals = ConversionUtils.stringArrToDoubleArr(lines);
                double result = sum(vals);                                   
                System.out.println(""+result);
            }
            else if (cmd.equals("avg")) {
                String[] lines = IOUtils.readLineArray(System.in);
                double[] vals = ConversionUtils.stringArrToDoubleArr(lines);
                double result = avg(vals, true);                                   
                System.out.println(""+result);
            }                                   
            else if (cmd.equals("weightedavg")) {
                double val1 = argParser.shiftDouble();
                double wgt1 = argParser.shiftDouble();
                double val2 = argParser.shiftDouble();
                double wgt2 = argParser.shiftDouble();
                double result = weightedAvg(val1, wgt1, val2, wgt2);  
                System.out.println(""+result);
            }
            else if (cmd.equals("log")) {
                double val = Double.parseDouble(args[1]);
                System.out.println(""+Math.log(val));                               
            }            
            else if (cmd.equals("exp")) {
                double val = Double.parseDouble(args[1]);
                System.out.println(""+Math.exp(val));                               
            }
            else if (cmd.equals("pow")) {
                double val1 = Double.parseDouble(args[1]);
                double val2 = Double.parseDouble(args[2]);
                System.out.println(""+Math.pow(val1, val2));                               
            }
            else if (cmd.equals("sqrt")) {
                double val = Double.parseDouble(args[1]);
                System.out.println(""+Math.sqrt(val));                               
            }
            else if (cmd.equals("range")) {
                int start = Integer.parseInt(args[1]);
                int end = Integer.parseInt(args[2]);
                int step = Integer.parseInt(args[3]);
                List<Integer> range = range(start, end, step);
                System.out.println(StringUtils.listToString(range, " "));                            
            }
            else {
                Utils.die("Illegal command: "+cmd);
            }                       
            Logger.endLog();
        }
        catch (Exception e) {
            Utils.die(e);
        }                            
                    
    }
       
    
    public static void test1() {
        /*        
        ArgParser argParser = new ArgParser(args);                
        String cmd = argParser.shift();        
        if (cmd.equals("ispowerof2")) {
            String number = argParser.shift();
            System.out.println(""+isPowerOf2(Integer.parseInt(number)));
        }
        */
                        
	/*
    int[] data = {1,2,3,4,5,6,7,8,9,10};
	double[] normalized = new double[data.length];
	double[] denormalized = new double[data.length];
	int[] denormalized2 = new int[data.length];
        for (int i=0; i<data.length; i++) {
	    normalized[i] = normalize(data[i],1,10);
	    denormalized[i] = unnormalize(normalized[i], 1, 10);	
	    denormalized2[i] = rint(denormalized[i]);
        }
	dbgMsg("original: "+StringUtils.arrayToString(data));
	dbgMsg("normalized: "+StringUtils.arrayToString(normalized));
	dbgMsg("denormalized: "+StringUtils.arrayToString(denormalized));      
	dbgMsg("denormalized2: "+StringUtils.arrayToString(denormalized2));
    */
    
        double [] vals = {  0.0,
                            0.125,
                            0.25,                                               
                            0.5, 
                            1.0,
                            2.0,                           
                            8.0 };
        for (int i=0; i<vals.length; i++) {
            dbgMsg("log("+vals[i]+",2) = "+log(vals[i], 2));        
        }
    }
    
    
}
