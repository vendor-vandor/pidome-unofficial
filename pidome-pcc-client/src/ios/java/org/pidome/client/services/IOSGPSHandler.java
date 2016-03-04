/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;
import org.robovm.apple.corelocation.CLErrorCode;
import org.robovm.apple.corelocation.CLLocation;
import org.robovm.apple.corelocation.CLLocationAccuracy;
import org.robovm.apple.corelocation.CLLocationManager;
import org.robovm.apple.corelocation.CLLocationManagerDelegateAdapter;
import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;

/**
 *
 * @author John & Marcel
 */
public final class IOSGPSHandler implements LocalizationInfoInterface {

    private final LocalPreferenceStorageInterface prefStorage;
    private final ObjectPropertyBindingBean<Double> currentDistance = new ObjectPropertyBindingBean<Double>(0.0);

    private final CLLocationManager GPSmanager;
    private List<CLLocation> locationMeasurements;
    private CLLocation bestEffortAtLocation;
    private boolean noTimeout = false;
    private boolean isRunning = false;
    private double accuracy;

    private final PCCSystem systemService;

    ScheduledExecutorService scheduledExecutorService;

    protected IOSGPSHandler(LocalPreferenceStorageInterface prefStorage, PCCSystem systemService) {
        this.prefStorage = prefStorage;
        GPSmanager = new CLLocationManager();
        accuracy = CLLocationAccuracy.NearestTenMeters;
        locationMeasurements = new ArrayList<>();
        this.systemService = systemService;
    }

    protected final void start() {
        this.setupGPS();
    }

    @Override
    public final boolean GPSEnabled() {
        return this.prefStorage.getBoolPreference("GPSEnabled", false);
    }

    @Override
    public long getGPSDelay() {
        return this.prefStorage.getLongPreference("GPSTimeOut", 0);
    }

    @Override
    public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled) {
        this.prefStorage.setBoolPreference("GPSEnabled", enabled);
        this.prefStorage.setLongPreference("GPSTimeOut", timeToWait);
        this.prefStorage.setBoolPreference("WifiHomeEnabled", wifiHomeEnabled);
        this.setupGPS();
        try {
            this.prefStorage.storePreferences("Pref GPS save");
        } catch (IOException ex) {
            Logger.getLogger(IOSGPSHandler.class.getName()).log(Level.SEVERE, "Could not save GPS/Home preferences", ex);
        }
    }

    @Override
    public boolean getHomeNetworkHomePresenceEnabled() {
        return this.prefStorage.getBoolPreference("WifiHomeEnabled", false);
    }

    @Override
    public String getHomeNetworkHomePresenceWifiNetworkName() throws IOException {
        return this.prefStorage.getStringPreference("WifiHomeName", "");
    }

    private void updateDistance(double distance) {
        this.currentDistance.setValue(distance);
    }

    @Override
    public ObjectPropertyBindingBean<Double> getCurrentDistanceProperty() {
        return currentDistance;
    }

    private void setupGPS() {
        if (!this.GPSEnabled() || !GPSmanager.isLocationServicesEnabled()) {
            if (this.scheduledExecutorService != null) {
                this.scheduledExecutorService.shutdownNow();
                this.scheduledExecutorService = null;
            }
            return;
        }

        GPSmanager.setDelegate(new CLLocationManagerDelegateAdapter() {

            @Override
            public void didUpdateLocations(CLLocationManager manager, NSArray<CLLocation> locations) {
                CLLocation newLocation = locations.last();

                locationMeasurements.add(newLocation);

                double locationAge = -newLocation.getTimestamp().getTimeIntervalSinceNow();
                if (locationAge > 5) {
                    return;
                }

                if (newLocation.getHorizontalAccuracy() < 0) {
                    return;
                }

                if (bestEffortAtLocation == null
                        || bestEffortAtLocation.getHorizontalAccuracy() > newLocation.getHorizontalAccuracy()) {

                    bestEffortAtLocation = newLocation;

                    if (newLocation.getHorizontalAccuracy() <= GPSmanager.getDesiredAccuracy()) {
                        sendCurrentLocation(newLocation);
                        stopUpdatingLocation();
                        bestEffortAtLocation = null;
                    }
                }
            }

            @Override
            public void didFail(CLLocationManager manager, NSError error) {
                if (error.getErrorCode() != CLErrorCode.LocationUnknown) {
                    stopUpdatingLocation();
                }
            }
        });

        GPSmanager.setDesiredAccuracy(accuracy);

        if (Foundation.getMajorSystemVersion() >= 8) {
            GPSmanager.requestAlwaysAuthorization();
        }

        if (this.scheduledExecutorService == null) {
            this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        this.scheduledExecutorService.scheduleAtFixedRate((Runnable) () -> {
            if (!isRunning) {
                GPSmanager.startUpdatingLocation();
                isRunning = true;
            }
        },
        0L,
        this.getGPSDelay() / 60000,
        TimeUnit.MINUTES);
    }

    private void stopUpdatingLocation() {
        GPSmanager.stopUpdatingLocation();
        isRunning = false;
    }

    protected final void sendCurrentLocation(final CLLocation newLocation) {
        if (systemService.getClient().isloggedIn()) {
            new Thread() {
                @Override
                public final void run() {
                    try {
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("lat", newLocation.getCoordinate().getLatitude());
                                put("lon", newLocation.getCoordinate().getLongitude());
                                put("acc", newLocation.getHorizontalAccuracy());
                            }
                        };
                        PCCEntityDataHandler result = systemService.getConnection().getJsonHTTPRPC("UserService.updateMyLocation", sendObject, "UserService.updateMyLocation");
                        currentDistance.setValue(((Number) ((Map<String, Object>) result.getResult().get("data")).get("distance")).doubleValue());
                    } catch (PCCEntityDataHandlerException ex) {
                        Logger.getLogger(IOSGPSHandler.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    } catch (Exception ex) {
                        Logger.getLogger(IOSGPSHandler.class.getName()).log(Level.SEVERE, "Uncaught exception: " + ex.getMessage(), ex);
                    }
                }
            }.start();
        }
    }
}
