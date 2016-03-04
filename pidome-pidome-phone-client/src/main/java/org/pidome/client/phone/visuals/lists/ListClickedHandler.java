/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.lists;

/**
 *
 * @author John
 */
public interface ListClickedHandler<Type> {

    public void itemClicked(Type item, String itemDescription);
    
}