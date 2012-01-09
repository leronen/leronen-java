package util;

/**
 * Currently, implementation is dependent leronen cygwin conventions (it being highly unlikely that
 * someone else is going to want to use leronen utils in cygwin).
 * 
 * @author leronen 
 */
public class CygwinUtils {
    
    /** Simply test whether environment variable CYGWIN has value "1" */
    public static boolean isCygwin() {
        String CYGWIN = System.getenv("CYGWIN");
        return CYGWIN.equals("1");
    }
        
    
    /** gravely assume cygwin is always located at D:\cygwin... */
    public static String cygwinPathToWindowsPath(String cygPath) {
//        String tmp = new File(cygPath).getAbsolutePath();
//        Logger.info("Converting absolute path: "+tmp);
       
        String tmp = cygPath;
        
        if (tmp.startsWith("/home/")) {
            // home is actually located under 
            // TODO: should do same for all 
            tmp = "/cygdrive/d/cygwin"+tmp;
        }
        
        if (tmp.startsWith("/cygdrive/")) {
            // e.g. "/cygdrive/d/" => "D:\"
            tmp = tmp.replace("/cygdrive/", "");
            char driveLetter_lowercase = tmp.charAt(0);
            char driveLetter_uppercase = Character.toUpperCase(driveLetter_lowercase);
            tmp = driveLetter_uppercase+":\\"+tmp.substring(2);            
        }
        
        tmp = tmp.replace("/", "\\");
        
        return tmp;
        
    }
}
