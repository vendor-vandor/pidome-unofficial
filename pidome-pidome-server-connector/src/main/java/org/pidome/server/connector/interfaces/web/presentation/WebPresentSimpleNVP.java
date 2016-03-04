/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation;

/**
 *
 * @author John
 */
public class WebPresentSimpleNVP extends WebPresentation {

    public WebPresentSimpleNVP(String label) {
        super(TYPE.SIMPLE_NVP, label);
    }

    @Override
    public final void setValue(Object value) {
        this.setPresentationValue(value);
    }
    
}
