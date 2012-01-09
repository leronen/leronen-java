package util.algorithm.classification;

import java.util.ArrayList;
import java.util.List;

import util.IOUtils;
import util.SU;
import util.Utils;
import util.algorithm.classification.ROCUtils.Point;
import util.dbg.Logger;

/** 
 * Minimal implementation for computing area under any (not necessarily ROC)
 * 
 *  read/write std streams.
 */
public class AUC {
    
    public static void main(String[] args) throws Exception {
        
        List<String> lines = IOUtils.readLines();
        List<Point> points = new ArrayList<Point>();
        
        for (String line: lines) {
            String[] tok = line.split("\\s+");
            if (tok.length != 2) {
                Utils.die("Invalid line: "+line);
            }
            double x = Double.parseDouble(tok[0]);
            double y = Double.parseDouble(tok[1]);
            Point p = new Point(x, y, null);
            points.add(p);
        }
        
        Logger.info("Read points: "+SU.toString(points));
        
        double result = ROCUtils.computeAUC_xSorted(points);
        
        Logger.info("Computed AUC: "+result);
        
        System.out.println(result);
        
    }
}
