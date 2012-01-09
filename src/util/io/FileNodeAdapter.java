package util.io;

import java.io.*;
import java.util.*;

/**
 *  Adapts File to general-purpose tree utility methods 
 *
 *  Note that file must be a canonical file to work!
 */
public class FileNodeAdapter implements util.collections.tree.NodeAdapter {
    
    public Object firstChild(Object pNode) {
        // at least currently not needeed
        throw new UnsupportedOperationException();       
    }
    public Object nextBrother(Object pNode) {
        // at least currently not needeed
        throw new UnsupportedOperationException();
    }           
    public Object parent(Object pNode) {
        File file = (File)pNode;
        return file.getParentFile();
    }
    public List children(Object pNode) {
        throw new UnsupportedOperationException();
    }           
}
