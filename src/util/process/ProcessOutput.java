package util.process;

import java.util.Arrays;
import java.util.List;

import util.*;

public class ProcessOutput {
    public String[] stdoutlines;
    public String[] stderrlines;
    public int exitValue;
    public Process theProcess;
    
    public ProcessOutput(String[] pStdoutlines,
                         String[] pStderrlines,
                         int pExitValue,
                         Process pTheProcess)  {
        stdoutlines = pStdoutlines;
        stderrlines = pStderrlines;    
        exitValue = pExitValue;
        theProcess = pTheProcess;        
    }
    
    /*
     * as a single string containing "\n":s. 
     */
    public String getStdOut() {
        return StringUtils.arrayToString(stdoutlines, "\n");
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
        return StringUtils.arrayToString(stderrlines, "\n");
    }
    
    public String toString() {
        return "Process output:\n"+
               "  stdout: \n\t"+StringUtils.arrayToString(stdoutlines, "\n\t")+
               "\n"+
               "  stderr: \n\t"+StringUtils.arrayToString(stdoutlines, "\n\t")+
               "\n"+
               "Exit value:"+exitValue;
    }
        
}
