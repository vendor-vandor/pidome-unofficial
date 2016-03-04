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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.pidome.server.system.config.ConfigPropertiesException;

/**
 *
 * @author John Sirach
 */
public class PackageConfig {
    
    Map<String,String> packageCollection = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(PackageConfig.class);
    
    /**
     * Constructor
     * @throws ConfigPropertiesException 
     */
    public PackageConfig() throws ConfigPropertiesException {
        setInstalledPackages();
    }
    
    /**
     * Initializes the package structure and preloads.
     * @throws ConfigPropertiesException 
     */
    final void setInstalledPackages() throws ConfigPropertiesException {
        PackageConfigLoader config = new PackageConfigLoader(PackageConfigLoader.PACKAGES);
        String configContent = config.getContents();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xPath = xpathFactory.newXPath();
        InputSource xmlSource = new InputSource(new StringReader(configContent));
        try {
            NodeList nodes = (NodeList) xPath.evaluate("/packagesfiles/packagefile", xmlSource, XPathConstants.NODESET);
            int nodeLength = nodes.getLength();
            if (nodeLength > 0) {
                for (int i = 0; i < nodeLength; i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        if (element.hasAttribute("file")) {
                            try {
                                setPackageData(element.getAttribute("file"));
                            } catch (ConfigPropertiesException ex) {
                                LOG.error("Package error: " + ex.getMessage(), ex);
                            }
                        } else {
                            LOG.error("incorrect package configuration found at node number: " + (i + 1));
                        }
                    }
                }
            } else {
                LOG.warn("No packages installed, you should minimal have the PiDome package");
            }
        } catch (XPathExpressionException ex) {
            LOG.error("This should absolutely not happen", ex);
        } catch (NullPointerException ex) {
            LOG.error("This should not happen", ex);
        }
    }
    
    /**
     * Sets package data in local cache.
     * @param packageFileName
     * @throws ConfigPropertiesException 
     */
    final void setPackageData(String packageFileName) throws ConfigPropertiesException {
        packageCollection.put(packageFileName, loadPackageFile(packageFileName));
    }
    
    /**
     * Loads a package file.
     * @param packageFileName
     * @return
     * @throws ConfigPropertiesException 
     */
    final String loadPackageFile(String packageFileName) throws ConfigPropertiesException {
        PackageConfigLoader config = new PackageConfigLoader(PackageConfigLoader.PACKAGE, packageFileName);
        return config.getContents();
    }
    
    /**
     * Returns the complete packages collection
     * @return 
     */
    public final Map<String,String> getPackages(){
        return packageCollection;
    }

}
