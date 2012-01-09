package util.matrix;

import util.collections.*;

/** implemented as row-col pair */
public final class Index extends Pair {    
                    
    public Index(int pRow, int pCol) {
        super(new Integer(pRow), new Integer(pCol));        
    }  

    public int getX() {
        return getCol();
    }
        
    public int getY() {
        return getRow();
    }
    
    public int getRow() {
        return ((Integer)mObj1).intValue();
    }
    
    public int getCol() {
        return ((Integer)mObj2).intValue();
    }

        
    
    
    
    
    
}
