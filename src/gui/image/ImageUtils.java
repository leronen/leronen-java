package gui.image;

import gui.color.ColorUtils;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import util.collections.SymmetricPair;

public class ImageUtils {
    
    public static Color getEdgeColor(BufferedImage i, Edge e) {
        List<Point> edgePoints = edgePoints(i,e);
        ArrayList<Color> colors = new ArrayList<Color>(edgePoints.size());
//        ColorModel cm = i.getColorModel();
        for (Point p: edgePoints) {
//            Logger.info("Getting image rgb value at: "+p+(" (height="+i.getHeight()+")"));
            Color c = new Color(i.getRGB(p.x, p.y));
//            Color c = new Color(cm.getRed(pixel), cm.getGreen(pixel), cm.getBlue(pixel));
            colors.add(c);
        }
        return ColorUtils.avg(colors);
        
    }
        
    /** Get a range of points on a horizontal or vertical line. Fails if points are not on such a line. Both endpoints are inclusive. */    
    public static List<Point> linePoints(Point p1, Point p2) {
        if (p1.x == p2.x) {
            // vertical
            int x = p1.x;
            int y1 = p1.y;
            int y2 = p2.y;
            if (y1 > y2) {
                // swap
                int tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            ArrayList<Point> result = new ArrayList(y2-y1+1);
            for (int y=y1; y<=y2; y++) {
                result.add(new Point(x,y));
            }
            return result;            
        }
        else if (p1.y == p2.y) {
            // horizontal
            int y = p1.y;
            int x1 = p1.x;
            int x2 = p2.x;
            if (x1 > x2) {
                // swap
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            ArrayList<Point> result = new ArrayList(x2-x1+1);
            for (int x=x1; x<=x2; x++) {
                result.add(new Point(x,y));
            }
            return result;
        }
        else {
            // we control the horizontal and the vertical, but not this
            throw new RuntimeException();
        }
    }       
    
    public static SymmetricPair<Point> corners(BufferedImage i, Edge e) {
        int x1,y1,x2,y2;
        if (e == Edge.TOP) {            
            x1=0;             
            x2=i.getWidth()-1;
            y1=y2=0;                                
        }
        else if (e == Edge.BOTTOM) {
            x1=0;             
            x2 = i.getWidth()-1;
            y1=y2=i.getHeight()-1;            
        }
        else if (e == Edge.LEFT) {
            x1=x2=0;
            y1=0;
            y2=i.getHeight()-1;
        }
        else if (e == Edge.RIGHT) {
            x1=x2=i.getWidth()-1;
            y1=0;
            y2=i.getHeight()-1;
        }
        else {
            throw new RuntimeException();            
        }
        
        Point p1 = new Point(x1,y1);
        Point p2 = new Point(x2,y2);
        return new SymmetricPair<Point>(p1,p2);
        
    }
    
    public static List<Point> edgePoints(BufferedImage i, Edge e) {
        SymmetricPair<Point> corners = corners(i, e);
        // Logger.info("Got corners for edge "+e+": "+corners);
        return linePoints(corners.getObj1(), corners.getObj2());
        
    }
}
