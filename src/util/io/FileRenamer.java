package util.io;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import util.Initializable;
import util.StringUtils;
import util.Utils;
import util.converter.Converter;
import util.dbg.Logger;

public class FileRenamer {
    
    public static void main(String[] args) throws Exception {        
        File dir = new File(args[0]);
        Class converterClass = Class.forName(args[1]);
        Converter<String, String> converter = (Converter)converterClass.newInstance();
        
        if (args.length >= 3) {
            String initString = args[2];
            ((Initializable)converter).init(initString);
        }        
        
        File[] files = dir.listFiles();
        
        // Map<String, String> mapping = new HashMap();
        Set<String> oldNames = new HashSet();
        Set<String> newNames = new HashSet();               
                
        // make a list of file names already in the dir
        for (File file: files) {
            String oldName = file.getName();
            oldNames.add(oldName);
        }         
        
        // first, check that files would map to unambiguous and non-existent names:
        for (File file: files) {
            String oldName = file.getName();
            String newName = converter.convert(oldName);
            if (oldNames.contains(newName)) {
                Utils.die("File already exists (mapping: "+oldName+" => "+newName+")");
            }            
            if (newNames.contains(newName)) {
                Utils.die("Duplicate mapping: "+oldName+" => "+newName);
            }
            if (StringUtils.isEmpty(newName)) {
                Utils.die("Maps to an empty name: "+oldName);
            }            
            // mapping.put(oldName, newName);
        }
        
        // Finally, do the actual renaming
        for (File file: files) {
            String oldName = file.getName();
            String newName = converter.convert(oldName);             
            Logger.info("Renaming: "+oldName+" => "+newName);
            file.renameTo(new File(newName));
        }
    }
    
    
    
}
