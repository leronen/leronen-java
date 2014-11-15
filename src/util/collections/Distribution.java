package util.collections;

import util.*;
import util.converter.StringToDoubleConverter;
import util.dbg.*;
import java.math.*;

import java.util.*;

 
 
/**   
 *
 * A discrete (multinomial) distribution. Can contain arbitrary weighted objects. The weights of the added 
 * objects need sum to 1; the distribution counts the total weight, and adjusts the weights 
 * accordingly when queried.
 * 
 ** TODO: should actually be called an "multinomial distribution"
 * Hmm, what is the relation of this distribution to the Dirichlet-distribution? Where should dirichlet distribution
 * be implemented?
 *
 * Note that there are dedicated methods to add ObjectWithWeight instances into the distribution.
 * The weights of these objects do not automatically become normalized when they are added; instead 
 * they may be recalculated into the objects themselves (after all objects have been added) by
 * calling assignRelativePropabilitiesToObjects(). Note that this makes it possible to take an arbitrary set
 * of ObjectWithSettableWeight instances, put them into a distribution, and normalize their weights into an
 * distribution.
 * 
 * Note2: The distribution cannot cope with the situation that it contains ObjectWithWeight instances, and 
 * then someone brutally changes the weight of those objects, without telling the Distribution. 
 *
 * Note3: Current implementation does not allow adding same object to the distribution twice (this is contrary to the 
 *        standard weighted set behauviour, where adding objects again just increases the weight...
 *   
 */  
public class Distribution<T> extends HashWeightedSet<T> implements WeightedSet<T> {
            
    /**
	 * 
	 */
	private static final long serialVersionUID = -6666671144178229275L;

	// sum of the original weights; used to normalize weights on demand             
    private double mWeightSum;
    
    // this is a bit vulgar, as really there may be multiple modes...
    private T mMode;
    private double mModeWeight;        

    // for sampling...                        
    private double[] mAccumulatedSums;
    private List<T> mAsList;                        
        
    /** @todo ensure that code in package genetiikka is changed accordingly, as this constructor has changed! */
    public Distribution () {
        super();            
        mWeightSum = 0;
        mModeWeight = 0;
        mMode = null;                
    }
    
    /** Only works when elements are Numbers! */
    public double expectation() {
        Distribution<Number> d = (Distribution<Number>)this;
        double sum = 0;
        for (Number n: d) {
            double prob = d.getWeight(n);
            double term = n.doubleValue() * prob;
            sum += term;
        }
        
        return sum;
        
    }
    
    /** Make a distribution out of a weighted set by normalizing the weights. */
    public Distribution(WeightedSet<T> pWeightedSet) {
        this();
        Iterator<T> elems = pWeightedSet.iterator();
        while(elems.hasNext()) {
            T elem = elems.next();
            double weight = pWeightedSet.getWeight(elem);
            add(elem, weight);
        }                
    }        
    
    /**
     *  An uniform distribution containing elements of pCollection (multiple
     *  occurences should lead to accumulating weights? 

     */
    public Distribution(Collection<T> pCollection) {
        super(pCollection);        
    }
    
    public void add(T pObj, double pFreq) {
        mAccumulatedSums = null; // thus sampling must be initiated again...
                
        if (pFreq < 0) {
            throw new RuntimeException("Trying to add object with <0 weight to distribution; soh, soh!");
        }
        if (Double.isNaN(pFreq)) {
            throw new RuntimeException("Trying to add an object with frequency NaN to distribution!");                        
        }        
        else if (Double.isInfinite(pFreq)) {                                                       
            dbgMsg("Just to make sure:\n"+
                   "positive infinity outputs as: "+Double.POSITIVE_INFINITY+"\n"+
                   "negative infinity outputs as: "+Double.NEGATIVE_INFINITY);                        
            throw new RuntimeException("cannot insert object: "+pObj+", freq is grossly inappropriate: "+pFreq);    
        }
                                                
        if (pFreq != 0) {
            // in the opposite case we don't bother ourselves further; just forget the weightless bastard!
            // OK, assertions tell us that everything seems to be OK        
            super.add(pObj, pFreq);    
           
            // maintain the total weight count                    
            mWeightSum += pFreq;
            
            // maintain the mode
            // @todo: note that, in the following, only the first-come mode gets considered!
            // we might consider randomizing this, such that the k:th mode replaces the old mode with propability 1/k.
            // It's easy to see by induction(if it's true!) that this gives each mode equal propability of ending up
            // as THE mode.
            if (pFreq > mModeWeight) {
                mModeWeight = pFreq;
                mMode = pObj;
            }
        }                                                    
    }
    
    private void initSampling() {
        mAsList = new ArrayList(this);
        int numObjects = size();
        mAccumulatedSums = new double[numObjects];        
        double accumumatedSum = 0.d;                
        for (int i=0; i<numObjects; i++) {            
            Object obj = mAsList.get(i);
            double weight = getWeight(obj);
            accumumatedSum += weight;
            mAccumulatedSums[i] = accumumatedSum;                        
        }                         
    }
        
    public T sample() {
        if (mAccumulatedSums == null) {
            initSampling();
        }
        double r = RandUtils.getRandomNumberGenerator().nextDouble();
        int index = Arrays.binarySearch(mAccumulatedSums, r);
        if (index < 0.d ) {
            // OK, now index = (-(insertion point) - 1)
            index = -index -1;
        }       
        else {
            // exact match (what an unusual occurence!)                        
            index = index + 1;         
        }
        return mAsList.get(index);                      
    }            
        
    public Object sample_old() {
        double r = RandUtils.getRandomNumberGenerator().nextDouble();
        double acccumumatedSum = 0.d;
        Iterator objects = iterator();
        while(objects.hasNext()) {
            Object obj = objects.next();
            double weight = getWeight(obj);
            acccumumatedSum += weight;
            if (acccumumatedSum>r) {
                return obj;    
            }
        }
        // should not happen...
        dbgMsg("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        dbgMsg("!! WARNING! Possible error in sampling !!!!!!!!!!!!!!");
        dbgMsg("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        throw new RuntimeException("Aaagh");
        // return iterator().next();
    }    

    public boolean remove(Object pObj) {
        mAccumulatedSums = null; // thus sampling must be initiated again...
        double weight = super.getWeight(pObj);
        if (weight == 0) {
            return false;
        }
        mWeightSum -= weight;
        return super.remove(pObj);                                     
    }
    
    /** pObjs must contains objects implementing ObjectWithWeight */
    public void addWeightedObjects(Collection pObjs) {
        Iterator objs = pObjs.iterator();
        while (objs.hasNext()) {
            addWeightedObject((ObjectWithWeight)objs.next());            
        }            
    }
    
    public void addWeightedObject(ObjectWithWeight pObj) {
        // dbgMsg("Adding weighted object: "+pObj);
        add((T)pObj, pObj.getWeight());
    }
         
    /** Overridden, as we want the normalized weight */ 
    public double getWeight(Object pObj) {        
        return super.getWeight(pObj)/mWeightSum;
    }
                            
    public List getBest(int pNumToGet) {
        return WeightedSetUtils.getBiggest(this, pNumToGet, false);        
    }
            
    /** 
     * Assign normalized weights to objects, so that the weighted set forms a distribution 
     * The contained objects must implement interface ObjectWithSettableWeight     
     */
    public void assignNormalizedPropabilitiesToObjects() {
        Iterator objs = iterator();
        while(objs.hasNext()) {
            ObjectWithSettableWeight obj = (ObjectWithSettableWeight)objs.next();
            obj.setWeight(getWeight(obj));
        }
    }
    
    public double modeProb() {
        return mModeWeight/mWeightSum;        
    }
                
    /** Odd name... */                
    public boolean isNull() {
        return mWeightSum == 0;
    }                
                
    public T mode() {
        return mMode;             
    }
    
    public double enthropy() {
        Iterator i = iterator();
        double sum = 0;
        double pi;
        
        while (i.hasNext()) {
            Object obj = i.next();                           
            pi = getWeight(obj);                 
            sum+=pi*MathUtils.log(pi, 2);
        }        
        return -sum;             
    }

    
    public static double maxEnthropy(BigInteger pTotalNumOfPossibleValues) {
        // dbgMsg("maxEnthropy: "+pTotalNumOfPossibleValues);
        BigDecimal one = new BigDecimal(1.d);
        one = one.setScale(50);
        BigDecimal singleObjectWeight_big = 
            one.divide(
                new BigDecimal(pTotalNumOfPossibleValues), 
                BigDecimal.ROUND_HALF_DOWN);
        double singleObjectWeight = singleObjectWeight_big.doubleValue();                                     
        // dbgMsg("singleObjectWeight_big: "+singleObjectWeight_big);
        // dbgMsg("singleObjectWeight: "+singleObjectWeight);
        return - MathUtils.log(singleObjectWeight, 2);                            
    }
    
    /** 
      * Calculate enthropy such that the weights of the objects are interpreted as absolute;
      * any excess weight (1-total weight of contained objects) is divided equally to the 
      * (pTotalNumOfPossibleValues-num of contained values) "hypothetical" values.
      */
    public double enthropy_frequentItemsOnly(BigInteger pTotalNumOfPossibleValues) {
        Iterator i = iterator();
        double enthropy = 0.d;        
        double sumWeights = 0.d;
        double pi;
        
        while (i.hasNext()) {
            Object obj = i.next();                           
            pi = super.getWeight(obj);
            sumWeights+=pi;                
            enthropy -= (pi*MathUtils.log(pi, 2));
        }        
        
        int numObjects = size();
        BigInteger numMissingObjects = pTotalNumOfPossibleValues.subtract(BigInteger.valueOf(numObjects));
        
        double missingWeight = 1.d-sumWeights;
        // dbgMsg("missingWeight: "+missingWeight);
        if(missingWeight > 0.d && !(numMissingObjects.equals(BigInteger.valueOf(0)))) {
            BigDecimal singleMissingObjectWeight_big = new BigDecimal(missingWeight).divide(new BigDecimal(numMissingObjects), BigDecimal.ROUND_DOWN);
            double singleMissingObjectWeight = singleMissingObjectWeight_big.doubleValue();                                     
            // dbgMsg("singleMissingObjectWeight_big: "+singleMissingObjectWeight_big);
            // dbgMsg("singleMissingObjectWeight: "+singleMissingObjectWeight);
            double enthropyIncrementForNonFrequentObjects = 
                (missingWeight*MathUtils.log(singleMissingObjectWeight, 2));
            // dbgMsg("enthropyIncrementForNonFrequentObjects: "+enthropyIncrementForNonFrequentObjects);
            enthropy -= enthropyIncrementForNonFrequentObjects;
        }
        
        return enthropy;             
    }
    
    
    /**       
     * Calculate the conditional enthropy, D(p||q) = Sum[over m] (pm * log2 (pm / qm)
     *
     * This intuitively means, in some way, the "cost" of coding the distribution p with the help of distribution q
     * 
     * It can also be interpreted as a similarity measure between p and q.
     *
     * This distribution is the p and pQ is the q.
     * 
     * Note that if there are objects o, for which p(o) != 0 and q(o)==0, then the conditional enthropy is infinite.
     *
     */     
    public double conditionalEnthropy(Distribution pQ) {
        Iterator i = iterator();
        double sum = 0;
        double pi, qi;
        
        while (i.hasNext()) {
            Object obj = i.next();                           
            pi = getWeight(obj);
            qi = pQ.getWeight(obj);
            if (qi  == 0) {
                return Double.POSITIVE_INFINITY;
            }
            else {                       
                sum+=pi*MathUtils.log(pi/qi, 2);
            }
        }        
        return sum;             
    }
    
    public Distribution average(Distribution pQ) {
        double pi, qi;
        Distribution average = new Distribution();
        HashSet allObjs = new HashSet(this);
        allObjs.addAll(pQ);
        Iterator i = allObjs.iterator();                
        while (i.hasNext()) {
            Object obj = i.next();                           
            pi = getWeight(obj);
            qi = pQ.getWeight(obj);
            average.add(obj, (pi+qi)/2);                        
        }        
        return average;        
    }                
    
    @SuppressWarnings("unused")
    private static class TestObject implements ObjectWithWeight {
        private double mFreq;
        
        TestObject (double pFreq) {
            mFreq = pFreq;
        }
        
        public double getWeight() {
            return mFreq;            
        }
        
        public void setFreq(double pFreq) {
            mFreq = pFreq;    
        }
        
        public String toString() {
            return "TestObject: "+mFreq;
        }
    }
    
    public String toString() {  
        return StringUtils.distributionToString(this, "; ", "\n");
    }              
    
    @SuppressWarnings("unused")
    private static final int[] TEST_ARRAY = {
        1,
        4,
        3,
        6
    };
                                    
    /** Note that this is somewhat costly, as new map needs to be created */
    public Map asObjToWeightMap() {        
        LinkedHashMap result = new LinkedHashMap();
        Iterator objs = iterator();
        while(objs.hasNext()) {
            Object obj = objs.next();
            result.put(obj, new Double(getWeight(obj)));
        }
        
        return Collections.unmodifiableMap(result);            
    }    
        
   /**
    * @return a map where keys are non-overlapping components of pRange, and
    * values are values of the Distribution.
    */         
    public Map<Range, T> splitRange(Range pRange) {
        // Logger.info("Starting Distribution.splitRange("+pRange+")");         
        int totalLen = pRange.length();
        int numComponents = MathUtils.min(totalLen, size());
        int[] componentLengths = new int[numComponents];        
        int totalComponentLen = 0;
        Iterator<T> objects = iterator();                
        for (int i=0; i<numComponents; i++) {
            Object o = objects.next();            
            double weight = getWeight(o);            
            componentLengths[i] = (int) (weight * totalLen);
            totalComponentLen += componentLengths[i];             
        }        
        int extraLen = totalComponentLen-totalLen;        
        int numComponentsToMakeShorter = extraLen > 0 ? extraLen : 0;
        int numComponentsToMakeLonger = extraLen < 0 ? -extraLen : 0;        
        for (int i=0; i<numComponentsToMakeShorter; i++) {
            componentLengths[i%numComponents]--;
        }        
        for (int i=0; i<numComponentsToMakeLonger; i++) {
            componentLengths[i%numComponents]++;
        }        
        
        LinkedHashMap<Range, T> result = new LinkedHashMap<Range, T>();
        int rangeStart = 0;
        objects = iterator();        
        for (int i=0; i<numComponents; i++) {
            int rangeEnd = rangeStart+componentLengths[i];
            Range range = new Range(rangeStart, rangeEnd);
            result.put(range, objects.next());
            rangeStart = rangeEnd;               
        }        
        return result;
    }        
        
    /**
     * Override the superclass implementation, as it is easier to check 
     * distributions for equality than it is for general weighted sets */        
    public boolean equals(Object pObj) {
        if (!(pObj instanceof Distribution)) {
            // the other object is not a distribution
            return false;    
        }
        Distribution otherDistribution = (Distribution)pObj;
        
        if (size() != otherDistribution.size()) {
            // the distributions have different number of objects
            return false;    
        }
            
        if (isEmpty() && otherDistribution.isEmpty()) {
            // both are empty
            return true;
        }            
        
        // OK,  both have the same number of objects and are non-empty
        // check whether the other distribution has the same objects with same weights
        Iterator ourObjs = iterator();
        while(ourObjs.hasNext()) {
            Object obj = ourObjs.next();
            if (getWeight(obj) != otherDistribution.getWeight(obj)) {
                return false;
            }            
        }        
        // The other distribution had the same entries as we, with same weights as well
        // note that there is no need to check that the opposite condition, that is that we have all the entries that 
        // the other distribution has, holds; that is because sum of weights in a distribution is always 1, so as sum of 
        // our weights sums 1 and we have allready checked that the other distribution has the same entries as we have, 
        // the other distribution cannot have any entries that we do not have, as it would then have sum of weights > 1
        // so, here's what we happily do:
        return true;
        
    }
    
    public int hashCode() {
        throw new RuntimeException("Hashcode not currently implemented!");
    }
    
    public static void main (String[] args) throws Exception {
        // test1(args);
        test2();
    }
    
        
    public static void test1 (String[] args) {
        
        Distribution d1 = new Distribution();
        for (int i=0; i<args.length; i++) {
            String s = args[i];
            double val = Double.parseDouble(s);
            d1.add(new Double(val), val);
        }        
        
        Distribution d2 = new Distribution();
        for (int i=0; i<10000000; i++) {
            if (i%10000 == 0) {
                System.out.println("sampling with new method...");
                d2.add(d1.sample());
            }                
        }
        
        for (int i=0; i<10000000; i++) {
            if (i%10000 == 0) {
                System.out.println("sampling with old method...");
                d2.add(d1.sample_old());
            }                
        }        
        System.out.println("d1:\n"+d1);
        System.out.println("d2:\n"+d2);        
    }
    
    public static void test2 () throws Exception {
        // int val = Integer.parseInt(args[0]); 
        // System.out.println("maxEnthropy for "+val+": "+maxEnthropy(BigInteger.valueOf(val)));
        
        List<Double> vals = IOUtils.readObjects(System.in, new StringToDoubleConverter()); 
        Distribution<Double> d = new Distribution<Double>(vals);  
        System.out.println("Expectation="+d.expectation());
    }

    
    
    private static void dbgMsg(String pMsg) {
        Logger.dbg("Distribution: "+pMsg);
    }
    
            
}
