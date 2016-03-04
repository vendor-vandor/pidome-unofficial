/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import org.pidome.server.services.accesscontrollers.AccessControllerServiceException;

/**
 *
 * @author John
 */
public interface AccessControllerServiceJSONRPCWrapperInterface {
    
    @PiDomeJSONRPCPrivileged
    public Object getAccessControllers();

    @PiDomeJSONRPCPrivileged
    public Object getAccessControllerCandidates();
    
    @PiDomeJSONRPCPrivileged
    public Object getControllerAccessTokens(Long accessController) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public Object getAccessTokens() throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public Object getPersonAccessTokens(Long personId) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public boolean deleteController(Long controllerId) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public boolean createController(Long wrappedDeviceId) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public boolean registerUserTokenByController(Long controllerId, Long userId) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public boolean deleteToken(Long tokenId) throws AccessControllerServiceException;
    
    @PiDomeJSONRPCPrivileged
    public boolean deleteTokenFromAccessController(Long tokenId, Long acessControllerId) throws AccessControllerServiceException;
    
}