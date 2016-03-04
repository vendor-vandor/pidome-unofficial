/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCPreferences;
import org.pidome.client.system.UnknownPCCPreferenceException;

/**
 *
 * @author John
 */
public class FrameSettings {
 
    /**
     * Just normal fading or be creative.
     */
    private static boolean erratic = false;
    
    /**
     * Mirror mode or not.
     */
    private static boolean mirrormode = false;

    /**
     * Show the clock or not.
     */
    private static boolean showClock = true;
    
    /**
     * Show the weather or not.
     */
    private static boolean showWeather = true;
    
    /**
     * Show the weather or not.
     */
    private static boolean showLogo = true;
    
    /**
     * Stand alone mode or not.
     */
    private static boolean standalone = false;
    
    /**
     * The delay before an other image appears.
     */
    private static long rotationDelay = 10;
    
    /**
     * Replace images from "unsupported" formats to supported formats.
     */
    private static boolean formatReplace = false;
    
    /**
     * Time a transition takes.
     */
    private static long transitionDelay = 5l;

    /**
     * The main setting if the room temperature device is enabled or not.
     */
    private static boolean roomDeviceEnabled = false;
    /**
     * If there is a room temperature device config, it will be put here.
     */
    private static String[] roomdevice = new String[3];
    
    /**
     * If the automation controls should be shown or not.
     */
    private static boolean showAutomationControls = true;
    
    /**
     * If the global user status should be shown or not.
     */
    private static boolean showGlobalUserStatus = true;
    
    static {
        Logger.getLogger(FrameSettings.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Sets the preferences from the user preferences file.
     * @param settings 
     */
    protected static void setPreferences(PCCPreferences settings){
        try {
            setLogLevel(settings.get("loglevel"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No loglevel preference, running with none as default", ex);
        }
        try {
            setMirrorMode(settings.get("runmode").equals("mirror"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No runmode preference, running in \"photos\" mode", ex);
        }
        try {
            setShowClock(settings.get("clock").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No clock preference, running with clock enabled", ex);
        }
        try {
            setShowWeather(settings.get("weather").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No weather preference, running with weather enabled", ex);
        }
        try {
            setShowWeather(settings.get("weather").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No weather preference, running with weather enabled", ex);
        }
        try {
            setErratic(!settings.get("tweens").equals("fade"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No tween preference, running with erratic enabled", ex);
        }
        try {
            setShowLogo(settings.get("logo").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No logo preference, running with logo enabled", ex);
        }
        try {
            setRotationDelay(Long.parseLong(settings.get("rotationdelay")));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No rotationdelay preference, running with 10 seconds default", ex);
        }
        try {
            setFormatReplace(settings.get("formatreplace").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No formatreplace preference, running with false as default", ex);
        }
        try {
            setTransitionDelay(Long.valueOf(settings.get("transitiontime")));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No transitiontime preference, running with 5 seconds as default", ex);
        }
        try {
            setRoomTempDeviceEnabled(settings, Boolean.valueOf(settings.get("showroomtemp")));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No roomtempdevice preference, device will not be shown.", ex);
        }
        try {
            setAutomationControlsEnabled(Boolean.valueOf(settings.get("automationcontrols")));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No automation controls preference, defaults to shown.", ex);
        }
        try {
            setGlobalUserStatusEnabled(Boolean.valueOf(settings.get("globaluserstatus")));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No automation controls preference, defaults to shown.", ex);
        }
        try {
            setStandAlone(settings.get("standalone").equals("true"));
        } catch (UnknownPCCPreferenceException ex) {
            Logger.getLogger(FrameSettings.class.getName()).log(Level.WARNING, "No stand alone preference, running with server present as default", ex);
        }
    }

    /**
     * Sets if global user status should be shown.
     * @param replace 
     */
    private static void setGlobalUserStatusEnabled(boolean enabled){
        showGlobalUserStatus = enabled;
    }
    
    /**
     * Returns true if global user status should be shown.
     * @return 
     */
    public static boolean getGlobalUserStatusEnabled(){
        return showGlobalUserStatus;
    }
    
    /**
     * Sets if automation controls should be shown.
     * @param replace 
     */
    private static void setAutomationControlsEnabled(boolean enabled){
        showAutomationControls = enabled;
    }
    
    /**
     * Returns true if automation controls should be shown.
     * @return 
     */
    public static boolean getAutomationControlsEnabled(){
        return showAutomationControls;
    }
    
    /**
     * Sets if a roomtempdevice is enabled or not.
     * @param settings
     * @param enabled
     * @throws UnknownPCCPreferenceException 
     */
    private static void setRoomTempDeviceEnabled(PCCPreferences settings, boolean enabled) throws UnknownPCCPreferenceException {
        roomDeviceEnabled = enabled;
        if(roomDeviceEnabled) {
            setRoomTempDevice(settings.get("roomtempdevice"));
        }
    }
    
    /**
     * Returns if a room temp device is enabled.
     * @return 
     */
    public static boolean getRoomDeviceEnabled(){
        return roomDeviceEnabled;
    }
    
    /**
     * Set's room device info.
     * @param device 
     */
    private static void setRoomTempDevice(String device){
        if(!device.equals("false")){
            try {
                roomdevice = device.split(":");
            } catch (Exception ex){
                Logger.getLogger(FrameSettings.class.getName()).log(Level.SEVERE, "Device configured, but incorrectly", ex);
            }
        }
    }
    
    /**
     * Returns the room device.
     * @return 
     */
    public static String[] getRoomDevice(){
        return roomdevice;
    }
    
    /**
     * Sets if images are needed to replace.
     * @param replace 
     */
    private static void setFormatReplace(boolean replace){
        formatReplace = replace;
    }
    
    /**
     * Returns true if images are requested to be replaced.
     * @return 
     */
    public static boolean getFormatReplace(){
        return formatReplace;
    }
    
    /**
     * Sets transition delay.
     * @param delay 
     */
    private static void setTransitionDelay(long delay){
        transitionDelay = delay;
    }
    
    /**
     * Gets transition delay
     * @return 
     */
    public static long getTransitionDelay(){
        return transitionDelay;
    }
    
    /**
     * Sets log level
     * @param level 
     */
    private static void setLogLevel(String level){
        Logger topLogger = java.util.logging.Logger.getLogger("");
        Handler consoleHandler = null;
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        switch(level){
            case "debug":
                consoleHandler.setLevel(Level.ALL);
            break;
            case "warn":
                consoleHandler.setLevel(Level.WARNING);
            break;
            case "info":
                consoleHandler.setLevel(Level.INFO);
            break;
            default:
                consoleHandler.setLevel(Level.SEVERE);
            break;
        }
    }
    
    /**
     * Set delay for photos rotation.
     * @param delay 
     */
    private static void setRotationDelay(long delay){
        rotationDelay = (delay<10)?10:delay;
    }
    
    /**
     * Get delay for photos rotation. 
     */
    public static long getRotationDelay(){
        return rotationDelay;
    }
    
    /**
     * Set normal fade mode or creative image rotating.
     * @param isErratic 
     */
    private static void setStandAlone(boolean standalone){
        FrameSettings.standalone = standalone;
        if(standalone){
            setShowClock(false);
            setShowWeather(false);
            setMirrorMode(false);
            roomDeviceEnabled = false;
            setAutomationControlsEnabled(false);
            setGlobalUserStatusEnabled(false);
        }
    }
    
    /**
     * Should be erratic or not.
     * @return 
     */
    public static boolean isStandAlone(){
        return standalone;
    }
            
    /**
     * Show clock or not.
     * @param show 
     */
    private static void setShowLogo(boolean show){
        showLogo = show;
    }
    
    /**
     * Show the clock or not.
     * @return 
     */
    public static boolean showLogo(){
        return showLogo;
    }
    
    /**
     * Set normal fade mode or creative image rotating.
     * @param isErratic 
     */
    private static void setErratic(boolean isErratic){
        erratic = isErratic;
    }
    
    /**
     * Should be erratic or not.
     * @return 
     */
    public static boolean isErratic(){
        return erratic;
    }
    
    /**
     * Mirror or photos mode.
     * @param isMirrorMode 
     */
    private static void setMirrorMode(boolean isMirrorMode){
        mirrormode = isMirrorMode;
    }
    
    /**
     * Should be mirror mode or not.
     * @return 
     */
    public static boolean isMirrorMode(){
        return mirrormode;
    }
    
    /**
     * Show clock or not.
     * @param show 
     */
    private static void setShowClock(boolean show){
        showClock = show;
    }
    
    /**
     * Show the clock or not.
     * @return 
     */
    public static boolean showClock(){
        return showClock;
    }
    
    /**
     * Show weather or not.
     * @param show 
     */
    private static void setShowWeather(boolean show){
        showWeather = show;
    }
    
    /**
     * Show the clock or not.
     * @return 
     */
    public static boolean showWeather(){
        return showWeather;
    }
    
}