package util.cmdline;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.CmdLineArgs2;
import util.DateUtils;
import util.StringUtils;
import util.comparator.ByFieldComparator;
import util.converter.Converter;
import util.process.ProcessOutput;
import util.process.ProcessUtils;

public class RecursiveDU {
	
	
	private List<String> files;
	/** limit in bytes to report */
	private long minsize_kilos;
		
	private RecursiveDU(CmdLineArgs2 args) {		
		
		files = args.getNonOptArgs();
		if (files.size() == 0) {
			files = Collections.singletonList(".");
		}
		String minszStr = args.get("minsize");
		if (minszStr == null) {
			minszStr = args.get("m");
		}
		if (minszStr != null) {
			minszStr = minszStr.replace("G", "000000000");
			minszStr = minszStr.replace("g", "000000000");
			minszStr = minszStr.replace("M", "000000");
			minszStr = minszStr.replace("m", "000000");
			minszStr = minszStr.replace("K", "000");
			minszStr = minszStr.replace("k", "000");			
			long minsize_bytes = Long.parseLong(minszStr);
			minsize_kilos = minsize_bytes/1000;
			
			if (minsize_kilos >= 1000) {				
				double minsize_megas = minsize_kilos / 1000;
				if (minsize_megas >= 1000) {
					// x >= 1G
					double minsize_gigas = minsize_megas / 1000;
					System.err.println("Using minsize: "+minsize_gigas+"G");
				}
				else {
					// 1M <= x < 1G
					System.err.println("Using minsize: "+minsize_megas+"M");
				}
			}
			else {
				// x < 1M
				System.err.println("Using minsize: "+minsize_kilos+"k");
			}
		}
		else {
			minsize_kilos = 10000000;
			System.err.println("Using default minsize of 10G");
		}		
	}

	private Entry fileToentry(File f, int depth) throws IOException {
		//System.err.println("Filetoentry: "+f);
		ArrayList<Entry> entries = new ArrayList<Entry>();
		String[] cmdArr = new String[3];
		cmdArr[0] = "/usr/bin/du";
		cmdArr[1] = "-s";
		cmdArr[2] = ""+f;
		ProcessOutput output = ProcessUtils.executeCommand(cmdArr);
		
		if (output.exitValue != 0) {
			System.err.println("Warning: du failed for file "+f);
			System.err.println(output.getStdErr());
			return null;
		}
		List<String> outputLines = output.getStdOutAslist();
				
		for (String line: outputLines) {
			try {
				Entry entry = new Entry(line, depth);
				entries.add(entry);
			}
			catch (IOException e) {
				if (e.getMessage().startsWith("No such file")) {
					System.err.println("Warning: "+e.getMessage()+" (probably a broken link)");
					return null;
				}
				else {
					throw e;
				}
			}
		}

		if (entries.size() == 0) {
			System.err.println("Warning: du returned no output for file "+f);
			return null;
		}
		if (entries.size() > 1) {
			throw new RuntimeException("More than one entry for file "+f+":"+
		                               StringUtils.listToString(entries));
		}
		
		Entry entry = entries.iterator().next();
		
		return entry;
	}
	
	private void recurse(File f, int depth) throws IOException {
					
		Entry entry = fileToentry(f, depth);
		if (entry != null) {
			recurse(entry);
		}		
	}
	
	private void recurse(Entry e) throws IOException {
			
		if (e.size > minsize_kilos) {
			System.out.println(e);
			if (e.file.isDirectory()) {
				ArrayList<Entry> childEntries = new ArrayList<Entry>();
				for (File child: e.file.listFiles()) {
					Entry childEntry = fileToentry(child, e.depth+1);
					if (childEntry != null) {
						childEntries.add(childEntry);
					}
				}
				Collections.sort(childEntries, new LengthComparator());
				Collections.reverse(childEntries);
				for (Entry child: childEntries) {
					recurse(child);
				}
			}
		}																
	}
	
	private void run() throws IOException {
		for (String f: files) {
			recurse(new File(f), 0);
		}				
	}
	
	private class Entry {
		
		File file;
		long size;
		int depth;
		
		private static final int SIZELEN = 12; 
		
		Entry(String line, int depth) throws IOException  {
			String[] cols = line.split("\t");
			this.size = Long.parseLong(cols[0]);			 		
			this.file = new File(line.replace(cols[0]+"\t", ""));
			if (!(file.exists())) {
				throw new IOException("No such file: "+file);
			}
			this.depth = depth;
		}
		
		public String toString() {
			String sizeString = StringUtils.h(size*1000);			
			int padLen = SIZELEN - sizeString.length();
			String numPad = StringUtils.stringMultiply(padLen, " ");
			String indent = StringUtils.stringMultiply(4*depth, " ");
			long lastModifiedMillis = file.lastModified();
			String lastModified = DateUtils.formatOrderableDate_no_time_of_day(lastModifiedMillis);
			return indent+sizeString+numPad+file+" "+lastModified;
		}
	}
		
	private class LengthComparator extends ByFieldComparator<Entry> {
		private LengthComparator() {
			super(new LengthExtractor());
		}
	}
	
	private class LengthExtractor implements Converter<Entry, Long> {
		public Long convert(Entry e) {
			return e.size;
		}
	}
	
	public static void main(String[] pArgs) throws Exception {
		// ArgsDef def = new ArgsDef();		
		CmdLineArgs2 args = new CmdLineArgs2(pArgs);
		RecursiveDU du = new RecursiveDU(args);
		du.run();
	}
}
