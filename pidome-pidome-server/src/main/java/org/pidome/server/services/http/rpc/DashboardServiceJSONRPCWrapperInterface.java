/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import org.pidome.server.services.dashboard.DashboardServiceException;

/**
 * This RPC interface is due to change and not final!
 * @author John
 */
public interface DashboardServiceJSONRPCWrapperInterface {
    
    /**
     * Returns the dashboard based on the requesting user type and user id.
     * @return an dashboard object
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    public Object getDashboard() throws DashboardServiceException;
    
    /**
     * Returns the dashboard by it's id.
     * @param id a dashboard id.
     * @return an dashboard object
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    @PiDomeJSONRPCPrivileged
    public Object getDashboardById(Long id) throws DashboardServiceException;
    
    /**
     * Returns the dashboard based on the requesting user type and user id.
     * @return an dashboard object
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    public Object getDashboards() throws DashboardServiceException;
    
    /**
     * Returns an user dashboard.
     * This RPC call is used by client types display to make it possible to show a personalized dashboard.
     * @param personId
     * @return 
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    @PiDomeJSONRPCLeveraged
    public Object getUserDashboardByClient(Long personId) throws DashboardServiceException;

    /**
     * Returns a client dashboard.
     * Can only be requested by a Privileged user (admin)
     * @param clientId
     * @return 
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getClientDashboard(Long clientId) throws DashboardServiceException;
    
    /**
     * Returns a persons (user) dashboard.
     * Can only be requested by a Privileged user (admin)
     * @param personId The id of the user
     * @param type The type of dashboard to request.
     * @return 
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getUserDashboard(Long personId, String type) throws DashboardServiceException;
    
    /**
     * Saves a dashboard for an user.
     * All though this in fact a privileged action it can be called by users who modify their own dashboard.
     * The backend will check if a privileged user is saving the setup. If not it will ALWAYS update the user's dashboard.
     * @param dashId The id of the dashboard to be updated, or use 0 to insert a new one.
     * @param name The name given to the dashboard.
     * @param personId The id of the person this dashboard is for.
     * @param type The dashboard type (WEB, MOBILE).
     * @param content The content of the dashboard.
     * @return true when saved.
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    public boolean saveUserDashboard(Long dashId, String name, Long personId, String type, Object content) throws DashboardServiceException;
    
    /**
     * Saves a dashboard for an user.
     * All though this in fact a privileged action it can be called by users who modify their own dashboard.
     * The backend will check if a privileged user is saving the setup. If not it will ALWAYS update the user's dashboard.
     * @param dashId The id of the dashboard to be updated, or use 0 to insert a new one.
     * @param name The name given to the dashboard.
     * @param personId The id of the person this dashboard is for.
     * @param clientId The id of the linked client (Mobile)
     * @param content The content of the dashboard.
     * @return true when saved.
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    public boolean saveUserMobileDashboard(Long dashId, String name, Long personId, Long clientId, Object content) throws DashboardServiceException;
    
    /**
     * Saves a dashboard for an user.
     * All though this in fact a privileged action it can be called by users who modify their own dashboard.
     * The backend will check if a privileged user is saving the setup. If not it will ALWAYS update the user's dashboard.
     * @param personId The id of the person this dashboard is for.
     * @param content The content of the dashboard.
     * @return true when saved.
     * @throws org.pidome.server.services.dashboard.DashboardServiceException
     */
    public boolean saveUserDashboardForClient(Long dashId, String name, Long personId, Long clientId, Object content) throws DashboardServiceException;
    
    /**
     * Saves a client dashboard.
     * This currently is only available in the web interface as the final setup is not
     * known yet for composing the dashboard.
     * @param clientid The client id this dashboard is for
     * @param content The setup of the dashboard.
     * @return true when saved
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean saveClientDashboard(Long dashId, String name, Long clientid, Object content) throws DashboardServiceException;
    
    /**
     * Deletes a dashboard.
     * Currently it is only a privileged method as it is being added for testing.
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteDashboard(Long dashId) throws DashboardServiceException;
    
}
