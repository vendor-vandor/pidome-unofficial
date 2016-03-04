/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.automations.rule.logic.compare;

import java.util.List;
import org.pidome.server.services.automations.impl.AutomationVariableImpl;

/**
 *
 * @author John
 */
public abstract class CompareBase {
    
    protected abstract boolean run();
    
    protected abstract List<AutomationVariableImpl> getVariablesList();
    
    public abstract void destroy();
    
}