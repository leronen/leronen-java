package util.collections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.TreeSet;

import util.CmdLineArgs;
import util.ReflectionUtils;
import util.StringUtils;
import util.Utils;
import util.dbg.Logger;

public class BitSetUtils {
    
    public static final String CMD_SET_MINUS = "set_minus";
    
    /** Write to stdout */
    public static void setMinus(File pFile1, File pFile2) throws IOException {
        BitSet set1 = readBitSet(pFile1);
        BitSet set2 = readBitSet(pFile2);
        set1.andNot(set2);        
        for (int i = set1.nextSetBit(0); i >= 0; i = set1.nextSetBit(i+1)) {
             System.out.println(i);     
        }
    }
    
    private static BitSet readBitSet(File pFile) throws IOException {
        return readBitSet(new FileInputStream(pFile));
    }
    
    /** Read from a file */ 
    private static BitSet readBitSet(InputStream pStream) throws IOException {
        BitSet result = new BitSet();
        BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
        String line = reader.readLine();
        while (line!=null) {                        
            int value = Integer.parseInt(line);
            if (value < 0) {
                throw new RuntimeException("Does not support negative values!");
            }
            result.set(value);
            line = reader.readLine();
        }
        reader.close();
        
        return result;    
    }
    
    public static void main(String[] args) throws Exception {        
                
        CmdLineArgs argParser = new CmdLineArgs(args);
        
        if (args.length == 0) {
            usageAndExit("First argument must be a command.");
        }
                                  
        String cmd = argParser.shift("cmd");
        args = argParser.getNonOptArgs();
        
        if (cmd.equals(CMD_SET_MINUS)) {
            File file1 = argParser.shiftFile();
            File file2 = argParser.shiftFile();
            setMinus(file1, file2);
            // Logger.endLog();
        }
        else {           
             Utils.die("Unknown command: "+cmd+".\n"+
                       "List of possible commands:\n"+
                       StringUtils.collectionToString(allCommands()));                                          
        }        
               
        // always do this nuisance        
    }
    
    private static void usageAndExit(String pErrMsg) {              
        Logger.error(pErrMsg);
        TreeSet<String> availableCommands = allCommands();
        Logger.info("List of available commands:\n"+StringUtils.collectionToString(availableCommands));
        Logger.endLog();
        System.exit(-1);        
    }
        
    public static TreeSet allCommands() {
        return new TreeSet(ReflectionUtils.getPublicStaticFieldsWithPrefix(BitSetUtils.class, "CMD_", true).values());
    }
}
