package util.comparator;

public class CompareUtils {

    public static int compare(double p1, double p2) {
        if (p1>p2) {
            return 1;
        }
        else if (p1 == p2) {
            return 0;
        }
        else {
            return -1;
        }        
    }
}
