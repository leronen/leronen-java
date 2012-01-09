package util.algorithm.classification;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.CmdLineArgs;
import util.IOUtils;
import util.SU;
import util.StringUtils;
import util.Utils;
import util.collections.MultiMap;
import util.collections.SymmetricPair;
import util.comparator.ReverseComparator;
import util.dbg.Logger;

public class ROCUtils {
    
    CmdLineArgs args;
    
    public static <T> List<Point> generateROCPoints(
            Map<T, BinaryClassification> pTrueClasses,
            Map<T, Double> pClassifierScores) {
        double N = 0;
        double P = 0;
        
        ArrayList<Point> R = new ArrayList();
        
        if (!(pTrueClasses.keySet().equals(pClassifierScores.keySet()))) {
            throw new RuntimeException("WhatWhatWhat?!?!?!?");
        }
                
//        Set<T> objects = pTrueClasses.keySet(); 
        Set<Double> scoreSet = new HashSet<Double>();
        MultiMap<Double, T> objectsByScore = new MultiMap<Double, T>();
        
        // init
        
        for (T o: pTrueClasses.keySet()) {
            BinaryClassification c = pTrueClasses.get(o);
            if (c == BinaryClassification.POS) {
                P++;
            }
            else if (c == BinaryClassification.NEG) { 
                N++;
            }
            else {
                throw new RuntimeException("WhatWhatWhat?!?!?!?");
            }
            
            Double score = pClassifierScores.get(o);
            scoreSet.add(score);
            objectsByScore.put(score, o);
        }
        
        ArrayList<Double> sortedScores = new ArrayList<Double>(scoreSet);
        Collections.sort(sortedScores, new ReverseComparator());            
        
        // generate points
        double TP = 0;
        double FP = 0;
        
        // the initial point (0,0) (threshold = ∞)
        R.add(new Point(0.d,0.d, Double.POSITIVE_INFINITY));
        
        for (double score: sortedScores) {                                          
        
            for (T o: objectsByScore.get(score)) {
                BinaryClassification c = pTrueClasses.get(o);
                if (c == BinaryClassification.POS) {
                    TP++;
                }
                else if (c == BinaryClassification.NEG) { 
                    FP++;
                }
                else {
                    throw new RuntimeException("WhatWhatWhat?!?!?!?");
                } 
            }
            
            R.add(new Point(FP/N, TP/P, score));
        }
        
        return R;
    }
            
    public static List<Instance<String>> readInstances(InputStream pStream) throws IOException {
        List<String> lines = IOUtils.readLines(pStream);
        List<Instance<String>> result= new ArrayList();
        for (String l: lines) {
            String[] tok = l.split("\\s+");
            result.add(new Instance(tok[0], 
                                    BinaryClassification.getByString(tok[1]),
                                    Double.parseDouble(tok[2])));
        }
        return result;
    }
    
    public static class Point extends SymmetricPair<Double> {
        Double score;
        
        /** pScore may be null... */
        public Point(Double x, Double y, Double pScore) {
            super(x,y);
            score = pScore;
        }
        
        public String toString() {
            if (score != null) {
                return super.toString()+" ("+score+")";
            }
            else {
                return super.toString();
            }
        }
        
        public double getX() {
            return getObj1();
        }
        
        public double getY() {
            return getObj2();
        }
                
        
    }        
        
//    private class PointFormatter() {
//        
//    }
    
    public static double computeAUC_scoreSorted(List<Point> pPoints) {
        
        List<Point> sortedPoints = new ArrayList(pPoints);
        Collections.sort(sortedPoints, new ScoreBasedPointComparator());
                                                           
        Point prevPoint = sortedPoints.get(0);
        sortedPoints.remove(0);        
        
        double AUC = 0;
                
        for (Point p: sortedPoints) {                        
            AUC += trapezoidArea(prevPoint.getX(), prevPoint.getY(), p.getX(), p.getY());
            
            prevPoint = p;             
        }
        
        return AUC;
        
    }
    
    public static double computeAUC_xSorted(List<Point> pPoints) {
        List<Point> sortedPoints = new ArrayList(pPoints);
        Collections.sort(sortedPoints, new XBasedPointComparator());
        
        Point first = sortedPoints.get(0);
        Point last = sortedPoints.get(sortedPoints.size()-1);
        
        // check first points
        if (first.getX() == 0.d) {
            // no action needed!

            // assert y is also zero!
//             if (first.getY() != 0) {
//                 Utils.die("Invalid first point: "+first);
//             }            
        }
        else {
            // first x not zero, add (0,0) as the first point
            Logger.info("Adding (0,0)");
            sortedPoints.add(0, new Point(0.d,0.d,null));
        }
        
        
        if (last.getX() == 1.d) {
            // assert y is also one!
            if (last.getY() != 1.d) {
                Utils.die("Invalid last point: "+last);
            }           
        }
        else {
            // last x not one, add (1,1) as the last point
            sortedPoints.add(new Point(1.d,1.d,null));
        }
        
        Logger.info("Preprocessed points:\n"+SU.toString(sortedPoints));
        Point prevPoint = sortedPoints.get(0);
        sortedPoints.remove(0);        
      
        double AUC = 0;
      
        for (Point p: sortedPoints) {                        
            AUC += trapezoidArea(prevPoint.getX(), prevPoint.getY(), p.getX(), p.getY());
          
            prevPoint = p;             
        }
      
        return AUC;      
    }
    
    public static double computeAUC_old(List<Point> pPoints) {
        Set<Double> xSet = new HashSet<Double>();
        Map<Double, Double> yByX = new HashMap();
        for (Point p: pPoints) {
            double x = p.getX();
            double y = p.getY();
            xSet.add(x);
            Double bestY = yByX.get(x);
            if (bestY == null || bestY.doubleValue() < y) {
                yByX.put(x, y);
            }
        }
        
        double initialX = 0;
        double initialY = yByX.get(initialX);
        xSet.remove(0);
        
        List<Double> sortedX = new ArrayList(xSet);
        Collections.sort(sortedX);
                
        Logger.info("sortedX:\n"+SU.toString(sortedX));
        Logger.info("yByX:\n"+SU.toString(yByX));
            
        double prevX = initialX;
        double prevY = initialY;
        
        double AUC = 0;
        
        for (double x: sortedX) {
            double y = yByX.get(x);
            
            AUC += trapezoidArea(prevX, prevY, x, y);
            
            prevX = x;
            prevY = y;            
        }
        
        return AUC;
        
    }
    
    private static double trapezoidArea(double x1, double y1, double x2, double y2) {
        double width = x2-x1;
        double avgY = (y1+y2)/2;         
        return width * avgY;
    }
    
    
    
    private void run() throws IOException {
        String infile = args.getNonOptArg(0);
        String baseName = StringUtils.removeExtension(infile);
        List<Instance<String>> instances = readInstances(new FileInputStream(infile));
        Map<String, BinaryClassification> classifications = new HashMap<String, BinaryClassification>();
        Map<String, Double> scores = new HashMap();
        for (Instance<String> i: instances) {
            classifications.put(i.key, i.trueClass);
            scores.put(i.key, i.score);
        }
        
        List<Point> points = generateROCPoints(classifications, scores);
        String pointsfile = args.getOpt("pointsfile");
        if (pointsfile == null) pointsfile = baseName+".points";
        IOUtils.writeToFile(pointsfile, SU.toString(points));
        
        String gpfile = args.getOpt("gnuplotdatafile");
        if (gpfile== null) gpfile = baseName+".gnuplot";        
        List<String> gnuplotLines = new ArrayList();
        for (Point p: points) {
            gnuplotLines.add(p.getObj1()+" "+p.getObj2());
            
        }
        IOUtils.writeCollection(gpfile, gnuplotLines);
        
        double AUC = computeAUC_scoreSorted(points);
        Logger.info("AUC: "+AUC);
//        IOUtils.writeToFile(baseName+".AUC", ""+AUC);
        String summaryFile = args.getOpt("summaryfile");
        if (summaryFile != null) {
            IOUtils.appendToFile(summaryFile, "AUC="+AUC+"\n");
        }          
        else {
            IOUtils.writeToFile(baseName+".AUC", ""+AUC);
        }
    }
    
    private static Object[][] OPTION_DEFINITIONS = {
        {"pos_class_name", "p", true, "p"},
        {"neg_class_name", "n", true, "n"},
        {"pointsfile", null, true, null}, // output roc points here
        {"gnuplotdatafile", null, true, null}, // output gnuplottable data here
        {"summaryfile", null, true, null} // append AUC here
    };
    
    private static String[] NAMES_OF_NON_OPT_ARGS = { 
        "datafile"        
    };
        
    
    /** 
     * Read a file $1 with following cols:
     *   1: instance id (~name)
     *   2: class ("p" and "n" by default, settable by opts) 
     *   3: score
     *   
     * @param pArgs
     * @throws Exception
     */
    public static void main(String[] pArgs) throws Exception {
        CmdLineArgs args = new CmdLineArgs(pArgs, 
                                            true, 
                                            OPTION_DEFINITIONS, 
                                            NAMES_OF_NON_OPT_ARGS);
        BinaryClassification.POS.name = args.getOpt("pos_class_name");
        BinaryClassification.NEG.name = args.getOpt("neg_class_name");
        ROCUtils roc = new ROCUtils();
        roc.args = args;
        roc.run();                
    }
    
    private static class ScoreBasedPointComparator implements Comparator<Point> {
        public int compare(Point p1, Point p2) {
            return -(new Double(p1.score).compareTo(p2.score));
        }
    }

    private static class XBasedPointComparator implements Comparator<Point> {
        public int compare(Point p1, Point p2) {
            return new Double(p1.getX()).compareTo(p2.getX());
        }
    }
    
    
    public static class Instance<T> {
        public T key;
        public BinaryClassification trueClass;
        public double score;
        
        public Instance(T key, BinaryClassification trueClass, double score) {
            super();
            this.key = key;
            this.trueClass = trueClass;
            this.score = score;
        }
        
        
        
    }
}


