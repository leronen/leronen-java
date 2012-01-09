package util.math;

import util.*;
import util.collections.*;
import java.util.*;
 
 
/**   
 *    
 */  
public class Histogram  {
            
    private Distribution mDistribution;
    
    private double mStart;
    private double mEnd;
    private int mNumBins;
    private double mBinLen;
    
    /* End indices are exclusive, except for the last bin */
    public Histogram(double pStart, double pEnd, int pNumBins) {
        mStart = pStart;
        mEnd = pEnd;
        mNumBins = pNumBins;
        mBinLen = (mEnd-mStart)/mNumBins;
        mDistribution = new Distribution();
    }                                               
    
    public void add(double pVal) {
        mDistribution.add(new Integer(bin(pVal)));   
    }
    
    private int bin(double pVal) {
        if (pVal < mStart || pVal > mEnd) {
            throw new RuntimeException("Illegal value!");
        }
        double offSet = pVal-mStart;
        int bin = (int)(offSet/mBinLen);
        if (bin < 0) {
            bin = 0;
        }
        else if (bin > mNumBins-1) {
            bin = mNumBins-1;
        }
        return bin;                                                   
    }
    
    
    
    private String binRep(int pBin) {
        double start = pBin*mBinLen;
        double end = (pBin+1)*mBinLen;
        return "["+start+","+end+"[";                
    }
    
    public LinkedHashMap asMap() {
        LinkedHashMap result = new LinkedHashMap();
        for (int bin=0; bin<mNumBins; bin++) {
            double freq = mDistribution.getWeight(new Integer(bin));             
            String binRep = binRep(bin);
            result.put(binRep, freq);
        }
        return result;
    }
    
    public List<Double> asList() {
        ArrayList<Double> result = new ArrayList<Double>();
        for (int bin=0; bin<mNumBins; bin++) {
            double freq = mDistribution.getWeight(new Integer(bin));             
            result.add(freq);                        
        }
        return result;            
    }
    
    
    /** read newline-delimited doubles from stdin */
    public static void main (String[] args) {                
        double left = Double.parseDouble(args[0]);
        double right = Double.parseDouble(args[1]);
        int numBins = Integer.parseInt(args[2]);
        
        Histogram h = new Histogram(left, right, numBins);
        
        try {
                
            String[] lines = IOUtils.readLineArray(System.in);
            for (String line: lines) {
                double val = Double.parseDouble(line);
                h.add(val);
            }
    
            System.out.println(StringUtils.listToString(h.asList()));            
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }                                                                    
    }        
    
    public static void test1() {
        Histogram h = new Histogram(0,1, 5);
        h.add(0.1);
        h.add(0.1);
        h.add(0.2);
        h.add(0.3);
        h.add(0.9);
        System.out.println(StringUtils.mapToString(h.asMap()));   
    }
    
            
}
