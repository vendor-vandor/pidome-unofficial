/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\John\\Documents\\pidome\\clients\\pidome-client\\PiDome-Client\\src\\android\\aidl\\org\\pidome\\client\\services\\aidl\\service\\SystemServiceAidlInterface.aidl
 */
package org.pidome.client.services.aidl.service;
public interface SystemServiceAidlInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.pidome.client.services.aidl.service.SystemServiceAidlInterface
{
private static final java.lang.String DESCRIPTOR = "org.pidome.client.services.aidl.service.SystemServiceAidlInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.pidome.client.services.aidl.service.SystemServiceAidlInterface interface,
 * generating a proxy if needed.
 */
public static org.pidome.client.services.aidl.service.SystemServiceAidlInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.pidome.client.services.aidl.service.SystemServiceAidlInterface))) {
return ((org.pidome.client.services.aidl.service.SystemServiceAidlInterface)iin);
}
return new org.pidome.client.services.aidl.service.SystemServiceAidlInterface.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_registerCallBack:
{
data.enforceInterface(DESCRIPTOR);
org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl _arg0;
_arg0 = org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl.Stub.asInterface(data.readStrongBinder());
this.registerCallBack(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_serviceLogin:
{
data.enforceInterface(DESCRIPTOR);
this.serviceLogin();
return true;
}
case TRANSACTION_startSearch:
{
data.enforceInterface(DESCRIPTOR);
this.startSearch();
return true;
}
case TRANSACTION_startInitialConnection:
{
data.enforceInterface(DESCRIPTOR);
this.startInitialConnection();
return true;
}
case TRANSACTION_hasInitialManualConnectData:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.hasInitialManualConnectData();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_manualConnect:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _arg2;
_arg2 = (0!=data.readInt());
this.manualConnect(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_disconnect:
{
data.enforceInterface(DESCRIPTOR);
this.disconnect();
reply.writeNoException();
return true;
}
case TRANSACTION_getHost:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getHost();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getSocketPort:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSocketPort();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSocketSSLPort:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSocketSSLPort();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSocketHasSSL:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getSocketHasSSL();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getHttpPort:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getHttpPort();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getHttpSSLPort:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getHttpSSLPort();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getHttpHasSSL:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getHttpHasSSL();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_sendData:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.sendData(_arg0);
return true;
}
case TRANSACTION_getSimpleXmlHttp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
java.lang.String _result = this.getSimpleXmlHttp(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getJsonHTTPRPCAsString:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.getJsonHTTPRPCAsString(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getBinaryHttp:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
byte[] _result = this.getBinaryHttp(_arg0, _arg1);
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
case TRANSACTION_inConnectionProgress:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.inConnectionProgress();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getCurrentConnectionStatusAsString:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCurrentConnectionStatusAsString();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getCurrentClientStatusAsString:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCurrentClientStatusAsString();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_isLoggedIn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isLoggedIn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_inLoginProgress:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.inLoginProgress();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_clientLogin:
{
data.enforceInterface(DESCRIPTOR);
this.clientLogin();
return true;
}
case TRANSACTION_GPSEnabled:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.GPSEnabled();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getGPSDelay:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getGPSDelay();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_setLocalizationPreferences:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
long _arg1;
_arg1 = data.readLong();
boolean _arg2;
_arg2 = (0!=data.readInt());
this.setLocalizationPreferences(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getHomeNetworkHomePresenceEnabled:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getHomeNetworkHomePresenceEnabled();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getHomeNetworkHomePresenceWifiNetworkName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getHomeNetworkHomePresenceWifiNetworkName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.pidome.client.services.aidl.service.SystemServiceAidlInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Register callback interfaces to flow data from the service to the client

@Override public void registerCallBack(org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallBack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Connection stuff.

@Override public void serviceLogin() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_serviceLogin, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void startSearch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startSearch, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void startInitialConnection() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startInitialConnection, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public boolean hasInitialManualConnectData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_hasInitialManualConnectData, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void manualConnect(java.lang.String host, int port, boolean secure) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(host);
_data.writeInt(port);
_data.writeInt(((secure)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_manualConnect, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void disconnect() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getHost() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHost, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSocketPort() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSocketPort, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSocketSSLPort() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSocketSSLPort, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getSocketHasSSL() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSocketHasSSL, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getHttpPort() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHttpPort, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getHttpSSLPort() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHttpSSLPort, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getHttpHasSSL() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHttpHasSSL, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void sendData(java.lang.String data) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(data);
mRemote.transact(Stub.TRANSACTION_sendData, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public java.lang.String getSimpleXmlHttp(java.lang.String url, java.util.Map params) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(url);
_data.writeMap(params);
mRemote.transact(Stub.TRANSACTION_getSimpleXmlHttp, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getJsonHTTPRPCAsString(java.lang.String method, java.util.Map params, java.lang.String requestId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(method);
_data.writeMap(params);
_data.writeString(requestId);
mRemote.transact(Stub.TRANSACTION_getJsonHTTPRPCAsString, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public byte[] getBinaryHttp(java.lang.String string, java.util.Map map) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(string);
_data.writeMap(map);
mRemote.transact(Stub.TRANSACTION_getBinaryHttp, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean inConnectionProgress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_inConnectionProgress, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getCurrentConnectionStatusAsString() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentConnectionStatusAsString, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// Client proxying stuff

@Override public java.lang.String getCurrentClientStatusAsString() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentClientStatusAsString, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isLoggedIn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isLoggedIn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean inLoginProgress() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_inLoginProgress, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void clientLogin() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_clientLogin, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
// GPS stuff

@Override public boolean GPSEnabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_GPSEnabled, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public long getGPSDelay() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getGPSDelay, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((enabled)?(1):(0)));
_data.writeLong(timeToWait);
_data.writeInt(((wifiHomeEnabled)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setLocalizationPreferences, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean getHomeNetworkHomePresenceEnabled() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHomeNetworkHomePresenceEnabled, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getHomeNetworkHomePresenceWifiNetworkName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getHomeNetworkHomePresenceWifiNetworkName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerCallBack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_serviceLogin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_startSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_startInitialConnection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_hasInitialManualConnectData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_manualConnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getHost = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getSocketPort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getSocketSSLPort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getSocketHasSSL = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getHttpPort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getHttpSSLPort = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getHttpHasSSL = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_sendData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getSimpleXmlHttp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_getJsonHTTPRPCAsString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_getBinaryHttp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_inConnectionProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_getCurrentConnectionStatusAsString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getCurrentClientStatusAsString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_isLoggedIn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_inLoginProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_clientLogin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_GPSEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_getGPSDelay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_setLocalizationPreferences = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_getHomeNetworkHomePresenceEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_getHomeNetworkHomePresenceWifiNetworkName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
}
// Register callback interfaces to flow data from the service to the client

public void registerCallBack(org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl callback) throws android.os.RemoteException;
// Connection stuff.

public void serviceLogin() throws android.os.RemoteException;
public void startSearch() throws android.os.RemoteException;
public void startInitialConnection() throws android.os.RemoteException;
public boolean hasInitialManualConnectData() throws android.os.RemoteException;
public void manualConnect(java.lang.String host, int port, boolean secure) throws android.os.RemoteException;
public void disconnect() throws android.os.RemoteException;
public java.lang.String getHost() throws android.os.RemoteException;
public int getSocketPort() throws android.os.RemoteException;
public int getSocketSSLPort() throws android.os.RemoteException;
public boolean getSocketHasSSL() throws android.os.RemoteException;
public int getHttpPort() throws android.os.RemoteException;
public int getHttpSSLPort() throws android.os.RemoteException;
public boolean getHttpHasSSL() throws android.os.RemoteException;
public void sendData(java.lang.String data) throws android.os.RemoteException;
public java.lang.String getSimpleXmlHttp(java.lang.String url, java.util.Map params) throws android.os.RemoteException;
public java.lang.String getJsonHTTPRPCAsString(java.lang.String method, java.util.Map params, java.lang.String requestId) throws android.os.RemoteException;
public byte[] getBinaryHttp(java.lang.String string, java.util.Map map) throws android.os.RemoteException;
public boolean inConnectionProgress() throws android.os.RemoteException;
public java.lang.String getCurrentConnectionStatusAsString() throws android.os.RemoteException;
// Client proxying stuff

public java.lang.String getCurrentClientStatusAsString() throws android.os.RemoteException;
public boolean isLoggedIn() throws android.os.RemoteException;
public boolean inLoginProgress() throws android.os.RemoteException;
public void clientLogin() throws android.os.RemoteException;
// GPS stuff

public boolean GPSEnabled() throws android.os.RemoteException;
public long getGPSDelay() throws android.os.RemoteException;
public void setLocalizationPreferences(boolean enabled, long timeToWait, boolean wifiHomeEnabled) throws android.os.RemoteException;
public boolean getHomeNetworkHomePresenceEnabled() throws android.os.RemoteException;
public java.lang.String getHomeNetworkHomePresenceWifiNetworkName() throws android.os.RemoteException;
}
