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

import org.pidome.server.system.packages.PackageException;

/**
 *
 * @author John
 */
public interface PackageServiceJSONRPCWrapperWrapperInterface {
 
    @PiDomeJSONRPCPrivileged
    public Object getPackages() throws PackageException;
    
    @PiDomeJSONRPCPrivileged
    public Object getPackageDetails(Number packageId) throws PackageException;
    
    @PiDomeJSONRPCPrivileged
    public boolean setPackageApproved(Number packageId) throws PackageException;
    
    @PiDomeJSONRPCPrivileged
    public boolean setPackageDisApproved(Number packageId) throws PackageException;
    
}