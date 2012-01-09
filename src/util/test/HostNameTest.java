package util.test;

import java.net.UnknownHostException;

import util.Utils;

public class HostNameTest {

    public static void main(String[] args) throws UnknownHostException  {        
        String hostName = args[0];
    
        System.out.println(Utils.getCanonicalHostName(hostName));
    }
    
    
}
