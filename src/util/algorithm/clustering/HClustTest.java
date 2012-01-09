package util.algorithm.clustering;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.IOUtils;
import util.StringUtils;

public class HClustTest {
    
    public static void main(String[] args) throws Exception {        
        List<Point2D> data = readData();
        
        HClusterDistanceFunction_single_linkage<Point2D> distF =  
            new HClusterDistanceFunction_single_linkage(new EuclideanDistance());
                         
            HCluster root = HClust.performClustering(distF, data);
            
            System.out.println(StringUtils.formatTree(root, new HCluster.TreeNodeAdapter(), 4, true, new HCluster.NodeFormatter()));
    }
    
    static List<Point2D> readData() throws IOException {
        List<Point2D> result = new ArrayList();
        for (String line: IOUtils.readLines()) {
            String[] tokens = line.split("\\s+");
            // String name = tokens[0];
            double x = Double.parseDouble(tokens[1]);
            double y = Double.parseDouble(tokens[2]);
            Point2D p = new Point2D.Double(x,y);
            result.add(p);
        }
        return result;
    }
}

