package org.pidome.client.services.aidl.client;

interface ClientCallbacksServiceAidl {

    oneway void updateGPSDistance(in double distance);

    oneway void updateConnectionStatus(in String status);

    oneway void updateClientStatus(in String status);

    oneway void broadcastServerRPCFromStream(in String RPCMessage);

    oneway void updateUserPresence(in int presenceId);

    oneway void handleUserLoggedIn();

    oneway void handleUserLoggedOut();

    oneway void broadcastLoginEvent(String status, int errCode, String message);

    oneway void broadcastConnectionEvent(String status, int errCode, String message);

    boolean appIsInForeGround();

}