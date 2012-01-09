package util.algorithm.clustering2;


/** 
 * Simultanious move of 2 elements: 
 *   -elem1 from src1 to dst1
 *   -elem2 from src2 to dst2
 */
public class DoubleMove<T> implements IMove<T> {
    
    public T elem1;
    public ICluster src1;
    public ICluster dst1;
    
    public T elem2;
    public ICluster src2;
    public ICluster dst2;
    
    public double score;

    public double getScore() {
        return score;
    }
    
    public DoubleMove(T elem1,
                      ICluster<T> src1, 
                      ICluster<T> dst1,          
                      T elem2,                            
                      ICluster<T> src2, 
                      ICluster<T> dst2,
                      double score) {        
        this.elem1 = elem1;
        this.src1 = src1;
        this.dst1 = dst1;
        
        this.elem2 = elem2;
        this.src2 = src2;
        this.dst2 = dst2;
        
        this.score = score;
    }               
    
    public String toString() {
        return ""+elem1+": "+src1.getId()+" => "+dst2.getId()+", "+
               ""+elem2+": "+src2.getId()+" => "+dst2.getId()+", "+ 
               "score="+score;
    }
}

   
