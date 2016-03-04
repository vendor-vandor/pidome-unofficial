/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network.connectors;

/**
 *
 * @author John Sirach
 */
public class XMLConnectorException extends Exception {
    
    static final long serialVersionUID = 1L;
	
    public XMLConnectorException(){
        super();
    }

    public XMLConnectorException(String message){
        super(message);
    }
}