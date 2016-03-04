/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.mqtt.pidomeMQTTBroker;

import java.nio.ByteBuffer;

/**
 *
 * @author John
 */
public interface FromBrokerToPiDomeInteral {
    public void handleFromExternal(String clientId, String topic, ByteBuffer message);
}
