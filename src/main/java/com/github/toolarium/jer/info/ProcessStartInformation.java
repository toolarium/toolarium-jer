/*
 * ProcessStartInformation.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.jer.info;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Process start information
 *  
 * @author patrick
 */
public class ProcessStartInformation {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessStartInformation.class);
    private static final String SPACE = " ";
    private Instant startupTime;
    private String command;
    private List<String> inputArguments;
    private String bootClassPath;
    private String classPath;
    private String libraryPath;
    private List<String> arguments;
    private Map<String, String> systemProperties;
    private Map<String, String> environmentSettings;
    private Set<String> sensitiveAttributes;

    
    /**
     * Construcotr
     *
     * @param args the arguments
     */
    public ProcessStartInformation(String[] args) {
        startupTime = ProcessHandle.current().info().startInstant().orElse(Instant.now());
        command = ProcessHandle.current().info().command().orElse("java");
                
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        if (mxBean != null) {
            inputArguments = mxBean.getInputArguments();
            
            if (inputArguments == null) {
                inputArguments = new ArrayList<>();
            }
            
            if (mxBean.isBootClassPathSupported()) {
                bootClassPath = mxBean.getBootClassPath();
            } else {
                bootClassPath = "";
            }
            
            classPath = mxBean.getClassPath();
            libraryPath = mxBean.getLibraryPath();
        }
        
        if (args != null) {
            arguments = Arrays.asList(args);
        } else {
            arguments = new ArrayList<>();
        }
        
        systemProperties = streamConvert(System.getProperties());
        environmentSettings = System.getenv();
        sensitiveAttributes = new HashSet<>(); 
    }    
    

    /**
     * Get command line
     * 
     * @param newCommand the new command
     * @param addEnvironment add environment
     * @param addSystemProperty add system property
     * @param escapeValue escape value
     * @param applySenstivieAttribute apply sensitive attributes
     * @return the command line
     */
    public String getCommandLine(final String newCommand, final boolean addEnvironment, final boolean addSystemProperty, final boolean escapeValue, final boolean applySenstivieAttribute) {
        StringBuilder builder = new StringBuilder();
        builder.append(command).append(SPACE);
        
        if (!inputArguments.isEmpty()) {
            builder.append(toString(inputArguments)).append(SPACE);
        }

        if (addSystemProperty && !systemProperties.isEmpty()) {
            LOG.info("System properties [" + filter(systemProperties, new String[] {"sun.", "java.", "jdk.", "os." }));
            builder.append(toString(filter(systemProperties, new String[] {"sun.", "java.", "jdk.", "os." }), "-D", escapeValue, applySenstivieAttribute)).append(SPACE);
        }

        if (!bootClassPath.isBlank()) {
            builder.append(bootClassPath).append(SPACE);
        }

        if (!classPath.isBlank()) {
            if (classPath.endsWith(".-jar")) {
                builder.append("-jar").append(SPACE);
            } else {
                builder.append("-cp").append(SPACE);
            }
            
            if (newCommand == null || newCommand.isBlank()) {
                builder.append(classPath).append(SPACE);
            } else {
                builder.append(newCommand).append(SPACE);
            }
        }

        //private String libraryPath;
        if (!arguments.isEmpty()) {
            builder.append(toString(arguments));
        }

        if (addEnvironment && !environmentSettings.isEmpty()) {
            builder.append(toString(environmentSettings, SPACE, escapeValue, applySenstivieAttribute));
        }

        return builder.toString();
    }


    /**
     * Get the startup time
     *
     * @return the startup time
     */

    public Instant getStartupTime() {
        return startupTime;
    }


    /**
     * Set the startup time
     *
     * @param startupTime the startup time
     */
    public void setStartupTime(Instant startupTime) {
        this.startupTime = startupTime;
    }


    /**
     * Get command
     *
     * @return the the command
     */
    public String getCommand() {
        return command;
    }

    
    /**
     * Set the command
     *
     * @param command the command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    
    /**
     * Get the input arguments
     *
     * @return the input arguments
     */
    public List<String> getInputArguments() {
        return inputArguments;
    }

    
    /**
     * Set the input arguments
     *
     * @param inputArguments the input arguments
     */
    public void setInputArguments(List<String> inputArguments) {
        this.inputArguments = inputArguments;
    }


    /**
     * Get the boot class path
     *
     * @return the boot class path
     */
    public String getBootClassPath() {
        return bootClassPath;
    }

    
    /**
     * Set the boot class path
     *
     * @param bootClassPath the boot class path
     */
    public void setBootClassPath(String bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    
    /**
     * Get the class path
     *
     * @return the class path
     */
    public String getClassPath() {
        return classPath;
    }

    
    /**
     * Set the class path
     *
     * @param classPath the class path
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }


    /**
     * Get the library path
     *
     * @return the library path
     */
    public String getLibraryPath() {
        return libraryPath;
    }


    /**
     * Set the library path
     *
     * @param libraryPath the library path
     */
    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }


    /**
     * Get the arguments
     *
     * @return the arguments
     */
    public List<String> getArguments() {
        return arguments;
    }

    
    /**
     * Set the arguments
     * 
     * @param arguments the arguments
     */

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    
    /**
     * Get the system properties
     *
     * @return the system properties
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }


    /**
     * Set the system properties
     *
     * @param systemProperties the system properties
     */
    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }


    /**
     * Get the environment settings
     *
     * @return the environment settings
     */
    public Map<String, String> getEnvironmentSettings() {
        return environmentSettings;
    }


    /**
     * Set the environment settings
     *
     * @param environmentSettings the environment settings
     */
    public void setEnvironmentSettings(Map<String, String> environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    
    /**
     * Get the sensitive sensitive system properties or environment attributes
     *
     * @return the sensitive system properties or environment attributes
     */
    public Set<String> getSensitiveAttribute() {
        return sensitiveAttributes;
    }

    
    /**
     * Add a sensitive system properties or environment attribute  
     *
     * @param name the name to add
     */
    public void addSensitiveAttribute(String name) {
        if (!sensitiveAttributes.contains(name)) {
            sensitiveAttributes.add(name);
        }
    }

    
    /**
     * Set the sensitive system properties or environment attributes
     *
     * @param sensitiveAttributes the sensitive system properties or environment attributes
     */
    public void setSensitiveAttribute(Set<String> sensitiveAttributes) {
        this.sensitiveAttributes = sensitiveAttributes;
    }

    
    /**
     * Convert a list to a string
     *
     * @param list the list
     * @return the string
     */
    private String toString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        
        int i = 0;
        for (String s : list) {
            if (i > 0) {
                builder.append(SPACE);
            }
            
            builder.append(s);
            i++;
        }

        return builder.toString();
    }

    
    /**
     * Convert a list to a string
     *
     * @param map the map
     * @param keyPrefix the key prefix or null
     * @param escapeValue true to escape values
     * @param applySenstivieAttribute true to suppress the value of the sensitive attributes
     * @return the string
     */
    public String toString(final Map<String, String> map, final String keyPrefix, final boolean escapeValue, final boolean applySenstivieAttribute) {
        StringBuilder builder = new StringBuilder();
        
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (i > 0) {
                builder.append(SPACE);
            }

            if (keyPrefix != null && !keyPrefix.isBlank()) {
                builder.append(keyPrefix);
            }
            
            builder.append(e.getKey());
            
            if (e.getValue() != null && !e.getValue().isBlank()) {
                builder.append("=");
                
                if (applySenstivieAttribute && sensitiveAttributes.contains(e.getKey())) {
                    builder.append("...");
                } else {
                    if (escapeValue) {
                        builder.append("\"");
                    }
                    builder.append(e.getValue());
                    if (escapeValue) {
                        builder.append("\"");
                    }
                }
            }
            
            i++;
        }

        return builder.toString();
    }

    
    /**
     * Convert properties to a map
     *
     * @param input the the input
     * @param ignoreFilter the ignore filter
     * @return the map
     */

    public Map<String, String> filter(Map<String, String> input, String[] ignoreFilter) {
        if (ignoreFilter == null || ignoreFilter.length == 0) {
            return input;
        }
        
        Map<String, String> result = new HashMap<>();
        for (Entry<String, String> e : input.entrySet()) {
            boolean add = e.getValue() != null && !e.getValue().isBlank();
            for (int i = 0; i < ignoreFilter.length; i++) {
                if (e.getKey().startsWith(ignoreFilter[i])) {
                    add = false;
                }
            }
            
            if (add) {
                result.put(e.getKey(), e.getValue());
            }
        }

        return result;
    }

    
    /**
     * Convert properties to a map
     *
     * @param prop the properties
     * @return the map
     */

    private Map<String, String> streamConvert(Properties prop) {
        return prop.entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue()), (prev, next) -> next, HashMap::new));
    }
}
