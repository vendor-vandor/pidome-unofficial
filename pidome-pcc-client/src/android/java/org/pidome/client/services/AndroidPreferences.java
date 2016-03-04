/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import android.content.Context;
import static android.content.Context.MODE_PRIVATE;
import android.content.SharedPreferences;
import java.io.IOException;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;

/**
 *
 * @author John
 */
public class AndroidPreferences implements LocalPreferenceStorageInterface {
    
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor preferencesEditor;
    
    protected AndroidPreferences(Context context){
        preferences = context.getSharedPreferences("preferences", MODE_PRIVATE);
        preferencesEditor = preferences.edit();
    }

    @Override
    public String getStringPreference(String name, String string1) {
        return preferences.getString(name, string1);
    }

    @Override
    public boolean getBoolPreference(String name, boolean bln) {
        return preferences.getBoolean(name, bln);
    }

    @Override
    public int getIntPreference(String name, int i) {
        return preferences.getInt(name, i);
    }

    @Override
    public long getLongPreference(String name, long l) {
        return preferences.getLong(name, l);
    }

    @Override
    public float getFloatPreference(String name, float f) {
        return preferences.getFloat(name, f);
    }    
    
    @Override
    public void setStringPreference(String name, String value) {
        preferencesEditor.putString(name, value);
    }
    
    @Override
    public void setBoolPreference(String name, boolean bln) {
        preferencesEditor.putBoolean(name, bln);
    }

    @Override
    public void setIntPreference(String name, int i) {
        preferencesEditor.putInt(name, i);
    }

    @Override
    public void setLongPreference(String name, long l) {
        preferencesEditor.putLong(name, l);
    }

    @Override
    public void setFloatPreference(String name, float f) {
        preferencesEditor.putFloat(name, f);
    }
    
    @Override
    public void storePreferences(String string) throws IOException {
        preferencesEditor.commit();
    }

    @Override
    public void storePreferences() throws IOException {
        storePreferences("");
    }
    
}
