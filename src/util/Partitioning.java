package util;

/** 
 * Partitioning of a range of integers into a disjoint set of sub-ranges
 *
 * Note instances of this class are immutable.
 */

public final class Partitioning {

    private int mStartPoint;
    private int[] mEndPoints;
    
    /** 
     * @param pRanges is expected to make up a valid partitioning (that if, for each i, 
     *  pRanges[i].end = pRanges[i+1].start. This condition, of course implies, that 
     *  the ranges must be given sorted by the start point.
     */
    public Partitioning(Range[] pRanges) {
        int numRanges = pRanges.length;
        mStartPoint = pRanges[0].start;               
        for (int i=0; i<numRanges-1; i++) {
            if (pRanges[i].end != pRanges[i+1].start) {
                throw new RuntimeException("Invalid ranges: "+StringUtils.arrayToString(pRanges));
            }
            mEndPoints[i] = pRanges[i].end;
        }
        mEndPoints[numRanges-1] = pRanges[numRanges-1].end;
    }
    
    /** The "division points", plus the last end point */    
    public int[] getEndPoints() {
        return mEndPoints;
    }
    
    public int getStartPoint() {
        return mStartPoint;
    }
    
    public int getEndPoint() {
        return mEndPoints[mEndPoints.length-1];
    }

}
