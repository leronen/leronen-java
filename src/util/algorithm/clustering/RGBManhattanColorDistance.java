package util.algorithm.clustering;

import gui.color.ColorUtils;

import java.awt.Color;

public class RGBManhattanColorDistance implements IDistanceFunction<Color> {

    public Double dist(Color p1, Color p2) {
        return ColorUtils.distance(p1 ,p2);
    }
    
}
