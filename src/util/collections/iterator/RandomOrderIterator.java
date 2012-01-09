package util.collections.iterator;

import util.*;

import java.util.*;

public class RandomOrderIterator implements Iterator {    
    
    private Iterator mIter;
    
    public RandomOrderIterator(Collection pCol) {
        ArrayList list = new ArrayList(pCol);
        Collections.shuffle(list);
        mIter = list.iterator();            
    }
    
    public boolean hasNext() {
        return mIter.hasNext();    
    }
            
    public Object next() {
        return mIter.next();    
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    /** read from stdin, write to stdout */
    public static void main(String[] args) {
        try {       
            if (args.length == 1) {
                int randSeed = Integer.parseInt(args[0]);
                System.err.println("Using random seed: "+randSeed);
                RandUtils.setRandSeed(randSeed);                
            }
            
            String[] lines = IOUtils.readLineArray(System.in);
            int numLines = lines.length;
            IntDoublePair[] ordering = new IntDoublePair[numLines];
            for (int i=0; i<numLines; i++) {
                ordering[i]=new IntDoublePair(i, RandUtils.getRandomNumberGenerator().nextDouble());
            }
            Arrays.sort(ordering);
            for (int i=0; i<numLines; i++) {
                System.out.println(lines[ordering[i].mInt]);
            }                                    
        }
        catch (Exception e) {
            e.printStackTrace();
        }                    
    }
        
    
}
