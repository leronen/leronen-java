package util.collections;

/** Used to scores while computing some kind of distance or other score between objects */
public class Match<T> {
	T object;
	double score;
	
	public Match(T object, double score) {
		this.object = object;
        this.score = score;
    }
	
	public T getObject() {
		return object;
	}
	
	public double getScore() {
		return score;
	}
	
	public String toString() {
		return object+": "+score;
	}
	
	
	public static class Comparator implements java.util.Comparator<Match> {
		int factor;
		
		public Comparator() {
			this(false);
		}	
		public Comparator(boolean reverse) {
			factor = reverse ? -1 : 1;
		}
                                              
	    public int compare(Match m1, Match m2) {                   
	        double diff = m1.getScore()-m2.getScore();                                 
	        if (diff < 0) return -1*factor;
	        else if (diff == 0) return 0;
	        else return factor;
	    }
	}
	
}
        
              
