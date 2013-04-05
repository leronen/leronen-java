package util.coding;

public class CodingUtils {
	public static final String COMMENT_PATTERN1 = "^\\s*//.*$";
    public static final String COMMENT_PATTERN2 = "^\\s*#.*$";
    public static final String COMMENT_PATTERN3 = "^\\s*%.*$";
    public static final String COMMENT_PATTERN4 = "^/\\*.*$";
    public static final String COMMENT_PATTERN5 = "^ \\*.*$";
    
    /**
     * A naive definition of comment, see the PATTERNS above,
     * as well as a naive implementation with multiple patterns...
     */
    public static boolean isComment(String pString) {
        return 
            pString.matches(COMMENT_PATTERN1) ||
            pString.matches(COMMENT_PATTERN2) ||
            pString.matches(COMMENT_PATTERN3) ||
            pString.matches(COMMENT_PATTERN4) ||
            pString.matches(COMMENT_PATTERN5);
    }
    
    private static final String[] TEST_DATA = {
		"// foo",
		"/* bar",
		"/** baz",
		" * gurply",
		" */ waldo",
		"fred"
    };
    
    public static void main(String[] args) {
    	for (String s: TEST_DATA) {    		
    		if (isComment(s)) {
    			System.out.println("COMMENT    : "+s);
    		}
    		else {
    			System.out.println("NOT COMMENT    : "+s);
    		}
    	}
    }
}
