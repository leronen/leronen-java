package leronen.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docopt.Docopt;

import util.collections.Pair;

public class EncodingTester {	
	
	private static final String doc =
		    "Encoding tester\n"
		    + "\n"
		    + "Usage:\n"
		    + "  encode_test create OUTFILE\n"
		    + "  encode_test read INFILE CHARSET\n"
		    + "  encode_test (-h | --help)\n"
		    + "\n";
			
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> opts = new Docopt(doc).parse(args);
		System.out.println(opts);
		      
		if ((Boolean)opts.get("create")) {
			createTestData((String)opts.get("OUTFILE"));
		}
		else if ((Boolean)opts.get("read")) {
			List<Record> result = readTestData((String)opts.get("INFILE"), (String)opts.get("CHARSET"));
			for (Record r: result) {
				System.out.println(r);
			}
		}		
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
	
	private static void createTestData(String outfile) throws Exception {
		System.out.println("createTestData");
		try (FileOutputStream fos = new FileOutputStream(outfile)) {		 
	        for (int i=0; i<256; i++) {
	        	byte b = (byte)i;
	        	fos.write(b);
	        	log("" + i + "\t" + b);
	        }
		}        
	}
	
	private static List<Record> readTestData(String infile, String charset) throws Exception {
		ArrayList<Record> result = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(infile)) {		 
	        for (int i=0; i<256; i++) {
	        	byte[] buf = new byte[1];    	
	        	buf[0] = (byte)fis.read();
	        	String s = new String(buf, charset);
	        	Record record = new Record(i, s);
	        	result.add(record);
	        }
		}
		return result;
	}
	
	private static class Record extends Pair<Integer, String>{		
		private static final long serialVersionUID = -7649363544687603750L;

		public Record(Integer i, String s) {
			super(i,s);
		}
	}
	
}
