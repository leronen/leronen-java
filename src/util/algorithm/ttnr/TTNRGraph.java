package util.algorithm.ttnr;

import util.collections.graph.defaultimpl.DefaultGraph;
import util.collections.graph.defaultimpl.GraphFactory;

/**
 *  As java has no proper typedefs, here we happily utilize the 
 *  "pseudo-typedef antipattern" to get rid of some verbosity in the graph clients.
 *  
 *  Please find below phintsanen's original specs:
 *  
 * Vaatimukset:
 *
 * - Verkko-I/O (bmgraph)
 *
 * - Kaarille kaksi attribuuttia: iter (int) ja decided (bool)
 *
 * - Dijkstran toteutukseen vaadittavat asiat:
 *   - Keko, jossa voidaan muuttaa mielivaltaisen alkion avainta
 *     siis tyyliin heap.set_key(item, new_key)
 *   - Solmun naapurien haku ja iterointi
 *   - Solmuattribuutteja: dist (double) ja prev (node)
 *
 * - Polkuluokka, so. lista solmuja.  Operaatiot: append, reverse,
 *   iterate.  Voinee olla myös lista kaaria.
 *
 * - Verkkojen kasaaminen lisäämällä polkuja, eli tyyliin
 *   graph.add_path(path) lisää verkkoon pathin solmut ja kaaret.
 *   
 *   TODO: implement mapping from TTNRGraph back to BMGraph To this end,
 *  can potentially store a reference to a source graph for concenience.
 *  
 *   Also need add(path) and add(edge) to class DefaultGraph, with 
 *   clear separation of edge attributes and node attributes to transient
 *   and non-transient ones, to enable cloning of non-transient attributes
 *   to the new graph! To this end, we could introduce a method 
 *   cloneNonTransientAttributes(dst) to both Node and edge classes.     
 */
public class TTNRGraph<T> extends DefaultGraph<T, TTNRNode<T>, TTNREdge<T>> {
    
    private static final Factory FACTORY = new Factory();
    
    public Object mSourceGraph;
    
    public TTNRGraph() {
        super(TTNRNode.FACTORY, TTNREdge.FACTORY);
    }
        
    /** Populate len and prob_int from prob */
    public void initializeProbs() {
        for (TTNREdge<T> e: getEdges()) {            
            e.attributes.prob_int = (int)(e.attributes.prob * Integer.MAX_VALUE);
            e.attributes.len = -Math.log(e.attributes.prob);
        }
    }
    
    public GraphFactory getFactory() {        
        return TTNRGraph.FACTORY;
    }       
    
    public static class Factory<T> implements GraphFactory<T, TTNRNode<T>, TTNREdge<T>, TTNRGraph<T>> {
        public TTNRGraph<T> makeGraph() {
            return new TTNRGraph<T>();
        }
    }
    
}
