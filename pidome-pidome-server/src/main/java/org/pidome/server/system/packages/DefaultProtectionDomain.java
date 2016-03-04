/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.packages;

import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.ProtectionDomain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class DefaultProtectionDomain extends ProtectionDomain {

    static Logger LOG = LogManager.getLogger(DefaultProtectionDomain.class);
    
    public DefaultProtectionDomain(CodeSource codesource, PermissionCollection permissions, PackageClassLoader classloader, Principal[] principals) {
        super(codesource, permissions, classloader, principals);
    }
    
}