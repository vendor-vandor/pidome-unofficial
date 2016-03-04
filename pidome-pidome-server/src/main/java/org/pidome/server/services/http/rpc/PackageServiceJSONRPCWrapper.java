/*
 * Copyright 2015 John.
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
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.packages.FormattedPermissions;
import org.pidome.server.system.packages.PackageException;
import org.pidome.server.system.packages.Packages;
import org.pidome.server.system.packages.Package;

/**
 *
 * @author John
 */
public class PackageServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements PackageServiceJSONRPCWrapperWrapperInterface {

    static Logger LOG = LogManager.getLogger(PackageServiceJSONRPCWrapper.class);
    
        /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPackages", null);
                put("getPackageDetails", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("setPackageApproved", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("setPackageDisApproved", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public ArrayList<Map<String,Object>> getPackages() throws PackageException {
        boolean secEnabled = false;
        try {
            secEnabled = SystemConfig.getProperty("system", "server.securitymanagerenabled").equals("true");
        } catch (ConfigPropertiesException ex) {
            LOG.error("Could not determine if packages restrictions are enabled or not: {}", ex.getMessage());
        }
        ArrayList<Map<String,Object>> allPackages = new ArrayList<>();
        for(Map.Entry<Integer,Package> installedPackage:Packages.getInstance().getPackages().entrySet()){
            Map<String,Object> singlePackage = new HashMap<>();
            singlePackage.put("id", installedPackage.getValue().getId());
            singlePackage.put("name", installedPackage.getValue().getPackageName());
            singlePackage.put("author", installedPackage.getValue().getPackageAuthor());
            singlePackage.put("website", installedPackage.getValue().getPackageWebsite());
            singlePackage.put("auth", installedPackage.getValue().isAuthorized());
            singlePackage.put("restrictionenabled", secEnabled);
            allPackages.add(singlePackage);
        }
        return allPackages;
        
    }

    @Override
    public Object getPackageDetails(Number packageId) throws PackageException {
        boolean secEnabled = false;
        try {
            secEnabled = SystemConfig.getProperty("system", "server.securitymanagerenabled").equals("true");
        } catch (ConfigPropertiesException ex) {
            LOG.error("Could not determine if packages restrictions are enabled or not: {}", ex.getMessage());
        }
        Map<String,Object> singlePackage = new HashMap<>();
        Package pack = Packages.getInstance().getPackage(packageId.intValue());
        singlePackage.put("id", pack.getId());
        singlePackage.put("name", pack.getPackageName());
        singlePackage.put("author", pack.getPackageAuthor());
        singlePackage.put("website", pack.getPackageWebsite());
        singlePackage.put("auth", pack.isAuthorized());
        singlePackage.put("delivers", pack.getPackageDelivers());
        singlePackage.put("currentpermissions", FormattedPermissions.formattedPermissions(pack.getCurrentPermissionsRaw()));
        singlePackage.put("definedpermissions", FormattedPermissions.formattedPermissions(pack.getDefinedPermissionsRaw()));
        singlePackage.put("restrictionenabled", secEnabled);
        return singlePackage;
    }

    @Override
    public boolean setPackageApproved(Number packageId) throws PackageException {
        return Packages.getInstance().approvePackage(packageId.intValue());
    }

    @Override
    public boolean setPackageDisApproved(Number packageId) throws PackageException {
        return Packages.getInstance().disApprovePackage(packageId.intValue());
    }
                
}