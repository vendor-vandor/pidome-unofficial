/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.automations.statements;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.system.network.Network;

/**
 *
 * @author John
 */
public class WOLExecStatement extends AutomationStatement {

    private final static String SEPARATOR = ":";

    private final static int[] portsList = {0,7,9};
    
    private int port;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeviceExecStatement.class);
    
    private final String targetMAC;
    
    public WOLExecStatement(String mac, String port) {
        super(new StringBuilder("RunWol_").append(UUID.randomUUID().toString()).toString());
        this.targetMAC = cleanMac(mac);
        try { 
            this.port = Integer.parseInt(port);
        } catch (Exception ex){
            this.port = Integer.MIN_VALUE;
            LOG.warn("Invalid port given, using the defaults of 0, 7 and 9 ({})", ex.getMessage());
        }
    }
    
    @Override
    public boolean run() {
        try {
            send();
            return true;
        } catch (UnknownHostException ex) {
            LOG.error("Could not determine local host: {}", ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public void destroy() {
        ///Not used.
    }
    
    private String send() throws UnknownHostException {
        // validate MAC and chop into array
        final String[] hex = validateMac(targetMAC);

        // convert to base16 bytes
        final byte[] macBytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            macBytes[i] = (byte) Integer.parseInt(hex[i], 16);
        }

        final byte[] bytes = new byte[102];

        // fill first 6 bytes
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        // fill remaining bytes with target MAC
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        // create socket to IP
        final InetAddress address = InetAddress.getByName(Network.getBroadcastAddressProperty().getValue().getHostAddress());
        if(this.port < 0){
            for(int localPort:portsList){
                final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, localPort);
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.send(packet);
                } catch (IOException ex) {
                    LOG.error("Could not send WOL package to '{}' because of: {}", ex.getMessage());
                }
            }
        } else {
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, this.port);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            } catch (IOException ex) {
                LOG.error("Could not send WOL package to '{}' because of: {}", ex.getMessage());
            }
        }

        return hex[0] + SEPARATOR + hex[1] + SEPARATOR + hex[2] + SEPARATOR + hex[3] + SEPARATOR + hex[4] + SEPARATOR + hex[5];
    }
    
    /**
     * Validate a MAC address.
     * @param mac The plain mac address
     * @return text array on valid mac.
     * @throws IllegalArgumentException When the mac is not valid
     */
    private static String[] validateMac(String mac) throws IllegalArgumentException {
        // error handle semi colons
        mac = mac.replace(";", ":");

        // attempt to assist the user a little
        String newMac = "";

        if (mac.matches("([a-zA-Z0-9]){12}")) {
            // expand 12 chars into a valid mac address
            for (int i = 0; i < mac.length(); i++) {
                if ((i > 1) && (i % 2 == 0)) {
                    newMac += ":";
                }
                newMac += mac.charAt(i);
            }
        } else {
            newMac = mac;
        }

        // regexp pattern match a valid MAC address
        final Pattern pat = Pattern.compile("((([0-9a-fA-F]){2}[-:]){5}([0-9a-fA-F]){2})");
        final Matcher m = pat.matcher(newMac);

        if (m.find()) {
            String result = m.group();
            return result.split("(\\:|\\-)");
        } else {
            throw new IllegalArgumentException("Invalid MAC address");
        }
    }
    
    /**
     * Cleans up an mac address able to be send.
     * @param mac
     * @return
     * @throws IllegalArgumentException 
     */
    private static String cleanMac(String mac) throws IllegalArgumentException {
        final String[] hex = validateMac(mac);

        StringBuilder sb = new StringBuilder();
        boolean isMixedCase = false;

        // check for mixed case
        for (int i = 0; i < 6; i++) {
            sb.append(hex[i]);
        }
        String testMac = sb.toString();
        if ((testMac.toLowerCase().equals(testMac) == false) && (testMac.toUpperCase().equals(testMac) == false)) {
            isMixedCase = true;
        }

        sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            // convert mixed case to lower
            if (isMixedCase == true) {
                sb.append(hex[i].toLowerCase());
            } else {
                sb.append(hex[i]);
            }
            if (i < 5) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }
    
}