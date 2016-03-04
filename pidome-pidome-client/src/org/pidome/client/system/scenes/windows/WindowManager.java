/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.windows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John Sirach
 */
public class WindowManager {

    static Logger LOG = LogManager.getLogger(WindowManager.class);
    
    final static List<WindowComponent> Windows = new ArrayList();
    final static ObservableList<WindowComponent> WindowsList = FXCollections.observableList(Windows);
    
    final static List<WindowManagerListener> windowListeners = new ArrayList();
    
    final static Map<String,Point2D> lastKnownCoords = new HashMap<>();
    
    static WindowManager me;
    
    static String windowingStyle = DisplayConfig.getRunMode();
    
    public static WindowManager getInstance(){
        if(me==null){
            me = new WindowManager();
        }
        return me;
    }
    
    WindowManager(){
        WindowsList.addListener((ListChangeListener.Change<? extends WindowComponent> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().stream().forEach((additem) -> {
                        BroadcastWindowAdded(additem);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().stream().forEach((remitem) -> {
                        BroadcastWindowRemoved(remitem);
                    });
                }
            }
        });
    }
    
    public static void openCreateWindow(String windowComponentClassPath, String name, Object[] params, double fromX, double fromY){
        try {
            Class<WindowComponent> windowClass = (Class<WindowComponent>)Class.forName(windowComponentClassPath);
            Constructor ctor = windowClass.getDeclaredConstructor(new Class[]{Object[].class});
            ctor.setAccessible(true);
            WindowComponent window = (WindowComponent)ctor.newInstance(new Object[] {params});
            openWindow(window, fromX, fromY);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            SimpleErrorMessage error = new SimpleErrorMessage("Window open error");
            error.setMessage("Could not open '"+name+"'");
            error.setSubMessage(ex.getMessage());
            WindowManager.openWindow(error);
            LOG.error("Could not open '"+name+"': {}", ex.getMessage(), ex);
        }
    }
    
    public static void openCreateWindow(String windowComponentClassPath, String name, Object[] params){
        try {
            Class<WindowComponent> windowClass = (Class<WindowComponent>)Class.forName(windowComponentClassPath);
            Constructor ctor = windowClass.getDeclaredConstructor(new Class[]{Object[].class});
            ctor.setAccessible(true);
            WindowComponent window = (WindowComponent)ctor.newInstance(new Object[] {params});
            openWindow(window);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            SimpleErrorMessage error = new SimpleErrorMessage("Window open error");
            error.setMessage("Could not open '"+name+"'");
            error.setSubMessage(ex.getMessage());
            WindowManager.openWindow(error);
            LOG.error("Could not open '"+name+"': {}", ex.getMessage(), ex);
        }
    }
    
    public static boolean containsWindow(String windowName){
        for (WindowComponent Window : Windows) {
            if (Window.getWindowName().equals(windowName)) {
                return true;
            }
        }
        return false;
    }
    
    public static WindowComponent getWindow(String windowName){
        for (WindowComponent Window : Windows) {
            if (Window.getWindowName().equals(windowName)) {
                return Window;
            }
        }
        return null;
    }
    
    public static void openWindow(WindowComponent window, double fromX, double fromY){
        if(WindowsList.contains(window)){
            LOG.debug("Reopen existing window: {}", window.getWindowName());
            if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
                window.getStagedWindow().show();
            } else {
                window.toFront();
            }
        } else if(containsWindow(window.getWindowName())){
            LOG.debug("Open child window: {}", window.getWindowName());
            if(windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
                getWindow(window.getWindowName()).getStagedWindow().show();
            } else {
                getWindow(window.getWindowName()).toFront();
            }
        } else {
            LOG.debug("Open new window: {}", window.getWindowName());
            WindowsList.add(window);
            if(lastKnownCoords.containsKey(window.getWindowName())){
                window.setPosition(lastKnownCoords.get(window.getWindowName()).getX(), lastKnownCoords.get(window.getWindowName()).getY());
            }
            window.open(fromX, fromY);
        }
    }
    
    public static void openWindow(WindowComponent window){
        if(WindowsList.contains(window)){
            window.toFront();
        } else if(containsWindow(window.getWindowName())){
            getWindow(window.getWindowName()).toFront();
        } else {
            if(!windowingStyle.equals(DisplayConfig.RUNMODE_WIDGET)){
                if(lastKnownCoords.containsKey(window.getWindowName())){
                    window.setPosition(lastKnownCoords.get(window.getWindowName()).getX(), lastKnownCoords.get(window.getWindowName()).getY());
                }
            }
            WindowsList.add(window);
            window.open();
        }
    }
    
    public static void closeAll(){
        for (WindowComponent Window : Windows) {
            Window.close(null);
        }
    }
    
    public static void closeWindow(WindowComponent window){
        if(WindowsList.contains(window)){
            lastKnownCoords.put(window.getWindowName(), window.localToScene(window.getBoundsInLocal().getMinX(), window.getBoundsInLocal().getMinY()));
            window.close(null);
            window.destroy();
            WindowsList.remove(window);
        }
    }
    
    public static void addWindowListener(WindowManagerListener listener){
        if(!windowListeners.contains(listener)){
            windowListeners.add(listener);
        }
    }
    
    public static void removeWindowListener(WindowManagerListener listener){
        if(windowListeners.contains(listener)){
            windowListeners.remove(listener);
        }
    }
    
    public static void BroadcastWindowAdded(WindowComponent node){
        Iterator listeners = windowListeners.iterator();
        while (listeners.hasNext()) {
            ((WindowManagerListener) listeners.next()).windowAdded(node);
        }
    }
    
    public static void BroadcastWindowRemoved(WindowComponent node){
        Iterator listeners = windowListeners.iterator();
        while (listeners.hasNext()) {
            ((WindowManagerListener) listeners.next()).windowRemoved(node);
        }
    }

    /*
    @Override
    public void windowMinimized(WindowComponent window) {
        //// Not yet
    }

    @Override
    public void windowClosed(WindowComponent window) {
        WindowsList.remove(window);
    }
    */
}
