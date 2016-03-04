/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.utils;

import android.os.Build;

/**
 *
 * @author John
 */
public class DeviceUtils {

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return PiDomeUtils.capitalize(model);
        } else {
            return PiDomeUtils.capitalize(manufacturer) + ", " + model;
        }
    }

}
