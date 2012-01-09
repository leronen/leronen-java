package util.math;

import java.io.IOException;
import java.util.Arrays;

import util.IOUtils;
import util.StringUtils;
import util.dbg.Logger;

/** 
 * A probability density function for a continuous variable that is defined in 
 * a number of bins of fixed size.
 */
public class PiecewiseConstantPDF {
    
    int numbins;
    double binSize;
    double[] binstart;
    double[] xi;
    double[] f;
    double binwidth;
    double firstbinstart;
    double lastbinend;

    
    public PiecewiseConstantPDF(String pFile) throws IOException {
        readFromFile(pFile);
    }
    
    /** Read from a file with 
     *   - col1 = xi = center of bin
     *   - col2 = pdf(xi)
     */
    private void readFromFile(String pFile) throws IOException {
        String[] lines = IOUtils.readLineArray(pFile);
        numbins = lines.length;
        xi = new double[numbins];        
        f = new double[numbins];
        
        for (int i=0; i<numbins; i++) {
            String line = lines[i];
            String[] tokens = StringUtils.fastSplit(line, ' ');
            xi[i] = Double.parseDouble(tokens[0]);
            f[i] = Double.parseDouble(tokens[1]);            
        }
    
        binstart = new double[numbins];
        for (int i=1; i<numbins; i++) {
            binstart[i] = (xi[i]+ xi[i-1]) / 2;            
        }
   
        double minBinWidth = Double.MAX_VALUE;
        double maxBinWidth = 0;
        for (int i=1; i<numbins-1; i++) {            
            double bs = this.binstart[i];
            double be = this.binstart[i+1];
            double binW = be-bs;
            if (binW < minBinWidth) {
                minBinWidth = binW;
            }
            if (binW > maxBinWidth) {
                maxBinWidth = binW;
            }            
        }    
        binwidth = (minBinWidth+maxBinWidth)/2;
        firstbinstart = xi[0]-binwidth/2;
        lastbinend = xi[numbins-1]+binwidth/2;
        binstart[0] = firstbinstart;
        
//        List<Pair<Double, Double>> pairs =
//            CollectionUtils.makePairs(ConversionUtils.asList(xi),
//                                      ConversionUtils.asList(f));
//        Logger.info("Read data: "+
//                    StringUtils.collectionToString(pairs));
        

//        Logger.info("min bin width: "+minBinWidth);
//        Logger.info("max bin width: "+maxBinWidth);
//        Logger.info("bin width: "+maxBinWidth);                           
    }
    
    /**
     * Normalize by discarding probability mass outside [a,b] and 
     * normalizing to sum to one again.
     * @param a start of range
     * @param b end of range
     */
    public void normalize(double a, double b) {
        double areaUnderRange = computeTotalArea(0,1);            
        double totalArea = computeTotalBinArea();
        double amountMissing = totalArea-areaUnderRange;
        double multiplier = 1.0 / areaUnderRange;
        Logger.info("total mass: "+totalArea);
        Logger.info("mass under range: "+areaUnderRange);
        Logger.info("mass missing : "+amountMissing);        
        Logger.info("Normalizing by multiplying by "+multiplier);
        for (int i=0; i<numbins; i++) {
            f[i] = f[i] * multiplier;
        }
        
        double newAreaUnderRange = computeTotalArea(0,1);
        Logger.info("mass under range after normalize: "+newAreaUnderRange);
    }
    
    public double f(double pVal) {
        int i = Arrays.binarySearch(binstart, pVal);
        
        if ( i >= 0 ) {
            // a surprising event
            if (i == 0) {
                // left border of first bin            
                return f[0];
            }
            else {
                // hit the border of two bins
                return f[i-1]+f[i];
            }
        }
        
        int ip = -i-1; 
        
        if (ip == 0) {            
            // below first bin
//            Logger.warning("val below first bin: "+pVal+" < "+firstbinstart+
//            		       " => returning 0");
            return 0;
        }
        else if (ip == numbins) {
            // in last bin or beyond
            if (pVal <= lastbinend) {
                // within last bin
                return f[ip-1];
            }
            else {
//                Logger.warning("val beyond last bin: "+pVal+" > "+lastbinend+
//                               " => returning 0");
                return 0;
            }
        }
        else {
            // within a non-last bin; ip is the index of the last bin start
            // below which pVal lies => return val from bin i-1  
            return f[ip-1];            
        }
            
    }
    
    public double computeTotalBinArea() {
        
        double sum = 0;
        
        for (int i=0; i<numbins; i++) {
            sum += binwidth * f[i];
        }
        
        return sum;
    }
    
    /** compute area in the interval (a,b) */
    public double computeTotalArea(double a, double b) {        
        double d = 0.0001;                  
        double sum = 0;        
        double xi = a;
        while (xi <= b) {
            double fxi = f(xi);
            sum += d * fxi;
            xi+=d;
        }
        return sum;
    }
    
    public static void main(String[] args) throws Exception {
        PiecewiseConstantPDF pdf = new PiecewiseConstantPDF(args[0]);
                
        
        if (args.length == 1) {
            // output normalized version
            
            pdf.normalize(0,1);
            double a = 0; double b = 1; double d = 0.001;                            
            double xi = a;
            while (xi <= b) {
                double fxi = pdf.f(xi);
                System.out.println(""+xi+" "+fxi);
                xi += d;
            }    
        }        
        else if (args.length == 2) {        
            String arg1 = args[1];
            if (StringUtils.isNumeric(arg1)) {
                // compute value at point given by arg 2
                double xi = Double.parseDouble(args[1]);
                System.out.println(pdf.f(xi));
            }
            else {
                // assume another file,
                // compute ratio f1 / (f1+f2)
                PiecewiseConstantPDF pdf2 = new PiecewiseConstantPDF(arg1);
                pdf.normalize(0,1);
                pdf2.normalize(0,1);
                
                double start = 0.;
                double end = 1;
                double step = 0.001;
                double xi = start;                
              
                while (xi <= end) {
                    double fxi = pdf.f(xi);
                    double f2xi = pdf2.f(xi);
                    double ratio = fxi / (fxi+f2xi);             
                    System.out.println(xi+" " + ratio);
                    xi+=step;
              }
            }
        }
    }
    
}
