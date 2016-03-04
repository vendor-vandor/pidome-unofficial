/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.accesscontrollers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface;
import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;
import org.pidome.server.services.clients.persons.Person;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public class AccessControllerProxy implements AccessControllerProxyInterface {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AccessControllerProxy.class);

    @Override
    public void registerCapabilities(AccessControllerDevice acd, Capabilities... cpbltss) {
        try {
            AccesControllersService.setCapabilities(acd, cpbltss);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not register extended capabilities");
        }
    }
    
    private enum SendType {
        NEW,NEXT,PREV;
    }
    
    @Override
    public void firstPerson(AccessControllerDevice acd) {
        sendPerson(acd, SendType.NEW, 0);
    }

    @Override
    public void nextPerson(AccessControllerDevice acd, int i) {
        sendPerson(acd, SendType.NEXT, i);
    }

    @Override
    public void previousPerson(AccessControllerDevice acd, int i) {
        sendPerson(acd, SendType.PREV, i);
    }
    
    @Override
    public void setSystemTamper(AccessControllerDevice acd, boolean bln) {
        //acd.setSystemTamper(bln);
    }

    @Override
    public void setSystemAlarmed(AccessControllerDevice acd, boolean bln) {
        //acd.setSystemAlarmed(bln);
    }

    @Override
    public void setSystemEdit(AccessControllerDevice acd, boolean bln) throws UnsupportedOperationException {
        //acd.setSystemEdit(bln);
    }

    @Override
    public boolean authorizeToken(AccessControllerDevice acd, PersonToken.TokenType tt, char[] c) {
        try {
            return AccesControllersService.authorizeToken(acd, tt, c);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not check for token: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean authorizeMasterToken(AccessControllerDevice acd, PersonToken.TokenType tt, char[] c) {
        try {
            return AccesControllersService.authorizeMasterToken(acd, tt, c);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not check for master token: {}", ex.getMessage(), ex);
        }
        return false;
    }
    
    @Override
    public boolean hasMasterToken(AccessControllerDevice acd) {
        try {
            return AccesControllersService.hasMasterToken(acd);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not check for master token: {}", ex.getMessage(), ex);
            return false;
        }
    }
    
    @Override
    public boolean removeToken(AccessControllerDevice acd, int i, PersonToken.TokenType tt, char[] c) {
        try {
            return AccesControllersService.removeToken(acd, i, tt, c);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not remove token: {}", ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public boolean addToken(AccessControllerDevice acd, int i, PersonToken.TokenType tt, char[] c) {
        try {
            return AccesControllersService.addToken(acd, i, tt, c);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not add token: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private static List<Person> getOrderedPersonList() throws PersonsManagementException {
        List<Person> persons = PersonsManagement.getInstance().getPersons();
        Collections.sort(persons, new PersonNameComparator());
        return persons;
    }
    
    private static class PersonNameComparator implements Comparator<Person>{
        @Override
        public int compare(Person e1, Person e2){
            return e1.getFirstName().compareTo(e2.getFirstName());
        }
    }
 
    private static void sendPerson(AccessControllerDevice acd, SendType type, int curIndex){
        try {
            /// Getting the ordered list is based on the first name of the user. The used curIndex is based on the user id, and not the array index.
            boolean supportsAdmin = AccesControllersService.supportsMasterToken(acd);
            if(supportsAdmin && !AccesControllersService.hasMasterToken(acd)){
                acd.sendPerson(new AccessControllerToken(0, 1, true, "Master token"));
            } else {
                try {
                    List<Person> list = getOrderedPersonList();
                    if(list.size()>1){
                        int setListIndex = (type==SendType.PREV)?list.size()-1:0;
                        ListIterator<Person> iter = list.listIterator(setListIndex);
                        boolean done = false;
                        switch(type){
                            case NEW:
                                if(list.get(setListIndex).getLoginName().toLowerCase().equals("admin")){
                                    setListIndex++;
                                }
                                acd.sendPerson(new AccessControllerToken(-1,
                                        list.get(setListIndex).getId(),
                                        false,
                                        list.get(setListIndex).getFirstName()
                                                                        )
                                               );
                                break;
                            case NEXT:
                                while(iter.hasNext()){
                                    Person pers = iter.next();
                                    if(pers.getId() == curIndex && iter.hasNext()){
                                        Person send = iter.next();
                                        if(!send.getLoginName().toLowerCase().equals("admin")){
                                            acd.sendPerson(new AccessControllerToken(-1, send.getId(), false, send.getFirstName()));
                                            done = true;
                                            break;
                                        }
                                    }
                                }
                                if(!done){
                                    if(list.get(setListIndex).getLoginName().toLowerCase().equals("admin")){
                                        setListIndex++;
                                        acd.sendPerson(new AccessControllerToken(-1, list.get(setListIndex).getId(), false, list.get(setListIndex).getFirstName()));
                                    } else {
                                        acd.sendPerson(new AccessControllerToken(-1, list.get(setListIndex).getId(), false, list.get(setListIndex).getFirstName()));
                                    }
                                }
                                break;
                            case PREV:
                                while(iter.hasPrevious()){
                                    Person pers = iter.previous();
                                    if(pers.getId() == curIndex && iter.hasPrevious()){
                                        Person send = iter.previous();
                                        if(!pers.getLoginName().toLowerCase().equals("admin")){
                                            acd.sendPerson(new AccessControllerToken(0, send.getId(), false, send.getFirstName()));
                                            done = true;
                                            break;
                                        }
                                    }
                                }
                                if(!done){
                                    if(list.get(setListIndex).getLoginName().toLowerCase().equals("admin")){
                                        setListIndex--;
                                        acd.sendPerson(new AccessControllerToken(-1, list.get(setListIndex).getId(), false, list.get(setListIndex).getFirstName()));
                                    } else {
                                        acd.sendPerson(new AccessControllerToken(-1, list.get(setListIndex).getId(), false, list.get(setListIndex).getFirstName()));
                                    }
                                }
                                break;
                        }
                    } else {
                        acd.sendError("No persons");
                    }
                } catch (PersonsManagementException ex) {
                    LOG.error("Could not check for persons: {}", ex.getMessage(), ex);
                }
            }
        } catch (UnknownDeviceException ex) {
            LOG.error("Asked an unknown device if it has a master token: {}", ex.getMessage(), ex);
        }
    }
    
}