package util.algorithm.clustering;

import java.io.File;
import java.io.IOException;
import java.util.*;

import util.CollectionUtils;
import util.IOUtils;
import util.Initializable;
import util.StringUtils;
import util.algorithm.clustering.DistanceMatrix.MissingDistancePolicy;
import util.collections.HashWeightedSet;
import util.collections.WeightedSet;
import util.dbg.Logger;


/**
 * Measures distance as the dot product of two (sparse) vectors with n and m
 * non-zero elements, respectively. Assume entries of the vectors are in [0,1].
 * To obtain a similarity s, the product is normalized by the length of the 
 * shorter vector (that is, the maximum value obtainable by the product of 
 * two vectors with lengths n and m). The obtained similarity s lies in [0,1], and 
 * if finally transformed into a distance in [0,1] by the very complex indeed
 * function f(s) = 1-s.
 * 
 *
 */   
public class DotProductDistanceFunction<E,A> implements IDataManagingDistanceFunction<E>, Initializable {

    private Set<E> mObjects;
    private Set<A> mAttributes;
    private Map<E, WeightedSet<A>> mData;
    /**
     * Buffer for the common set of attributes.
     * Recycle this set, as computing intersections is needed constantly.
     * Of course, destroys any possibility of concurrent access...
     */
    private Set<A> mIntersectionBuf = new HashSet();
    
    /**
     * Remember to init()!
     */
    public DotProductDistanceFunction() {
        // no action, please remember to init()
    }
    
    public DotProductDistanceFunction(String pDataFile) throws IOException {
        initFromFile(pDataFile);
    }
    
    public DotProductDistanceFunction(Map<E, WeightedSet<A>> pData) {
        init(pData);                
    }
     
    private void init(Map<E, WeightedSet<A>> pData) {
        mData = pData;
        mObjects = mData.keySet();
        mAttributes = new HashSet();
        for (WeightedSet<A> aSet: pData.values()) {
            mAttributes.addAll(aSet);
        }
    }
    
    private void initFromFile(String pFile) throws IOException {
        Map<E, WeightedSet<A>> data = new LinkedHashMap();
        // char dataPoint = 'A';
        String header = IOUtils.readFirstLine(new File(pFile));
        String[] cols = header.split("\\s+");
        List<String> vars = Arrays.asList(cols).subList(0,cols.length);
        Iterator<String> lineIter = IOUtils.lineIterator(pFile);
        lineIter.next();
        while(lineIter.hasNext()) {
            String line = lineIter.next();
            cols = line.split("\\s+");
            String id = cols[0];
            WeightedSet<A> vec = new HashWeightedSet();                                    
            for (int j=1; j<vars.size(); j++) {
                double weight = Double.parseDouble(cols[j]);
                vec.add((A)vars.get(j), weight);
            }
            data.put((E)(""+id), vec);            
        }
                        
        init(data);
    }
    
    /**
     * Init by reading data from this file. Assume row and column headers.
     * The file is assumed to contain a "full" matrix rep.
     */
    public void init(String pFile) {
        try {
            initFromFile(pFile);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed reading dot product distance function data from file", e);        
        }        
    }
    
    public Set<E> getDataPoints() {
        return mObjects;
        // return new ArrayList(mObjects);
    }
    
    /** Can be optimized heavily, I suppose */
    public Double dist(E p1, E p2) {
        // Logger.info("Computing distance for "+p1+", "+p2);
        WeightedSet<A> v1 = mData.get(p1);
        WeightedSet<A> v2 = mData.get(p2);
//        Logger.info("v1: "+v1);
//        Logger.info("v2: "+v2);
        CollectionUtils.intersection(v1, v2, mIntersectionBuf);
        double sum = 0;
        for (A a: mIntersectionBuf) {
            sum += v1.getWeight(a) * v2.getWeight(a);
        }
        // max sum is the maximum number of common elements, given the 
        // number of non-zero elements in each vector
        // (TODO: should we actually normalize by the sum of vector elements?)
        int maxsum = Math.min(v1.size(), v2.size());
        return 1 - (sum/maxsum);
    }
    
    public String toString() {
        return "DotProductDistanceFunction ("+mObjects.size()+" objects and "+mAttributes.size()+" attributes)";
    }
    
    public static void main(String[] args) throws Exception {
        // read vectors, naming attributes 1,2,3,... and data points A, B, C        
        Map<String, WeightedSet> data = new LinkedHashMap();
        char dataPoint = 'A';
        for (String line: IOUtils.readLines()) {
            String tokens[] = line.split("\\s+");            
            WeightedSet vec = new HashWeightedSet();
            vec.setAllowZeros(true); // this is crucial!
            for (int j=0; j<tokens.length; j++) {
                double weight = Double.parseDouble(tokens[j]);
                vec.add(j, weight);
            }
            data.put(""+dataPoint, vec);
            dataPoint++;
        }
        
        Logger.info("Read data:");
        for (Object point: data.keySet()) {
            WeightedSet vec = data.get(point);
            Map asMap = vec.asObjToWeightMap();
            Logger.info(point+": "+StringUtils.mapToString(asMap, "=", ", "));
        }
        
        DotProductDistanceFunction func = new DotProductDistanceFunction(data); 
        
        Logger.info("Compute distances...");
        DistanceMatrix dMatrix = new DistanceMatrix(func, 
                                                    data.keySet(),
                                                    MissingDistancePolicy.ONE);                                                   
            
        Logger.info("The distance matrix:\n"+ dMatrix.format());
        
    }
    
    
   
}
