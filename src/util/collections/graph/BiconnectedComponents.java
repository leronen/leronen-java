package util.collections.graph;

import java.io.*;
import java.util.*;

import util.Utils;
import util.collections.IPair;
import util.collections.MultiMap;
import util.dbg.Logger;

/**
 * Finds articulation points and biconnected components
 * in a UNDIRECTED graph, and uses them to filter away
 * nodes that are not found on any acyclic path between a given pair
 * of nodes s and t. 
 *  
 * Currently:
 *   - reads graph from stdin
 *   - takes s and t nodes as cmd-line args $1 and $2
 *   - writes the set of nodes TO BE PRESERVED
 * 
 * Of course, this is just for debug purposes. I imagine the main use would
 * be to filter bmgraphs, both stand-alona and as part of the crawler.
 * I imagine these functionalities shall be implemented shortly. 
 *  
 * Parametrized by the class used to represent the nodes.
 *
 * Original author: Hyung-Joon Kim. 
 * Customized by Lauri.
 */
public class BiconnectedComponents<T>  {
   
   private Map<T, NodeWrapper> mVertices;   
   private int numEdges;  // total number of edges in a graph
   private int numBCC;  // number of biconnected components in a graph
   private int dfsCounter = 1;  // will be used to assign dfsNum to each node
   
   private IUndirectedGraph <T> mInputGraph;
   
   /** Store the articulation points here. */
   private ArrayList<NodeWrapper> mArticPointList;
   
   /** Store the biconnected components here */
   private ArrayList<BCC> mBccList;   
   
   private LinkedList<Edge> stackVisited;  // a stack in which all visited edges are stored during DFS
         
   /**
    * @param pData a NON-directed graph (somewhat confusingly represented
    * by IDirectedGraph...); todo: refactor away such oddities...
    */   
    public BiconnectedComponents(IUndirectedGraph<T> pData) {
        mVertices = new HashMap();       
        mArticPointList = new ArrayList();
        mBccList = new ArrayList();
        stackVisited = new LinkedList();
        mInputGraph = pData;
        Set<IPair<T,T>> edges = mInputGraph.edges();
        for (IPair<T,T> edge: edges) {
            addEdge(edge.getObj1(), edge.getObj2());
        }   
    }
   
   private NodeWrapper getOrCreateNodeWrapper(T pNode) {
       NodeWrapper v = mVertices.get(pNode);
       if (v == null) {
           v = new NodeWrapper(pNode);
           // Logger.info("Created vertex: "+v);
           mVertices.put(pNode, v);
       }      
       return v;
   }
   
    /**
     * Add an edge to the adjacent edge list of vertices while reading pairs
     * from the input. 
     * 
     * @param pU one node incident to the edge
     * @param pV the other vertex incident to the edge
     */
    public void addEdge(T pU, T pV) {               
    	NodeWrapper u = getOrCreateNodeWrapper(pU);
    	NodeWrapper v = getOrCreateNodeWrapper(pV);      
    	// Add the edge to the adjacent edge list of both vertices
    	u.neighborList.add(pV);
    	v.neighborList.add(pU);
    	numEdges++;       
    }
   
    /**
     * Using recursive DFS, find articulation points and biconnected components
     * in a graph. While doing DFS, all information needed to find artic. point
     * and BCC. will be stored and updated. And also, as soon as an artic. point
     * is found, it will build a list of all edges in each BCC rooted at the
     * artic. point by using a stack of global scope.
     * @param v the vertex at which DFS will be performed.
     */
    public void doArticPointDFS(NodeWrapper v) {
      
        // Set DFS info of node 'v'      
        v.dfsnum = dfsCounter++;
        v.low = v.dfsnum;
      
    	///////////////////////////////////////////////////////////////////////
    	// The followings are only for eye-debugging purpose
    	//
    	// Create a sorted set of neighbors 'x' of vertex 'v' so that DFS can
    	// visit edge (v,x) in order. 
    	//SortedSet neighbors = new TreeSet();
    	//Iterator eIter = v.eList.iterator();
    	//while(eIter.hasNext()) {
    	//   neighbors.add(eIter.next());  // guaranteed log(n) time by Java Lib. 
    	//}
    	//Iterator xIter = neighbors.iterator();      
    	///////////////////////////////////////////////////////////////////////
          
        for (T neighbor: v.neighborList) {            
            NodeWrapper x = mVertices.get(neighbor);         
            if (x.dfsnum == -1) {  // x is undiscovered
                x.dfslevel = v.dfslevel + 1;
                v.numChildren = v.numChildren + 1;            
                stackVisited.push(new Edge(v, x));  // add this edge to the stack        
                
                doArticPointDFS(x);  // recursively perform DFS at children nodes
                
                v.low = Math.min(v.low, x.low);
                
                if (v.dfsnum == 1) {
                    // Special Case for root
                    // Root is an artic. point iff there are two or more children   
                    if (v.numChildren >=2) {
                        if (!mArticPointList.contains(v)) {
                            mArticPointList.add(v);                      
                        }                                   
                    }
                    retrieveBCCEdges(v, x);
                }
                else{
                    if (x.low >= v.dfsnum) {
                        // v is artic. point seperating x. That is, children of v
                        // cannot climb higher than v without passing through v.
                        if (!mArticPointList.contains(v)) {
                            mArticPointList.add(v);                      
                        } 
                        retrieveBCCEdges(v, x);
                    }
                }
            }
            else if (x.dfslevel < v.dfslevel - 1) {   
                // x is at a lower level than the level of v's parent.
                // equiv. (v,x) is a back edge
                v.low = Math.min(v.low, x.dfsnum);           
                stackVisited.push(new Edge(v, x)); // add the back edge to the stack        
            }         
        }     
    }
   
    /**
     * Retrieve all edges in a biconnected component. Since DFS is a recursive
     * algorithm, using a global stack, all edges in a bicconected component
     * can be traced back whenever each articulation point is found. If v is
     * an artic. point, all edges of the subtree rooted at v can be retrieved
     * from the top of the stack down to (v,x) where (v,x) is the last edge
     * incident to the artic. point.
     * @param v articulation point
     * @param x child node of the articulation point
     */
    public void retrieveBCCEdges(NodeWrapper v, NodeWrapper x) {      
        // Whenever this method is called, a new biconected component must be
        // found. So, number of BCC. increment by 1 and create a new instance of
        // BCC.
        numBCC++;
        BCC bcc = new BCC(numBCC);     
      
    	Edge top = (Edge)stackVisited.peek();      
    	// Until the top of the stack is (v,x), trace back and build a list of
    	// all edges in each BCC. 
    	while (!top.equal(v, x)) {
    	    Edge e = (Edge)stackVisited.pop();
    	    bcc.edgeList.add(e);
    	    top = (Edge)stackVisited.peek();         
    	}      
    	Edge e = (Edge)stackVisited.pop();      
    	bcc.edgeList.add(e);      
    	mBccList.add(bcc);  // add the BCC to a list
    }      
    
    /**
     * Show all the results of DFS.
     */
    public void showResult() {
    	System.out.println("       Total number of vertices : "+mVertices.size());
    	System.out.println("          Total number of edges : "+numEdges);      
    	System.out.println("  Number of articulation points : "+mArticPointList.size());
    	System.out.print("    List of articulation points : ");  
    	if (mArticPointList.isEmpty()) {
    	    System.out.print("No articulation point in the graph.");
    	}
    	else {
    	    Iterator iter1 = mArticPointList.iterator();
    	    while (iter1.hasNext()) {
    		NodeWrapper v = (NodeWrapper)iter1.next();
    		System.out.print(v.wrappedNode + " ");
    	    }
    	}           
    	System.out.println("\nNumber of biconnected component : "+numBCC);
    	System.out.print("Edges in each biconnected component : ");
    	if (mArticPointList.isEmpty()) {
    	    System.out.print("The graph is biconnected since no articulation point exists.");                  
    	}
    	
	    for (int i=0; i<mBccList.size(); i++) {
    		BCC bcc = (BCC)mBccList.get(i);
    		System.out.print("\n  Component "+bcc.id+" : ");
    		Iterator eIter = bcc.edgeList.iterator();
    		while (eIter.hasNext()) {
    		    Edge e = (Edge) eIter.next();
    		    System.out.print(e.getEdgeString()+" ");
    		}
        }
        
    	System.out.println();
    	// logStatistics()
    }
      
    //////////////////////////////////////////////////////////////////////////
    // Inner classes - Vertex, Edge, BCC - are data structures for a graph. 
    //////////////////////////////////////////////////////////////////////////   
    class NodeWrapper {     
    	T wrappedNode;
    	int low;  // lowest tree level reachable from this vertex
    	int dfsnum;
    	int dfslevel;   // tree level of this vertex in DFS
    	int numChildren;
    	LinkedList<T> neighborList;  // list of edges incident to this vertex
        Set<BCC> mBCCSet;
        
    	// Create a vertex with given ID number
    	NodeWrapper(T pNode) {         
    	    wrappedNode = pNode;
    	    dfsnum = -1;  // initially undiscovered
    	    neighborList = new LinkedList();  // create empty list for adjacency edges
            mBCCSet = new HashSet(2);
    	}     
      
    	public String toString() {
    	    return ""+wrappedNode;
    	}
    }         
    
    /**
     * Given some s-t-path, compute the set of nodes that are on at least 
     * one acyclic s-t-path (of course not necessarily the same as the
     * initially found path.
     */
    public Set<T> getNodesOnAcyclicSTPaths(T pSrc, T pTgt) {
        Set<T> result = new HashSet();        
    
        List<T> path = GraphUtils.findPath_bfs(mInputGraph, pSrc, pTgt);
        // Logger.info("Path: "+path);       
        
        if (path == null) {
            // No paths at all
            return Collections.EMPTY_SET;
        }
        
        // a clumsy implementation indeed follows:
        Set<T> pathNodeSet = new HashSet(path);
        MultiMap<BCC, T> visitedNodesByBCC = MultiMap.makeHashMapBasedMultiMap();
        
        // Logger.info("*********************************************************");
        // Logger.info("There are "+mBccList.size()+" biconnected components.");
        
        for (BCC bcc: mBccList) {
            for (Edge edge: bcc.edgeList) {                
                T u = edge.u.wrappedNode;
                T v = edge.v.wrappedNode;
                if (pathNodeSet.contains(u)) {
                    visitedNodesByBCC.put(bcc, u);
                }
                if (pathNodeSet.contains(v)) {
                    visitedNodesByBCC.put(bcc, v);
                }                                
            }
        }
        
        for (BCC bcc: visitedNodesByBCC.keySet()) {
            if (visitedNodesByBCC.get(bcc).size() >= 2) {
                // at least 2 nodes visited in this bcc, whole BCC will 
                // be part or the result
                for (Edge edge: bcc.edgeList) {                
                    T u = edge.u.wrappedNode;
                    T v = edge.v.wrappedNode;
                    result.add(u);
                    result.add(v);
                }
            }
        }
        
        Logger.dbg("There are "+result.size()+" nodes on acyclic paths");
        return result;
    }
    
    @SuppressWarnings("unused")
    private static class SimpleNode  {
       
    	private Integer mData;
           
    	private SimpleNode(Integer pData) {
    	    mData = pData;
    	}
           
    	public int hashCode() {
    	    return mData.hashCode();
    	}    
       
    	public boolean equals(Object p) {
    	    return mData.equals(((SimpleNode)p).mData);
    	}
        
        public String toString() {
            return ""+mData;            
        }
       
    }
   
    class Edge {      
    	NodeWrapper u, v;   // two vertices incident to this edge       
          
    	// Create an edge with given vertices
    	Edge(NodeWrapper a, NodeWrapper b) {
    	    u = a;  v = b;
    	}
          
        // BCC mBCC;
        
    	String getEdgeString() {
    	    return "("+u.wrappedNode+","+v.wrappedNode+")";
    	}
          
    	// Check if two edges are the same by comparing two vertices in edges
    	boolean equal(NodeWrapper a, NodeWrapper b) {
    	    return ((u.wrappedNode == a.wrappedNode) && (v.wrappedNode == b.wrappedNode));
    	}   
          
    	public String toString() {
    	    return "("+u+","+v+")";
    	}
      
    }
   
    class BCC {      
    	int id;
    	LinkedList<Edge> edgeList;  // list of edges in a biconnected component
        
    	// Create a bicconected component with given ID number
    	BCC(int n) {
    	    id = n;
    	    edgeList = new LinkedList();
    	}
    }
    //////////////////////////////////////////////////////////////////////////
    //                      End of Inner class definitions                  
    //////////////////////////////////////////////////////////////////////////
 

    /**
     * Top-level function which creates an instance of 'ArtcPointDFS' class and
     * invokes its methods to find articulation points and biconnected component
     * in graphs.
     * @param args strings of graph representation - total number of vertices
     *             followed by pairs of vertices which indicate edges.
     * @throws IOException
     */    
    public static void main(String[] args) throws Exception {                    
        
        Logger.setProgramName("BiconnectedComponents");
        
        String s=null;
        String t=null;
        
        if (args.length == 2) {
            // s and t given on cmd line
            s = args[0];
            t = args[1];
        }
        else {
            // assume s and t are given in bmg file            
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));              
        
        HashBasedUndirectedGraph<String> graph = new HashBasedUndirectedGraph<String>();
                       
        // Read all edges in the form of pairs of vertices
        String line = br.readLine();
        
        while (line != null) {
            // Logger.info("Handling line: "+line);
            if (line.length() > 0 && line.charAt(0) != '#') {
                String[] tokens = line.split("\\s+");
                if (tokens.length == 1) {
                    // a query node
                    if (s == null) {
                        s = tokens[0];
                    }
                    else if (t == null) {                        
                        t = tokens[0];                        
                    }
                    else {
                        Utils.die("Too many query nodes (need 2)");
                    }                    
                }
                else {
                    if (s == null || t == null) {
                        Utils.die("s, t, or both null");
                    }
                    // a link
                    String u = tokens[0];
                    String v = tokens[1];
                    graph.addEdge(u,v);                                      
                }
            }
            line = br.readLine();
        }
              
        // Logger.info("Graph before creating BiConnectedComponents:\n"+graph);        
        
        BiconnectedComponents bcc = new BiconnectedComponents<String>(graph);
        
        // bcc.logGraph();
        Logger.info("Graph has been read.");
        
        bcc.run();        
        // bcc.showResult();
        
        // System.out.println("Nodes in result:");
        // System.out.println("Nodes in result:");        
        Set<String> nodesInResult = bcc.getNodesOnAcyclicSTPaths(s,t);
        for (String node: nodesInResult) {
            System.out.println(node);
        }
    }

    public void run() throws Exception {        
                         
        // Determine the starting vertex for search
        if (mVertices.size() == 0) {
            // no vertices.
            return;
        }
        
        NodeWrapper startVertex = mVertices.values().iterator().next();  
        
        // Stamp the starting time of the algorithm.
        // long time1 = System.currentTimeMillis();
        
        doArticPointDFS(startVertex); // Perform aritc. point search 
        
        // Stamp the ending time of the algorithm.
        // long time2 = System.currentTimeMillis();

        // Determine running time of DFS
        // long elapse = time2 - time1;
        
        // Logger.info("\n  Running Time of the algorithm : "+(long)elapse+" ms.");
            
    }
}   