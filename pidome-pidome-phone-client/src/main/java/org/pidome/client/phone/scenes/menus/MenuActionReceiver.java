/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.menus;

import org.pidome.client.phone.scenes.menus.MainMenu.MainMenuItem;

/**
 *
 * @author John
 */
public interface MenuActionReceiver {
    
    public void closeMenu(MenuBase menu);
    
    public void handleClickedMainMenuItem(MainMenuItem menuItem);
    
}