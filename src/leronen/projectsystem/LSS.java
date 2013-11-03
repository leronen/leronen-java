package leronen.projectsystem;

import java.util.Set;

import util.ReflectionUtils;

/** The leronen shell system° main java node */ 
public class LSS {
	public static final String ENV_VAR_NAME_LERONEN_HOME      = "LERONEN_HOME";
	public static final String ENV_VAR_NAME_LERONEN_REGISTRY  = "LERONEN_REGISTRY";
	public static final String ENV_VAR_NAME_LERONEN_UTILS_DIR = "LERONEN_UTILS_DIR";
	public static final String ENV_VAR_NAME_PROJECTS_FILE     = "PROJECTS_FILE";
	public static final Set<String> ALL_ENV_VARIABLE_NAMES; 		
	
	static {			       
		ALL_ENV_VARIABLE_NAMES = ReflectionUtils.getPublicStaticStringFieldsWithPrefix(LSS.class, "ENV_VAR_NAME_");	    		
	}
	
	public static String getUtilsDir() {
		return System.getenv(ENV_VAR_NAME_LERONEN_UTILS_DIR);
	}
	
	public static String getProjectsFile() {
		return System.getenv(ENV_VAR_NAME_PROJECTS_FILE);
	}
	
	public static String getRegistryDir() {
		return System.getenv(ENV_VAR_NAME_LERONEN_REGISTRY);
	}
	
	private static void reportAllEnvVars() {
		for (String var: ALL_ENV_VARIABLE_NAMES) {
			String val = System.getenv(var);
			if (val != null) {
				System.out.println(var+"="+val);
			}
			else {
				throw new RuntimeException("No such env var: "+var);
			}
		}
	}
	
	public static void main(String[] args) {
		reportAllEnvVars();
	}
	
	
	
}
