/// aidl interface for passinf to service.
package org.pidome.client.services.aidl.service;

import org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl;

interface SystemServiceAidlInterface {
 
    // Register callback interfaces to flow data from the service to the client
    void registerCallBack(ClientCallbacksServiceAidl callback);

    // Connection stuff.
    oneway void serviceLogin();
    oneway void startSearch();
    oneway void startInitialConnection();
    boolean hasInitialManualConnectData();
    oneway void manualConnect(in String host, in int port, in boolean secure);
    void disconnect();
    String getHost();
    int getSocketPort();
    int getSocketSSLPort();
    boolean getSocketHasSSL();
    int getHttpPort();
    int getHttpSSLPort();
    boolean getHttpHasSSL();
    oneway void sendData(in String data);
    String getSimpleXmlHttp(in String url, in Map params);
    String getJsonHTTPRPCAsString(in String method, in Map params, in String requestId);
    byte[] getBinaryHttp(String string, in Map map);
    boolean inConnectionProgress();
    String getCurrentConnectionStatusAsString();

    // Client proxying stuff
    String getCurrentClientStatusAsString();
    boolean isLoggedIn();
    boolean inLoginProgress();
    oneway void clientLogin();

    // GPS stuff
    boolean GPSEnabled();
    long getGPSDelay();
    void setLocalizationPreferences(in boolean enabled, in long timeToWait, in boolean wifiHomeEnabled);
    boolean getHomeNetworkHomePresenceEnabled();
    String getHomeNetworkHomePresenceWifiNetworkName();

}