package util.io;


import java.io.*;
import java.util.*;


/** Sniff files in a given dir, having the given extension */
public final class FileSniffer {

    /** for each map name, the set of timestamps that the map has
      * an associated image for
      */
    private ArrayList mFileNames;
    private String mExtension;
    private String mDirectory;

    public FileSniffer(String pDirectory) {
        this(pDirectory, null);
    }
        
    public FileSniffer(String pDirectory,
                       String pExtension) {                                   
        mDirectory = pDirectory;
        mExtension = pExtension;

        init();        
    }

    private void init() {
        // dbgMsg("init, mDirectory="+mDirectory+", mExtension="+mExtension);
        mFileNames = new ArrayList();
        // create file representing the grid directory
        File file = new File(mDirectory);
        // dbgMsg("grid file list:");
        String[] files = file.list();
        for (int i = 0; i<files.length; i++) {
            // dbgMsg(files[i]);
            if (mExtension != null) {
                // require that files have this extension
                int extensionInd = files[i].lastIndexOf(mExtension);
                if (extensionInd > 0) {
                    String name = files[i].substring(0, extensionInd);
                    mFileNames.add(name);
                }
            }    
            else {
                // just get all files, including the extension
                mFileNames.add(files[i]);
            }
        }
        // dbgMsg("sniffed "+mExtension+" files, file count = " + getCount());
    }
    
    public void refresh() {
        init();    
    }
    
    /** Get the file names, excluding the extension */
    public String[] getNames() {
        return (String[])mFileNames.toArray(new String[0]);
    }
    
    public int getCount() {
        return mFileNames.size();
    }
    
}
