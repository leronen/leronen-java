package leronen.projectsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.CollectionUtils;
import util.ReflectionUtils;
import util.StringUtils;
import util.collections.tree.TreeNodeAdapter;
import util.comparator.ByFieldComparator2;
import util.io.TableFileReader;
import util.process.ProcessUtils;

/** Manages process information originating from ps   */
public class ProcessManager {
	
	// fields from ps axl: F   UID   PID  PPID PRI  NI    VSZ   RSS WCHAN  STAT TTY        TIME COMMAND	
	
	public static final String KEY_PID = "PID";
	public static final String KEY_PPID = "PPID";
	public static final String KEY_UID = "UID";
	public static final String KEY_COMMAND = "COMMAND";
	
	public static final Set<String> ALL_KEYS = ReflectionUtils.getPublicStaticStringFieldsWithPrefix(ProcessManager.class, "KEY_");
	
	private Map<String, Process> processById;
	
	public ProcessManager() {
		processById = new LinkedHashMap<String, Process>();
	}
	
	public String getParent(String pid) {
		Process proc = processById.get(pid);
		return proc.getPPid();
	}
		
	public void createChildLinks() {
		// clear previous links
		for (Process proc: processById.values()) {
			proc.children.clear();
		}

		// add new parent => child links
		for (Process proc: processById.values()) {
			String ppid = proc.getPPid();			
			Process parent = processById.get(ppid);
			if (parent != null) { 
				parent.children.add(proc);
			}
		}
		
		// sort children by pid
		ByFieldComparator2<Process, Integer> comparator = new ByFieldComparator2<Process,Integer>() {
			protected Integer extractField(Process proc) {
				return Integer.parseInt(proc.getPid());
			}
		};
		
		for (Process proc: processById.values()) {
			Collections.sort(proc.children, comparator);
		}
	}
	
	public static class Process extends HashMap<String, String> {
		List<Process> children = new ArrayList<Process>();
		
		public Process(Map<String, String> data)  {
			putAll(CollectionUtils.subMap(data, ALL_KEYS, false));					
		}
		
		public String getPid() {
			return get(KEY_PID);
		}
		
		public String getUid() {
			return get(KEY_UID);
		}
		
		public String getPPid() {
			return get(KEY_PPID);
		}
		
		public String getCommand() {
			return get(KEY_COMMAND);
		}
		
		public String toString() { 
			return getPid() + " (" +getCommand()+ ")";
		}
	}
	
	public Process get(int procId) {
		return processById.get(""+procId);
	}
	
	private class ProcessTreeNodeAdapter implements TreeNodeAdapter<Process> {

		@Override
		public List<Process> children(Process proc) {
			return proc.children;
		}
		
	}
	
	public void refresh() throws IOException {
		String tmpFile = "/tmp/ProcessManager.tmp";
		ProcessUtils.bash("ps axl | sed 's/  */ /g' | cut -d \" \" -f 1-13 > " + tmpFile);
		processById.clear();
		Map<String,String> rootData = new LinkedHashMap<String,String>();		
		rootData.put(KEY_PID, "0");
		rootData.put(KEY_PPID, "no parent");
		rootData.put(KEY_UID, "0");
		rootData.put(KEY_COMMAND, "no command");
		Process root = new Process(rootData);
		processById.put("0", root);
		TableFileReader reader = new TableFileReader(tmpFile);
		Map<String, Map<String,String>> data = reader.readAsMapMap(KEY_PID);
		for (Map<String,String> map: data.values()) {
			Process proc = new Process(map);
			processById.put(proc.getPid(), proc);
		}
		
		createChildLinks();
	}
	
	public String toString() {
		return StringUtils.formatTree(processById.get("0"), new ProcessTreeNodeAdapter(), 4, false);
		// return StringUtils.mapToString(processById, ":\n\t", "\n");
	}
	
	public static void main (String[] args) throws IOException {
		ProcessManager pm = new ProcessManager();
		pm.refresh();
		System.out.println(pm);
	}
}
