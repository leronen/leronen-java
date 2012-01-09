package util.dbg;


import util.*;
import util.collections.*;

import java.io.*;

import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.tree.*;

public final class Log {
                        
    public static final String SECTION_START = "#START_SECTION_";
    public static final String SECTION_END = "#END_SECTION_";
    
    public static final String SEPARATOR = "_";        
        
    private static Pattern sSectionStartPattern = Pattern.compile("^"+SECTION_START+"(\\d+)"+SEPARATOR+"(.*)$");
    private static Pattern sSectionEndPattern = Pattern.compile("^"+SECTION_END+"(\\d+)"+SEPARATOR+"(.*)$");
    // public static Pattern sErrorPattern = Pattern.compile("("+Logger.ERROR_PREFIX+")|(Exception)");
    public static Pattern sErrorPattern = Pattern.compile(".*?"+Logger.ERROR_PREFIX+".*");
    public static Pattern sExceptionPattern = Pattern.compile(".*?Exception.*");
            
    private Block mRootBlock = new Block(0, null);
    private Block mCurrentBlock = mRootBlock;

    private String mFirstErrorLine;
    private DefaultMutableTreeNode mFirstErrorNode;

    private File mFile;                                         
    
    public Log(String pFileName) throws IOException {
        this(new File(pFileName));        
    }    
    
    public Log(File pFile) throws IOException {
        // mFile = new File(pFileName);
        mFile = pFile;
        mRootBlock = new Block(0, null);
        mCurrentBlock = mRootBlock;
        
        String[] lines = IOUtils.readLineArray(pFile);                
        for (int i=0; i<lines.length; i++) {            
            Matcher startMatcher = sSectionStartPattern.matcher(lines[i]);
            Matcher endMatcher = sSectionEndPattern.matcher(lines[i]);
                                    
            if (startMatcher.matches()) {                                    
                // block start
                int level = Integer.parseInt(startMatcher.group(1));
                String msg = startMatcher.group(2);
                startSection(level, msg);                
            }
            else if (endMatcher.matches()) {
                // block start                                
                int level = Integer.parseInt(endMatcher.group(1));
                // String msg = endMatcher.group(2);
                endSection(level);
            }                
            else {
                // ordinary line
                mCurrentBlock.add(lines[i]);
             
                // remember the line of first error, if any
                if (mFirstErrorLine == null && 
                    (sErrorPattern.matcher(lines[i]).matches() || sExceptionPattern.matcher(lines[i]).matches())) {                
                    mFirstErrorLine = lines[i];                                                    
                }                              
            }                                
        }                         

        if (mFirstErrorLine == null) {
            Logger.info("No errors found in log.");
        }
        else {
            Logger.info("First error line in log: "+mFirstErrorLine);
        }                 
    }        
            
    public File getFile() {
        return mFile;
    }
    
    /** 
      * Each pair contains as the first element the block name, and as 
      * the second element the time taken up by that block 
      */
    public Pair[] getTimeHistogram() {
        List topLevelBlocks = ReflectionUtils.extractObjectsOfClass(Block.class, mRootBlock.mData);
        
        Long prevBlockStart = null;
        String prevBlockText = null;
        
        Pair[] textTimePairs = new Pair[topLevelBlocks.size()];
        
        
        dbgMsg("Top level blocks:");
        for (int i=0; i<topLevelBlocks.size(); i++) {
            Block block = (Block)topLevelBlocks.get(i);
            String msg = block.mMsg;
            dbgMsg("msg: "+msg);
            
            Pattern p = Pattern.compile("^(.*?)date=(.*?)\\.\\.\\.$");
            Matcher m = p.matcher(msg);
            Long timeTaken = null;
            if (m.matches()) {
                String textPart = m.group(1);                
                String datePart = m.group(2);
                dbgMsg("datePart:<"+datePart+">");
                try {
                    long millis = DateUtils.parseDateIntoMillis(datePart);                    
                    if (prevBlockStart != null) {
                        timeTaken = new Long((millis - prevBlockStart.longValue())/1000);                                
                    }                                
                    prevBlockStart = new Long(millis);
                }
                catch (java.text.ParseException e) {
                    dbgMsg("Could not parse date: "+datePart);       
                }
                textTimePairs[i] = new Pair(prevBlockText, timeTaken);

                prevBlockText = textPart;                
            }
            else {
                dbgMsg("did not match.");
                textTimePairs[i] = new Pair(msg, null);
            }
        }         
        return textTimePairs;                           
    }
    
    private void startSection(int pLevel, String pMsg) {            
        if (mCurrentBlock.mLevel+1 == pLevel) {
            // start a lower-pLevel block -> create sub block
            Block subBlock = new Block(pLevel, mCurrentBlock, pMsg);
            mCurrentBlock.add(subBlock);
            mCurrentBlock = subBlock;                    
        }
        else if (mCurrentBlock.mLevel == pLevel) {
            // start another block on the same pLevel
            Block siblingBlock = new Block(pLevel, mCurrentBlock.mParentBlock, pMsg);
            if (mCurrentBlock.mParentBlock == null) {
                throw new RuntimeException("trying to add sibling, but no parent! Block:  "+mCurrentBlock);
            }
            mCurrentBlock.mParentBlock.add(siblingBlock);
            mCurrentBlock = siblingBlock;
        }
        else if (mCurrentBlock.mLevel > pLevel) {
            // start a higher-pLevel block
            Block ancestor = mCurrentBlock.findAncestorOfLevel(pLevel);
            Block newBlock = new Block(pLevel, ancestor, pMsg);
            ancestor.add(newBlock);
            mCurrentBlock = newBlock;                                        
        }
        else {
            // pLevel > mCurrentBlock.pLevel + 1
            throw new RuntimeException("Cannot create block of pLevel: "+pLevel+", as current pLevel is: "+mCurrentBlock.mLevel);
        }        
    }
    
   private void endSection(int pLevel) {                    
        if (mCurrentBlock.mLevel == pLevel) {
            // end current block
            mCurrentBlock = mCurrentBlock.mParentBlock;                            
        }
        else if (mCurrentBlock.mLevel > pLevel) {
            // end an ancestor block                    
            Block ancestor = mCurrentBlock.findAncestorOfLevel(pLevel);
            mCurrentBlock = ancestor.mParentBlock;
        }
        else {
            // mCurrentBlock.mLevel < pLevel, trying to end and non-existent block
            // no need for action            
            // throw new RuntimeException("Cannot end section of level: "+pLevel+", as the current level is mCurrentBlock.mLevel");
        }        
    }
          
    
    private class Block {
        private int mLevel;
        private String mMsg;
        private ArrayList mData;
        private Block mParentBlock;               
        
        Block(int pLevel, Block pParentBlock) {
            mMsg = "";
            mLevel = pLevel;
            mParentBlock = pParentBlock;
            mData = new ArrayList();
        }
        
                
        Block(int pLevel, Block pParentBlock, String pMsg) {
            mMsg = pMsg;
            mLevel = pLevel;
            mParentBlock = pParentBlock;
            mData = new ArrayList();
        }            
        
        public String toString() {
            return "("+mLevel+") "+mMsg;    
        }        
                        
        void add(Block pSubBlock) {
            mData.add(pSubBlock);
        }
        
        void add(String pLine) {
            int maxLine = 160;
            if (pLine.length() <= maxLine) {
                mData.add(pLine);
            }
            else {
                String[] lines = StringUtils.splitToSegmentsOfLen(pLine, maxLine);
                mData.addAll(Arrays.asList(lines));
            }
        }
        
        public Block findAncestorOfLevel(int pLevel) {
            Block ancestor = mParentBlock;
            while (ancestor.mLevel > pLevel) {
                ancestor = ancestor.mParentBlock;                    
            }
            return ancestor;
        }        
    }
    
    
    public JTree asJTree() {
        return createTree();                    
    }
    
    public DefaultMutableTreeNode getFirstErrorNode() {
        return mFirstErrorNode;
   }
    
    private JTreeWrapper createTree() {                       
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(mRootBlock);
        createChildren(root);
        JTreeWrapper tree = new JTreeWrapper(root);            
        return tree;                    
    }    
    
    private void createChildren(DefaultMutableTreeNode pNode) {                            
        Iterator children = ((Block)pNode.getUserObject()).mData.iterator();    
        while (children.hasNext()) {
            Object child = children.next();            
            DefaultMutableTreeNode swingChild = new DefaultMutableTreeNode(child);
            if (child == mFirstErrorLine) {
                mFirstErrorNode = swingChild;
            }            
            if (child instanceof Block) {                                                    
                createChildren(swingChild);                                    
            }                                
            pNode.add(swingChild);
        }                        
    }
    
    private class JTreeWrapper extends JTree {                        
        
        public JTreeWrapper(DefaultMutableTreeNode pRoot) {            
            super(pRoot);    
        }                                                                                                      
                        
    }    
    
    public static void main (String[] args) {
        try {
            Log log = new Log(args[0]);
            Pair[] times = log.getTimeHistogram();
            for (int i=0; i<times.length; i++) {
                dbgMsg("block: "+times[i].getObj1()+", time: "+times[i].getObj2());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }                
        
    }
        
    private static void dbgMsg(String pMsg) {
        Logger.dbg("Log: "+pMsg);
    }
        
        
        
}
