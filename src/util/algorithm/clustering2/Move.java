package util.algorithm.clustering2;

public class Move<T> implements IMove<T> {
    
    public Move(T elem, ICluster<T> src, ICluster<T> dst, double score) {        
        this.elem = elem;
        this.src = src;
        this.dst = dst;
        this.score = score;
    }
    
    public T elem;
    public ICluster src;
    public ICluster dst;
    public double score;
    
    public double getScore() {
        return score;
    }
    
    public String toString() {
        return ""+elem+": "+src.getId()+" => "+dst.getId()+", score="+score; 
    }
}