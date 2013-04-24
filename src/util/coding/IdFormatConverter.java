package util.coding;

import java.util.ArrayList;
import java.util.List;

import util.IOUtils;


/**
 * Provides format conversions between different identifier naming conventions,
 * needed at modeling stage.
 * 
 * Tokens represented as lower case string lists within this class.
 */ 
public class IdFormatConverter {
	
	private static String format(List<String> data, IdFormat format) {
		StringBuffer buf = new StringBuffer();
		if (format == IdFormat.CAPITALISED_WITH_UNDERSCORES) {
			buf.append(data.get(0).toUpperCase());
			for (int i=1; i<data.size(); i++) {
				buf.append("_");
				buf.append(data.get(i).toUpperCase());
			}			
		}
		else if (format == IdFormat.JAVA_IDENTIFIER) {
			buf.append(data.get(0));
			for (int i=1; i<data.size(); i++) {
				String s = data.get(i);
				buf.append(Character.toUpperCase(s.charAt(0)));
				for (int j=1; j<s.length(); j++) {
					buf.append(s.charAt(j));
				}				
			}
		}
		else if (format == IdFormat.JAVA_CLASS_NAME) {			
			for (int i=0; i<data.size(); i++) {
				String s = data.get(i);
				buf.append(Character.toUpperCase(s.charAt(0)));
				for (int j=1; j<s.length(); j++) {
					buf.append(s.charAt(j));
				}				
			}
		}
		else if (format == IdFormat.JAVA_GETTER) {
			buf.append("get");
			for (int i=0; i<data.size(); i++) {
				String s = data.get(i);
				buf.append(Character.toUpperCase(s.charAt(0)));
				for (int j=1; j<s.length(); j++) {
					buf.append(s.charAt(j));
				}				
			}
			buf.append("()");
		}
		else {
			throw new RuntimeException("Unsupported format: "+format);
		}
		
		return buf.toString();
		
	}	
		
	private static boolean isCapitalizedWithUnderscores(String s) {
		String[] tok = s.split("_");
		for (String t: tok) {
			if (!(t.matches("[A-Z,0-9]*"))) {
				return false;
			}
		}
		return true;
	}
	
	private static List<String> parse(String s) {
		if (isCapitalizedWithUnderscores(s)) {		
			return parse(s, IdFormat.CAPITALISED_WITH_UNDERSCORES);
		}
		else {
			throw new RuntimeException("Cannot recognize format of string: "+s);
		}
	}
	
	private static List<String> parse(String s, IdFormat format) {
		switch(format) {
		case CAPITALISED_WITH_UNDERSCORES:
			String[] tok = s.split("_");
			List<String> result = new ArrayList<String>(tok.length);
			for (String t: tok) {
				result.add(t.toLowerCase());
			}
			return result;
		default:
			throw new RuntimeException("Cannot parse format: "+format);
		}
	}
	
	enum IdFormat {				
		CAPITALISED_WITH_UNDERSCORES, // e.g. these column names		
		JAVA_IDENTIFIER, // e.g. thisIsAnIdentifier
		JAVA_CLASS_NAME, // e.g. ThisIsAClass
		JAVA_GETTER // e.g. getThisVariable
	}
	
	public static void main(String[] args) throws Exception {
		
		IdFormat format = IdFormat.valueOf(args[0]);
		
		for (String val: IOUtils.readLines()) {
			List<String> tok = parse(val);
			System.out.println(format(tok, format));			
		}
	}
	
}
