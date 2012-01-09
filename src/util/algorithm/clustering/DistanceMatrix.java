package util.algorithm.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import util.CollectionUtils;
import util.ConversionUtils;
import util.IOUtils;
import util.collections.UnorderedPair;
import util.converter.Converter;
import util.converter.ObjectToStringConverter;

public class DistanceMatrix<T> implements IDataManagingDistanceFunction<T> {

    protected TreeSet<T> mObjects;
    protected Map<UnorderedPair<T>, Double> mData;
    private MissingDistancePolicy mMissingDistancePolicy;
    private Double mMax;
    private Converter<String,T> mObjectFactory;
    
    public DistanceMatrix(String pDistanceFile) throws IOException {
        mMissingDistancePolicy = MissingDistancePolicy.PERMIT_NULL;
        readData(pDistanceFile);
    }
    
    public DistanceMatrix(String pDistanceFile, MissingDistancePolicy pMissingDistancePolicy) throws IOException {
        mMissingDistancePolicy = pMissingDistancePolicy;
        readData(pDistanceFile);
    }
    
    /** Compute the function once and use the matrix to serve the distances */
    public DistanceMatrix(IDistanceFunction pFunction,
                          Collection pElements,
                          MissingDistancePolicy pMissingDistancePolicy) throws IOException {
        mData = new HashMap();
        mMissingDistancePolicy = pMissingDistancePolicy;
        mObjects = new TreeSet(pElements);
        
        int nElems = pElements.size();
        ArrayList elemList = new ArrayList(pElements);
        // int nPairs = nElems * (nElems-1) / 2; 
                               
        for (int i=0; i<nElems-1; i++) {
            for (int j=i+1; j<nElems; j++) {
                Object e1 = elemList.get(i);
                Object e2 = elemList.get(j);                
                Double d = pFunction.dist(e1, e2);
                if (d != null) {
                    // only store if not null (null means "missing", or "max", 
                    // and is the responsibility of pMissingDistancePolicy
                    UnorderedPair key = new UnorderedPair(e1, e2);
                    mData.put(key, d);
                }                
            }
        }
                
    }
     
    public String format() {
        
        StringBuffer buf = new StringBuffer();
        
        List<T> objects = new ArrayList(mObjects);       
        for (int i=0; i<objects.size(); i++) {
            buf.append(dist(objects.get(i),objects.get(0)));
            for (int j=1; j<objects.size(); j++) {
                buf.append("\t"+dist(objects.get(i),objects.get(j)));
            }
            buf.append("\n");
        }
        
        return buf.toString();
    }
    
    public void minusLogTransform() {        
        for (UnorderedPair<T> pair: mData.keySet()) {
            double d = mData.get(pair);
            double d2 = -Math.log(d);
            mData.put(pair, d2);
        }               
    }        

    public Set<T> getDataPoints() {
        return mObjects;
        // return new ArrayList(mObjects);
    }
    
    public Double dist(T p1, T p2) {        
        Double d = mData.get(new UnorderedPair(p1, p2));
        if (d == null) {
            if (mMissingDistancePolicy == MissingDistancePolicy.PERMIT_NULL) {
                return null;
            }
            else if (mMissingDistancePolicy == MissingDistancePolicy.DOUBLE_MAX) {
                if (mMax == null) {
                    findMax();
                }
                return mMax * 2;                                
            }
            else if (mMissingDistancePolicy == MissingDistancePolicy.MAX) {
                if (mMax == null) {
                    findMax();
                }
                return mMax;                                
            }
            else if (mMissingDistancePolicy == MissingDistancePolicy.ONE) {
                return 1.0;                                
            }
            else if (mMissingDistancePolicy == MissingDistancePolicy.NULL_IS_AN_ERROR) {
                throw new RuntimeException("No distance for pair: "+p1+", "+p2);
            }
            else {
                throw new RuntimeException("Unknown MissingDistancePolicy: "+mMissingDistancePolicy);
            }
        }
        else {
            return d;
        }
    }
    
    private void findMax() {
        mMax = CollectionUtils.findMax(mData.values()).doubleValue();
    }
    
    public String toString() {
        return "DistanceMatrix ("+mData.size()+" pairs, "+mObjects.size()+" objects)";
    }
    
    private void readData(String pDistanceFile) throws IOException {        
        
        mObjects = new TreeSet<T>();
        mData = new HashMap();
                
        for (String line: IOUtils.readLines(pDistanceFile)) {
            String[] tokens = line.split("\\s+");
            String rep1 = tokens[0];
            String rep2 = tokens[1];
            T obj1 = mObjectFactory != null ? mObjectFactory.convert(rep1) : (T)rep1;
            T obj2 = mObjectFactory != null ? mObjectFactory.convert(rep2) : (T)rep2;
            
            double d = Double.parseDouble(tokens[2]);
            UnorderedPair<T> key = new UnorderedPair(obj1, obj2);           
            mObjects.add(obj1);
            mObjects.add(obj2);
            mData.put(key, d);            
        }                            
    }
    
    public enum MissingDistancePolicy {
        PERMIT_NULL("permit_null"), // hmm...
        DOUBLE_MAX("double_max"),   // shall ALWAYS break the triangle inequality!!!
        MAX("max"),   // shall never break the triangle inequality!!!
        ONE("one"),   // ???
        NULL_IS_AN_ERROR("error");   // assert that all distances are there
                         
        String mName; 
            
        MissingDistancePolicy(String pName) {
            mName = pName;
        }
        
        public static MissingDistancePolicy getByName(String pName) {
            for (MissingDistancePolicy strategy: MissingDistancePolicy.values()) {
                if (strategy.mName.equals(pName)) {
                    return strategy;
                }
            }
            throw new RuntimeException("No such missing distance policy: " + pName);
        }
        
        public String toString() {
            return mName;
        }

        public static List<String> names() {
            return ConversionUtils.convert(Arrays.asList(MissingDistancePolicy.values()),
                                           new ObjectToStringConverter());
        }
        
    }
}

  