/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.fxactivity;

import android.content.res.Configuration;
import android.view.KeyEvent;
import javafxports.android.FXActivity;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.services.PlatformOrientation;
import org.pidome.client.services.aidl.client.AndroidServiceConnectorAidl;

/**
 * 
 * @author John
 */
public class BadBoy extends FXActivity {
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)  {
        if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0 ) {
            if(ScenesHandler.isOnRootLevel()){
                moveTaskToBack(true);
            } else {
                ScenesHandler.hardwareBackButtonPressed();
            }
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0 ) {
            ScenesHandler.toggleAppMenu();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                AndroidServiceConnectorAidl.handleOrientationChanged(PlatformOrientation.Orientation.LANDSCAPE);
            break;
            case Configuration.ORIENTATION_PORTRAIT:
                AndroidServiceConnectorAidl.handleOrientationChanged(PlatformOrientation.Orientation.PORTRAIT);
            break;
            default:
                /// Do nothing
        }
    }
    
}