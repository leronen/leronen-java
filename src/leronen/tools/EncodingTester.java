package leronen.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docopt.Docopt;

import util.collections.Triple;

public class EncodingTester {

    private static final File rawDataFile = new File("256.raw");

    private static final File latinDataFile = new File("256.latin1");
    private static final File win1252DataFile = new File("256.win1252");
    private static final File iso8859_15_Datafile = new File("256.iso8859_15");

    List<Record> latin1DecodedData;
    List<Record> win1252DecodedData;
    List<Record> iso8859_15_DecodedData;

    public EncodingTester() throws Exception {
        if (!rawDataFile.exists()) {
            createTestData(rawDataFile.getPath());
        }

        latin1DecodedData = readRawData(rawDataFile.getPath(), "ISO-8859-1");
        iso8859_15_DecodedData = readRawData(rawDataFile.getPath(), "ISO-8859-15");
        win1252DecodedData = readRawData(rawDataFile.getPath(), "Windows-1252");
    }

	private static final String doc =
		    "Encoding tester\n"
		    + "\n"
		    + "Usage:\n"
		    + "  encode_test create OUTFILE\n"
		    + "  encode_test read INFILE CHARSET\n"
		    + "  encode_test byte_to_inttable OUTFILE\n"
		    + "  encode_test int_to_byte_table OUTFILE\n"
		    + "  encode_test (-h | --help)\n"
		    + "\n";

	public static void main(String[] args) throws Exception {
		Map<String, Object> opts = new Docopt(doc).parse(args);
		// System.out.println(opts);

		if ((Boolean)opts.get("create")) {
			createTestData((String)opts.get("OUTFILE"));
		}
		else if ((Boolean)opts.get("byte_to_inttable")) {
		    byteToIntTable((String)opts.get("OUTFILE"));
		}
		else if ((Boolean)opts.get("int_to_byte_table")) {
		    intToByteTable((String)opts.get("OUTFILE"));
		}
		else if ((Boolean)opts.get("read")) {
			read((String)opts.get("INFILE"), (String)opts.get("CHARSET"));
		}
	}

	public void writeData() throws FileNotFoundException {
	    writeData(latin1DecodedData, latinDataFile);
	    writeData(win1252DecodedData, win1252DataFile);
	    writeData(iso8859_15_DecodedData, iso8859_15_Datafile);
	}

	public static void writeData(List<Record> decodedData, File outfile) throws FileNotFoundException {
	    try (PrintStream ps = new PrintStream(new FileOutputStream(outfile))) {
            for (Record r: decodedData) {
                ps.println(r);
            }
	    }
	}

	private static void read(String infile, String charset) throws Exception {
	    System.out.println("CODE\tSTRING\tUTF16CODE");
	    List<Record> result = readRawData(infile, charset);
        for (Record r: result) {
            System.out.println(r);
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
	        	// log("" + i + "\t" + b);
	        }
		}
	}

	private static void intToByteTable(String outfile) throws Exception {
        log("intToByteTable");
        try (PrintStream ps = new PrintStream(new FileOutputStream(outfile))) {
            ps.println("INT" + "\t" + "BYTE");
            for (int i=0; i<256; i++) {
                byte b = (byte)i;
                ps.println("" + i + "\t" + b);
            }
        }
    }

    private static void byteToIntTable(String outfile) throws Exception {
        log("byteToIntTable");
        try (PrintStream ps = new PrintStream(new FileOutputStream(outfile))) {
            ps.println("BYTE" + "\t" + "INT");
            for (byte b=0; b<=127 && b >= 0; b++) {
                int i = b;
                ps.println("" + b + "\t" + i);
            }
            for (byte b=-128; b<0; b++) {
                int i = b & 0xff;
                ps.println("" + b + "\t" + i);
            }
        }
    }

	private static List<Record> readRawData(String infile, String charset) throws Exception {
		ArrayList<Record> result = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(infile)) {
	        for (int code=0; code<256; code++) {
	        	byte[] buf = new byte[1];
	        	buf[0] = (byte)fis.read();
	        	String s = new String(buf, charset);
	        	Record record = new Record(code, s, s.charAt(0));
	        	result.add(record);
	        }
		}
		return result;
	}

	/** Single raw character (0-255), decoded using some decoding */
	@SuppressWarnings("serial")
	private static class Record extends Triple<Integer, String, Character>{

	    public Record(Integer code, String string, char utf16code) {
			super(code, string, utf16code);
		}

		public Integer getCode() {
		    return getObj1();
		}

		public String getString() {
            return getObj2();
        }

		public Character getUtf16Code() {
            return getObj3();
        }

		@Override
        public String toString() {
		    return getCode()+ "\t" + getString() + "\t" + (int)getUtf16Code() +
		           (getCode() != (int)getUtf16Code() ? " WARNING: differing codes!" : "");
		}
	}

}
