/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.hooks;

/**
 *
 * @author John
 */
public interface PiDomeRPCHookInterpretor {
    public String interpretExternal(PiDomeRPCHookListener originator, String message);
}
