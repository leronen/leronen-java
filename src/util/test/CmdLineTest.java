package util.test;

import util.process.ProcessOutput;
import util.process.ProcessUtils;

public class CmdLineTest {
    
    public static void main(String[] args) throws Exception {
        ProcessOutput output = ProcessUtils.exec("ls");
        
        for (String line: output.getStdOutAslist()) {
            System.out.println(line);
        }
    }
}
