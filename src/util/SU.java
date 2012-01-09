package util;

import java.util.Collection;
import java.util.Map;

import util.converter.Converter;

/**
 * A convenient shorthand for most frequently used methods in StringUtils.
 * Usage of StringUtils is too heavy for just renaming it... * 
 */
public class SU {
    public static String toString(Collection pCol, String pDelim) {
        return StringUtils.collectionToString(pCol, pDelim);
    }
    
    public static String toString(Collection pCol) {
        return StringUtils.collectionToString(pCol);
    }
        
    
    public static <T> String toString(Collection pCol, String pDelim, Converter<T, String> pFormatter) {
        return StringUtils.collectionToString(pCol, pDelim, pFormatter);
    }
    
    public static <K,V> String toString(Map pMap, String pKeyValDelim, String pEntryDelim) {
        return StringUtils.mapToString(pMap, pKeyValDelim, pEntryDelim); 
    }
    
    public static <K,V> String toString(Map<K, V> pMap) {
        return StringUtils.mapToString(pMap); 
    }
    
    public static String toString(String[] pArr) {
        return StringUtils.arrayToString(pArr);
    }
    
    public static String format(double p) {
        return StringUtils.formatFloat(p);
    }
    
    /** Split by white space */
    public static String[] split(String pString) {
        return StringUtils.split(pString);        
    }
    
    public static String formatOrdinal(int pOrdinal) {
        return StringUtils.formatOrdinal(pOrdinal);
    }
    
}
