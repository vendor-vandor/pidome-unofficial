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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;

/**
 * Loads the classes for the packages
 * @author John Sirach
 */
public class PackageClassLoader extends PackageLibraryLoader {

    static Logger LOG = LogManager.getLogger(PackageClassLoader.class);
    boolean permissionsRead = false;
    String permissions = "{}";
    
    /**
     * Constructor also initializes the jar loader
     */
    PackageClassLoader(String packageName, String jarFile) throws ConfigPropertiesException { 
        super(packageName,jarFile);
    }
    
    /**
     * Loads a class from a package.
     * @param className
     * @return The class contained in the current package
     * @see #loadPackageClass(java.lang.String, java.lang.String) 
     * @throws ClassNotFoundException 
     */
    public final Class<?> classFromPackage(String className) throws ClassNotFoundException {
        try {
            LOG.debug("Loading package {} from {}", className,getPackageName());
            return loadPackageClass(createClassUrl(className));
        } catch (ConfigPropertiesException ex) {
            throw new ClassNotFoundException("Problem loading " + className + " from " + getPackageName() + ": " + ex.getMessage());
        }
    }
    
    /**
     * Loads a class from a package and adds the package to the package loader.
     * @param className
     * @return The class in the current package
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.config.ConfigPropertiesException 
     */
    protected final Class<?> loadPackageClass(String className) throws ClassNotFoundException,ConfigPropertiesException {
        LOG.debug("Loading class {} from {}", className, getPackageName());
        Class<?> loadedClass = loadClass(className, false);
        return loadedClass;
    }
    
    /**
     * Creates a correct ClassPath because PiDome does calls on packages and not on classes.
     * 
     * @param classPackage
     * @return FQN of a driver class
     */
    protected final String createClassUrl(String classPackage){
        String className = classPackage.substring(classPackage.lastIndexOf("."));
        return classPackage + "." + className.substring(1, 2).toUpperCase() + className.substring(2);
    }
 
    /**
     * Returns the configured package permissions.
     * This package contained file is read only once and at the moment only the master jar file is known in the class loader where the policy file should reside.
     * @return
     * @throws PackagePermissionsNotAvailableException 
     */
    protected final String getPackagePermissions() throws PackagePermissionsNotAvailableException {
        if(!permissionsRead){
            permissionsRead = true;
            String policyFile = new StringBuilder(getPackageName()).append(".policy.json").toString();
            try (InputStreamReader input = new InputStreamReader(this.getResourceAsStream(policyFile));
                 BufferedReader br = new BufferedReader(input)) {
                permissions = br.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException | NullPointerException ex){
                LOG.error("Could not read policy file '{}' from {}: {}",policyFile, getPackageName(), ex.getMessage());
                throw new PackagePermissionsNotAvailableException("Could not read package needed permissions from '" + getPackageName() + "'. Package invalid, contact author.", ex);
            }
        }
        return permissions;
    }
    
}