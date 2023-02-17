/*
 * JarExtractor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.jer.archive;

import com.github.toolarium.jer.util.StreamUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jar file extractor
 * 
 * @author patrick
 */
public class JarExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(JarExtractor.class);
    private File desitionationFile;
    private boolean createdPath;

    
    /**
     * Constructor for JarExtractor
     */
    public JarExtractor() {
        desitionationFile = null;
        createdPath = false;
        
    }
    
    
    /**
     * Extract file
     *
     * @param destination the destination
     * @param filename the filename
     * @param jarResource the jar resource
     * @param overwrite true to overwrite
     * @return the destination
     * @throws IllegalAccessException In case the file can not be accessed
     * @throws IOException In case of an I/O issue
     */
    public File extract(String destination, String filename, String jarResource, boolean overwrite) throws IllegalAccessException, IOException {
        File jarfile = validateFilename(filename);
        JarFile jar = new JarFile(filename);
        
        try {
            desitionationFile = prepareDestinationPath(jarfile, destination);
            if (!overwrite && desitionationFile.exists()) {
                LOG.info(".: Already exist [" + desitionationFile + "]!");
                return desitionationFile;
            }
            createdPath = desitionationFile.mkdirs();

            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry jarFileEntry = enumEntries.nextElement();
                
                if (!filterFile(jarResource, jarFileEntry)) {
                    File f = new File(desitionationFile.getPath() + "/" + jarFileEntry.getName());
                    if (jarFileEntry.isDirectory()) { // if its a directory, create it
                        f.mkdir();
                    } else {
                        copyJarContent(jar, jarFileEntry, f);
                    }
                    
                    //Files.setLastModifiedTime(f.toPath(), jarFileEntry.getLastModifiedTime());
                }
            }
            return desitionationFile;
        } catch (IOException e) {
            LOG.warn("Could not exatract archive: " + e.getMessage(), e);
            cleanUp();
            throw e;
        } finally {
            try {
                jar.close();
            } catch (IOException e) {
                // NOP
            }
        }
    }

    
    /**
     * Validate the filename
     * 
     * @param filename the file name
     * @return the file
     * @throws IllegalAccessException In case the file can not be accessed
     */
    public File validateFilename(String filename) throws IllegalAccessException {
        if (filename == null) {
            throw new IllegalAccessException("Invalid setup.");
        }
        
        File jarfile = new File(filename);
        if (!jarfile.canRead()) {
            throw new IllegalAccessException("Can not read file [" + filename + "]!");
        }
        return jarfile;
    }


    /**
     * Cleanup
     */
    public void cleanUp() {
        if (createdPath && desitionationFile != null) {
            if (!desitionationFile.delete()) {
                desitionationFile.deleteOnExit();
            }
        }
    }

    
    /**
     * Create the destination Path
     * 
     * @param jarfile the jar file
     * @param destination the destination
     * @return the created path
     * @throws IOException In case of an I/O issue
     */
    private File prepareDestinationPath(File jarfile, String destination) throws IOException {
        String jarFileName = jarfile.getName();
        int idx = jarFileName.lastIndexOf('.');
        if (idx > 0) {
            jarFileName = jarFileName.substring(0, idx);
        }
        
        String destinationPath = destination;
        if (destinationPath == null) {
            destinationPath = System.getProperty("java.io.tmpdir").replace('\\', '/');
        }
        
        if (!destinationPath.endsWith("/")) {
            destinationPath += "/";
        }
        
        FileTime fileTime = Files.getLastModifiedTime(jarfile.toPath(), LinkOption.NOFOLLOW_LINKS);
        String outputPath = destinationPath + jarFileName + "-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss.SSS").withZone(ZoneId.of("Z")).format(fileTime.toInstant());
        LOG.debug("Created path [" + outputPath + "]");
        return new File(outputPath);
    }

    
    /**
     * Copy the jar content
     * 
     * @param jar the jar file
     * @param jarFileEntry the jar file entry
     * @param outputFile the output file
     * @return true if it was successful copied
     * @throws IOException In case of an I/O error
     */
    private boolean copyJarContent(JarFile jar, JarEntry jarFileEntry, File outputFile) throws IOException {
        boolean successfileCopied = false;
        InputStream is = jar.getInputStream(jarFileEntry); 
        
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        StreamUtil.getInstance().channelCopy(is, fos);
        successfileCopied = true;
        
        try {
            fos.close();
        } catch (IOException e) {
            successfileCopied = false;
        }
        
        try {
            is.close();
        } catch (IOException e) {
            successfileCopied = false;
        }
        
        return successfileCopied;
    }
    
    
    /**
     * Check if the file must be filtered
     * 
     * @param jarResource the jar resource
     * @param file the file to check
     * @return true to filter; otherwise do not filter
     */
    private boolean filterFile(String jarResource, JarEntry file) {
        if (jarResource == null || jarResource.isBlank()) {
            return false;
        }
        
        return file.getName().startsWith(jarResource);
    }
}
