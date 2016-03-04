/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.phone.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 * @author John
 */
public class AndroidBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SystemService.isCalled() && !SystemService.isServiceRunning()){
            Intent startServiceIntent = new Intent(context, SystemService.class);
            context.startService(startServiceIntent);
        }
    }
}