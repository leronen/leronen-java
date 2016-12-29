package util.process;

import java.util.Arrays;
import java.util.List;

import util.*;

public class ProcessOutput {
    private String cmd;
    private String[] stdoutlines;
    private String[] stderrlines;
    private int exitValue;
    private Process process;
    
    public ProcessOutput(String cmd,
                         String[] stdoutlines,
                         String[] stderrlines,
                         int exitValue,
                         Process process)  {
        this.cmd = cmd;
        this.stdoutlines = stdoutlines;
        this.stderrlines = stderrlines;    
        this.exitValue = exitValue;
        this.process = process;        
    }
    
    public int getExitValue() {
        return exitValue;
    }
    
    public Process getProcess() {
        return process;
    }
    
    /** the executed command */
    public String getCmd() {
        return cmd;
    }
    
    /*
     * as a single string containing "\n":s. 
     */
    public String getStdOut() {
        return Strings.format(stdoutlines, "\n");
    }
    
    public List<String> getStdOutAslist() {
        return Arrays.asList(stdoutlines);
    }
    
    public List<String> getStdErrAslist() {
        return Arrays.asList(stderrlines);
    }
    
    /*
     * as a single string containing "\n":s. 
     */
    public String getStdErr() {
        return Strings.format(stderrlines, "\n");
    }
    
    @Override
    public String toString() {
        return "Process output:\n"+
               "  stdout:\n\t"+Strings.format(stdoutlines, "\n\t")+
               "\n"+
               "  stderr:\n\t"+Strings.format(stderrlines, "\n\t")+
               "\n"+
               "Exit value: "+exitValue;
    }
        
}
