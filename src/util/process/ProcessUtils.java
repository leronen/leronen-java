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
import util.ConversionUtils;
import util.IOUtils;
import util.StringUtils;
import util.Strings;
import util.Timer;
import util.dbg.Logger;

/** Developer likes executing executables. */
public class ProcessUtils {
                  
    /** 
     * Execute a command using java's standard system command utility.
     * Command must be a "simple" command splittable into arguments by delimited white space 
     * - commands having quoted arguments wont work!
     * Use {@link ProcessOutput#exec(List)} for such cases. 
     * 
     * Use {@link ProcessOutput#getExitValue()} to check exit status.
     * (IOException is only thrown on system errors, not when executed command fails
     * with non-zero exit status)!
     * 
     * Use {@link executor()} for more versatile behavior.  
     * 
     * @return {@link ProcessOutput} object providing access to the exit value, stdout and stderr of the executed process. 
     * @throws IOException only in exceptional cases; in particular, if the process exits a non-zero value, an exception is NOT thrown. 
     */
    public static ProcessOutput exec(String command) throws IOException {
        return executor().simpleCommand(command).exec();        
    }
    
    public static ProcessOutput exec(String... command) throws IOException {
        return executor().command(command).exec();        
    }
                    
    

    public static Process executeCommand_nowait(String pCommand, String pDir) throws IOException {
        File dir = null;
        if (pDir!=null) {
            debug("Executing command: "+pCommand+" in dir "+pDir);
            dir = new File(pDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }
        else {
            debug("Executing command: "+pCommand+" in current dir");
        }

        Process proc = Runtime.getRuntime().exec(pCommand, null, dir);       
        
        debug("returning process: "+proc);
        return proc;
    }

    /** 
     * Execute a command using frankly quite poor java's standard system command utility.
     * <p>
     * Warning: commands having quoted arguments wont work! - ise {@link ProcessOutput#exec(List)} for such cases. 
     * <p>
     * Use {@link ProcessOutput#getExitValue()} to check exit status of executed command
     * (IOException is only thrown on system errors, not when executed command fails
     * with non-zero exit status)!
     * <p>
     * See {@link #executor()} for more control over the executed command.
     * See {@link #bash()} for a richer shell experience, including but not limited to pipes ("|") 
     * and process substitution ("<(command)") 
     * <p>
     * @return {@link ProcessOutput} object containing the exit value, stdout and stderr of the executed process. 
     * @throws IOException only in exceptional cases; in particular, if the process exits a non-zero value, an exception is NOT thrown!
     */
    public static ProcessOutput exec(List<String> arguments) throws IOException {
        return executor().command(ConversionUtils.stringCollectionToArray(arguments)).exec();        
    }

    /**
     * Execute a shell command (or why not even several commands) using the quite reliable bourne again shell.
     * 
     * Caller should use {@link ProcessOutput#getExitValue()} to check exit status of the executed command
     * (IOException is only thrown on system errors, not when executed command fails with non-zero exit status!).
     * 
     * See {@link #executor()} for more control over the executed command.
     * 
     * @return {@link ProcessOutput} object containing the exit value, stdout and stderr of the executed process. 
     * @throws IOException only in exceptional cases; in particular, if the process exits a non-zero value, an exception is NOT thrown!
     */
    public static ProcessOutput bash(String script) throws IOException {
        Executor executor = executor()
                .command(script)
                .bash();
        
        return executor.exec();
    }
      
    public static Executor executor() {
        return new Executor();
    }
    
    public static Executor executor(String... args) {
        return new Executor().command(args);
    }
    
    public static class Executor {
        private String[] commandArray;
        private String dir;
        private StreamListener outputStreamListener;
        private StreamListener errorStreamListener;
        private ProcessOwner processOwner;
        private boolean logCommand = true;
        private boolean timing = true;
        private boolean bash;        
        
        /** Command must be a simple command splittable into arguments simply by delimited white space. Quoted arguments wont work */
        public Executor simpleCommand(String command) {
            String[] arguments = command.split("\\s+");
            return command(arguments);
        }
        
        public Executor command(String... arguments) {
            this.commandArray = arguments;
            return this;
        }
        
        public Executor command(List<String> arguments) {
            this.commandArray = ConversionUtils.stringCollectionToArray(arguments);
            return this;
        }
        
        private Executor bash() {
            this.bash = true;
            return this;
        }
        
        private Executor bash(boolean value) {
            this.bash = value;
            return this;
        }
        
        private Executor copy() {
            Executor copy = new Executor();
            copy.commandArray = commandArray;
            copy.dir = dir;
            copy.outputStreamListener = outputStreamListener;
            copy.errorStreamListener = errorStreamListener;
            copy.processOwner = processOwner;
            copy.logCommand = logCommand;
            copy.timing = timing;
            
            return copy;
        }
        
        public Executor log(boolean value) {
            this.logCommand = value;
            return this;
        }
        
        public Executor timing(boolean value) {
            this.timing = value;
            return this;
        }
        
        
        public Executor dir(String dir) {
            this.dir = dir;
            return this;
        }

        public Executor outListener(StreamListener outputStreamListener) {
            this.outputStreamListener = outputStreamListener;
            return this;
        }

        public Executor errListener(StreamListener errorStreamListener) {
            this.errorStreamListener = errorStreamListener;
            return this;
        }

        public Executor owner(ProcessOwner processOwner) {
            this.processOwner = processOwner;
            return this;
        }
        
        public ProcessOutput exec() throws IOException {
            if (bash) {
                return ProcessUtils.bash(this);
            }
            else {
                return ProcessUtils.exec(this);
            }
        }        
    }
    
    /**
     * Executes process in given directory and waits for it's termination.
     * Uses java's own system call facility (unless Executor speficies bash flag, 
     * in which case bash is used).
     * 
     * Lines outputted by stdout and stderr of the process are returned.
     * 
     */
    private static ProcessOutput exec(Executor args) throws IOException {

        File dir = null;
        String cmdAsString = Strings.format(args.commandArray, " ");
        if (args.dir != null) {
            dir = new File(args.dir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new RuntimeException("cannot exec in dir: directory does not exist!");
            }
        }        

        Process proc = Runtime.getRuntime().exec(args.commandArray, null, dir);        

        if (args.processOwner != null) {
            args.processOwner.registerExternalProcess(proc, cmdAsString);
        }

        // store output and errors
        InputStream outStream = proc.getInputStream();
        InputStream errStream = proc.getErrorStream();
        OutputStream inStream = proc.getOutputStream();

        // we shall provide no input
        inStream.close();
        // out streams shall be closed by their respective readers...
               
        RunnableStreamReader stdoutReader = new RunnableStreamReader(outStream, args.outputStreamListener);
        RunnableStreamReader stderrReader = new RunnableStreamReader(errStream, args.errorStreamListener);

        Thread stdOutReaderThread = new Thread(stdoutReader);
        Thread stdErrReaderThread = new Thread(stderrReader);

        stdOutReaderThread.start();
        stdErrReaderThread.start();

        try {
            if (args.timing) {
                Timer.startTiming("Executing command line: " + cmdAsString);
            }
            if (args.logCommand) {
                debug("Executing command line: " + cmdAsString);
            }
            
            stdOutReaderThread.join();
            stdErrReaderThread.join();
            String[] outlist = stdoutReader.getResult();
            String[] errlist = stderrReader.getResult();

            // wait for process to terminate, just in case (although the fact that we
            // have joined with the reader streams should already guarantee this)
            proc.waitFor();
            
            // all seems to have went well, return output of process
            return new ProcessOutput(cmdAsString, outlist, errlist, proc.exitValue(), proc);
        }
        catch (InterruptedException e) {
            info("interrupted while executing command: "+cmdAsString);
            e.printStackTrace();
            info("Destroying process: "+cmdAsString);
            // try to close streams, just in case...
            closeStreamsAndDestroy(proc);            
            throw new IOException("Failed due to being interrupted while executing command " + cmdAsString + ". execution status unknown);");
        }
        finally {
            if (args.timing) {                
                Timer.endTiming("Executing command line: " + cmdAsString);
            }
        }
    }
    
    /**
     * Execute a bash script. 
     * 
     * Implementation is based on creating a tmp script file under 
     * $BCOS_BASH_TMP (defaults to /tmp/bash) and then executing the script using
     * <i>bash -c 'source <script>'</i>. Directory is created if it does not exist.
     * Script is temporarily stored into file named as: "<pid>.<threadname>.<nanos>.bash".
     * Script file is immediately deleted after execution, regardless of the outcome.
     * 
     * More elegant solutions are welcomed.
     * 
     * <b>Note</b>: failing the command with non-zero exit status DOES NOT throw an {@link IOException} instead,
     * one must check the return value of the process using {@link ProcessOutput#getExitValue()}.
     * IOException is only thrown in cases of more esoteric system errors.
     */
    private static ProcessOutput bash(Executor params) throws IOException {        
        File tmpFile = null;        
        String[] cmdArr = params.commandArray;
        if (cmdArr.length != 1) {
            throw new RuntimeException("Wrong length of command array (" + cmdArr.length + "): " + Strings.format(cmdArr));
        }
        
        if (!params.bash) {
            throw new RuntimeException("Executor does not define bash flag");
        }
        
        String script = cmdArr[0];
        
        if (params.logCommand) {
            debug("Executing bash: " + script);
        }
        
        if (params.timing) {
            Timer.startTiming("Executing bash: " + script);
        }
        
        try {
            // resolve directory and create if needed 
            String bcosBashDir = System.getenv("BCOS_BASH_TMP");
            if (bcosBashDir == null) {
                bcosBashDir = "/tmp/bash/";
            }
            File bashDir = new File(bcosBashDir);
            if (!bashDir.exists()) {
                if (!bashDir.mkdirs()) {
                    throw new IOException("Failed creating directory " + bashDir);
                }
            }
                        
            // generate script file 
            String pidStr = ManagementFactory.getRuntimeMXBean().getName();
            String threadName = Thread.currentThread().getName();
            long nanos = System.nanoTime();
            tmpFile = new File(bashDir.getPath() + "/" + pidStr + "." + threadName + "." + nanos + "." + "bash");            
            IOUtils.writeToFile(tmpFile, script + "\n");
            executor("chmod", "u+x", tmpFile.getPath())
                    .log(false)
                    .exec();
            
            // execute script using an actual executor 
            return params.copy()
                .command("/bin/bash", "-c", "source " + tmpFile.getPath())
                .log(false)
                .timing(false)
                .bash(false)
                .exec();
                        
        }
        finally {
            // burn after reading
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
            
            if (params.timing) {
                Timer.endTiming("Executing bash: " + script);
            }
        }
    }
 
    
    private static void info(String msg) {        
        Logger.info(msg);
    }       
    
    /** Try to avoid closed streams (TODO: is this hack needed in these modern days?) */
    private static void closeStreamsAndDestroy(Process proc) {
        InputStream outStream = proc.getInputStream();
        InputStream errStream = proc.getErrorStream();
        OutputStream inStream = proc.getOutputStream();
        for (Closeable stream: Arrays.asList(outStream, errStream, inStream)) {
            try {
                stream.close();
            }
            catch (IOException foo) {
                // no action
            }
        }
        
        proc.destroy();        
    }

    public static void main(String[] pArgs) throws Exception {
        String cmd = pArgs[0];
        List<String> args = CollectionUtils.tailList(Arrays.asList(pArgs), 1);
        if (cmd.equals("bash")) {
            String script = StringUtils.colToStr(args, " ");
            ProcessOutput out = ProcessUtils.bash(script);
            System.out.println(""+out.toString());
            System.exit(out.getExitValue());
        }
        else if (cmd.equals("exec")) {                       
            ProcessOutput out = ProcessUtils.exec(args);
            System.out.println(""+out.toString());
            System.exit(out.getExitValue());
        }
        else {
            System.err.println("No such command: "+cmd);
            System.exit(1);
        }
    }         
    
    /** Open pFile with pEditor. A new process is created. */
    public static void openWithEditor(File pFile, String pEditor, ProcessOwner pProcessOwner) throws IOException {
        executeCommand_nowait(pEditor+" "+pFile.getPath(), null);
    }

    private static void debug(String pMsg) {        
        Logger.dbg(pMsg);
    }
}
