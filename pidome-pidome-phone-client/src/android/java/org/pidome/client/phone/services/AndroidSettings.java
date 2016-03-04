/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;

/**
 *
 * @author John
 */
public class AndroidSettings implements LocalSettingsStorageInterface {
    
    private final SharedPreferences settings;
    private final SharedPreferences.Editor settingsEditor;
    
    protected AndroidSettings(Context context){
        settings = context.getSharedPreferences("settings", 0);
        settingsEditor = settings.edit();
    }

    @Override
    public void storeSettings(String string) throws IOException {
        settingsEditor.commit();
    }

    @Override
    public void storeSettings() throws IOException {
        storeSettings("");
    }

    @Override
    public String getStringSetting(String name, String string1) {
        return settings.getString(name, string1);
    }

    @Override
    public boolean getBoolSetting(String name, boolean bln) {
        return settings.getBoolean(name, bln);
    }

    @Override
    public int getIntSetting(String name, int i) {
        return settings.getInt(name, i);
    }

    @Override
    public long getLongSetting(String name, long l) {
        return settings.getLong(name, l);
    }

    @Override
    public float getFloatSetting(String name, float f) {
        return settings.getFloat(name, f);
    }

    @Override
    public void setStringSetting(String name, String value) {
        settingsEditor.putString(name, value);
    }
    
    @Override
    public void setBoolSetting(String name, boolean bln) {
        settingsEditor.putBoolean(name, bln);
    }

    @Override
    public void setIntSetting(String name, int i) {
        settingsEditor.putInt(name, i);
    }

    @Override
    public void setLongSetting(String name, long l) {
        settingsEditor.putLong(name, l);
    }

    @Override
    public void setFloatSetting(String name, float f) {
        settingsEditor.putFloat(name, f);
    }

}
