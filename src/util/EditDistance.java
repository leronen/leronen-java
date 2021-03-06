package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import util.collections.Pair;
import util.comparator.ByFieldComparator;
import util.converter.Converter;
/**
 * Computes the string edit distance.
 *
 * <p>
 * Refer to a computer science text book for the definition
 * of the "string edit distance".
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class EditDistance {

    /**
     * Computes the edit distance between two strings.
     *
     * <p>
     * The complexity is O(nm) where n=a.length() and m=b.length().
     */
    public static int editDistance( String a, String b ) {
        return new EditDistance(a,b).calc();
    }

    /**
     * Finds the string in the <code>group</code> closest to
     * <code>key</code> and returns it.
     *
     * @return null if group.length==0.
     */
    public static String findNearest( String key, String[] group ) {
        return findNearest(key,Arrays.asList(group));
    }

    public static String findNearest( String key, Collection<String> group ) {
        int c = Integer.MAX_VALUE;
        String r = null;

        for (String g : group) {
            int ed = editDistance(key, g);
            if (c > ed) {
                c = ed;
                r = g;
            }
        }
        return r;
    }

    /** cost vector. */
    private int[] cost;
    /** back buffer. */
    private int[] back;

    /** Two strings to be compared. */
    private final String a,b;

    private EditDistance( String a, String b ) {
        this.a=a;
        this.b=b;
        cost = new int[a.length()+1];
        back = new int[a.length()+1]; // back buffer

        for( int i=0; i<=a.length(); i++ )
            cost[i] = i;
    }

    /**
     * Swaps two buffers.
     */
    private void flip() {
        int[] t = cost;
        cost = back;
        back = t;
    }

    private int min(int a,int b,int c) {
        return Math.min(a,Math.min(b,c));
    }

    private int calc() {
        for( int j=0; j<b.length(); j++ ) {
            flip();
            cost[0] = j+1;
            for( int i=0; i<a.length(); i++ ) {
                int match = (a.charAt(i)==b.charAt(j))?0:1;
                cost[i+1] = min( back[i]+match, cost[i]+1, back[i+1]+1 );
            }
        }
        return cost[a.length()];
    }
    
    /** If multiple as good matches, just return the first one */ 
    public static Match findClosestMatch(String query, List<String> list) {
        String bestMatch = CollectionUtils.findSmallest(list, new DistanceComparator(query));
        int d = editDistance(query,bestMatch);
        return new Match(bestMatch,d);
    }
    
    /** 
     * Rank elements of list according to their similarity with query. 
     * Ones with smallest edit distance (largest similarity) are returned first.
     * Do not modify list given as parameter, but instead return ranking results in a new ArrayList.
     */
    public static List<String> rank(String query, List<String> list) {
        ArrayList<String> result = new ArrayList<String>(list);
        Collections.sort(result, new DistanceComparator(query));
        return result;
    }
    
    protected static final class DistanceComparator extends ByFieldComparator<String> {       
        DistanceComparator(String s) {
            super(new DistanceComputer(s));
        }
        
    }
    
    private static class DistanceComputer implements Converter<String, Integer> {
        private String s;
        public DistanceComputer(String s) {
            this.s = s;
        }
        
        public Integer convert(String p) {
            return editDistance(s, p);
        }
    }
    
    public static Map<String,Match> findClosestMatches(List<String> list1, List<String> list2) {
        Map<String,Match> result = new LinkedHashMap<String,Match>();
        for (String s1: list1) {
            Match s2 = findClosestMatch(s1, list2);
            result.put(s1, s2);
        }
        return result;
    }
    
    private static class Match extends Pair<String,Integer> {
        
        /**
		 * 
		 */
		private static final long serialVersionUID = -7969459669882986987L;

		Match(String s, int d) {
            super(s,d);
        }
        
        public String getString() {
            return getObj1(); 
        }
        
        public int getDistance() {
            return getObj2(); 
        }
        
        public String toString() {
            return getString()+" "+getDistance();
        }
    }
    
    public static void main(String[] pArgs) throws Exception {
                    
        CmdLineArgs args = new CmdLineArgs(pArgs);
        
        String mode = args.getOpt("mode");
        
        if (mode == null) {
            // no mode
            if (args.getNumNonOptArgs() == 0) {
                // compute edit distance between all pairs of input elements read from stdin
                String[] lines = IOUtils.readLineArray(System.in);
                int n = lines.length;
                for (int i=0; i<n; i++) {
                    String s1 = lines[i];
                    for (int j=0; j<n; j++) {
                        String s2 = lines[j];
                        int d = editDistance(lines[i],lines[j]);                
                        System.out.println(s1+"\t"+s2+"\t"+d);
                    }
                }
    
            }
            else if (args.getNumNonOptArgs() == 1) {
                // compare all pairs of items in list read from file
                String[] lines = IOUtils.readLineArray(args.getNonOptArg(0));
                int n = lines.length;
                for (int i=0; i<n; i++) {
                    String s1 = lines[i];
                    for (int j=0; j<n; j++) {
                        String s2 = lines[j];
                        int d = editDistance(lines[i],lines[j]);                
                        System.out.println(s1+"\t"+s2+"\t"+d);
                    }
                }
    
            }
            else if (args.getNumNonOptArgs() == 2) {
                // find item2, the best match for each item1 in file $1 from file $2.
                List<String> list1 = IOUtils.readLines(args.getNonOptArg(0));
                List<String> list2 = IOUtils.readLines(args.getNonOptArg(1));
                Map<String,Match> closestMatches = findClosestMatches(list1, list2);
                System.out.println(StringUtils.mapToString(closestMatches));
            }
        }
        else if (mode.equals("rank")) {
            // assume single param "query", and rank lines of stdin according to
            // their similarity to the query
            String query = args.getNonOptArg(0);
            List<String> input = IOUtils.readLines();
            List<String> output = rank(query, input);
            for (String line: output) {
                System.out.println(line);
            }
        }

            
        
        
        
    }
}
