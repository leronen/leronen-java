package util.collections.graph.defaultimpl;


/** 
 * The weight is interpreted directly as the length regarding IDijkstraEdge 
 * methods #getLen() and #setLen(double)
 */
public class WeightedEdge<T, N extends DefaultNode<T>> extends DefaultEdge<T,N> implements IDijkstraEdge {
    
    private double mWeight = Double.NaN;
        
    private static final Factory FACTORY = new Factory();
    
    WeightedEdge(N pSrc, N pTgt) {
        super(pSrc, pTgt);
    }    
    
    public EdgeFactory getFactory() {
        return WeightedEdge.FACTORY;
    }
    
    public String toString() {
        return super.toString()+" ("+mWeight+")"; 
    }
    
    public static class Factory<T,N extends DefaultNode<T>> implements EdgeFactory<T, N, WeightedEdge<T,N>> {
        public WeightedEdge<T, N> makeEdge(N pSrc, N pTgt) {
            return new WeightedEdge<T, N>(pSrc, pTgt);
        }
    }
    
    protected void initAttributes() {
        // no action needed
    }    
    
    /**
     * Clone data (other than mSrc and mTgt) from this edge to pDst. 
     * Subclasses should override this!
     */
    public void cloneInitializedAttributesToReverseRep(DefaultEdge pDst) {
        ((WeightedEdge)pDst).mWeight = mWeight; 
    }
    
    /** Return the weight as is (no transformations!) */
    public double getLen() {        
        return mWeight;
    }
    
    /** exp(-weight) */
    public double getProb() {        
        return Math.exp(-mWeight);
    }
    
        /** Remember to update also the reverse!!! */
    public void setLen(double pLen) {        
        mWeight = pLen;
        ((WeightedEdge)reverse).mWeight = pLen;
    }

}
