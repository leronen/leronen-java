package util.matrix;

/** Maps a matrix index into some other index representation */
public interface IndexMapper {    
    
    public Object matrixIndexToIndexObject(int pRow, int pCol);
    public Index indexObjectToMatrixIndex(Object pObj);        

}
