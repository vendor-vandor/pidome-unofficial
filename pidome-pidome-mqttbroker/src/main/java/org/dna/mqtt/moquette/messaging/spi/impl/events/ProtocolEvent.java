/*
 * Copyright (c) 2012-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package org.dna.mqtt.moquette.messaging.spi.impl.events;

import org.dna.mqtt.moquette.proto.Utils;
import org.dna.mqtt.moquette.proto.messages.AbstractMessage;
import org.dna.mqtt.moquette.server.ServerChannel;

/**
 * Event used to carry ProtocolMessages from front handler to event processor
 */
public class ProtocolEvent extends MessagingEvent {
    ServerChannel m_session;
    AbstractMessage message;

    public ProtocolEvent(ServerChannel session, AbstractMessage message) {
        this.m_session = session;
        this.message = message;
    }

    public ServerChannel getSession() {
        return m_session;
    }

    public AbstractMessage getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "ProtocolEvent wrapping " + Utils.msgType2String(message.getMessageType());
    }
}
