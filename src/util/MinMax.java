package util;

/**
 * Hopefully facilitates implementing methods that can flexibly do both 
 * minimization and maximization.
 *  
 * @author leronen
 */
public enum MinMax {
    
    MIN(-Double.MAX_VALUE, "score"), 
    MAX(Double.MAX_VALUE, "cost");
    
    public final double optValue;
    public final double worstValue;
    public final String costOrScore;
    
    private MinMax(double optValue,
                   String costOrScore) {
        this.optValue = optValue;
        this.worstValue = -optValue;
        this.costOrScore = costOrScore;
    }
        
}


