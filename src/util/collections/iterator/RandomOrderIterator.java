package util.collections.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import util.IOUtils;
import util.IntDoublePair;
import util.RandUtils;

public class RandomOrderIterator implements Iterator {

    private final Iterator mIter;

    public RandomOrderIterator(Collection pCol) {
        ArrayList list = new ArrayList(pCol);
        Collections.shuffle(list);
        mIter = list.iterator();
    }

    @Override
    public boolean hasNext() {
        return mIter.hasNext();
    }

    @Override
    public Object next() {
        return mIter.next();
    }

    @Override
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

