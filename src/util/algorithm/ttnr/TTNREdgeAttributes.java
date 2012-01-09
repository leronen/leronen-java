package util.algorithm.ttnr;

public class TTNREdgeAttributes {
    
    /** -log(prob) */
    public double len = Double.NaN;
    public double prob = Double.NaN;
    public int prob_int = -1;    
    public boolean exists = false;
    
    /**
     * Possible auxiliary len. not to be confused with len, which is -log(prob). 
     * Used when computing expectedShortestPathlen_weighted. Otherwise NaN, 
     * according to current wisdom (2.6.2010).
     */
    public double len2 = Double.NaN;
    
    public String toString() {
        return prob+","+len+","+prob_int+","+exists;
    }
}
