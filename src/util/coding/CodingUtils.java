package util.coding;

public class CodingUtils {
	public static final String COMMENT_PATTERN1 = "^\\s*//.*$";
    public static final String COMMENT_PATTERN2 = "^\\s*#.*$";
    public static final String COMMENT_PATTERN3 = "^\\s*%.*$";
    
    /**
     * A naive definition of comment, see the PATTERNS above,
     * as well as a naive implementation with multiple patterns...
     */
    public static boolean isComment(String pString) {
        return 
            pString.matches(COMMENT_PATTERN1) ||
            pString.matches(COMMENT_PATTERN2) ||
            pString.matches(COMMENT_PATTERN3);
    }
}
