package util;


import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import util.converter.Converter;

/** Shorthands for most commonly used StringUtils functions */
public class Strings {
    
    @SafeVarargs
    public static <T> String format(T... list) {
        return format(Arrays.asList(list));
    }
    
    /** Format as "elem",elem2,elem3,..." */
    public static <T> String format(Collection<T> list) {
        return StringUtils.colToStr(list, ",");
    }
    
    /** Format as "elem",elem2,elem3,..." */
    public static <T> String format(Collection<T> list, Converter<T,String> formatter) {
        return StringUtils.colToStr(list, ",", formatter);
    }
    
    /** 
     * Format as:<br>
     *  KEY1=VAL1<br>
     *  KEY2=VAL2<br>
     *  ...<br>
     */
    public static <K,V> String formatMultiLine(Map<K,V> map) {
        return StringUtils.mapToString(map);
    }
           
    /** Format as: KEY1=VAL1,KEY2=VAL2,... */
    public static <K,V> String formatOneLine(Map<K,V> map) {
        return StringUtils.format(map, "=", ",");
    }

    public static String format(Collection<String> list, String separator) {
        return StringUtils.colToStr(list, separator);
    }
    
    public static String formatTabbed(Collection<String> list) {
        return StringUtils.colToStr(list, "\t");
    }
        
    
    public static String format(String[] list, String separator) {
        return StringUtils.arrToStr(list, separator);
    }
    
    public static String format(String[] list) {
        return StringUtils.arrToStr(list, ",");
    }
}

