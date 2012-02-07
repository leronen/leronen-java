package util;

import java.io.IOException;
import java.util.*;

import util.collections.BinaryHeap;
import util.collections.HashWeightedSet;
import util.collections.IPair;
import util.collections.SymmetricPair;
import util.collections.UnorderedPair;
import util.collections.BinaryHeap.Mode;
import util.collections.graph.HashBasedUndirectedGraph;
import util.collections.iterator.RandomOrderIterator;
import util.collections.iterator.SamplingIterator;
import util.converter.StringToDoubleConverter;
import util.dbg.Logger;

public class RandUtils {   
           
    private static Random sRandom;

    public static final String CMD_RANDINT = "randint";
    public static final String CMD_RANDINTS = "randints";
    public static final String CMD_SAMPLE = "sample";
    public static final String CMD_SAMPLE_WITHOUT_REPLACEMENT = "sample_without_replacement";
    public static final String CMD_SAMPLE_WITH_REPLACEMENT = "sample_with_replacement";
    public static final String CMD_SAMPLEPAIRS = "samplepairs";
    public static final String CMD_SAMPLEPAIRS2 = "samplepairs2";
    public static final String CMD_BERNOULLI = "bernoulli";
    public static final String CMD_SAMPLETEST = "sampletest";
    public static final String CMD_SAMPLEAVERAGES = "sampleaverages";
    
    public static Random getRandomNumberGenerator() {
        if (sRandom == null) {
            sRandom = new Random();
        }
        return sRandom;
    }
                      
    public static void setRandSeed(long pSeed) {
        sRandom = new Random(pSeed);     
    }

    public static void setRandSeed() {
        sRandom = new Random();
    }
    
    public static double samplePoisson(double pLambda) {
    	double z = getRandomNumberGenerator().nextDouble();    	
    	return -(Math.log(z) / pLambda);    	
    }
           
    
    public static <T> List<T> sampleWithReplacement(List<T> pElements, int pNumToSample) {
        List<T> result = new ArrayList(pNumToSample);
    
        int maxInd = pElements.size()-1;
        for (int i=0; i<pNumToSample; i++) {
            int ind = randInt(0, maxInd);
            result.add(pElements.get(ind));
        }
        return result;
    }
    
    /** Both limits are inclusive */
    public static int randInt(int pMin, int pMax) {
        double rand = getRandomNumberGenerator().nextDouble();
        // Logger.info("RandInt double: "+rand);
        int result = pMin+(int)(rand*(pMax-pMin+1));
        // Logger.info("Returning result: "+result);
        return result;                 
    }
    
    public static boolean randBoolean() {
        int zeroOrOne = RandUtils.randInt(0,1);
        return zeroOrOne == 1;                        
    }       
    
    /** 
     * Perform a bernoulli trial with probability pBrob. Return true,
     * if trial is "successful" (e.g. for 0, always return false and for 1,
     * always return true.)
     */
    public static boolean sampleBernoulli(double pProb) {
        double r = getRandomNumberGenerator().nextDouble(); // a double in the range [0,1[        
        return r < pProb; 
    }
    
    /**
      * Sample a "weighted coin. Note that weights do not have to sum to 1,
      * as they will be normalized anyway.
      *
      * @return 0:take first("heads", 1: take second("tails") 
      */ 
    public static int tossAWeightedCoin(double pWeight_0, double pWeight_1) {
        double sum = pWeight_0+pWeight_1;
        double normalizedWeight_0 = pWeight_0/sum;        
        double r = getRandomNumberGenerator().nextDouble();
        if (r<normalizedWeight_0) {
            // take first
            return 0;
        }
        else {
            // take second
            return 1;
        }            
    }
    
    public static <T> SymmetricPair<T> samplePair(List<T> pList) {
        return new SymmetricPair(sampleWithoutReplacement(pList, 2));
    }
    
    public static <T> SymmetricPair<T> samplePairFromCollection(Collection<T> pCollection) {
        return new SymmetricPair(sampleFromCollection(pCollection, 2));
    }

    /**
     * Sample one element from a collection. Implementation delegated to multi-element sampling,
     * consequently being less efficient as it could be.
     * */
    public static <T> T sampleWithoutReplacement(Collection<T> pList) {
        return sampleFromCollection(pList, 1).iterator().next();
    }
        
    
    /**
     * Sample one element from a list. Implementation delegated to multi-element sampling,
     * consequently being less efficient as it could be.
     * */
    public static <T> T sample(List<T> pList) {
        return sampleWithoutReplacement(pList, 1).iterator().next();
    }
    
    /** Sample WITHOUT replacement  */
    public static <T> List <T> sampleWithoutReplacement(List<T> pList, int pNumToSample) {        
        int numObjects = pList.size();
        if (pNumToSample > numObjects) {
            throw new RuntimeException("Not enough objects in list: asked for "+pNumToSample+ "and we have only "+numObjects);
        }
        ArrayList result = new ArrayList();
            
        double K = pNumToSample;
        double N = numObjects;
        for (int i=0; i<numObjects; i++) {    
            double r = getRandomNumberGenerator().nextDouble();
            double tresh = K/N; 
            if (r < tresh) {
                // dbgMsg("below treshold, get "+pList.get(i)+" to sample");
                // select i
                result.add(pList.get(i));                
                K--;
            }
            else {
                // dbgMsg("above treshold, forget about "+pList.get(i));
            }
                
            N--;			
        }                    
        return result;        
    }
    
    
    /** Sample a number of elements from a collection, WITHOUT replacement */
    public static <T> List<T> sampleFromCollection(Collection<T> pCollection, int pNumToSample) {
        SamplingIterator<T> samplingIter = new SamplingIterator(pCollection.iterator(), pCollection.size(), pNumToSample);
        return CollectionUtils.makeArrayList(samplingIter);
    }       
        
        
    /** Sample with no bootstrapping  */
    public static List<Integer> sampleIndices(int pListSize, int pNumToSample) {        
        int numObjects = pListSize;
        if (pNumToSample > numObjects) {
            throw new RuntimeException("Not enough objects in list: asked for "+pNumToSample+ "and we have only "+numObjects);
        }
        ArrayList<Integer> result = new ArrayList<Integer>();
            
        double K = pNumToSample;
        double N = numObjects;
        for (int i=0; i<numObjects; i++) {    
            double r = getRandomNumberGenerator().nextDouble();
            double tresh = K/N; 
            if (r < tresh) {
                // dbgMsg("below treshold, get "+pList.get(i)+" to sample");
                // select i
                result.add(i);                
                K--;
            }
            else {
                // dbgMsg("above treshold, forget about "+pList.get(i));
            }
                
            N--;			
        }                    
        return result;        
    }
    
    public static void main(String[] args) {
        
        if (args.length == 0) {
            usageAndExit("First argument must be a command.");
        }
        
        if (args[0].equals(CMD_RANDINT)) {
            int min = Integer.parseInt(args[1]);
            int max = Integer.parseInt(args[2]);
            System.out.println(randInt(min, max));
        }
        else if (args[0].equals(CMD_RANDINTS)) {
            int min = Integer.parseInt(args[1]);
            int max = Integer.parseInt(args[2]);
            int num = Integer.parseInt(args[3]);                
            for (int i=0; i<num; i++) {
                System.out.println(randInt(min, max));
            }
        }
        else if (args[0].equals(CMD_SAMPLEPAIRS)) {
            try {
                // given a set of objects, try to sample as balanced set of pairs
                // as possible; by this we mean that each node will appear
                // in almost equal number of pairs
                double numpairs = Integer.parseInt(args[1]);            
                List<String> data = IOUtils.readLines();
                Collections.shuffle(data);
                double  numobjects = data.size();
                List<UnorderedPair<String>> pairs = CollectionUtils.makeUnorderedPairs(new HashSet(data));
                Collections.shuffle(pairs);
//                for (UnorderedPair<String> pair: pairs) { 
//                    System.out.println(pair.getObj1()+" "+pair.getObj2());                    
//                }
                double expectedNumOccurences = (numpairs * 2) / numobjects;
                Logger.info("num objects: "+numobjects);
                Logger.info("total num pairs: "+pairs.size());
                Logger.info("wanted num pairs: "+numpairs);
                Logger.info("Expected num occurences of each object in pairs: "+expectedNumOccurences);
                Logger.info("Max num occurences of each object in pairs: "+Math.ceil(expectedNumOccurences*1.1));
                
                HashWeightedSet<String> counts = new HashWeightedSet();
                counts.setAllowZeros(true);
                BinaryHeap<String, Integer> heap = new BinaryHeap(Mode.MIN);
                
                HashBasedUndirectedGraph<String> result = new HashBasedUndirectedGraph();                
                
                for (String o: data) {
                    heap.add(o, 0);
                    counts.set(o,0);
                }
                
                while (result.numEdges() < numpairs) {
                    String o1 = sampleOneMinObject(heap, (Set<String>)Collections.EMPTY_SET);
                    int count1 = heap.key(o1);
                    heap.remove(o1);                    
                    
                    Set<String> neighbors = result.followers(o1);                     
                    String o2 = sampleOneMinObject(heap, neighbors);
                    int count2 = heap.key(o2);
                    heap.remove(o2);                    
                    
                    result.addEdge(o1, o2);
                    
                    heap.add(o1, count1+1);
                    heap.add(o2, count2+1);                                        
                }
                
                List<String> output = new ArrayList();
                
                for (IPair<String, String> pair: result.edges()) {
                    output.add(pair.getObj1()+ " " +pair.getObj2());                    
                }
                
                Collections.shuffle(output);
                
                IOUtils.writeCollection(System.out, output);
                
            }
            catch (Exception e) {
                Utils.die(e);
            }
        }
        else if (args[0].equals(CMD_SAMPLEPAIRS2)) {
            try {
                // given a set of objects in file $1, sample $2 arbitrary
                // pairs of them, excluding pairs in file $3
                
                List<String> data = IOUtils.readLines(args[1]);
                double numRequested = Integer.parseInt(args[2]);
                
                Set<SymmetricPair<String>> excludedPairs = Collections.EMPTY_SET;
                
                if (args.length >=4) {
                    List<String> excludeStrings = IOUtils.readLines(args[3]);
                    excludedPairs = new HashSet( 
                            ConversionUtils.convert(excludeStrings, 
                                                    new SymmetricPair.Parser()));
                }                
                                
                int numGot = 0;
                int numFail = 0;
                
                Set<SymmetricPair<String>> result = new HashSet();
                
                while (numGot < numRequested) {
                    SymmetricPair<String> pair = RandUtils.samplePair(data);
                    
                    if (! excludedPairs.contains(pair) && ! result.contains(pair)) {
                        result.add(pair);
                        numGot++;
                    }
                    else {
                        numFail++;
                        if (numFail > 1000000) {
                            Utils.die("Bailing out...");
                        }
                    }
                                     
                }
                                                       
                for (SymmetricPair pair: result) {
                    System.out.println(pair.getObj1()+" "+pair.getObj2());
                }
                
            }
            catch (Exception e) {
                Utils.die(e);
            }
        }                                            
        else if (args[0].equals(CMD_BERNOULLI)) {
            double p = Double.parseDouble(args[1]);
            double n = Integer.parseInt(args[2]);            
            for (int i=0; i<n; i++) {
                System.out.println(sampleBernoulli(p));
            }
        }
        else if (args[0].equals(CMD_SAMPLETEST)) {
        Collection<Integer> data = new Range(0,1000000).asList();            
            List<Integer> sample = sampleFromCollection(data, 100000);
            for (Integer val: new IteratorIterable<Integer>(new RandomOrderIterator(sample))) {
                System.out.println(val);
            }
        }
        else if (args[0].equals(CMD_SAMPLE)) {
        	// legacy backwards compatibility; same as CMD_SAMPLE_WITHOUT_REPLACEMENT
            int numToSample; 
        
            if (args.length == 1) {
                numToSample = 1;
            }
            else if (args.length == 2) {
                numToSample = Integer.parseInt(args[1]);
            }
            else {
                Utils.die("Too many args");
                throw new RuntimeException("WhatWhatWhat?!?!?!?");
            }
            
            try {
                List<String> data = IOUtils.readLines();
                List<String> result = RandUtils.sampleWithoutReplacement(data, numToSample); 
                IOUtils.writeCollection(result);
            }
            catch (IOException e) {
                Utils.die(e);
            }                
        }
        else if (args[0].equals(CMD_SAMPLE_WITHOUT_REPLACEMENT)) {
            int numToSample; 
        
            if (args.length == 1) {
                numToSample = 1;
            }
            else if (args.length == 2) {
                numToSample = Integer.parseInt(args[1]);
            }
            else {
                Utils.die("Too many args");
                throw new RuntimeException("WhatWhatWhat?!?!?!?");
            }
            
            try {
                List<String> data = IOUtils.readLines();
                List<String> result = RandUtils.sampleWithoutReplacement(data, numToSample); 
                IOUtils.writeCollection(result);
            }
            catch (IOException e) {
                Utils.die(e);
            }                
        }
        else if (args[0].equals(CMD_SAMPLE_WITH_REPLACEMENT)) {
            int numToSample; 
            String file = null;
            
            if (args.length == 3) {
                file = args[1];
                numToSample = Integer.parseInt(args[2]);
            }
            else {
                Utils.die("Invalid number of arguments (!=3)");
                throw new RuntimeException("Not going to end up here, as we have just died");
            }
            
            try {
                List<String> data = IOUtils.readLines(file);
                List<String> result = RandUtils.sampleWithReplacement(data, numToSample); 
                IOUtils.writeCollection(result);
            }
            catch (IOException e) {
                Utils.die(e);
            }                
        }        
        else if (args[0].equals(CMD_SAMPLEAVERAGES)) {
            try {
                List<Double> data = IOUtils.readObjects(System.in, new StringToDoubleConverter());
                int nToSample = Integer.parseInt(args[1]);
                int nIter = Integer.parseInt(args[2]);                
                for (int i=0; i<nIter; i++) {
                    List<Double> sample = sampleFromCollection(data, nToSample);
                    double sum = MathUtils.sum(sample);
                    System.out.println(sum / nToSample);
                }
                
            }
            catch (IOException e) {
                Utils.die(e);
            }
        }
        else {
        	Utils.die("No such command: "+args[0]);
        }
        
        // test1();
        // test2();
    	// sampleIndicesTest(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        // test3();
    }
    
    /** 
     * Sample one of the objects from the heap having the minimal (or maximal) 
     * key. The heap shall not be modified. Do not accept objects in excludeSet.
     * To be more precise: for the set of objects with the smallest weight 
     * possible such that all objects are not in the excludeset,
     * sample one object. The implementation is probably not optimal.
     */
    public static <T,K extends Comparable<? super K>> T sampleOneMinObject(BinaryHeap<T,K> heap,
                                                                           Set<T> excludeSet) {
        
        if (heap.isEmpty()) {
            throw new RuntimeException("The heap is empty!!!");
        }
        
        Map<T,K> removedObjects = new HashMap();
        
        List<T> validObjects = new ArrayList();
        
        do {
            K topKey = heap.topKey();
            // get all objects with the top key                         
            while (! heap.isEmpty() && heap.topKey() == topKey) {        
                T obj = heap.pop();
                removedObjects.put(obj, topKey);
                if (!(excludeSet.contains(obj))) {
                    validObjects.add(obj);
                }
            }             
        } while (validObjects.size() == 0 && !heap.isEmpty());
        
        if (validObjects.size() == 0) {
            throw new RuntimeException("No objects could be sampled; this implies that excludeset is a superset of the objects in the heap!");
        }
        
        // sample one of the valid objects as the result!
        T result = sampleFromCollection(validObjects, 1).iterator().next();
        
        // put all objects back        
        for (T removedObj: removedObjects.keySet()) {
            K key = removedObjects.get(removedObj);
            heap.add(removedObj, key);                    
        }
        
        return result;
    }
    
    public static void test2 () {
        for (int i=0; i<3; i++) {
            System.out.println(""+randInt(1,2));
        }
    }
    
    public static void sampleIndicesTest (int pListSize, int pNumToSample) {    	
        List<Integer> indices = sampleIndices(pListSize, pNumToSample);
        System.out.println(StringUtils.listToString(indices));
    }
    
    public static void test3 () {
        for (int i=0; i<1000; i++) {
            double rand = getRandomNumberGenerator().nextDouble();
            System.out.println(rand);
        }
    }
    
    public static void test1 (String[] args) {
        int N = Integer.parseInt(args[0]);
        int K = Integer.parseInt(args[1]);
        Range range = new Range(1, N+1);
        int[] vals = range.asIntArr();
        List valList = ConversionUtils.asList(vals);
        System.out.println("original vals:\n"+StringUtils.listToString(valList));
        List sample = sampleWithoutReplacement(valList,  K);
        System.out.println("sampled "+K+"vals:\n"+StringUtils.listToString(sample));
    }
    
        
    private static void usageAndExit(String pErrMsg) {              
        Logger.error(pErrMsg);
        TreeSet<String> availableCommands = new TreeSet(ReflectionUtils.getPublicStaticStringFieldsWithPrefix(RandUtils.class, "CMD_"));
        Logger.info("List of available commands:\n"+StringUtils.collectionToString(availableCommands));
        System.exit(-1);        
    }
    


}
