package util.test;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import util.IOUtils;
import util.bc.ArgsDef;
import util.bc.CmdLineArgs;

public class GzipTest {


    public static void main(String[] args) throws Exception {
        run(args);
    }

    private static void run(String[] pArgs) throws Exception {
        ArgsDef argsDef = new ArgsDef();
        argsDef.addMandatoryOptions("n");
        argsDef.addFlag("z");
        CmdLineArgs args = new CmdLineArgs(pArgs, argsDef);
        long n = args.getLongOpt("n");
        boolean zip = args.hasFlag("z");
        IOUtils.setFastStdout();
        PrintWriter out;

        if (zip) {
            out = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(System.out)));
        }
        else {
            out = new PrintWriter(new OutputStreamWriter(System.out));
        }

        for (long i=1; i<=n; i++) {
            out.println(i);
        }

        out.close();
    }

}
