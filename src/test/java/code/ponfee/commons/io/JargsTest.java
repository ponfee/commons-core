package code.ponfee.commons.io;

import jargs.gnu.CmdLineParser;

public class JargsTest {
    public static void main( String[] arguments) {
        final CmdLineParser cmdLineParser = new CmdLineParser();
        final CmdLineParser.Option fileOption = new CmdLineParser.Option.StringOption('f', "file");
        cmdLineParser.addOption(fileOption);
        final CmdLineParser.Option verbosityOption = new CmdLineParser.Option.BooleanOption('v', "verbose");
        cmdLineParser.addOption(verbosityOption);
        try
        {
            cmdLineParser.parse(arguments);


            final String filePathName = cmdLineParser.getOptionValue(fileOption).toString();
            System.out.println(filePathName);
            System.out.println(
                    "File path/name is " + filePathName
                            + " and verbosity is " + cmdLineParser.getOptionValue(verbosityOption)
                            + ".");
        }
        catch (CmdLineParser.IllegalOptionValueException | CmdLineParser.UnknownOptionException exception)
        {
            System.out.println("Unable to parse command line options - " + exception);
            System.exit(-1);
        }
    }
}
