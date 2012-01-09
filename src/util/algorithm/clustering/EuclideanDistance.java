package util.algorithm.clustering;

import java.awt.geom.Point2D;

public class EuclideanDistance implements IDistanceFunction<Point2D> {

    public Double dist(Point2D p1, Point2D p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.pow(dx*dx + dy*dy, 0.5);
    }
    
}
