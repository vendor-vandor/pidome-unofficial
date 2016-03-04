/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.pidome.client.services.aidl.service.SystemServiceAidl;

/**
 *
 * @author John
 */
public class AndroidBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SystemServiceAidl.isCalled() && !SystemServiceAidl.isServiceRunning()){
            Intent startServiceIntent = new Intent(context, SystemServiceAidl.class);
            context.startService(startServiceIntent);
        }
    }
}