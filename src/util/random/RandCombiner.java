package util.random;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.CmdLineArgs;
import util.CollectionUtils;
import util.IOUtils;
import util.RandUtils;
import util.SU;
import util.Utils;
import util.collections.MultiMap;
import util.dbg.Logger;

/**
 * Given n sets S_i, generate random sets where there is at most one item from
 * each original set. Do this such that first combine all items into one large
 * set and sample stuff from there; once an item has been sampled, remove the
 * whole set from the set of possible objects to be sampled.
 * To ensure that no object is part of more than one of the original sets,
 * remove first all objects that appear in more than one set. 
 */
public class RandCombiner {
    
    public static void main(String[] pArgs) throws Exception {
        CmdLineArgs args = new CmdLineArgs(pArgs);
  
        int numSetsToSample = args.getIntOpt("numsamples");
        int sampleSize = args.getIntOpt("samplesize");
        String baseName = args.getOpt("basename");
        int firstSample = args.getIntOpt("firstsample", 1);
        
        Pattern p = Pattern.compile("^\\D+(\\d+)\\D+$");        
        
        MultiMap<String, Set<String>> setsByObj = new MultiMap();
        MultiMap<String, String> setNamesByObj = new MultiMap();
        
        List<Integer> setSizes = new ArrayList();
        Set<String> allObjects = new HashSet();
        Set<String> originalSetNames = new HashSet();
        for (String file: args.getNonOptArgs()) {
            Matcher m = p.matcher(file);
            String setName;
            if (m.matches()) {
                setName = m.group(1);
            }
            else {
                setName = file;
            }
            originalSetNames.add(setName);
            List<String> lines = IOUtils.readLines(file);
            Set<String> set = new HashSet(lines);
            allObjects.addAll(set);
            for (String o: set) {
                setsByObj.put(o, set);
                setNamesByObj.put(o, setName);                
           }          
        }
        Logger.info("Originally, there are "+args.getNonOptArgs().length+" sets");
        Logger.info("There are "+allObjects.size()+" objects in total");
        allObjects.clear();
        
//        Set<String> all = new HashSet();
        Map<String, Set<String>> setByObj = new LinkedHashMap<String, Set<String>>();
        Map<String, String> setNameByObj = new LinkedHashMap<String, String>();
        Set<Set<String>> sets = new LinkedHashSet<Set<String>>();
        for (String o: setsByObj.keySet()) {
            Set<Set<String>> setsForThisObj = setsByObj.get(o);
            if (setsForThisObj.size() == 1) {
                // appears in only one set
                Set<String> set = setsForThisObj.iterator().next();
                setByObj.put(o, set);
                setNameByObj.put(o, setNamesByObj.get(o).iterator().next());
//                all.addAll(set);
                sets.add(set);
                allObjects.add(o);
            }
            else {
                // belongs to more than one set => have to discard the object
                // (no action)
            }
        }
        
        Set<String> setNames = new HashSet();
        setNames.addAll(setNameByObj.values());
        Set<String> discardedSetNames = CollectionUtils.minus(originalSetNames, setNames);
        Logger.info("Following sets were discarded altogether: "+SU.toString(discardedSetNames," "));
                
        
        for (Set<String> set: sets) {
            setSizes.add(set.size());
        }
         
        setsByObj = null;
        setNamesByObj = null;
        
        Logger.info("There are "+setByObj.size()+" objects that belong to exactly one set");
        
        Collections.sort(setSizes);
        Logger.info("When all objects that belong to more than one set are removed, we are left with "+setSizes.size()+" sets, with following sizes: ("+SU.toString(setSizes, ",")+")");
        if (sampleSize > setSizes.size()) {
            Utils.die("Not enough sets to sample from.");
        }        
        
        for (int i=0; i<numSetsToSample; i++) {
            int sampleInd = firstSample+i;
            Logger.info("Doing sample: "+sampleInd);
            Set<String> possibleSamplings = new HashSet(allObjects);
            List<String> oneSample = new ArrayList<String>(sampleSize);
            for (int j=0; j<sampleSize; j++) {
                if (possibleSamplings.size() == 0) {
                    throw new RuntimeException("Noo!");
                }
                // sample one
                String o = RandUtils.sampleFromCollection(possibleSamplings, 1).iterator().next();
                oneSample.add(o);
                Set<String> members = setByObj.get(o);
                possibleSamplings.removeAll(members);                
            }
            PrintStream ps = new PrintStream(new FileOutputStream(baseName+"."+sampleInd));
            for (String o: oneSample) {
                String setName = setNameByObj.get(o);
                ps.println(o+" "+setName);
            }
            ps.close();
            
            
        }
        
        
        
    }
}
