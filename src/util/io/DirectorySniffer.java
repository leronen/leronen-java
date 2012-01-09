package util.io;

import util.*;
import util.dbg.*;

import java.io.*;
import java.util.*;

public final class DirectorySniffer {

    /** for each map name, the set of timestamps that the map has
      * an associated image for
      */
    private ArrayList mSubDirDirNames = new ArrayList();    
    private String mDirectory;
    
    private static boolean sDbg = false;
    
    public DirectorySniffer(String pDirectory) {
        // dbgMsg("creating dir sniffer for dir: "+pDirectory);
        mDirectory = pDirectory;             
        init();              
    }

    private void init() {
        // create file representing the directory
        File dir = new File(mDirectory);
        dbgMsg("sniffing file list of directory: "+mDirectory);
        String path = dir.getPath();
        dbgMsg("Path: "+path);
        String name = dir.getName();
        dbgMsg("Name: "+name);
        String[] files = dir.list();
        dbgMsg("Found "+files.length+" files.");
        dbgMsg ("files in dir "+mDirectory+":\n"+StringUtils.arrayToString(files,"\n")); 
        
        for (int i = 0; i<files.length; i++) {
            String dirName = StringUtils.chop(mDirectory, '/')+'/';
            // dbgMsg("looking for sub dirs in dir: "+dirName);
            String filename = dirName+files[i];
            File file = new File(filename);
            if (file.exists() && file.isDirectory()) {
                // dbgMsg("found dir: "+filename);                  
                mSubDirDirNames.add(files[i]);
            }    
            else {
                if(!file.exists()) {
                    // dbgMsg("file does not exist: "+filename);
                }
                else {
                    // dbgMsg("not a dir: "+filename);
                }
            }                        
        }
        // dbgMsg("sniffed sub directories in directory "+ mDirectory+", dir count = " + getSubdirCount());    
    }
    
    public void refresh() {
        mSubDirDirNames.clear();
        init();    
    }
    
    public String[] getSubdirNames() {
        return (String[])mSubDirDirNames.toArray(new String[mSubDirDirNames.size()]);
    }
    
    public boolean exists(String pName) {
        return mSubDirDirNames.contains(pName);    
    }
    
    public int getSubdirCount() {
        return mSubDirDirNames.size();
    }
    
    private static void dbgMsg(String pMsg) {
        if (sDbg) {
            Logger.dbg("DirectorySniffer: "+pMsg);
        }
    }
    
    public static void main (String[] args) {
        String dir = null;
        if (args.length==0) {
            dir = ".";
        }
        else {
            dir = args[0];
        }
        DirectorySniffer sniffer = new DirectorySniffer(dir);
        String[] subdirNameArr = sniffer.getSubdirNames();
        String subdirNames = StringUtils.arrayToString(subdirNameArr, "\n");
        System.out.println(subdirNames);
    }
    
    
}
