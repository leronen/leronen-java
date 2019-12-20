package gui.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import gui.GuiUtils;
import util.CollectionUtils;
import util.MathUtils;
import util.ReflectionUtils;
import util.StringUtils;
import util.algorithm.clustering.Clustering_old;
import util.algorithm.clustering.HClust;
import util.algorithm.clustering.HSBColorDistance;
import util.converter.Converter;
import util.converter.MapConverter;
import util.converter.MapConverter.NotFoundBehauvior;
import util.dbg.Logger;

public class ColorUtils {

    public static final Color LIGHT_BLUE = average(Color.blue, Color.white);
    public static final Color LIGHTEST_BLUE = weightedAverage(Color.blue, 0.25, Color.white, 0.9);
    public static final Color BLUE = Color.blue;
    public static final Color DARK_BLUE = average(Color.blue, Color.black);
    public static final Color DARKEST_BLUE = weightedAverage(Color.blue, 0.1, Color.black, 0.75);

    public static final Color LIGHTEST_RED = weightedAverage(Color.red, 0.1, Color.white, 0.9);
    public static final Color LIGHT_RED = average(Color.red, Color.white);
    public static final Color RED = Color.red;
    public static final Color DARKISH_RED = weightedAverage(Color.red, 0.8, Color.black, 0.2);
    public static final Color DARK_RED = average(Color.red, Color.black);
    public static final Color DARKEST_RED = weightedAverage(Color.red, 0.25, Color.black, 0.75);

    public static final Color LIGHTEST_GREEN = weightedAverage(Color.green, 0.1, Color.white, 0.9);
    public static final Color LIGHT_GREEN = average(Color.green, Color.white);
    public static final Color GREEN = Color.green;
    public static final Color DARK_GREEN = average(Color.green, Color.black);
    public static final Color DARKEST_GREEN = weightedAverage(Color.green, 0.25, Color.black, 0.75);

    public static final Color LIGHTEST_YELLOW = weightedAverage(Color.yellow, 0.1, Color.white, 0.9);
    public static final Color LIGHT_YELLOW = average(Color.yellow, Color.white);
    public static final Color YELLOW = Color.yellow;
    public static final Color DARK_YELLOW = average(Color.yellow, Color.black);
    public static final Color DARKEST_YELLOW = weightedAverage(Color.yellow, 0.25, Color.black, 0.75);

    public static final Color GRAY = Color.gray;
    public static final Color LIGHT_GRAY = average(Color.gray, Color.white);
    public static final Color DARK_GRAY = average(Color.gray, Color.black);
    public static final Color DARKEST_GRAY = weightedAverage(Color.gray, 0.25, Color.black, 0.75);

    public static final Color BROWN = new Color(160,64,0);
    public static final Color LIGHTEST_BROWN = weightedAverage(BROWN, 0.1, Color.white, 0.9);
    public static final Color LIGHT_BROWN = average(BROWN, Color.white);
    public static final Color DARK_BROWN = average(BROWN, Color.black);
    public static final Color DARKEST_BROWN = weightedAverage(BROWN, 0.25, Color.black, 0.75);

    public static final Color BROWNISH_YELLOW = weightedAverage(Color.YELLOW, 0.75, ColorUtils.BROWN, 0.25);

    public static final Color PURPLE = new Color(255, 0, 255);
    public static final Color LIGHT_PURPLE = average(PURPLE, Color.white);
    public static final Color DARK_PURPLE = average(PURPLE, Color.black);

    /** Let's have our own orange, as the orange from java.awt.Color sucks */
    public static final Color ORANGE = weightedAverage(Color.orange, 0.7, Color.RED, 0.3);
    public static final Color DARK_ORANGE = average(ORANGE, Color.black);

    private static Map<? extends Object, Color> CACHED_COLORS = null;

    public static Color average(Color pColor1, Color pColor2) {
        return new Color((pColor1.getRed() + pColor2.getRed())/2,
                         (pColor1.getGreen() + pColor2.getGreen())/2,
                         (pColor1.getBlue() + pColor2.getBlue())/2);
    }

    public static List <Color> getAllColors() {
        return new ArrayList(ReflectionUtils.getPublicStaticFieldsWithPrefix(ColorUtils.class, "", true).values());
    }

    /** Magically a coloring for an arbitrary set of objects */
    public static <T> Map<T,Color> getColoring(Collection<T> pObjects) throws TooManyColorsRequestedException {

        if (CACHED_COLORS != null
                && CACHED_COLORS.keySet().containsAll(pObjects)) {
            return CollectionUtils.subMap(CACHED_COLORS, (Collection)pObjects, true);
        }

        ArrayList objectList = new ArrayList(pObjects);

        if (pObjects.size() > 0 && pObjects.iterator().next() instanceof Comparable) {
            Collections.sort(objectList);
        }

        List<Color> repColors = getRepresentativeColors_clustering(pObjects.size());
        if (repColors.size() != pObjects.size()) {
            throw new RuntimeException(
              "Discrepancy between number of colors and objects. "+
              "There are "+pObjects.size()+" objects and "+repColors.size()+" colors;"+
              "The sets are "+pObjects+" and "+repColors+", respectively");

        }

        Map<T, Color> result = CollectionUtils.makeMap(objectList, repColors);
        CACHED_COLORS = result;
        return result;

    }

    /**
     * Get gradual coloring for numeric object in pObjects. Absolute differences
     * do not matter, only the order.
     *
     * Of course, pObjects must be Comparable.
     */
    public static <T> Map<T,Color> getColoring_ordinal(Collection<T> pObjects,
                                                       Color pMinColor,
                                                       Color pMaxColor) {
        Map result = new HashMap();
        Map<T, Integer> ranking = MathUtils.rank(pObjects);
        int minRank = Collections.min(ranking.values());
        int maxRank = Collections.max(ranking.values());

        int rMax,bMax,gMax;
        int rMin,bMin,gMin;

        rMax=pMaxColor.getRed();
        gMax=pMaxColor.getGreen();
        bMax=pMaxColor.getBlue();

        rMin=pMinColor.getRed();
        gMin=pMinColor.getGreen();
        bMin=pMinColor.getBlue();

        int r,g,b;

        for (T n: pObjects) {
            int rank = ranking.get(n);
            double maxWeight = MathUtils.normalize(rank, minRank, maxRank);
            double minWeight = 1.d-maxWeight;
            r = (int) MathUtils.weightedAvg(rMin, minWeight, rMax, maxWeight);
            g = (int) MathUtils.weightedAvg(gMin, minWeight, gMax, maxWeight);
            b = (int) MathUtils.weightedAvg(bMin, minWeight, bMax, maxWeight);
            Color color = new Color(r,g,b);
            result.put(n, color);
        }

        return result;

    }

//        /** Get gradual coloring for numeric object in pObjects */
//    public static <T> Map<T,Color> getColoring_numeric(Collection<Number> pNumbers,
//                                                       Color pMinColor,
//                                                       Color pMaxColor,
//                                                       Double pMinValue,
//                                                       Double pMaxValue) {
//
//    }

    /** Get gradual coloring for numeric object in pObjects */
    public static <T> Map<T,Color> getColoring_numeric(Collection<Number> pNumbers,
                                                       Color pMinColor,
                                                       Color pMaxColor,
                                                       Double pMinValue,
                                                       Double pMaxValue) {
        Map result = new HashMap();
        double min = pMinValue != null ? pMinValue : MathUtils.min(pNumbers);
        double max = pMaxValue != null ? pMaxValue : MathUtils.max(pNumbers);

        int rMax,bMax,gMax;
        int rMin,bMin,gMin;

        rMax=pMaxColor.getRed();
        gMax=pMaxColor.getGreen();
        bMax=pMaxColor.getBlue();

        rMin=pMinColor.getRed();
        gMin=pMinColor.getGreen();
        bMin=pMinColor.getBlue();

        int r,g,b;

        for (Number n: pNumbers) {
            double maxWeight = MathUtils.normalize(n.doubleValue(), min, max);
            double minWeight = 1.d-maxWeight;
            r = (int) MathUtils.weightedAvg(rMin, minWeight, rMax, maxWeight);
            g = (int) MathUtils.weightedAvg(gMin, minWeight, gMax, maxWeight);
            b = (int) MathUtils.weightedAvg(bMin, minWeight, bMax, maxWeight);
            Color color = new Color(r,g,b);
            result.put(n, color);
        }

        return result;
    }

    public static List<Color> getForbiddenColors() {
        ArrayList result = new ArrayList(ReflectionUtils.getPublicStaticFieldsWithPrefix(ColorUtils.class, "DARK", true).values());
        result.add(BLUE);
        result.add(GRAY);
        result.add(BROWN);
        return result;
    }

    public static List<Color> getRepresentativeColors_clustering(int pNumColors) throws TooManyColorsRequestedException {

//        if (CACHED_COLORS != null) {
//            if (CACHED_COLORS.size() >= pNumColors) {
//                return CACHED_COLORS.subList(0, pNumColors);
//            }
//        }

        if (pNumColors == 1) {
            // have to handle as a special case, as clustering does not seem to work
            // with only one object...
            return Collections.singletonList(LIGHT_GREEN);
        }
        else if (pNumColors == 2) {
            return CollectionUtils.makePair(LIGHT_GREEN, LIGHT_PURPLE);
        }
        else if (pNumColors == 3) {
            return CollectionUtils.makeArrayList(LIGHT_GREEN, LIGHT_PURPLE, LIGHT_YELLOW);
        }
        else if (pNumColors == 4) {
            return CollectionUtils.makeArrayList(LIGHT_GREEN, LIGHT_PURPLE, LIGHT_YELLOW, LIGHT_BROWN);
        }


        // Hope is mostly, but not completely, lost, resort to clustering...

        Set<Color> candidateColors = new HashSet(getAllColors());
        candidateColors.removeAll(getForbiddenColors());

        Clustering_old<Color> clustering =
            HClust.cluster_avg_linkage(candidateColors, new HSBColorDistance(), pNumColors);
            // HClust.cluster_avg_linkage(candidateColors, new RGBManhattanColorDistance(), pNumColors);

        clustering.setFormatter(getFormatter());

        Logger.dbg("Obtained the following clustering:\n"+clustering);
        List<Color> centroids = clustering.centroids();

        Logger.dbg("Centroids:\n"+StringUtils.collectionToString(centroids, "\n", getFormatter()));

//        CACHED_COLORS = centroids;

        return centroids;

    }

    public static Map<Color, String> getColorToNameMap() {
        Map<String, Color> colorByString = (Map)ReflectionUtils.getPublicStaticFieldsWithPrefix(ColorUtils.class, "", true);
        return CollectionUtils.inverseMap(colorByString);
    }

    public static Converter<Color, String> getFormatter() {
        Map<Color, String> map = getColorToNameMap();
        return new MapConverter(map, NotFoundBehauvior.ERROR);
    }




    /** Return a distance in the range of 0..1 */
    public static double distance(Color p1, Color p2) {
	return rgbDistance(p1, p2);
//         return hsbDistance(p1, p2, HSBWeights.UNIFORM);
//        return hsbDistance(p1, p2, HSBWeights.HUE_FIRST);
//        return hsbDistance(p1, p2, HSBWeights.HUE_ONLY);
    }
        
    /** Compute normalized manhattan distance between two colors in RGB space */
    public static double rgbDistance(Color p1, Color p2) {
        int d_r = Math.abs(p1.getRed()-p2.getRed());
        int d_g = Math.abs(p1.getGreen()-p2.getGreen());
        int d_b = Math.abs(p1.getBlue()-p2.getBlue());
        return ((double)(d_r + d_g + d_b)) / (255*3);
    }
    
    public enum HSBWeights {
        UNIFORM(1,1,1),
        HUE_FIRST(1,0.5,0.5),
        HUE_ONLY(1,0,0);
    
        double h;
        double s;
        double b;

        private HSBWeights(double h, double s, double b) {
            this.h = h;
            this.s = s;
            this.b = b;
        }
    }

    /**
     * Attempt to do a bit more sensible distance. Fails badly, looks horrible.
     * Trivial RGB distance on the other hand works very nicely...
     */
    public static double hsbDistance(Color p1,
                                     Color p2,
                                     HSBWeights weights) {
        return hsbDistance(p1, p2, weights.h, weights.s, weights.b);
    }
    
    /** Attempt to do a bit more sensible distance */
    public static double hsbDistance(Color p1,
                                     Color p2,
                                     double hWeight,
                                     double sWeight,
                                     double bWeight) {

        float[] hsv1 = Color.RGBtoHSB(p1.getRed(), p1.getGreen(), p1.getBlue(), null);
        float[] hsv2 = Color.RGBtoHSB(p2.getRed(), p2.getGreen(), p2.getBlue(), null);

        float h1 = hsv1[0]; float s1 = hsv1[1]; float b1 = hsv1[2];
        float h2 = hsv2[0]; float s2 = hsv2[1]; float b2 = hsv2[2];

        float d_h = Math.abs(h2-h1);
        if (d_h > 0.5) {
            d_h = 1.0f-d_h;
        }
        d_h = d_h * 2; // as the maximum is 0.5, let's put this in line with the other two

        float d_s = Math.abs(s2-s1);
        float d_b = Math.abs(b2-b1);

        return MathUtils.weightedAvg(d_h, hWeight,
                                     d_s, sWeight,
                                     d_b, bWeight);
    }


    public static Color weightedAverage(Color pColor1, double pWeight1,
                                        Color pColor2, double pWeight2) {
        double weightSum = pWeight1+pWeight2;
        double normalizedWeight1 = pWeight1/weightSum;
        double normalizedWeight2 = pWeight2/weightSum;
        double r = normalizedWeight1*pColor1.getRed() + normalizedWeight2*pColor2.getRed();
        double g = normalizedWeight1*pColor1.getGreen() + normalizedWeight2*pColor2.getGreen();
        double b = normalizedWeight1*pColor1.getBlue() + normalizedWeight2*pColor2.getBlue();

        return new Color((int)r, (int)g, (int)b);

    }

    public static Color inverse(Color pColor) {
		double r = 255-pColor.getRed();
		double g = 255-pColor.getGreen();
		double b = 255-pColor.getBlue();

		return new Color((int)r, (int)g, (int)b);
    }

    public static Color avg(Collection<Color> colors) {
        long rs=0, gs=0, bs=0;
        for (Color c: colors) {
            rs+=c.getRed();
            gs+=c.getGreen();
            bs+=c.getBlue();
        }

        int n = colors.size();
        int r = (int)((double)rs/n), g = (int)((double)gs/n), b = (int)((double)bs/n);
        return new Color(r,g,b);
    }

    private static void createAndShowGUI(String pTitle, Map<String, Color> pColorMap) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame(pTitle);

        JComponent colorPanel = Box.createVerticalBox();

        // Map<String, Object> colorMap = ReflectionUtils.getPublicStaticFieldsWithPrefix(ColorUtils.class, "", true);
        for (String colorName: pColorMap.keySet()) {
        	Color color = pColorMap.get(colorName);
        	JLabel label = new JLabel();
        	label.setText(colorName);
        	label.setBackground(color);
        	label.setOpaque(true);
        	label.setForeground(inverse(color));
        	colorPanel.add(label);
        }

        frame.getContentPane().add(colorPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        GuiUtils.center(frame);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        test1();

    }



    public static void test1() throws Exception {
        Logger.setLogLevel(Logger.LOGLEVEL_DBG);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Map<String, Color> colorMap = (Map)ReflectionUtils.getPublicStaticFieldsWithPrefix(ColorUtils.class, "", true);
                createAndShowGUI("All colors", colorMap);


                Map<String, Color> map2 = new LinkedHashMap();
                try {
                    HashSet<Color> repColors = new HashSet(getRepresentativeColors_clustering(10));

                    for(String name: colorMap.keySet()) {
                        Color c = colorMap.get(name);
                        if (repColors.contains(c)) {
                            map2.put(name, c);
                        }
                    }
                }
                catch (TooManyColorsRequestedException e) {
                    e.printStackTrace();
                }


                try {
                    createAndShowGUI("Representative colors (10)", map2);

                    Map<String, Color> map3 = new LinkedHashMap();
                    HashSet<Color> repColors2 = new HashSet(getRepresentativeColors_clustering(20));
                    for(String name: colorMap.keySet()) {
                        Color c = colorMap.get(name);
                        if (repColors2.contains(c)) {
                            map3.put(name, c);
                        }
                    }
                    createAndShowGUI("Representative colors (20)", map3);
                }
                catch (TooManyColorsRequestedException e) {
                    e.printStackTrace();
                }



            }
        });
    }



    public class TooManyColorsRequestedException extends Exception {

		private static final long serialVersionUID = 5069563418007101717L;
        // no specific impl needed
    }
}


