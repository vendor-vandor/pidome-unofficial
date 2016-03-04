/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.robovm.apple.foundation.NSMutableDictionary;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSString;

/**
 *
 * @author John
 */
public class IOSPreferenceStorage implements LocalPreferenceStorageInterface {

    private NSMutableDictionary<NSString, NSObject> keychainQuery;

    final File finalPath;

    IOSPreferenceStorage() {
        File libraryPath = new File(System.getenv("HOME"), "Library");
        finalPath = new File(libraryPath, "preferences.plist");

        keychainQuery = (NSMutableDictionary<NSString, NSObject>) NSMutableDictionary.read(finalPath);

        // if it fails to get an existing dictionary, create a new one.
        if (keychainQuery == null) {
            keychainQuery = new NSMutableDictionary<NSString, NSObject>();
            keychainQuery.write(finalPath, true);
        }
    }

    @Override
    public String getStringPreference(String string, String string1) {
        return keychainQuery.getString(string, string1);
    }

    @Override
    public boolean getBoolPreference(String string, boolean bln) {
        return keychainQuery.getBoolean(string, bln);
    }

    @Override
    public int getIntPreference(String string, int i) {
        return keychainQuery.getInt(string, i);
    }

    @Override
    public long getLongPreference(String string, long l) {
        return keychainQuery.getLong(string, l);
    }

    @Override
    public float getFloatPreference(String string, float f) {
        return keychainQuery.getFloat(string, f);
    }

    @Override
    public void setStringPreference(String string, String string1) {
        keychainQuery.put(string, string1);
    }

    @Override
    public void setBoolPreference(String string, boolean bln) {
        keychainQuery.put(string, bln);
    }

    @Override
    public void setIntPreference(String string, int i) {
        keychainQuery.put(string, i);
    }

    @Override
    public void setLongPreference(String string, long l) {
        keychainQuery.put(string, l);
    }

    @Override
    public void setFloatPreference(String string, float f) {
        keychainQuery.put(string, f);
    }

    @Override
    public void storePreferences(String string) throws IOException {
        keychainQuery.write(finalPath, true);
    }

    @Override
    public void storePreferences() throws IOException {
        storePreferences("No reason given");
    }

}
