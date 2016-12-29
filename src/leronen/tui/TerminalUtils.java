package leronen.tui;

import util.StringUtils;


public class TerminalUtils {    
    
    public static char ANSI_ESCAPE_CHAR = '\u001B';
    public static String ANSI_ESCAPE_RESET = ANSI_ESCAPE_CHAR + "[0m";
    public static String XTERM_256_COLOR_FG = ANSI_ESCAPE_CHAR + "[38;5;";
    public static String XTERM_256_COLOR_BG = ANSI_ESCAPE_CHAR + "[48;5;";
        
    
    public static String getXtermFgColor(int colorCode) {
        return XTERM_256_COLOR_FG + colorCode + "m";
    }
    
    public static String getXtermBgColor(int colorCode) {
        return XTERM_256_COLOR_BG + colorCode + "m";
    }
    
    public static String red() {
        return getXtermFgColor(9);
    }
    
    public static String black() {
        return getXtermFgColor(0);
    }    
    
    private static void printSegment(int offset, boolean reverseY) {
        for (int y=0; y<6; y++) {
            int y2 = y;
            if (reverseY) {
                y2 = 5-y; 
            }
            for (int x=0; x<12; x++) {                
                int color = 
                    x < 6
                    ? offset + x * 6 + y2
                    : offset + (17-x) * 6 + y2;
                String displayNumber = StringUtils.formatFixedWidthField("" + color, 3);
                System.out.print(getXtermBgColor(color) + displayNumber);                
            }
                        
            System.out.println(ANSI_ESCAPE_RESET);            
        }
    }
    
    
    
    public static void main(String[] args) {
        System.out.println(red() + "RED" + black() + "BLACK");
        printSegment(16, false);
        System.out.println();
        printSegment(88, true);
        System.out.println();
        printSegment(160, false);
        
//        for (int y=0; y<6; y++) {
//            for (int x=0; x<12; x++) {            
//                int color = 
//                    x < 6
//                    ? 16 + x * 6 + y
//                    : 16 + 6 * 6 + (11-x) * 6 + y;
//                String displayNumber = StringUtils.formatFixedWidthField("" + color, 3);
//                System.out.print(getXtermBgColor(color) + displayNumber);                
//            }
//                        
//            System.out.println(ANSI_ESCAPE_RESET);            
//        }
        
    }      
    
}

