package util;

import util.dbg.*;

import java.io.IOException;
import java.text.*;

import java.util.*;
import java.util.regex.*;



/** Todo: this class contains inconsistent parsing/formatting schaiBe */
public class DateUtils {

    public static final Calendar CALENDAR = Calendar.getInstance();
    
    public static final String DATE_PATTERN_STRING_1 = "^\\s*(\\d+)_(\\d+)_(\\d+)_(\\d+)\\s*$"; 
    public static final Pattern DATE_PATTERN_1 = Pattern.compile(DATE_PATTERN_STRING_1);    
        
    private static final String[] ENGLISH_MONTHS = {
        "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
    };

    private static final String[] ENGLISH_SHORT_MONTHS = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };       
        
    public static final DateFormat DEFAULT_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    public static final DateFormat DAY_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    public static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    public static final SimpleDateFormat ORDERABLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    public static final SimpleDateFormat ORDERABLE_DATE_FORMAT_NO_TIME_OF_DAY = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat NON_WHITE_SPACE_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    public static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("MMM dd, yyyy KK:mm:ss aa");

    private static final int[] COMMON_FIELDS = {
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DAY_OF_MONTH,                
        Calendar.HOUR_OF_DAY,
        Calendar.MINUTE,        
        Calendar.SECOND
    };

    private static final int[] DAY_FIELDS = {
        Calendar.YEAR,
        Calendar.MONTH,
        Calendar.DAY_OF_MONTH,        
    };

    private static final List<Integer> DAY_FIELDS_AS_INTEGERS = ConversionUtils.asList(DAY_FIELDS);        

    // Ok, DATE_FORMAT_3 will be a copy of DATE_FORMAT_2, with english symbols... 
    private static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("MMM dd, yyyy KK:mm:ss aa");
        
    static {
        // Logger.info("Setting custom date format symbols...");
        DateFormatSymbols DATE_FORMAT_3_SYMBOLS = DATE_FORMAT_3.getDateFormatSymbols();
        DATE_FORMAT_3_SYMBOLS.setMonths(ENGLISH_MONTHS);
        DATE_FORMAT_3_SYMBOLS.setShortMonths(ENGLISH_SHORT_MONTHS);    
        DATE_FORMAT_3.setDateFormatSymbols(DATE_FORMAT_3_SYMBOLS);
    }
    
    // private static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("MMM dd,yyyy KK:mm:ss");
    // private static final SimpleDateFormat DATE_FORMAT_4 = new SimpleDateFormat("dd,yyyy KK:mm:ss");    
        
    private static final DateFormat[] ALL_DATE_FORMATS = {
        DEFAULT_FORMAT,
        ORDERABLE_DATE_FORMAT,
        DATE_FORMAT_1,
        DATE_FORMAT_2,
        DATE_FORMAT_3,
        // DATE_FORMAT_4
    };                            

    public static Date boldlyParseAnyDate(String p) {        
        for (int i=0; i<ALL_DATE_FORMATS.length; i++) {
            try {
                Date candidate = ALL_DATE_FORMATS[i].parse(p);
                CALENDAR.setTime(candidate);
                if (CALENDAR.get(Calendar.YEAR)>= 1975) {
                	// do not accept dates before the birth of the author
                    return candidate;
                }
            }
            catch (java.text.ParseException e) {
                // Logger.warning("Failed parsing with "+(i+1)+":th date format");                
                // Pyh, maybe the next one will succeed...                                
            }                
        }
        // All attempts failed, yhyy!
        Logger.warning("Could not parse date: "+p);
        return null;        
    }    
    
   /**
    * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
    * that was "yyyy-MM-dd HH:mm:ss"
    */
    public static String formatOrderableDate(Date pDate) {                
        return ORDERABLE_DATE_FORMAT.format(pDate);                    
    }
   
    /**
     * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
     * that was "yyyy-MM-dd"
     */
    public static String formatOrderableDate_no_time_of_day(Date pDate) {
        return ORDERABLE_DATE_FORMAT_NO_TIME_OF_DAY.format(pDate);
    }
    
   public static String formatBmzDate() {
       Calendar cal = Calendar.getInstance();
       String prefix = formatOrderableDate_no_time_of_day();             
       String suffix = ""+(cal.get(Calendar.HOUR_OF_DAY)-21)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
       return prefix+"_"+suffix+" BMZ";
   }
    
    /* Format with {@link #NON_WHITE_SPACE_DATE_FORMAT} (at the time of last checking,
    * that was just yyyy_MM_dd_HH_mm_ss
    */
   public static String formatDate_no_white_space(long millis) {        
       return NON_WHITE_SPACE_DATE_FORMAT.format(new Date(millis));                    
   }
    
    /**
     * Format with {@link #NON_WHITE_SPACE_DATE_FORMAT} (at the time of last checking,
     * that was just yyyy_MM_dd_HH_mm_ss
     */
    public static String formatDate_no_white_space() {        
        return NON_WHITE_SPACE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));                    
    }
    
    /** format: year_mon_day_hour */
    public static long parseDateStringIntoMillis(String pString) throws RuntimeParseException {
        Matcher m;
        
        m = DATE_PATTERN_1.matcher(pString);
        if (m.matches()) {
            Calendar calendar = parseDateIntoCalendar(pString, "_");
            return calendar.getTimeInMillis();                   
        }
        else {
            throw new RuntimeParseException("With our humble abilities we cannot parse \""+pString+"\" as date!");
        }                                    
    }
    
    
    
            
    // REALLY OLD STUFF BEGINS ---            
    
    public static String getPrefixPaddedString(String pString, int pMinLen, char pPadCharacter) {
        int len = pString.length();
        if (len<pMinLen) {
            return StringUtils.stringMultiply((pMinLen-len), ""+pPadCharacter)+pString;
        }
        else return pString;
        
    }
                
    public static String formatDateForPathName(Calendar pCalendar) {                
        String result = pCalendar.get(Calendar.YEAR)+
                        "_"+StringUtils.makePrefixPaddedString(""+(pCalendar.get(Calendar.MONTH)+1),2,'0')+
                        "_"+StringUtils.makePrefixPaddedString(""+pCalendar.get(Calendar.DAY_OF_MONTH),2,'0')+
                        "_"+StringUtils.makePrefixPaddedString(""+pCalendar.get(Calendar.HOUR),2,'0')+
                        "_"+StringUtils.makePrefixPaddedString(""+pCalendar.get(Calendar.MINUTE),2,'0')+
                        "_"+StringUtils.makePrefixPaddedString(""+pCalendar.get(Calendar.SECOND),2,'0');
        return result;                                                
    }
    
    public static String formatDateForPathName(Date pDate) {        
        CALENDAR.setTime(pDate);
        return formatDateForPathName(CALENDAR);
    }
    
    
    public static String formatDate() { 
        return formatDate(System.currentTimeMillis());
    }    
    

    /**
     * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
     * that was "yyyy-MM-dd"
     */
    public static String formatOrderableDate_no_time_of_day() {
        return formatOrderableDate_no_time_of_day(System.currentTimeMillis());
    }
    
    /**
     * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
     * that was "yyyy-MM-dd"
     */
    public static String formatOrderableDate_no_time_of_day(long pMillis) {
        Date date = new Date(pMillis);
        return formatOrderableDate_no_time_of_day(date);
    }
    
    /**
     * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
     * that was "yyyy-MM-dd HH:mm:ss"
     */
    public static String formatOrderableDate() {
        return formatOrderableDate(System.currentTimeMillis());
    }
    
    /**
     * Format with {@link #ORDERABLE_DATE_FORMAT} (at the time of last checking,
     * that was "yyyy-MM-dd HH:mm:ss"
     */
    public static String formatOrderableDate(long pMillis) {
        Date date = new Date(pMillis);
        return formatOrderableDate(date);
    }
    
    /** Format day as well as time */
    public static String formatDate(Date pDate) { 
        return DEFAULT_FORMAT.format(pDate);        
    }
    
    public static String formatDate(long pMillis) {
        Date date = new Date(pMillis);                            
        return DEFAULT_FORMAT.format(date);
    }
    
    public static Map<Integer,Integer> asMap(Date pDate) {
        CALENDAR.setTime(pDate);
        HashMap<Integer,Integer> result = new HashMap<Integer,Integer>(COMMON_FIELDS.length);        
        for (int i=0; i<COMMON_FIELDS.length; i++) {
            int field = COMMON_FIELDS[i];
            int val = CALENDAR.get(field);
            result.put(new Integer(field), new Integer(val));
        }
        return result;
    }
        
    public static int daysDifference(Date p1, Date p2) {
        if (p1 == null) {
            throw new RuntimeException("Date 1 is null!");
        }
        if (p2 == null) {
            throw new RuntimeException("Date 2 is null!");
        }                
        double diff = p2.getTime() - p1.getTime();
        return (int)(diff/86400000);
    }                

    
    /** e.g. "2011-12-12",3 => "2011-12-09" */
    public static String nDaysBefore(String dateString, int n) throws ParseException {
    	Date orig = ORDERABLE_DATE_FORMAT_NO_TIME_OF_DAY.parse(dateString);
    	long origMillis = orig.getTime();
    	long nDaysAsMillis = 24l*3600*1000*n;    	
    	long millis_transformed = origMillis - nDaysAsMillis;    	
    	Date transformed = new Date(millis_transformed);    	
    	return ORDERABLE_DATE_FORMAT_NO_TIME_OF_DAY.format(transformed);
    }                

    
    public static boolean sameDay(Date p1, Date p2) {
        Map<Integer,Integer> map1 = asMap(p1);
        Map<Integer,Integer> map2 = asMap(p2);
        
        Map<Integer,Integer> dayMap1 = CollectionUtils.subMap(map1, DAY_FIELDS_AS_INTEGERS, false);
        Map<Integer,Integer> dayMap2 = CollectionUtils.subMap(map2, DAY_FIELDS_AS_INTEGERS, false);
        
        return dayMap1.equals(dayMap2); 
    }       
    
    public static String formatInterval(List<Date> pDates) {
        Collections.sort(pDates);
        Date first = (Date)pDates.get(0);
        Date last = (Date)pDates.get(pDates.size()-1);
                
        if (sameDay(first, last)) {
            // same day            
            return DAY_FORMAT.format(first)+" "+TIME_FORMAT.format(first)+"-"+TIME_FORMAT.format(last); 
        }
        else {
            // different days
            return DAY_FORMAT.format(first)+"-"+DAY_FORMAT.format(last);
        }     
    }
    
    public static long parseDateIntoMillis(String pDateString) throws java.text.ParseException {
        // Date date = new Date(pMillis);                
        Date date = DEFAULT_FORMAT.parse(pDateString);             
        return date.getTime();
    }
    
    /** Format as "xh ymin zsec" */ 
    public static String formatSeconds(int pSec) {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        
        seconds = pSec;
        
        if (seconds >= 3600) {
            hours = seconds / 3600;
            seconds = seconds % 3600;
        }
        
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds = seconds % 60;
        }
        
        if (hours > 0) {
            return ""+hours+"h "+minutes+"min "+seconds+"sec";
        }
        else if (minutes > 0) {
            return ""+minutes+"min "+seconds+"sec";
        }
        else {
            return ""+seconds+"sec";
        }
    }
    
    public static String formatDateForPathName(long pMillis) {
        return formatDateForPathName(new Date(pMillis));
    }
    
    // The assumed order of the fields: year, month, day, hour, minute, second
    public static Calendar parseDateIntoCalendar(String pDateString, String pDelimPattern) {
        // StringTokenizer tok = new StringTokenizer(pDateString);
        String[] tokenArray = pDateString.split(pDelimPattern);
        // StringIteratorWrapper tokens = 
        Iterator<String> tokens = Arrays.asList(tokenArray).iterator();        
        Calendar calendar = Calendar.getInstance();        
        String year = null;
        String month = null;
        String day = null; 
        String hour = null; 
        String min = null; 
        String sec = null; 
                    
        if (tokens.hasNext()) {
            year = tokens.next();
            calendar.set(Calendar.YEAR, Integer.parseInt(year));
        }
        if (tokens.hasNext()) {
            month = tokens.next();
            calendar.set(Calendar.MONTH, Integer.parseInt(month));
        }
        if (tokens.hasNext()) {
            day = tokens.next();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        }
        if (tokens.hasNext()) {
            hour = tokens.next();
            calendar.set(Calendar.HOUR, Integer.parseInt(hour));
        }
        if (tokens.hasNext()) {
            min = tokens.next();
            calendar.set(Calendar.MINUTE, Integer.parseInt(min));
        }
        if (tokens.hasNext()) {
            sec = tokens.next();
            calendar.set(Calendar.SECOND, Integer.parseInt(sec));
        }                      
        return calendar;                             
    }
    
    
    public static String formatMillis(long pTotalMillis) {      
        long millis = pTotalMillis%1000;                
        long totalSec = pTotalMillis/1000;
        long sec = totalSec%60;
        long totalMin = totalSec/60;
        long min = totalMin%60;
        long totalHr = totalMin/60;
        long hr = totalHr%24;
        long totalD = totalHr/24;
        
        return String.format("%dd %dh %dmin %dsec %dmillis", totalD, hr, min, sec, millis);
    }
    
    public static void main(String[] args) {                        
        String cmd = args[0];
        if (cmd.equals("getformatteddatetime")) {
            System.out.println(formatDate(System.currentTimeMillis()));
        }
        else if (cmd.equals("bmz")) {
            System.out.println(formatBmzDate());
        }
        else if (cmd.equals("ndaysbefore")) {
        	String date = args[1];
        	int n = Integer.parseInt(args[2]);        	
        	try {
        		String result = nDaysBefore(date, n);
        		System.out.println(result);
        	}
        	catch (ParseException e) {
        		System.err.println("failed parsing date: "+date);
        	}
        }
        else if (cmd.equals("formatmillis")) {
            if (args.length == 1) {
                try {
                    List<String> lines = IOUtils.readLines(System.in);
                    for (String line: lines) {
                        System.out.println(formatMillis(Long.parseLong(line)));
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }                
            }
            else {
                System.out.println(formatMillis(Integer.parseInt(args[1])));
            }
        }
        else if (cmd.equals("dbg")) {
            // String difficultCase = "Mar 7, 2004 03:33:19 AM";
            // String difficultCase = "Mar 7, 2004 03:33:19";
            // String difficultCase = "7, 2004 03:33:19";
            String difficultCase = "Mar 7, 2004 03:33:19 AM";
            Date date = boldlyParseAnyDate(difficultCase);
            if (date != null) {
                Logger.info("Parsed: "+difficultCase+". Result: "+formatOrderableDate(date));
            }
        }  
        else {
        	System.err.println("Illegal command: "+cmd);
        }
    }
     
    /** Generates current date as yyyy-mm-dd_hh:mm:ss */
    public static class DateGenerator implements StringGenerator {
        
        public String generate() {
            return formatOrderableDate();
        }
    }
    
    public static class BMZGenerator implements StringGenerator {
        
        public String generate() {
            return formatBmzDate();
        }
    }
    
    

}
