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
public final class WebPresentComplexNVP extends WebPresentation {

    public WebPresentComplexNVP(String label) {
        super(WebPresentation.TYPE.COMPLEX_NVP, label);
    }

    @Override
    public final void setValue(Object value) {
        this.setPresentationValue(value);
    }
}
