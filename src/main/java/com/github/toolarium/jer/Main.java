/*
 * Main.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.jer;


import com.github.toolarium.jer.archive.JarExtractor;
import com.github.toolarium.jer.info.ProcessStartInformation;
import com.github.toolarium.system.command.IAsynchronousProcess;
import com.github.toolarium.system.command.ISystemCommandExecuter;
import com.github.toolarium.system.command.SystemCommandExecuterBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;


/**
 * The jar extracted / exposed runner.
 *   
 * @author patrick
 */
@Command(name = "jer", mixinStandardHelpOptions = true, version = "jer v" + Version.VERSION, description = "Java extract runner.")
public final class Main implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    @Option(names = { "-o", "--overwrite" }, paramLabel = "overwrite", defaultValue = "false", description = "Overwrite already existing extractions.")
    private boolean overwrite;
    @Option(names = { "-d", "--destination" }, paramLabel = "destination", description = "The destination directory, by default the system temp.")
    private String destination;
    @Option(names = { "-v", "--version" }, versionHelp = true, description = "Display version info")
    private boolean versionInfoRequested;
    @Option(names = {"-h", "--help" }, usageHelp = true, description = "Display this help message")
    private boolean usageHelpRequested;
    @Option(names = { "-rf", "--jarResource" }, paramLabel = "resource", description = "Defines the resource inside the jar to execute.")
    private String jarResource;
    @Option(names = { "-rp", "--jarResourcePath" }, paramLabel = "resource", description = "Defines the subpath in the jar to extract, by default everthing will be extracted.")
    private String jarResourcePath;
    private ColorScheme colorSchema = Help.defaultColorScheme(Help.Ansi.AUTO);
    private ProcessStartInformation processStartInformation;

    
    /**
     * Constructor for Main
     * 
     * @param args the arguments
     */
    private Main(String[] args) {
        processStartInformation = new ProcessStartInformation(args);
    }

    
    /**
     * Tha main 
     *
     * @param args the arguments
     * @throws IllegalAccessException In case the file can not be accessed
     * @throws IOException In case of an I/O issue
     */
    public static void main(String[] args) throws IllegalAccessException, IOException {
        // try to install jansi
        AnsiConsole.systemInstall();

        // new webserver
        Main main = new Main(args);

        // parse command line and run
        CommandLine commandLine = new CommandLine(main).setColorScheme(main.colorSchema);        
        commandLine.execute(args);
        
        // try to uninstall jansi
        AnsiConsole.systemUninstall();
    }

    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            File desitionationFile = new JarExtractor().extract(destination, getJarFileName(), jarResourcePath, overwrite);
            
            if (jarResource == null || jarResource.isBlank()) {
                LOG.error("No jar resource found, ending.");
                return;
            }
            
            LOG.info("Start command: " + processStartInformation.getCommandLine(jarResource, false, false, false, true));
            String command = processStartInformation.getCommandLine(jarResource, false, false, false, false);      
            ISystemCommandExecuter executer = SystemCommandExecuterBuilder.create()
                                                                .workingPath(desitionationFile.getName())
                                                                .addToCommand(command)
                                                                .build();
            
            IAsynchronousProcess process = executer.runAsynchronous();
            
            process.waitFor();
            
        } catch (Exception e) {
            LOG.warn("Could not exatract archive: " + e.getMessage(), e);
        }
    }
    
    
    /**
     * Get the jar file name
     *
     * @return the jar file name
     */
    private String getJarFileName() {
        URL urlJar = ClassLoader.getSystemClassLoader().getResource(Main.class.getPackageName().replaceAll("[.]", "/"));
        if (urlJar != null) {
            String urlStr = urlJar.toString();
            int from = "jar:file:".length();
            if (from >= 0) {
                int to = urlStr.indexOf("!/", from);
                if (to >= 0) {
                    String jarFileName = urlStr.substring(from, to);
                    LOG.info("Found jar file [" + jarFileName + "].");
                    return jarFileName;
                }
            }
        }
        return null;
    }
}
