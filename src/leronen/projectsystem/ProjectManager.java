package leronen.projectsystem;

import static leronen.projectsystem.Project.*; 

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import util.StringUtils;
import util.io.TableFileReader;
import util.io.NoSuchColumnException;
import util.process.ProcessOutput;
import util.process.ProcessUtils;

public class ProjectManager {
	
	private TreeMap<Integer, Project> projectById;
	
	public ProjectManager() {
		projectById = new TreeMap<Integer, Project>();
	}
	
	private void readProjects(String projectFile) throws StringUtils.UnexpectedNumColumnsException, NoSuchColumnException, IOException {
		TableFileReader reader = new TableFileReader(projectFile);
		Map<String, Map<String,String>> dataMap = reader.readAsMapMap(COLUMN_NAME_ID);		
		for (String idStr: dataMap.keySet()) {
			Map<String,String> data = dataMap.get(idStr);
			int id = Integer.parseInt(idStr); 
			String name = data.get(COLUMN_NAME_TITLE);
			String rootDir = data.get(COLUMN_NAME_ROOT_DIR);
			String setenvScript = data.get(COLUMN_NAME_SETENV_SCRIPT);
			Project project = new Project(id, name, rootDir, setenvScript, null);
			projectById.put(id, project);
		}					
	}
	
	private static void fixProjectFile() throws IOException {
		// a kludge to put empty setenv script for every project
		ProcessOutput output = ProcessUtils.bash(
				"cp -v "+LSS.getProjectsFile() + " " + LSS.getRegistryDir()+"/projects.bu\n"+
				"cat "+LSS.getProjectsFile() +
				" | awk -F '\\t' '\n"+
			    "   NF == 3 { print $0 \"\\t\"}\n"+
				"   NF == 4 { print $0 }'> projects.tmp\n"+
				"mv -v projects.tmp "+LSS.getProjectsFile());
		System.out.println(output);		
	}
	
	public static void main(String[] args) throws Exception {
		fixProjectFile();		
		ProjectManager damager = new ProjectManager();
		damager.readProjects(LSS.getProjectsFile());
		for (Project project: damager.projectById.values()) {
			System.out.println(project);
		}
	}
	
}
 