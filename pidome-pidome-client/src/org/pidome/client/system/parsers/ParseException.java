/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.parsers;

/**
 *
 * @author John Sirach
 */
public class ParseException extends Exception {
    
    static final long serialVersionUID = 1L;
	
    public ParseException(){
        super();
    }

    public ParseException(String message){
        super(message);
    }
}