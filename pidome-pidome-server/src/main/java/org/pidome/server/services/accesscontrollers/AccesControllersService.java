/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.accesscontrollers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.audit.Notifications;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.system.hardware.devices.DeviceStruct;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public class AccesControllersService implements ServiceInterface {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AccesControllersService.class);
    
    boolean isRunning = false;
    
    private static List<AccessControllerDeviceWrapper> accessControllers = new ArrayList<>();
    
    PropertyChangeListener presenceNameListener = this::presenceChanged;
    
    public AccesControllersService(){
        PresenceService.getCurrentPresenceTextProperty().addPropertyChangeListener(presenceNameListener);
    }
    
    @Override
    public void interrupt() {
        isRunning = false;
    }

    @Override
    public void start() {
        try {
            loadControllers();
        } catch (AccessControllerServiceException ex) {
             LOG.error("Could not load controllers: {}", ex.getMessage());
        }
        isRunning = true;
    }

    public static List<AccessControllerDeviceWrapper> getAccessControllers(){
        return accessControllers;
    }
    
    private void presenceChanged(PropertyChangeEvent evt){
        if(isRunning){
            new Timer().schedule(new TimerTask(){
                @Override
                public final void run(){
                    Iterator<AccessControllerDeviceWrapper> iter = AccesControllersService.getAccessControllers().iterator();
                    while(iter.hasNext()){
                        AccessControllerDeviceWrapper acd = iter.next();
                        try {
                            acd.sendMessage((String)PresenceService.getCurrentPresenceTextProperty().getValue());
                        } catch (UnknownDeviceException ex) {
                            LOG.error("Security device {} seems not to be loaded.", ex.getMessage(), ex);
                        }
                    }
                }
            }, 5000);
        }
    }
    
    public static List<AccessControllerDevice> getAccessControllerCandidates(){
        List<AccessControllerDevice> list = new ArrayList<>();
        for(DeviceInterface device:DeviceService.getActiveDevices()){
            boolean found = false;
            if(((DeviceStruct)device).getDevice() instanceof AccessControllerDevice){
                for(AccessControllerDeviceWrapper wrapped:accessControllers){
                    if(wrapped.getWrappedDeviceId()==device.getId()){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    list.add((AccessControllerDevice)((DeviceStruct)device).getDevice());
                }
            }
        }
        return list;
    }
    
    private void loadControllers() throws AccessControllerServiceException{
        LOG.info("Loading access controller devices wrappers");
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT * FROM tokendevices")) {
                    while (rsEvents.next()) {
                        AccessControllerDeviceWrapper wrap = new AccessControllerDeviceWrapper(rsEvents.getInt("id"), rsEvents.getInt("device"));
                        accessControllers.add(wrap);
                    }
                } catch (Exception ex) {
                    LOG.error("Problem loading access controllers list {}", ex.getMessage());
                    throw new AccessControllerServiceException("Problem loading access controllers list: " + ex.getMessage());
                }
            } catch (Exception ex) {
                LOG.error("Problem loadinga ccess controllers list {}", ex.getMessage());
                throw new AccessControllerServiceException("Problem loading access controllers list: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading access controllers list {}", ex.getMessage());
            throw new AccessControllerServiceException("Problem loading access controllers list: " + ex.getMessage());
        }
    }
    
    public static boolean registerUserTokenByController(int controllerId, int userId) throws AccessControllerServiceException {
        if(userId==1){
            throw new AccessControllerServiceException("You can not assign a token to an admin user");
        }
        for(AccessControllerDeviceWrapper controller:getAccessControllers()){
            if(controller.getControllerId()==controllerId){
                try {
                    controller.sendEditEnabled(true);
                    controller.sendUser(userId);
                    return true;
                } catch (UnknownDeviceException ex) {
                    throw new AccessControllerServiceException("Selected device is invalid: " + ex.getMessage());
                } catch (PersonsManagementException ex) {
                    throw new AccessControllerServiceException("Selected user is invalid: " + ex.getMessage());
                }
            }
        }
        return false;
    }
    
    public static boolean bindAccessController(int wrappedDeviceId) throws AccessControllerServiceException {
        for(AccessControllerDeviceWrapper controller:getAccessControllers()){
            if(controller.getWrappedDeviceId()==wrappedDeviceId){
                throw new AccessControllerServiceException("Controller already exists");
            }
        }
        try {
            Device device = ((DeviceStruct)DeviceService.getDevice(wrappedDeviceId)).getDevice();
            if(device instanceof AccessControllerDevice){
                try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                        PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO tokendevices ('device') values (?)",Statement.RETURN_GENERATED_KEYS)){
                    prep.setInt(1, wrappedDeviceId);
                    prep.executeUpdate();
                    try (ResultSet rs = prep.getGeneratedKeys()) {
                        if (rs.next()) {
                            AccessControllerDeviceWrapper wrap = new AccessControllerDeviceWrapper(rs.getInt(1), wrappedDeviceId);
                            wrap.setCapabilities((AccessControllerProxyInterface.Capabilities[])((AccessControllerDevice)device).getCapabilities().toArray());
                            accessControllers.add(wrap);
                        } else {
                            return false;
                        }
                    } catch (SQLException ex) {
                        LOG.error("Controller created but not started, you need to restart the server: {}", ex.getMessage(), ex);
                    }
                } catch (SQLException ex) {
                    LOG.error("Could not create controller: {}", ex.getMessage(), ex);
                }
                return true;
            } else {
                throw new AccessControllerServiceException("Selected device is not the correct type");
            }
        } catch (UnknownDeviceException ex) {
            throw new AccessControllerServiceException("Selected device is not available");
        }
    }
    
    public static final AccessControllerDeviceWrapper getAccessController(int controllerId) throws AccessControllerServiceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getControllerId()==controllerId){
                return device;
            }
        }
        throw new AccessControllerServiceException("Selected access controller does not exist");
    }
    
    public static boolean detachAccessController(int controllerId) throws AccessControllerServiceException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM tokendevices WHERE id=?")) {
            prep.setInt(1, controllerId);
            prep.executeUpdate();
            AccessControllerDeviceWrapper toDelete = null;
            for(AccessControllerDeviceWrapper device:accessControllers){
                if(device.getControllerId()==controllerId){
                    toDelete = device;
                }
            }
            if(toDelete!= null){
                accessControllers.remove(toDelete);
            }
        } catch (SQLException ex) {
            LOG.error("Could not detach access controller: {}", ex.getMessage(), ex);
            throw new AccessControllerServiceException("Could not detach access controller");
        }
        return true;
    }
    
    public static final List<AccessControllerToken> getAccessTokensForPerson(int personId){
        List<AccessControllerToken> tokenlist = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ts.id, ts.person, p.firstname as personname, ts.tokentype,ts.master, d.name as devicename "
                                                                       + "FROM tokensets ts "
                                                                 + "INNER JOIN persons p ON p.id=ts.person "
                                                                 + "INNER JOIN tokenset_has_devices thd ON thd.tokenset=ts.id "
                                                                 + "INNER JOIN devices d ON d.id=thd.tokendevice "
                                                                      + "WHERE ts.person=?")) {
            prep.setInt(1, personId);
            try (ResultSet rsResult = prep.executeQuery()) {
                while(rsResult.next()){
                    AccessControllerToken token = new AccessControllerToken(rsResult.getInt("id"),
                                                                            rsResult.getInt("person"),
                                                                            rsResult.getBoolean("master"),
                                                                            rsResult.getString("personname"));
                    token.setTokenType(rsResult.getString("tokentype"));
                    tokenlist.add(token);
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return tokenlist;
    }
    
    public static final List<AccessControllerToken> getAccessTokens(){
        List<AccessControllerToken> tokenlist = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ts.id, ts.person, p.firstname as personname, ts.tokentype,ts.master FROM tokensets ts "
                                                                 + "INNER JOIN persons p ON p.id=ts.person ORDER BY p.firstname ASC")) {
            try (ResultSet rsResult = prep.executeQuery()) {
                while(rsResult.next()){
                    AccessControllerToken token = new AccessControllerToken(rsResult.getInt("id"),
                                                                            rsResult.getInt("person"),
                                                                            rsResult.getBoolean("master"),
                                                                            rsResult.getString("personname"));
                    token.setTokenType(rsResult.getString("tokentype"));
                    tokenlist.add(token);
                }
            } catch (Exception ex){
                LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error("Could not check if token already exists: {}", ex.getMessage(), ex);
        }
        return tokenlist;
    }
    
    protected static boolean authorizeToken(AccessControllerDevice acd, PersonToken.TokenType tt, char[] b) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                int personId = device.authorizeToken(tt, b);
                if(personId>1){
                    try {
                        String presenceNext = "";
                        try {
                            int curPresence = (int)PresenceService.getCurrentPresenceIdProperty().getValue();
                            if(curPresence==1){
                                presenceNext = "Bye ";
                                //PresenceService.setPresence(personId, 2);
                                PresenceService.setGlobalPresence(2);
                            } else {
                                presenceNext = "Hi ";
                                //PresenceService.setPresence(personId, 1);
                                PresenceService.setGlobalPresence(1);
                            }
                        } catch (Exception ex){
                            
                        }
                        sendAuthSuccessFailure(acd, personId);
                        acd.sendAuthConfirmed(true, new StringBuilder(presenceNext).append(PersonsManagement.getInstance().getPerson(personId).getFirstName()).toString());
                        new Timer().schedule(new TimerTask(){
                            @Override
                            public final void run(){
                                acd.sendMessage((String)PresenceService.getCurrentPresenceTextProperty().getValue());
                            }
                        }, 5000);
                    } catch (PersonsManagementException ex) {
                        acd.sendAuthConfirmed(true, "Authorized");
                    }
                    return true;
                } else {
                    sendAuthSuccessFailure(acd, personId);
                    acd.sendAuthConfirmed(false, "Auth. failed");
                    new Timer().schedule(new TimerTask(){
                        @Override
                        public final void run(){
                            acd.sendMessage((String)PresenceService.getCurrentPresenceTextProperty().getValue());
                        }
                    }, 5000);
                    return false;
                }
            }
        }
        acd.sendError("Not registered");
        return false;
    }

    private static void sendAuthSuccessFailure(AccessControllerDevice device, int personId){
        try {
            if(personId>1){
                Notifications.sendMessage(Notifications.NotificationType.INFO, "Access", new StringBuilder((personId!=0)?PersonsManagement.getInstance().getPerson(personId).getFirstName():"Unknown").append(" has been authorized via ").append(device.getDeviceName()).toString());
            } else {
                Notifications.sendMessage(Notifications.NotificationType.ERROR, "Access", new StringBuilder("An unknown access token is used on ").append(device.getDeviceName()).toString());
            }
        } catch (PersonsManagementException ex) {
            /// No notification send.
        }
    }
    
    protected static void setCapabilities(AccessControllerDevice acd, AccessControllerProxyInterface.Capabilities... cpbltss) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                device.setCapabilities(cpbltss);
            }
        }
    }
    
    protected static boolean hasMasterToken(AccessControllerDevice acd) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                return device.hasMasterToken();
            }
        }
        return false;
    }
    
    protected static boolean supportsMasterToken(AccessControllerDevice acd) {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                return device.supportsMasterToken();
            }
        }
        return false;
    }
    
    protected static boolean authorizeMasterToken(AccessControllerDevice acd, PersonToken.TokenType tt, char[] b) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                boolean supportsMasterToken = supportsMasterToken(acd);
                boolean hasMasterToken      = hasMasterToken(acd);
                if((supportsMasterToken && !hasMasterToken) || !supportsMasterToken){
                    acd.sendMasterAuthConfirmed(true);
                    return true;
                } else if (supportsMasterToken && hasMasterToken){
                    if(device.authorizeMasterToken(tt, b)){
                        acd.sendMasterAuthConfirmed(true);
                        return true;
                    } else {
                        acd.sendMasterAuthConfirmed(false);
                        return false;
                    }
                }
            }
        }
        acd.sendError("Not registered");
        return false;
    }
    
    public static boolean deleteToken(int tokenId) throws AccessControllerServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM tokensets WHERE id=?")) {
            prep.setInt(1, tokenId);
            prep.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Could not delete token: {}", ex.getMessage(), ex);
            throw new AccessControllerServiceException("Could not delete token: " + ex.getMessage());
        }
        return true;
    }
    
    public static boolean deleteToken(int tokenId, int controllerId) throws AccessControllerServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM tokenset_has_devices WHERE tokenset=? AND tokendevice=?")) {
            prep.setInt(1, tokenId);
            prep.setInt(2, controllerId);
            prep.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Could not delete token from controller: {}", ex.getMessage(), ex);
            throw new AccessControllerServiceException("Could not delete token from controller: " + ex.getMessage());
        }
        return true;
    }
    
    protected static boolean removeToken(AccessControllerDevice acd, int i, PersonToken.TokenType tt, char[] b) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                if(device.removeTokenByDevice(tt, i, b)){
                    acd.sendEditSuccess(true);
                    return true;
                } else {
                    acd.sendEditSuccess(false);
                    return false;
                }
            }
        }
        acd.sendError("Not registered");
        return false;
    }

    protected static boolean addToken(AccessControllerDevice acd, int i, PersonToken.TokenType tt, char[] b) throws UnknownDeviceException {
        for(AccessControllerDeviceWrapper device:accessControllers){
            if(device.getWrappedDeviceId()==acd.getId()){
                if(device.addToken(tt, i, b)){
                    acd.sendEditSuccess(true);
                    return true;
                } else {
                    acd.sendEditSuccess(false);
                    return false;
                }
            }
        }
        acd.sendError("Not registered");
        return false;
    }
    
    @Override
    public boolean isAlive() {
        return isRunning;
    }

    @Override
    public String getServiceName() {
        return "AccesControllersService";
    }
    
}
