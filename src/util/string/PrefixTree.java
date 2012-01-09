package util.string;

/** 
 * TODO: finish this beautiful prefix tree for ordinary strings, possibly using PatternTree as a model.
 * This will be used to rescue the GUI.
 */
public class PrefixTree {

    /*
    private Node mRoot;

    public void add(String p) {                                     
        Node deepestNode = findDeepestNode(p);           
                
        if (deepestNode.mDepth == p.length()) {
            // found a node corresponding to this String
            deepestNode.mWeight++;
            
            if (deepestNode.mIsAuxiliary) {
                // the node is an "auxiliary" node; just turn into a "real" node:                
                deepestNode.mIsAuxiliary = false;
            }                       
        }
        else {            
            // build new subtree for this node
            int depth = deepestNode.mDepth;            
            int stringLen = p.length();
            // dbgMsg("building subtree, depth="+depth);
            while(depth < stringLen) {                                                         
                double weight;
                if (depth == stringLen -1) {
                    // create the node corresponding to p...                    
                    deepestNode = deepestNode.createChild(p.charAt(depth), 1);
                }
                else {
                    // create an auxiliary node...
                    deepestNode = deepestNode.createChild(p.charAt(depth));                                        
                }                
                depth++;                
            }            
        }                                 
    }        
    
    private class Node {
        private HashMap mChildren;
        
        private int mLevel;
        private String mString;        
        private int mCount;
        private boolean mIsAuxiliary;
        private char mChar;
        
        private Node(char pChar,
                     int pLevel,
                     String pString,        
                     int pCount,
                     boolean pIsAuxiliary) {
            mChar = pChar;                         
            mLevel pLevel;
            mString = pString;       
            mCount = pCount;
            mIsAuxiliary = pIsAuxiliary;                                                  
        }    

        public Node createChild(String p) {            
            char c = p.charAt(mLevel);
            
            Node node = (Node)mChildren.get(new Character(c));
            if (node != null) {
                throw new RuntimeException("Cannot create node, as node already exists!");
            }
            
            if (mLevel+1 == p.length()) {
                // this will be the home for p
                Node node = new Node(mLevel+1, pString, 1, false);                  
            }
            else {
                // just an auxiliary node...
                Node node = new Node(mLevel+1, null, 1, false);
            }           
        }                     
        
      
        
      
    }

    */
}
