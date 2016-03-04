/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import org.pidome.server.connector.plugins.media.MediaEvent;

/**
 *
 * @author John
 */
public interface MediaHookListener {
    public void handleMediaEvent(MediaEvent event);
}
