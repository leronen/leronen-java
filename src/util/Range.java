package util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.collections.HashMultiSet;

/**
 * An 1-d range of integers. As in java in general, start index is inclusive,
 * end index is exclusive,  e.g. range [1,4] means 1,2 and 3.
 *
 * This class is immutable, as is, for example java.lang.String.
 * Hmm, nowadays those public fields do not seem very immutable,
 * so we just have to trust the user...
 */
public class Range {

    /** inclusive. please do not set after constructing, as this field is supposed to be immutable... */
    public final int start; // inclusive
    /** exclusive. please do not set after constructing, as this field is supposed to be immutable... */
    public final int end;   // exclusive
    public boolean isNull;

    private BitSet mAsBitset;

    /** Note that in pStringRep, the last index is inclusive, while in our internal rep it is not! */
    public Range(String pStringRep) throws java.text.ParseException {

        Pattern p1 = Pattern.compile("^(\\d+)(-|,)(\\d+)$");
        Pattern p2 = Pattern.compile("^\\[(\\d+)(-|,)(\\d+)\\]$");
        Pattern p3 = Pattern.compile("^(\\d+)$");
        Matcher m1 = p1.matcher(pStringRep);
        Matcher m2 = p2.matcher(pStringRep);
        Matcher m3 = p3.matcher(pStringRep);
        if (m1.matches()) {
            start = Integer.parseInt(m1.group(1));
            end = Integer.parseInt(m1.group(3))+1;
        }
        else if (m2.matches()) {
            start = Integer.parseInt(m2.group(1));
            end = Integer.parseInt(m2.group(3))+1;
        }
        else if (m3.matches()) {
            start = Integer.parseInt(m3.group(1));
            end = start+1;
        }
        else {
            throw new java.text.ParseException("Not a valid range string: "+pStringRep, 0);
        }
        if (end < start) {
        	throw new RuntimeException("End index smaller than begin index; could be because end index is max int!");
        }
        isNull = false;
    }

    /** End index is exclusive, as is customary */
    public Range(int pStart, int pEnd) {
        if (pStart >= pEnd) {
            isNull = true;
            start = Integer.MAX_VALUE;
            end = Integer.MIN_VALUE;
        }
        else {
            start = pStart;
            end = pEnd;
        }
    }

    public Range add(int pOffset) {
        return new Range(start+pOffset, end+pOffset);
    }

    public int end_inclusive() {
        return end-1;
    }

    public boolean onRange(int pInd) {
        return (pInd>=start && pInd<end);
    }


    public BitSet asBitSet() {
        if (mAsBitset == null) {
            if (isNull) {
                mAsBitset = new BitSet();
            }
            else if (start<0) {
                throw new RuntimeException("bitset cannot represent ranges below zero");
            }
            else {
                mAsBitset = new BitSet(end);
                mAsBitset.set(start, end);
            }
        }
        return mAsBitset;
    }

    @Override
    public boolean equals(Object pOther) {
        Range otherRange = (Range)pOther;
        if (start == otherRange.start && end == otherRange.end) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (start*113 + end*67)%199;
    }

    /** return true, if there exist an index that is within both ranges */
    public boolean overlaps(Range pRange) {
        if (isNull || pRange.isNull) {
            return false;
        }
        if (pRange.end <= start) {
            // pRange lies to the left of this range
            return false;
        }
        if (pRange.start >= end) {
            // pRange lies to the right of this range
            return false;
        }
        // pRange overlaps this range
        return true;
    }

    /** return true, if this Range contains pRange */
    public boolean contains(Range pRange) {
        if (start <= pRange.start  && pRange.end<= end) {
            return true;
        }
        else {
            return false;
        }
    }

    /** return true, if this Range contains pRange */
    public boolean contains(int pInt) {
        if (start <= pInt  && pInt< end) {
            return true;
        }
        else {
            return false;
        }
    }

    public Range centerRange(int pLen) {
        // dbgMsg("centerRange");
        int len = length();
        // dbgMsg("len="+len);
        int difference = len-pLen;
        // dbgMsg("difference="+difference);
        if (difference < 0) {
            throw new RuntimeException("Cannot get center range: param smaller than range length!");
        }
        if (difference%2!=0) {
            throw new RuntimeException("Cannot get center range: difference not divisible by 2!");
        }
        int excessSpace = difference/2;
        // dbgMsg("excessSpace: "+excessSpace);
        return new Range(excessSpace, len-excessSpace);
    }

    public int length() {
        return end-start;
    }

    /** return null, if no intersection of at least length 0 */
    public Range intersection(Range other) {
        int start = Math.max(this.start, other.start);
        int end   = Math.min(this.end,   other.end);
        if (end-start >= 1) {
            return new Range(start, end);
        }
        else {
            return null;
        }
    }

    public static boolean hasOverlap(Range[] pRanges) {
        HashMultiSet reservedIndices = new HashMultiSet();
        for (int i=0; i<pRanges.length; i++) {
            int[] indices = pRanges[i].asIntArr();
            reservedIndices.addAll(ConversionUtils.asList(indices));
        }

        return reservedIndices.getMaxWeight() > 1;
    }

    /** In the string rep, end index is inclusive */
    @Override
    public String toString() {
        if (isNull) {
            return "<null range>";
        }
        else {
            return "["+start+","+(end-1)+"]";
        }
    }

    public int[] pickIndicesOnRange(int[] pArr) {
        ArrayList tmp = new ArrayList();
        for (int i=0; i<pArr.length; i++) {
            if (onRange(pArr[i])) {
                tmp.add(new Integer(pArr[i]));
            }
        }
        return ConversionUtils.integerCollectionToIntArray(tmp);
    }

    /**
     * @return all integers within the range. That is: (start, start+1, ... end-1).
     */
    public int[] asIntArr() {
        if (isNull) {
            return new int[0];
        }
        else {
            // not null range
            int len = length();
            int[] result = new int[len];
            for (int i=0; i<len; i++) {
                result[i] = start+i;
            }
            return result;
        }
    }

    /**
     * @return all integers within the range. That is: (start, start+1, ... end-1).
     */
    public List<Integer> asList() {
    	return ConversionUtils.asList(asIntArr());
    }

    /** get first half of the range; if length%2!=0, throw a RuntimeException */
    public Range firstHalf() {
        int len = length();
        int firsthalflen = len/2 + (len%2 == 0 ? 0 : 1);
        return new Range(start, start+firsthalflen);
    }

    /** get second half of the range; if length%2!=0, throw a RuntimeException */
    public Range secondHalf() {
        int len = length();
        int firsthalflen = len/2 + (len%2 == 0 ? 0 : 1);
        // int secondhalflen = len/2;
        return new Range(start+firsthalflen, end);
    }


    public static void main (String[] args) {
        // test1();
        test2();
    }

    private static void test2() {
        Range range = new Range(4,20);
        List<Range> segments = range.split(5);
        System.out.println("Segments:\n"+
                           StringUtils.listToString(segments));
    }

    /** Compares patters by their start indices */
    public static class StartComparator implements Comparator {
        @Override
        public int compare(Object p1, Object p2) {
            Range r1 = (Range)p1;
            Range r2 = (Range)p2;
            if (r1.isNull || r2.isNull) {
                throw new RuntimeException("Cannot compare ranges "+r1+" and "+r2+": either one or both are null!");
            }
            return  r1.start-r2.start;
        }
    }

    public List<Range> split(int pSegmentLen) {
        int[] segmentLengths = Utils.deduceSplitSegmentLengths(length(), pSegmentLen);
        int segmentLenSum = MathUtils.sum(segmentLengths);
        if (segmentLenSum != length()) {
            throw new RuntimeException("Cannot split to segments: sum of segment lengths <> lenght of list!");
        }
        // alles in ordnung, ja?
        int segmentStart = start;
        int numSegments = segmentLengths.length;
        List<Range> result = new ArrayList();
        for (int i=0; i<numSegments; i++) {
            int segmentLen = segmentLengths[i];
            int segmentEnd = segmentStart+segmentLen;
            Range segment = new Range(segmentStart, segmentEnd);
            result.add(segment);
            segmentStart = segmentEnd;
        }
        return result;
    }

    /** Compares patters by their lengths */
    public static class LengthComparator implements Comparator {
        @Override
        public int compare(Object p1, Object p2) {
            Range r1 = (Range)p1;
            Range r2 = (Range)p2;
            if (r1.isNull || r2.isNull) {
                throw new RuntimeException("Cannot compare ranges "+r1+" and "+r2+": either one or both are null!");
            }
            return  r1.length()-r2.length();
        }
    }

}
