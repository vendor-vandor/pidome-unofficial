/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.permissions;

import java.security.Permission;

/**
 *
 * @author John
 */
public final class SharedDataPermission extends Permission {

    private final String action;
    
    public SharedDataPermission(String target, String action){
        super(target);
        this.action = action;
    }

    @Override
    public final boolean implies(Permission permission) {
        if (!(permission instanceof SharedDataPermission)) return false;
        SharedDataPermission sd = (SharedDataPermission)permission;
        return (sd.getName().equals(getName()) && sd.getActions().equals(getActions()));
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) return false;
        if (!getClass().equals(obj.getClass())) return false;
        SharedDataPermission sd = (SharedDataPermission) obj;
        return (sd.getName().equals(getName()) && sd.getActions().equals(getActions()));
    }

    @Override
    public final int hashCode() {
         return getName().hashCode() + action.hashCode();
    }

    @Override
    public final String getActions() {
        return action;
    }
    
}
