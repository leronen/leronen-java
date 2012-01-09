package util.algorithm.frequentsets;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import util.StringUtils;
import util.collections.HashWeightedSet;
import util.collections.UnorderedPair;
import util.collections.WeightedSet;
import util.comparator.ByStringComparator;
import util.dbg.Logger;

public class FSetMiner {

    private TransactionDB mDb;
    private int mFreqTr_abs;
    private Integer mNormalizationFactor;
    
    public FSetMiner(TransactionDB pDb,
                     double pFreqTr,
                     Integer pNormalizationFactor) {
        mDb =  pDb;        
        mNormalizationFactor = pNormalizationFactor;        
        
        if (pNormalizationFactor != null) {
            // interpret as relative
            mFreqTr_abs = (int)Math.ceil(pNormalizationFactor * pFreqTr);            
        }
        else {
            // interpret as absolute
            mFreqTr_abs = (int)Math.ceil(pFreqTr);
        }
        
        Logger.info("Using freq thresh: "+mFreqTr_abs);
        
    }
        
    public static void main(String[] args) throws Exception {        
        TransactionDB db = new TransactionDB(args[0]);
        double freqTr = Double.parseDouble(args[1]);        
        Integer normalizationFactor = null;
        if (args.length >= 3) {
            normalizationFactor = Integer.parseInt(args[2]);
        }
        FSetMiner miner = new FSetMiner(db, freqTr, normalizationFactor);
        miner.run();                
        
        // System.out.println(db.toString());
    }
    
    public WeightedSet<Item> computeSingleItemFrequencies() {
        WeightedSet<Item> result = new HashWeightedSet();
        for (Transaction t: mDb.getTransactions()) {
            for (Item item: t.itemList) {
                result.add(item);
            }
        }
        return result;
    }
    
    private String freqString(int freq) {
        if (mNormalizationFactor != null) {
            // relative
            return ""+((double)freq)/mNormalizationFactor;
        }
        else {
            // absolute
            return ""+freq;
        }
        
    }
    
    private void run() {
        WeightedSet<UnorderedPair<Item>> pairFreqs = computePairFrequencies();
        List<UnorderedPair<Item>> pairs = new ArrayList(pairFreqs);
        HashSet<UnorderedPair<Item>> frequentPairs = new HashSet();
        Collections.sort(pairs, new ByStringComparator());
        for (UnorderedPair<Item> pair: pairs) {
            int freq = (int)pairFreqs.getWeight(pair);
            if (freq >= mFreqTr_abs) {
//                System.out.println("Frequent pair: "+pair+" "+pairFreqs.getWeight(pair));
                frequentPairs.add(pair);
                System.out.println(freqString(freq)+" "+StringUtils.collectionToString(pair," "));
            }
        }        
                 
        List<ItemSet> fSets_prev = new ArrayList<ItemSet>();
        for (UnorderedPair<Item> pair: pairs) {
            fSets_prev.add(new ItemSet(pair));
        }
        
        ArrayList<ItemSet> fSets = new ArrayList();
        fSets.addAll(fSets_prev);
        
        for (int n = 3; n<=mDb.getNumItems(); n++) {
            if (fSets_prev.size() < n) {
                break;
            }
            Logger.info("Looking for sets of size "+n);            
            
//        for (int n = 3; n<=3; n++) {
            
            // Count here the number of subsets for each candidate (should be n
            // in order for the candidate to be valid 
            WeightedSet<ItemSet> candidates = new HashWeightedSet();
            
            for (ItemSet prevSet: fSets_prev) {
                for (Item item: mDb.getItems()) {
                    if (!(prevSet.contains(item))) {
                        ItemSet candidate = new ItemSet(n);
                        candidate.addAll(prevSet);
                        candidate.add(item);
                        candidates.add(candidate);
                    }
                }               
            }                       
            
            // verify that all subsets are frequent
            List<ItemSet> validCandidates = new ArrayList();
            for (ItemSet candidate: candidates) {
                int numFrequentSubsets = (int)candidates.getWeight(candidate);
                if (numFrequentSubsets == n) {
//                    System.out.println("Valid candidate: "+candidate+" nfrequentsubsets="+numFrequentSubsets);
                    validCandidates.add(candidate);
                }
                else {
//                    System.out.println("Invalid candidate: "+candidate+" nfrequentsubsets="+numFrequentSubsets);
                }                                
            }
            
            System.err.println("There are "+validCandidates.size()+
                               " valid candidates (out of "+candidates.size()+
                               " original candidates)");
                                    
            // (rows 1-4) For each item, record which itemsets it occurs in):

            for (Item A: mDb.getItems()) {
                A.is_contained_in.clear();
            }                         

            for (ItemSet X: validCandidates) {                
                for (Item A: X) {
                    A.is_contained_in.add(X);
                }
            }  

            // (row 5) Set freq count of all itemsets to zero
            for (ItemSet X: validCandidates) {
                X.freq_count = 0;
            }
            
            // (rows 6-12) "database access": count frequencies of all item sets
            for (Transaction t: mDb.getTransactions()) {
                for (ItemSet X: validCandidates) {
                    X.item_count = 0;
                }
                for (Item A: t.itemList) {
                    for (ItemSet X: A.is_contained_in) {
                        X.item_count++;                        
                        if (X.item_count == X.size()) {
                            X.freq_count++;
                        }
                        else if (X.item_count > X.size()) {
                            // TODO: remove assertion
                            throw new RuntimeException("WhatWhatWhat?");
                        }
                    }
                }
            }                       
            
            fSets_prev.clear();
            
            for (ItemSet X: validCandidates) {
                if (X.freq_count >= mFreqTr_abs) {
                    fSets.add(X);
                    fSets_prev.add(X);
                    System.out.println(freqString(X.freq_count)+" "+StringUtils.collectionToString(X," "));
                }
                else {
//                    System.out.println("Candidate not frequent: "+X+" "+X.freq_count);
                }
            }                                   
                    
        }
    }
    
    public WeightedSet<UnorderedPair<Item>> computePairFrequencies() {
        WeightedSet<UnorderedPair<Item>> result = new HashWeightedSet();
        for (Transaction t: mDb.getTransactions()) {
            int n = t.itemList.size();
            for (int i=0; i<n; i++) {
                for (int j=i+1; j<n; j++) {
                    UnorderedPair<Item> pair = 
                        new UnorderedPair<Item>(t.itemList.get(i),
                                                t.itemList.get(j));
                    result.add(pair);
                }
            }
                        
        }
        return result;
    }
    
}
