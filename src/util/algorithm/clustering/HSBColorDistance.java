package util.algorithm.clustering;

import gui.color.ColorUtils;

import java.awt.Color;

public class HSBColorDistance implements IDistanceFunction<Color> {

    public Double dist(Color p1, Color p2) {
        return ColorUtils.hsbDistance(p1 ,p2, 1, 1, 1);
    }
    
}
