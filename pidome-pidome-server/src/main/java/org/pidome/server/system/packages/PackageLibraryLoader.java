/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.system.packages;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 * Responsible for adding jar files to the class loader used by the packaging system.
 * @author John Sirach
 */
public class PackageLibraryLoader extends URLClassLoader {
    
    private String mainPackageName = "";
    private String jarFile = "";
    
    private URL absoluteJarLocation;
    
    private boolean packageApproved = false;
    
    static Logger LOG = LogManager.getLogger(PackageLibraryLoader.class);

    /**
     * Constructor, also registers an history of previously loaded jars.
     * Need to check if a history is wanted.
     * @param packageName
     * @param jarFile
     * @throws org.pidome.server.system.config.ConfigPropertiesException
     */
    protected PackageLibraryLoader(String packageName,String jarFile) throws ConfigPropertiesException {
        super(getPackageUrl(packageName, jarFile).toArray(new URL[1]));
        this.mainPackageName = packageName;
        this.jarFile = jarFile;
        absoluteJarLocation = getURLs()[0];
    }

    /**
     * Returns the absolute path to the jar file.
     * @return 
     */
    protected final URL getAbsoluteLibraryPath(){
        return absoluteJarLocation;
    }
    
    /**
     * Sets the base dependencies for this package.
     */
    protected void setPackageLibraries(){
        for(URL url:SystemConfig.getLibraryPaths()){
            this.addURL(url);
        }
        try {
            String dirName = SystemConfig.getProperty("system", "packages.location") + mainPackageName + File.separator + "lib" + File.separator;
            File file = new File(dirName);
            if(file.exists()){
                Files.walkFileTree(file.toPath(), new urlPaths());
                LOG.trace("Library paths for this package: {}", (Object[])this.getURLs());
            } else {
                LOG.debug("No external library dependencies loaded as {} does not exist.", dirName);
            }
        } catch (IOException | ConfigPropertiesException ex) {
            LOG.error("Could not collect libary paths: {}", ex.getMessage());
        }
    }
    
    /**
     * Class to be used to set the paths with a walkFileTree.
     */
    private class urlPaths extends SimpleFileVisitor<Path> {
        
        private List<URL> librariesList = new ArrayList<>();
        
        /**
         * Executed when a file is found.
         * It gets the url from a file to be added to the library paths.
         * @param file
         * @param attr
         * @return 
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            try {
                librariesList.add(file.toUri().toURL());
            } catch (MalformedURLException ex) {
                LOG.error("Could not add {} to libraries path", file.getFileName());
            }
            return CONTINUE;
        }
        
        /**
         * Executed when a file failed.
         * @param file
         * @param exc
         * @return 
         */
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                LOG.error("cycle detected while adding libraries: " + file);
            }
            return CONTINUE;
        }
        
        /**
         * Executed when all done.
         * @param file
         * @param exc
         * @return 
         */
        @Override
        public FileVisitResult postVisitDirectory(Path file, IOException exc) {
            for (URL url : librariesList) {
                addURL(url);
            }
            return CONTINUE;
        }
        
    }
    
    /**
     * Returns the package name.
     * @return 
     */
    protected final String getPackageName(){
        return mainPackageName;
    }
    
    /**
     * Adds the package to the class loader if not done already.
     * It checks if the package exists before adding 
     * @throws org.pidome.server.system.config.ConfigPropertiesException 
     */
    private static List<URL> getPackageUrl(String mainPackageName, String jarFile) throws ConfigPropertiesException {
        try {
            String filename = SystemConfig.getProperty("system", "packages.location") + mainPackageName + File.separator + jarFile;
            File file = new File(filename);
            if (!file.exists()) {
                LOG.error("Package '{}' does not exist ({})", mainPackageName,jarFile);
                throw new ConfigPropertiesException("Package "+filename+" does not exist");
            } else {
                return new ArrayList<URL>(){{
                    add(file.toURI().toURL());
                }};
            }
        } catch (IOException ex) {
            LOG.error("Problem loading {} ({})", mainPackageName,jarFile);
            throw new ConfigPropertiesException("Package "+mainPackageName+" ("+jarFile+") could not be found");
        }
    }

    /**
     * Adds the package URL to the class loader and the history of loaded jars.
     * The question is if we want this. If it is to much a breach disable this.
     * @param jar 
     */
    protected final void addPackageUrl(URL jar) {
        absoluteJarLocation = jar;
        addURL(jar);
        LOG.debug("{} added to loader for main package '{}'", jar.toString(), mainPackageName);
    }
}
