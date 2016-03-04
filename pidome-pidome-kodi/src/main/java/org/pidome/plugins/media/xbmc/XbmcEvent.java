/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

/**
 *
 * @author John Sirach
 */
public class XbmcEvent extends java.util.EventObject {

    /**
     * Constructs the event object.
     * @param source 
     */
    public XbmcEvent(XbmcConnectionData source) {
        super(source);
    }
    
    /**
     * Returns an XMBCData object.
     * @return 
     */
    @Override
    public final XbmcConnectionData getSource(){
        return (XbmcConnectionData)super.getSource();
    }
    
}
