package util.collections.graph.defaultimpl;

import java.util.*;

import util.collections.SymmetricPair;
import util.condition.Condition;
import util.dbg.Logger;



/**
 * Once again, head for a fresh start regarding graph implementations.
 *  
 * This time, we shall keep it simple and efficient !
 * 
 * The first goal is to enable ttnr and related calculations with minimum clutter.
 * 
 * Let's see also, if we can learn something from jgrapht (hmm, as expected,
 * it also seems to be a mess...)
 *
 *  axioms: 
 *    * the graph is undirected
 *    * there shall only be two AbstractEdge instances for each logical edge, one
 *     for each direction
 *    
 * TODO: decide, whether nodes need any special attributes, or shall node
 * attributes be stored to graph also... let's store them to nodes,
 * and use again a default implementation for node. However, let's allow
 * for arbitrary node identifiers! Again, how to then ensure efficient
 * hashing & equality for the identifiers?!?!? We could resort to default 
 * object hashcode & equals here, as we shall indeed enforce uniqueness of
 * nodes (let's not allow the user to create any nodes, but create nodes
 * for arbitrary keys instead on demand)
 * 
 * How to do mapping from original nodes (and even edges) to graph-resident
 * nodes and edges? How to do the opposite?
 * 
 * Let users provide their own node and edge implementations by OVERRIDING
 * the default node implementation (which shall be axiomatic, of course), to enable 
 * efficient storage of attributes. Use factories for this, as expected...
 * TODO: it is probably clear that no-one is going to implement a node class
 * from scratch?
 * 
 * Note that is very probable that we shall not manage without hashing.
 * Let's spend some effort on efficient hashing, then.
 * 
 * Also, let's not resort to frequent object creation (even recycle objects
 * when possible); also, let's not create any sets or lists in response to 
 * queries, but instead use iterators whenever possible. Note that even
 * recycling iterators might be possible, after all objects have been iterated;
 * in that stage, the iterator impl reference might get assigned to a EMPTY_ITERATOR. 
 * 
 * Initially, let's not allow multigraphs, or even more than one type of edge for
 * a node pair.
 * 
 * Let's not strictly require, for now, that node keys are comparable (that
 * would maybe complicate various signatures too much). However, 
 * requiring them to be comparable might come in handy or even mandatory 
 * in the future, if we need to be able to provide links in some canonical
 * representation direction... However, we might also be generate such a 
 * ordering internally even, if needed (?) 
 * 
 * Questions: how to expose Node and Edge objects to the caller?
 * Let's let caller specify factories for both, to enable efficient usage
 * of arbitrary attributes. However, as Graph is largely responsible for
 * the Nodes and Edges, require that the caller-provided subclasses always
 * extends util.collections.graph.defaultimpl.Node and 
 * util.collections.graph.defaultimpl.Edge. Later, we perhaps, just perhaps,
 * might, just might, also possibly, just possibly, them to be implementations 
 * for a specific interface?
 * 
 * Let's not allow the implementations to change equals and hashCode, though!
 * (they are only meant for managing transient attributes).
 * 
 * Note that we separate the node key class T from the node implementation class N,
 * to allow for flexibly changing the key implementation.
 * 
 * 
 * Param types: 
 *   * T = node key
 *   * N = node impl class (may define key?)
 *   * E = edge impl class (may define key?)
 *   
 * Todo: better resolve the relationship between T and N. Namely, do we want
 * the user to provide keys or node instances? The user has to get hold of 
 * node instances, actually (what use would there be for a specific node impl 
 * otherwise!?!?!), so we should definitely move to that direction ASAP.
 * 
 * Why do we actually need to enable arbitrary keys? Let the subclasses
 * specify the key, or then provide a default impl which can use any hashable
 * keys; anyway we must use node instances everywhere, it is not sufficient
 * that the caller only gets hold of the keys.
 * 
 * An issue to resolved is where we are going to store a node's neighbors.
 * For now, use a dedicated ArrayList which is an instance variable of Node.
 * Note that this enforces each Node instance to only appear in exactly one 
 * Graph!!! For a more general impl, we might have to move the neighbors 
 * functionality into the graph from the node impl...
 * 
 * @see DirectedGraph
 * @see DefaultGraph
 * @see IGraph
 * @see MultiMap
 * @see NonInjectiveBidirectionalMap
 */
public class DefaultGraph<T, N extends DefaultNode<T>, E extends AbstractEdge<T,N>> {    
        
    // private static final Factory FACTORY = new Factory();
    
    private NodeFactory<T,N> mNodeFactory;
    private EdgeFactory<T,N,E> mEdgeFactory;
   
    private HashMap<T, N> mNodeById;

    /**
     * Mapping of link=> link. Unfortunately, Set does not have a get-method, so
     * we have to use a Map (not so bad actually, as HashSet is implemented with 
     * a HashMap!) The idea of this, of course, is to to avoid creating duplicate 
     * links. Note that here, each edge is only represented in one direction
     * so some caution may be necessary (edges should courtly provide access to their 
     * reverse rep also, whenever needed).
     * 
     * Hmm, this might be more efficient when edges were allocated integer id:s.
     * Or not.
     */
    private Map<E, E> mEdgeByEdge;
    
    public DefaultGraph(NodeFactory<T,N> pNodeFactory,
                        EdgeFactory<T,N,E> pEdgeFactory) {
        mNodeById = new HashMap();
        mEdgeByEdge = new LinkedHashMap();
        mNodeFactory = pNodeFactory;
        mEdgeFactory = pEdgeFactory;       
    }
    
    public Collection<N> getNodes() {
        return mNodeById.values();
    }    
    
    /**
     * Note that there is currently no efficient way to remove a given edge;
     * this is because edges are stored into nodes as ArrayList.
     * 
     * This method of mass-removing using a condition may be efficient enough 
     * for many situations, though.
     * 
     * @param pCondition Condition should return true for edges that are to 
     * be removed.
     * 
     * @return number of edges removed
     */
    public int removeEdges(Condition<E> pCondition) {
        
        // remove from nodes
        int numEdgesRemovedFromNodes = 0;
        for(N node : getNodes()) {
            // uhh, the type parameter horrors:
            Iterator<E> edgeIter = (Iterator<E>)(Iterator)node.mEdges.iterator();
            while (edgeIter.hasNext()) {
                E e = edgeIter.next();
                if (pCondition.fulfills(e)) {
                    edgeIter.remove();
                    numEdgesRemovedFromNodes++;
                }                
            }
            
        }
        
        if (numEdgesRemovedFromNodes % 2 != 0) {
            throw new RuntimeException("Data inconsistency; number of edges" +
            		                   " removed from nodes is an uneven number: "+numEdgesRemovedFromNodes);
        }
                
        ArrayList<E> edgesToRemoveFromMap = new ArrayList(numEdgesRemovedFromNodes/2);
        
        // remove from edge map        
        for(E edge: mEdgeByEdge.keySet()) {
            if (pCondition.fulfills(edge)) {
                edgesToRemoveFromMap.add(edge); 
            }                       
        }
        
        if (edgesToRemoveFromMap.size() != numEdgesRemovedFromNodes/2) {
            throw new RuntimeException(
                   "Number of edges removed from nodes is different from the "+
                   "number of edges removed from the edge map! "+
                   "("+edgesToRemoveFromMap.size()+" and "+numEdgesRemovedFromNodes/2);
        }
        
        mEdgeByEdge.keySet().removeAll(edgesToRemoveFromMap);
        
        return edgesToRemoveFromMap.size();
    }
           
    
    /**
     * 
     * Return an _unmodifiable_ list of edges.
     * 
     * _SHOULD_ return the edges such that edge.src is always the node identified by pNodeId
     */
    public List<E> getEdges(T pNodeId) {
        N node = mNodeById.get(pNodeId);
        
        if (node == null) {
            throw new RuntimeException("No such node: "+pNodeId);
        }
        
        // we have not parametrized nodes with the edge impl, so have to cast:
        return Collections.unmodifiableList((List<E>)(List)node.mEdges);
    }
    
    /** 
     * Return an _unmodifiable_ list of edges. 
     * 
     * _SHOULD_ return the edges such that edge.src is always the node identified by pNodeId*/     
    public List<E> getEdges(N pNode) {
               
        // we have not parametrized nodes with the edge impl, so have to cast:
        return Collections.unmodifiableList((List<E>)(List)pNode.mEdges);
    }
        
    
    /**
     * Add or get a node. The preference is that users already beforehand call 
     * addNode(T,int) which utilizes knowledge on the node degree to optimize 
     * memory usage. 
     */
    public N addOrGetNode(T pNodeId) {
        N node = mNodeById.get(pNodeId);
        if (node == null) {                                
            // create a node
            node = mNodeFactory.makeNode(pNodeId);
            node.initEdgeList();
            node.mGraph = this;
            mNodeById.put(pNodeId, node);            
        }
        
        return node;
    }
    
    /**
     * Add a node, with the additional, optimizational, knowledge on the 
     * node degree. Trying to add a duplicate node is considered an error
     * and shall be rewarded with a potentially cryptic RuntimeException.
     */
    public N addNode(T pNodeId, int pDegree) {
        N node = mNodeById.get(pNodeId);
        if (node == null) {                                
            // create a node
            node = mNodeFactory.makeNode(pNodeId);
            node.mGraph = this;
            node.initEdgeList(pDegree);
            mNodeById.put(pNodeId, node);            
        }
        else {
            // already exists, raise a cryptic exception as promised
            throw new RuntimeException("Hämää Sörenlinjalla; teekin spjassi-investointi!");           
        }
        
        return node;
    }
    
    
    /** Return null, if no such node */
    public N getNode(T pNodeId) {
        return mNodeById.get(pNodeId);        
    }

    /** 
     * Remember to call compactEdges() after adding all edges!
     * 
     * @see #addOrGetEdge(T, T, util.collections.graph.defaultimpl.Graph.EdgeAddPolicy) 
     */
    public E addOrGetEdge(SymmetricPair<T> pNodePair, EdgeAddPolicy pPolicy) {
        return addOrGetEdge(pNodePair.getObj1(), pNodePair.getObj2(), pPolicy);
    }       
    
    public int numNodes() {
        return mNodeById.size();
    }    
    
    /** 
     * Reduce edge lists to hold only as many neighbors as there actually exists 
     * Call this after adding edges one by one.
     */ 
    public void compactEdges() {
       for (N n: mNodeById.values()) {
           n.mEdges.trimToSize();
       }
    }
    
    /** 
     * Add an edge THAT ORIGINALLY BELONGS TO ANOTHER GRAPH to this graph 
     * (the fact that we have an Edge instance implies that it already exists 
     * in some graph, as only graphs can create edges; and of course it does not make
     * sense to add the same edge to this same graph!). 
     * 
     * Therefore, adding the edge is performed by constructing 
     * a copy of the edge and adding that; the original instance itself is not
     * stored or referred to in this graph in any way. Also, new nodes
     * are created using the node keys in the original edge.  
     */
    public E addEdgeCopy(E pEdge, EdgeAddPolicy pPolicy ) {
        E e = addOrGetEdge(pEdge.src.mKey, pEdge.tgt.mKey, pPolicy);        
        pEdge.cloneNonTransientAttributes(e);        
        return e;        
    }
    
    /** 
     * Add an edge; trying to add the same edge twice is considered an runtime 
     * error. 
     * 
     * Just calls {@link #addOrGetEdge(SymmetricPair, EdgeAddPolicy)} with 
     * EdgeAddPolicy.ASSERT_NOT_EXISTS.
     * 
     * @return the newly created edge.
     */
    
    public E addEdge(T pSrc, T pTgt) {
        return addOrGetEdge(pSrc, pTgt, EdgeAddPolicy.ASSERT_NOT_EXISTS);
    }
    
    public E getEdge(T pSrc, T pTgt ) {
        return addOrGetEdge(pSrc, pTgt, EdgeAddPolicy.IGNORE_NEW);
    }
    
    /** 
     * What happens when trying to add duplicate edges is dictated by @pPolicy.
     * 
     * Returns the new (or existing) edge. The returned edge will always 
     * in the rep dir {pSrc,pTgt}
     * 
     * Nodes are added if they do not exist yet.
     */
    public E addOrGetEdge(T pSrc, T pTgt, EdgeAddPolicy pPolicy ) {
        N src = addOrGetNode(pSrc);
        N tgt = addOrGetNode(pTgt);        
        E edge = mEdgeFactory.makeEdge(src, tgt);        
        E existingEdge = mEdgeByEdge.get(edge);
        
        if (existingEdge != null) {
            // edge exists
            if (pPolicy == EdgeAddPolicy.ASSERT_NOT_EXISTS) {
                throw new RuntimeException("Edge already exists: "+edge+": "+existingEdge);
            }
            else if (pPolicy == EdgeAddPolicy.WARN_IF_EXISTS) {
                Logger.warning("Edge already exists: "+edge+": "+existingEdge);
                return (E)existingEdge.startingFrom(src);
            }
            else if (pPolicy == EdgeAddPolicy.IGNORE_NEW) {
                return (E)existingEdge.startingFrom(src);
            }
            else {
                throw new RuntimeException("Unknown edge add policy: "+pPolicy);                
            }            
        }
        else {    
            // a new edge
            
            // create the reverse as well:
            E reverse = mEdgeFactory.makeEdge(tgt, src);            
            edge.reverse = reverse;
            reverse.reverse = edge;

            edge.mGraph = this;
            
            // give the edge a chance to init it's attributes (should 
            // also init the reverse's attributes!)
            edge.initAttributes();
                        
            // Note that the reverse shall not be explicitly contained in the 
            // edge set, but is represented by edge instead
            mEdgeByEdge.put(edge, edge);
            
            // add to neighbor lists
//            src.mEdges.add(edge);
//            tgt.mEdges.add(reverse);
            src.addEdge((AbstractEdge) edge);
            tgt.addEdge((AbstractEdge) reverse);
            
            return (E)edge;
        }
        
    }
    
    public Set<E> getEdges() {
        return mEdgeByEdge.keySet();
    }
    
    /** Clone the edges of this graph into pDst */
    public void cloneEdges(DefaultGraph pDst) {
        for (E e: getEdges()) {
            E clonedEdge = (E)pDst.addEdgeCopy(e, EdgeAddPolicy.ASSERT_NOT_EXISTS);
            e.cloneNonTransientAttributes(clonedEdge);
        }
        
    }
    
    /**
     *  Subclasses should definitely override this!
     */
    public final <G extends DefaultGraph<T, N, E>> G createClone() {
        
        if (getClass() != DefaultGraph.class) {
            Logger.uniqueWarning("DefaultGraph.getFactory() not overridden in class "+getClass());
        }
        
        DefaultGraph<T, N, E> clone = new DefaultGraph(mNodeFactory, mEdgeFactory);
        
        cloneEdges(clone);
        
        return (G)clone ;
    }
    
    public String toString() {                    

        StringBuffer sb = new StringBuffer();
        for (E e : getEdges()) {                                       
            sb.append(e.toString());                                  
            sb.append("\n");
       }

        return sb.toString();
    }
    
//    /** It is essential to override this in subclasses */
//    public GraphFactory getFactory() {
//        if (getClass() != DefaultGraph.class) {
//            Logger.uniqueWarning("DefaultGraph.getFactory() not overridden in class "+getClass());
//        }
//        return DefaultGraph.FACTORY;
//    }       
    
//    public static class Factory<T> implements GraphFactory<T, DefaultNode<T>, DefaultEdge<T,DefaultNode<T>>, DefaultGraph<T,DefaultNode<T>,DefaultEdge<T,DefaultNode<T>>>> {
//
//        public DefaultGraph<T, DefaultNode<T>, DefaultEdge<T,DefaultNode<T>>> makeGraph() {
//            return new DefaultGraph(new DefaultNode.Factory(), new DefaultEdge.Factory());
//        }
//    }              
    
    
    /** Dictates what happens when trying to add a duplicate link */  
    public enum EdgeAddPolicy {
        /** a RuntimeException is thrown */
        ASSERT_NOT_EXISTS,
        /** The new link is discarded, a warning is generated and the old link is returned */
        WARN_IF_EXISTS,
        /** The new link is discarded and the old link is returned */
        IGNORE_NEW;
    }
                
}
