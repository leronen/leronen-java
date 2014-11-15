package util.bc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import util.CollectionUtils;
import util.StringUtils;
import util.collections.graph.BipartiteGraph;

public class ArgsDef {

    private Set<String> flagNames;
    /** null, if not specified (anything goes) */
    private Set<String> optionNames;
    /** null, if not specified (anything goes). This is a subset of non-mandatory options. */
    private Set<String> mandatoryOptions;
    private final Map<String, String> defaultValues;
    /**Names of non-opt arguments  (empty if not specified).
     * Dedicated list for each number of allowed arguments */
    private final Map<Integer, List<String>> argumentNamesByLength;
    /** Not used if null. Used only for USAGE purposes */
    private String progName;

    boolean checkNumArguments = false;

    public ArgsDef(String ... flagNames) {
        this(Arrays.asList(flagNames));
    }

    public ArgsDef(List<String> flagNames) {
        this(new HashSet<String>(flagNames));
    }

    public ArgsDef(Set<String> flagNames) {
        this.flagNames = flagNames;
        this.defaultValues = new HashMap<String, String>();
        this.argumentNamesByLength = new TreeMap<Integer, List<String>>();
        this.argumentNamesByLength.put(0, new ArrayList<String>());
    }

    private final BipartiteGraph<Integer, String> optNameByOptGroup = new BipartiteGraph<Integer, String>();

    /** Add a group of mutually exclusive options. At most one must be selected. No separate adding of same options is needed */
    public void addOptionGroup(String... opts) {
        int groupId = optNameByOptGroup.getSrcNodes().size()+1;
        if (optionNames == null) {
            optionNames = new HashSet<String>();
        }
        for (String opt: opts) {
            optionNames.add(opt);
            optNameByOptGroup.put(groupId, opt);
        }
    }

    public List<Set<String>> getOptionGroups() {
        List<Set<String>> result = new ArrayList<Set<String>>();
        for (int groupId: optNameByOptGroup.getSrcNodes()) {
            Set<String> opts = optNameByOptGroup.followers(groupId);
            result.add(opts);
        }
        return result;

    }

    public void addDefaultValue(String name, String value) {
        this.defaultValues.put(name, value);
    }

    /** may be called multiple times with argument lists of different length! */
    public void setArgumentNames(String...names) {
        checkNumArguments = true;
        List<String> list = Arrays.asList(names);
        argumentNamesByLength.put(list.size(), list);
    }

    public void setFlagNames(String... names) {
        this.flagNames =  new LinkedHashSet<String>(Arrays.asList(names));
    }

    /**
     * Check if option names have been defined. If yes, then it is considered an error to have options
     * that are not included in the specified set of names.
     */
    public boolean hasOptionNames() {
        return optionNames != null;
    }

    public void setProgname(String progName) {
        this.progName = progName;
    }

    /**
     * Check whether this is a valid option name.
     *
     * @throws RuntimeException if no option names have been defined.
     */
    public boolean isOptionName(String name) {
        if (optionNames == null) {
            throw new RuntimeException("No option names have been defined");
        }

        return optionNames.contains(name);
    }

    boolean isValidNumArgs(int numArgs) {
        if (!checkNumArguments) {
            return true;
        }
        return argumentNamesByLength.keySet().contains(numArgs);
    }

    public void addOptions(String... names) {
        if (this.optionNames == null) {
            this.optionNames = new LinkedHashSet<String>();
        }
        this.optionNames.addAll(Arrays.asList(names));
    }

    public boolean hasDefaultValue(String key) {
        return defaultValues.containsKey(key);
    }

    public void addFlag(String flagName) {
        flagNames.add(flagName);
    }

    /** sets optionnames as a side-effect */
    public void addMandatoryOptions(String... names) {
        if (optionNames == null) {
            optionNames = new LinkedHashSet<String>();
        }
        if (mandatoryOptions == null) {
            mandatoryOptions = new LinkedHashSet<String>();
        }

        optionNames.addAll(Arrays.asList(names));
        mandatoryOptions.addAll(Arrays.asList(names));
    }

    private Set<String> getNonMandatoryOptions() {
        return CollectionUtils.minus(optionNames, mandatoryOptions);
    }

    @Override
    public String toString() {
        List<String> tmp = new ArrayList<String>();
        if (mandatoryOptions != null) {
            tmp.add("MANDATORY OPTIONS: \n\t-"+StringUtils.colToStr(mandatoryOptions, "\n\t-"));
            Set<String> nonMandatory = getNonMandatoryOptions();
            if (nonMandatory.size() > 0) {
                tmp.add("NON-MANDATORY OPTIONS: \n\t-"+StringUtils.colToStr(nonMandatory, "\n\t-"));
            }
        }
        else {
            // no mandatory options
            if (optionNames != null) {
                tmp.add("OPTIONS: \n\t-"+StringUtils.colToStr(optionNames, "=VAL\n\t-")+"=VAL");
            }
        }

        if (optNameByOptGroup.getSrcNodes().size() > 0) {
            tmp.add("MUTUALLY EXCLUSIVE OPTION GROUPS:");
            for (Set<String> group: getOptionGroups()) {
                tmp.add("\t(-"+StringUtils.colToStr(group, ", -" )+")");
            }
        }

        if (flagNames.size() > 0) {
            tmp.add("FLAGS: \n\t-"+StringUtils.colToStr(flagNames, "\n\t-"));
        }
        return StringUtils.colToStr(tmp, "\n");
    }


    /** check whether this option has been defined as a flag with no value */
    public boolean isFlag(String name) {
        return flagNames.contains(name);
    }

    public boolean hasMandatoryOptionNames() {
        return mandatoryOptions != null;
    }

    public Set<String> getOptionNames() {
        return Collections.unmodifiableSet(optionNames);
    }

    /** Does not include flags! */
    public boolean hasOption(String value) {
        return optionNames.contains(value);
    }

    public boolean isMandatoryOption(String name) {
        return mandatoryOptions.contains(name);
    }

    /** @return null if no default value for said arg */
    public String getDefaultValue(String name) {
        return defaultValues.get(name);
    }

    public Set<String> getMandatoryOptions() {
        return Collections.unmodifiableSet(mandatoryOptions);
    }

    /**
     * Rturn the position of a positional argument by name.
     * Naturally requires that names have been previously registered.
     *
     * @param numArgs as there may be multiple alternative arg name vectors with different numargs, always require this.
     * @throws RuntimeException if one of the following:
     *   - no such number of args registered.
     *   - no such arg (with the given number of args)
     */
    public int getArgPos(String argName, int numArgs) {
        List<String> names = argumentNamesByLength.get(numArgs);
        if (names == null) {
            throw new RuntimeException("No argument names registered (numargs: "+numArgs+")");
        }
        int index = names.indexOf(argName);
        if (index == -1) {
            throw new RuntimeException("No such argument: "+argName);
        }
        return index;
    }

    public String usage() {
        String progNameForUsage = progName != null ? progName : "PROGNAME";
        String optString = hasOptionNames() ? " [OPTIONS]" : "";
        String flagString = flagNames.size() > 0 ? " [FLAGS]" : "";
        if (argumentNamesByLength.size() == 0 ) {
            // no argument names specified
            return "Usage: " + progNameForUsage + optString + flagString + " [ARGUMENTS]";
        }
        else if (argumentNamesByLength.size() == 1 ) {
            // single usage
            List<String> names = argumentNamesByLength.values().iterator().next();
            return "Usage: " + progNameForUsage + optString + flagString + " " + StringUtils.colToStr(names, " ")+"\n"+
                    toString();
        }
        else {
            // many usages
            StringBuffer result = new StringBuffer();
            int usageNum = 1;
            for (Integer numArgs: argumentNamesByLength.keySet()) {
                if (usageNum > 1) {
                    result.append("\n");
                }
                List<String> names = argumentNamesByLength.get(numArgs);
                String tmp = "Usage " + usageNum + ": " + progNameForUsage + optString + flagString + " " + StringUtils.colToStr(names, " ");
                result.append(tmp);
                usageNum++;
            }
            result.append("\n");
            result.append(toString());
            return result.toString();
        }

    }

}
