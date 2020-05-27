/**
 *
 * Authors: Shin Imai
 *          Ronny Recinos
 *          Jon-Michael Hoang
 *
 *
 *
 */

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class launcherClass{
    public static void main(String[] args) throws VMStartException, IllegalConnectorArgumentsException, IOException {

        LaunchingConnector conn = null;
        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();

        for (Connector connector : connectors) {
            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                conn = (LaunchingConnector) connector;
            }
    }

        if (conn == null) throw new Error("No launching connector");

        Map<String, Connector.Argument> arguments = conn.defaultArguments();

        Connector.Argument mainArg = (Connector.Argument) arguments.get("main");

        mainArg.setValue("test");

        Connector.Argument options = (Connector.Argument) arguments.get("options");

        String currentDir = System.getProperty("user.dir");
        System.out.println("The current directory is " + currentDir);
        String option_val;

        option_val = "-cp " + currentDir + "\\src\\main\\java\\MyClass.java";

        options.setValue(option_val);

        VirtualMachine vm = conn.launch(arguments);

        ProcessBuilder pb = new ProcessBuilder("java","-jar","C:\\Users\\Grtis\\Desktop\\course\\launcher\\src\\main\\java\\parseAST.jar");
        Process p = pb.start();
        ///Process process = vm.process();

        File dirOut = new File("C:\\Users\\Grtis\\Desktop\\course\\launcher\\processOutput.txt");
        File errFile = new File("C:\\Users\\Grtis\\Desktop\\course\\launcher\\Errors.txt");

        pb.redirectOutput(dirOut);
        pb.redirectError(errFile);

    }
}
