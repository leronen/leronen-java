package gui.color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class DefaultColorTable implements ColorTable {

    private List<Color> mColors;
    
    public DefaultColorTable() {
        this(Color.white, Color.black);
    }
    
    public DefaultColorTable(List<Color> pColors) {
        mColors = pColors;
    }
    
    public DefaultColorTable(Color pColor1, 
                             Color pColor2) {
        mColors = new ArrayList<Color>();
        mColors.add(pColor1);
        mColors.add(pColor2);
    }
    
    public DefaultColorTable(Collection<Color> pColors) {
        mColors = new ArrayList<Color>(pColors);
    }

    public DefaultColorTable(int pNumIntensityLevels,
                             Color pMinColor,
                             Color pMaxColor) {
        mColors = new ArrayList(pNumIntensityLevels);

        if (pMaxColor == null || pMinColor == null) {
            throw new RuntimeException("Trying to create a default color table with null min or max color");
        }
        
        int rMax,bMax,gMax;
        int rMin,bMin,gMin;

        rMax=pMaxColor.getRed();
        gMax=pMaxColor.getGreen();
        bMax=pMaxColor.getBlue();
        rMin=pMinColor.getRed();
        gMin=pMinColor.getGreen();
        bMin=pMinColor.getBlue();
        int r,g,b;
        for (double i=0; i<pNumIntensityLevels; i++) {
            double maxFactor = i/(pNumIntensityLevels-1);
            double minFactor = 1.d-maxFactor;
            r = (int)(maxFactor*rMax+minFactor*rMin);
            g = (int)(maxFactor*gMax+minFactor*gMin);
            b = (int)(maxFactor*bMax+minFactor*bMin);
            mColors.add(new Color(r, g, b));
        }
    }

    public Color getColor(int pInd) {
        return mColors.get(pInd);
    }
    
    public int getNumColors() {
        return mColors.size();
    }

}
