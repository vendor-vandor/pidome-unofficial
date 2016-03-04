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
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.robovm.apple.foundation.NSMutableDictionary;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;

/**
 *
 * @author John
 */
public class IOSSettingsStorage implements LocalSettingsStorageInterface {

    private NSMutableDictionary<NSString, NSObject> keychainQuery;

    final File finalPath;
    
    IOSSettingsStorage() {
        File libraryPath = new File(System.getenv("HOME"), "Library");
        finalPath = new File(libraryPath, "settings.plist");

        keychainQuery = (NSMutableDictionary<NSString, NSObject>) NSMutableDictionary.read(finalPath);

        // if it fails to get an existing dictionary, create a new one.
        if (keychainQuery == null) {
            keychainQuery = new NSMutableDictionary<NSString, NSObject>();
            keychainQuery.write(finalPath, true);
        }
    }

    @Override
    public String getStringSetting(String string, String string1) {
        return keychainQuery.getString(string, string1);
    }

    @Override
    public boolean getBoolSetting(String string, boolean bln) {
        return keychainQuery.getBoolean(string, bln);
    }

    @Override
    public int getIntSetting(String string, int i) {
        return keychainQuery.getInt(string, i);
    }

    @Override
    public long getLongSetting(String string, long l) {
        return keychainQuery.getLong(string, l);
    }

    @Override
    public float getFloatSetting(String string, float f) {
        return keychainQuery.getFloat(string, f);
    }

    @Override
    public void setStringSetting(String string, String string1) {
        keychainQuery.put(string, string1);
    }

    @Override
    public void setBoolSetting(String string, boolean bln) {
        keychainQuery.put(string, bln);
    }

    @Override
    public void setIntSetting(String string, int i) {
        keychainQuery.put(string, i);
    }

    @Override
    public void setLongSetting(String string, long l) {
        keychainQuery.put(string, l);
    }

    @Override
    public void setFloatSetting(String string, float f) {
        keychainQuery.put(string, f);
    }

    @Override
    public void storeSettings(String string) throws IOException {
        keychainQuery.write(finalPath, true);
    }

    @Override
    public void storeSettings() throws IOException {
        storeSettings("No reason given");
    }

}
