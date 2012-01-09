package util;

public class IntDoublePair implements Comparable {
    public int mInt;
    public double mDouble;
    
    public IntDoublePair(int pInt, double pDouble) {
        mInt = pInt;
        mDouble = pDouble;
    }
           
    public int getInt() {
        return mInt;
    }

    public double getDouble() {
        return mDouble;
    }           
           
    public int compareTo(Object pObj) {
        IntDoublePair other = (IntDoublePair)pObj;
        double diff = mDouble - other.mDouble;
        if (diff < 0) {
            return -1;
        }
        else if (diff > 0) {
            return 1;
        }
        else return 0;            
    }            
}
