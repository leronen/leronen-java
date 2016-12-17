package util.process;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import util.CollectionUtils;
import util.IOUtils;
import util.StringUtils;
import util.dbg.Logger;

/** Lauri's version of tools for executing executables. Originates from my tried and tested previous libraries */
public class ProcessUtils {

    private static int sExecCount = 0;
    private static int sDestroyCount = 0;

    public static int getExecCount() {
        return sExecCount;
    }

    public static int getDestroyCount() {
        return sDestroyCount;
    }

    /** Open pFile with pEditor. A new process is created. */
    public static void openWithEditor(File pFile, String pEditor, ProcessOwner pProcessOwner) throws IOException {
        executeCommand_nowait(//"rxvt -e "+
                              pEditor+" "+pFile.getPath(), null, pProcessOwner);
    }
    

    public static Process executeCommand_nowait(String pCommand, String pDir, ProcessOwner pProcessOwner) throws IOException {
        File dir = null;
        if (pDir!=null) {
            dbgMsg("Executing command: "+pCommand+" in dir "+pDir);
            dir = new File(pDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }
        else {
            dbgMsg("Executing command: "+pCommand+" in current dir");
        }
//        ProcessBuilder pb = new ProcessBuilder(pCommand);
//        if (dir != null) {
//        	pb.directory(dir);
//        }
//        Logger.info("Envinronment of the processbuilder:\n"+
//        		    StringUtils.mapToString(pb.environment()));
//        Process proc = pb.start();

        Process proc = Runtime.getRuntime().exec(pCommand, null, dir);
        sExecCount++;

        if (pProcessOwner != null) {
            pProcessOwner.registerExternalProcess(proc, pCommand);
        }
        dbgMsg("returning process: "+proc);
        return proc;
    }

    public static ProcessOutput executeCommand(String pCommand,
                                               ProcessOwner pOwner) throws IOException {
        return executeCommand(pCommand,
                              null, // no work dir
                              pOwner);
    }

    public static ProcessOutput executeCommand(String pCommand) throws IOException {
        return executeCommand(pCommand,
                null, // no work dir
                null, // no listener for stdout
                null, // no listener for stderr
                null); // no process owner
    }


    public static ProcessOutput bash(String script) throws IOException {
    	File tmpFile = null;
    	try {
	    	String pidStr = ManagementFactory.getRuntimeMXBean().getName();
	    	String threadName = Thread.currentThread().getName();
	    	long nanos = System.nanoTime();
	    	tmpFile = new File(pidStr+"."+threadName+"."+nanos+"."+"processutils.executeBash.tmp");
	    	script = "#!/bin/bash\n"+script+"\n";
	    	IOUtils.writeToFile(tmpFile, script);
	    	executeCommand("chmod u+x "+tmpFile);
	    	ProcessOutput out = executeCommand(tmpFile.getPath());
	    	return out;
    	}
    	finally {
    		if (tmpFile != null && tmpFile.exists()) {
    			tmpFile.delete();
    		}
    	}
    }

    public static ProcessOutput executeCommand(String[] cmdArr) throws IOException {
        return executeCommand(cmdArr,
                			  null, // no work dir
                			  null, // no listener for stdout
                			  null, // no listener for stderr
                			  null,
                			  false,
                			  false); // no process owner

    }

    /** see below for explanation */
    public static ProcessOutput executeCommand(String pCommand,
                                               String pDir,
                                               ProcessOwner pProcessOwner) throws IOException {
        return executeCommand(pCommand,
                              pDir,
                              null, // no listener for stdout
                              null, // no listener for stderr
                              pProcessOwner);
    }

    /**
     * Executes process in directory pDir and waits for it's termination.
     * Lines outputted by stdout and stderr of the process are returned.
     * the listeners may be null.
     */
    public static ProcessOutput executeCommand(String pCommand,
                                               String pDir,
                                               StreamListener pOutputStreamListener,
                                               StreamListener pErrorStreamListener,
                                               ProcessOwner pProcessOwner) throws IOException {
        return executeCommand(pCommand,
                              pDir,
                              pOutputStreamListener,
                              pErrorStreamListener,
                              pProcessOwner,
                              false,  // Do not output stdout
                              false); // Do not output stderr
    }


    /**
     * Executes process in give directory and waits for it's termination.
     *  Lines outputted by stdout and stderr of the process are returned.
     * the listeners may be null.
     */
    public static ProcessOutput executeCommand(String pCommand,
                                               String pDir,
                                               StreamListener pOutputStreamListener,
                                               StreamListener pErrorStreamListener,
                                               ProcessOwner pProcessOwner,
                                               boolean pOutputStdOut,
                                               boolean pOutputStdErr) throws IOException {
    	String[] cmdArr = pCommand.split("\\s+");
    	return executeCommand(cmdArr, pDir, pOutputStreamListener, pErrorStreamListener, pProcessOwner, pOutputStdOut, pOutputStdErr);
    }

    /**
     * Executes process in give directory and waits for it's termination.
     *  Lines outputted by stdout and stderr of the process are returned.
     * the listeners may be null.
     */
    public static ProcessOutput executeCommand(String[] pCommandArray,
                                               String pDir,
                                               StreamListener pOutputStreamListener,
                                               StreamListener pErrorStreamListener,
                                               ProcessOwner pProcessOwner,
                                               boolean pOutputStdOut,
                                               boolean pOutputStdErr) throws IOException {
    	File dir = null;
    	String cmdAsString = StringUtils.arrayToString(pCommandArray, " ");
        if (pDir!=null) {
            dbgMsg("Executing command: "+cmdAsString+" in dir "+pDir);
            dir = new File(pDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }
        else {
            dbgMsg("Executing command: "+cmdAsString+" in current dir");
        }

        Process proc = Runtime.getRuntime().exec(pCommandArray, null, dir);
        sExecCount++;

        if (pProcessOwner != null) {
            pProcessOwner.registerExternalProcess(proc, cmdAsString);
        }

        // store output and errors
        InputStream outStream = proc.getInputStream();
        InputStream errStream = proc.getErrorStream();
        OutputStream inStream = proc.getOutputStream();
        // we shall provide no input: (!)
        inStream.close();
        // out streams shall be closed by their respective readers...

        String arg0 = pCommandArray[0];
        String commandName = new File(arg0).getName();
        RunnableStreamReader stdoutReader = new RunnableStreamReader(commandName, "stdout", outStream, pOutputStreamListener, pOutputStdOut);
        RunnableStreamReader stderrReader = new RunnableStreamReader(commandName, "stderr", errStream, pErrorStreamListener, pOutputStdErr);

        Thread stdOutReaderThread = new Thread(stdoutReader);
        Thread stdErrReaderThread = new Thread(stderrReader);

        stdOutReaderThread.start();
        stdErrReaderThread.start();

        try {
            stdOutReaderThread.join();
            stdErrReaderThread.join();
            String[] outlist = stdoutReader.getResult();
            String[] errlist = stderrReader.getResult();

            // print debug info
            // Logger.dbg("**************** stdout of the executed process: *********************");
            // dbgMsg(arrayToString(outlist, "\n"));
            // Logger.dbg("**************** strerr of the executed process: *********************");
            // dbgMsg(arrayToString(errlist, "\n"));

            // wait for process to terminate, just in case...
            proc.waitFor();
            // all seems to have went well, return output of process
            dbgMsg("finished executing command: "+cmdAsString);
            // dbgMsg("Returning process output...");
            return new ProcessOutput(outlist, errlist, proc.exitValue(), proc);
        }
        catch (InterruptedException e) {
            Logger.info("interrupted while executing command: "+cmdAsString);
            e.printStackTrace();
            Logger.info("Destroying process: "+cmdAsString);
            // try to close streams, just in case...
            closeStreamsAndDestroy(proc);
//            outStream = proc.getInputStream();
//            errStream = proc.getErrorStream();
//            inStream = proc.getOutputStream();
//            for (Closeable stream: CollectionUtils.makeArrayList(outStream, errStream, inStream)) {
//                try {
//                    stream.close();
//                }
//                catch (IOException foo) {
//                    // foo
//                }
//            }
//            proc.destroy();
            Logger.info("Process should rest in peace now.");
            Logger.warning("Returning null, as we failed to complete the processing due to the irritating interruption.");
            return null;
        }
    }

    /** Try to avoid closed streams using this... */
    public static void closeStreamsAndDestroy(Process proc) {
        InputStream outStream = proc.getInputStream();
        InputStream errStream = proc.getErrorStream();
        OutputStream inStream = proc.getOutputStream();
        for (Closeable stream: CollectionUtils.makeArrayList(outStream, errStream, inStream)) {
            try {
                stream.close();
            }
            catch (IOException foo) {
                // foo
            }
        }
        proc.destroy();
        sDestroyCount++;
    }

    public static void main(String[] pArgs) throws Exception {
    	String cmd = pArgs[0];
    	List<String> args = CollectionUtils.tailList(Arrays.asList(pArgs), 1);
    	if (cmd.equals("bash")) {
    		String script = StringUtils.collectionToString(args, " ");
        	ProcessOutput out = ProcessUtils.bash(script);
        	System.out.println(""+out.toString());
        	System.exit(out.exitValue);
    	}
    	else {
    		System.err.println("No such command: "+cmd);
    		System.exit(1);
    	}
    }

    private static void dbgMsg(String pMsg) {
        Logger.dbg("ProcessUtils: "+pMsg);
    }


}
