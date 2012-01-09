package util.test;

import util.process.ProcessOutput;
import util.process.ProcessUtils;

public class CmdLineTest {
    
    public static void main(String[] args) throws Exception {
        ProcessOutput output = ProcessUtils.executeCommand("ls", 
                                                           ".",  // dir in which command is run
                                                           null);
        
        for (String line: output.stdoutlines) {
            System.out.println(line);
        }
    }
}
