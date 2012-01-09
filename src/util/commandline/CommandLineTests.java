package util.commandline;

import util.*;
import util.dbg.ILogger;
import util.dbg.StdErrLogger;

import java.util.*;


/**
 * Base class for implementing unit tests runnable from the command line.
 * Aimed to enable implementing unit tests with minimum effort.
 * 
 * See e.g. class com.bcplatforms.bcos.custom.koiranet.test.EYEUnitTests
 * for example usage. 
 */
public abstract class CommandLineTests {
	
	protected CmdLineArgs2 args;
	protected ILogger log = new StdErrLogger();
		
	protected CommandLineTests(String[] pArgs) {
        try {
            args = new CmdLineArgs2(pArgs);
        }
        catch (CmdLineArgs2.IllegalArgumentsException e) {
            System.err.println("Illegal arguments: "+e.getMessage());            
            System.exit(1);
            return;
        }             
        
        if (args.getNonOptArgs().size() == 0) {
        	printUsageAndExit("First argument should be a command!");
        }
        
        String cmd = args.getNonOptArgs().get(0);
        
        if (!(allCommands().contains(cmd))) {
        	printUsageAndExit("No such command: "+cmd);        	
        }
	}
	
	/**
	 * Should be called as first and only thing in subclass main after constructing.
	 * Just shifts the first argument from args as the command name, and 
	 * calls subclass run(command). All checked exceptions thrown in subclass are caught
	 * and handled trivially.  
	 */
	protected final void run()  {
		String cmd = args.shift();
		try {			
			run(cmd);
		}
		catch (Exception e) {
			log.error("Failed running command: "+cmd, e);
			System.exit(-1);
		}
	}
	
	/**
	 * Subclasses should implement running. Note that this class always processes
	 * the first argument on the command line, which should always be the name
	 * of the command. It is extracted using CmdLineArgs.shift, so first actual
	 * argument is args.get(0), as seen by the subclass.
	 */
	protected abstract void run(String cmd) throws Exception;
	
	protected void printUsageAndExit(String msg) {
		log.error(msg);
		TreeSet<String> availableCommands = allCommands();
		log.info("List of available commands:\n"+StringUtils.colToStr(availableCommands, "\n"));
		System.exit(-1);
	}
		
	
	public TreeSet<String> allCommands() {
    	return new TreeSet<String>(ReflectionUtils.getPublicStaticStringFieldsWithPrefix(this.getClass(), "CMD_"));
    }
}
