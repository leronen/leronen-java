package util;

/** Todo: split argParser into "Arguments" and "ArgParser" */
public interface IArgs {
    public String[] getDefinedOptions();
    public String[] getNonOptArgs();                                                                                                                                                                                                
    public String getOpt(String pName);                                                            
    public boolean isDefined(String pName);
    public ArgsDef getDef();

}
