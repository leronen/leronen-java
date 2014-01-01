package util.test;


import java.util.Arrays;
import java.util.List;

/**
 * Test how are tab-delimited rows with white space as the last token split.
 *
 *
 */
public class SplitTest2 {

    private static final List<String> DATA_TABBED = Arrays.asList(
            "A\tB\tC",
            "A\tB\t",
            "A\t\tC");

    private static final List<String> DATA_WHITESPACE = Arrays.asList(
            "A B C",
            "A B ",
            "A B",
            "A  B ",
            "A  B  ");

    // private static final List<Integer> limits = Arrays.asList(-1,0,1,2);
    private static final List<Integer> limits = Arrays.asList(-1);

    @SuppressWarnings("unused")
    private static void test_tabs() {
        for (int l: limits) {
            System.out.println("limit: "+l);
            for (String s: DATA_TABBED) {
                String[] tok = s.split("\t", l);
                System.out.println("  "+s+" "+tok.length);

            }
            System.out.println();
        }
    }

    private static void test_whitespace() {
        for (int l: limits) {
            System.out.println("limit: "+l);
            for (String s: DATA_WHITESPACE) {
                String[] tok = s.split("\\s+", l);
                System.out.println("  "+s+" "+tok.length);

            }
            System.out.println();
        }
    }

    public static void main(String args[]) throws Exception {
        // test_tabs()
        test_whitespace();
    }


}
