/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.service;

import org.pidome.client.services.aidl.service.SystemServiceAidl;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.services.aidl.AndroidPreferencesAidl;
import org.pidome.client.settings.LocalizationInfoInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class LocalizationServiceAidl implements LocalizationInfoInterface {
    
    ///// GPS stuff.
    LocationManager locationManager;
    Location currentBestLocation;
    
    boolean gpsListenerAdded = false;
    boolean locationProviderEnabled = false;
    //// Gps defaults:
    static long  timeToWait      = 300000; 
    
    private final AndroidPreferencesAidl prefs;
    private final SystemServiceAidl systemService;
    
    private final ObjectPropertyBindingBean<Double> currentDistanceProperty = new ObjectPropertyBindingBean(0.0);
    
    private final BooleanPropertyBindingBean isHome = new BooleanPropertyBindingBean(Boolean.TRUE);
    
    protected LocalizationServiceAidl(SystemServiceAidl systemService, AndroidPreferencesAidl prefs){
        this.prefs = prefs;
        this.systemService = systemService;
        isHome.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if((boolean)evt.getNewValue() == true){
                startGPSExecutor();
            } else {
                stopGPSExecutor();
            }
        });
    }
    
    protected final void setHome(boolean home){
        isHome.setValue(home);
    }
    
    @Override
    public final void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled){
        prefs.setBoolPreference("gpsEnabled",   enabled);
        prefs.setLongPreference("GPSDelay",     timeToWait);
        setHomeNetworkHomePresenceEnabled(wifiHomeEnabled);
        try {
            prefs.storePreferences();
            Handler handler = new Handler(systemService.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                   Toast toast = Toast.makeText(systemService, "Localization settings saved", Toast.LENGTH_SHORT);
                   toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 40);
                   toast.show();
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(LocalizationServiceAidl.class.getName()).log(Level.SEVERE, "Could not store GPS preferences", ex);
        }
        restartGPSExecutor();
    }
    
    @Override
    public boolean GPSEnabled(){
        return prefs.getBoolPreference("gpsEnabled", false);
    }
    
    @Override
    public long getGPSDelay(){
        return prefs.getLongPreference("GPSDelay", timeToWait);
    }
    
    private void setHomeNetworkHomePresenceEnabled(boolean wifiHomeEnabled){
        prefs.setBoolPreference("wifiConnectHomeEnabled", wifiHomeEnabled);
        Log.i("LocalizationService", "WiFi home enabled");
        if(wifiHomeEnabled){
            try {
                ConnectivityManager connManager =  (ConnectivityManager)systemService.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    final WifiManager wifiManager = (WifiManager)systemService.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (connectionInfo != null) {
                        String SSID = connectionInfo.getSSID();
                        String BSSID= connectionInfo.getBSSID();
                        if(SSID!=null && BSSID!=null){
                            prefs.setStringPreference("wifiConnectSSID", SSID.replace("\"", ""));
                            prefs.setStringPreference("wifiConnectBSSID", BSSID);
                        }
                    }
                }
            } catch (Exception ex){
                Log.e("Saving home network failed", ex.getMessage());
            }
        }
    }
    
    @Override
    public boolean getHomeNetworkHomePresenceEnabled(){
        return prefs.getBoolPreference("wifiConnectHomeEnabled", false);
    }
    
    @Override
    public String getHomeNetworkHomePresenceWifiNetworkName() throws IOException {
        ConnectivityManager connManager =  (ConnectivityManager)systemService.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo!= null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            final WifiManager wifiManager = (WifiManager)systemService.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String SSID = connectionInfo.getSSID();
                String BSSID= connectionInfo.getBSSID();
                if(SSID!=null && BSSID!=null){
                    return SSID.replace("\"", "");
                }
            }
        }
        throw new IOException("Not wifi connected or no wifi information available");
    }
    
    private void restartGPSExecutor(){
        stopGPSExecutor();
        if(isHome.getValue()==false){
            startGPSExecutor();
        }
    }
    
    private void startGPSExecutor(){
        if(prefs.getBoolPreference("gpsEnabled", false) && !gpsListenerAdded){
            new Thread(){
                @Override
                public final void run(){
                    locationManager = (LocationManager)systemService.getSystemService(Context.LOCATION_SERVICE);
                    try {
                        Criteria criteria = new Criteria();
                        criteria.setHorizontalAccuracy (Criteria.ACCURACY_FINE);
                        criteria.setPowerRequirement(Criteria.POWER_LOW);
                        criteria.setAltitudeRequired(false);
                        criteria.setSpeedRequired(false);
                        criteria.setCostAllowed(true);
                        criteria.setBearingRequired(false);
                        List<String> lProviders = locationManager.getProviders(true);
                        for(int i=0; i<lProviders.size(); i++){
                            Log.d("LocalizationService", "Have provider: " + lProviders.get(i));
                        }
                        String provider = locationManager.getBestProvider(criteria, true); // null
                        Log.d("LocalizationService", "Using provider: " + provider);
                        locationManager.requestLocationUpdates(provider, prefs.getLongPreference("GPSDelay", timeToWait), 0, locationListener, Looper.getMainLooper() );
                        Log.i("LocalizationService", "Localization enabled, Setting for wait: " + prefs.getLongPreference("GPSDelay", timeToWait));
                        gpsListenerAdded = true;
                    } catch (Exception ex){
                        Log.w("Localozation not available", ex);
                    }
                }
            }.start();
        } else {
            Log.i("PidomeService", "Localization disabled");
        }
    }
    
    private void stopGPSExecutor(){
        Log.i("PidomeService", "Stopping localization");
        try {
            if(locationManager!=null){
                locationManager.removeUpdates(locationListener);
            }
            if(gpsListenerAdded){
                gpsListenerAdded = false;
            }
        } catch (Exception ex){
            Log.w("Localozation not available", ex);
        }
    }
    
    private void sendLocation(Location location){
        //if(isBetterLocation(location, currentBestLocation)){
            currentBestLocation = location;
            Log.i("LocalizationService", "New lat: " + currentBestLocation.getLatitude() + ", lon: " + currentBestLocation.getLongitude() + ", accuracy: " + currentBestLocation.getAccuracy());
            sendCurrentLocation();
        //}
    }
    
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location newLocation) {
            sendLocation(newLocation);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("PidomeService", "Localization provider "+provider+" enabled");
            restartGPSExecutor();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("PidomeService", "Localization provider "+provider+" disabled");
            restartGPSExecutor();
        }
    };
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    
    protected final void sendCurrentLocation(){
        if(systemService.getClient().isloggedIn()){
            new Thread(){
                @Override
                public final void run(){
                    try {
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("lat", currentBestLocation.getLatitude());
                                put("lon", currentBestLocation.getLongitude());
                                put("acc", currentBestLocation.getAccuracy());
                            }
                        };
                        PCCEntityDataHandler result = systemService.getConnection().getJsonHTTPRPC("UserService.updateMyLocation", sendObject, "UserService.updateMyLocation");
                        currentDistanceProperty.setValue(((Number)((Map<String,Object>)result.getResult().get("data")).get("distance")).doubleValue());
                    } catch (PCCEntityDataHandlerException ex) {
                        Logger.getLogger(LocalizationServiceAidl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    } catch (Exception ex){
                        Logger.getLogger(LocalizationServiceAidl.class.getName()).log(Level.SEVERE, "Uncaught exception: " + ex.getMessage(), ex);
                    }
                }
            }.start();
        }
    }

    @Override
    public ObjectPropertyBindingBean<Double> getCurrentDistanceProperty() {
        return this.currentDistanceProperty;
    }
    
}