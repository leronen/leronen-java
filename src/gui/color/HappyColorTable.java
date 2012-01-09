package gui.color;

import util.*; 
import util.collections.*;

import java.awt.Color;

import java.util.*;




public class HappyColorTable implements ColorTable {     
    
    private static HappyColorTable sSharedInstance;    
    
    private Distribution mAsDistribution;     
                   
    private static final Color[] BASE_COLORS = {
        Color.red,
        Color.yellow,
        Color.green,
        Color.blue,
        Color.orange,
        Color.pink,
        Color.cyan,        
    };

    private Color[] mColors;
    private int mNumColors;
    private boolean mWrap;         
    
    public HappyColorTable() {
        // by default, do not wrap the colors
        this(false);
    }
    
    public static HappyColorTable getInstance() {
        if (sSharedInstance == null) {
            sSharedInstance = new HappyColorTable();
        }
        return sSharedInstance;
    }
    
    public Distribution asDistribution() {
        // Logger.info("Starting HappyColorTable.asDistribution()");
        if (mAsDistribution == null) {            
            mAsDistribution = new Distribution();
            for (int i=0; i<mColors.length; i++) {
                mAsDistribution.add(mColors[i]);
            }
        }                        
        return mAsDistribution;
    }
    
    public HappyColorTable(boolean pWrap) {
        mNumColors = BASE_COLORS.length;
        mWrap = pWrap;        
        init();        
    }
        
    public HappyColorTable(int pNumColors) {
        mNumColors = pNumColors;
        init();
    }
    
    private void init() {
        if (mNumColors > BASE_COLORS.length) {
            throw new RuntimeException("There are not enough happy colours: asked for "+mNumColors+", but we have only "+BASE_COLORS.length);
        }        
        List happiestColors = CollectionUtils.extractFirst(mNumColors, Arrays.asList(BASE_COLORS));
        mColors =(Color[])happiestColors.toArray(new Color[mNumColors]);    
    }
    
    public Color getColor(int pInd) {
        if (mWrap) {
            return mColors[pInd % mNumColors];
        }
        else {
            return mColors[pInd];
        }
    }
          
    public int getNumColors() {
        return mColors.length;
    }        
    
}
