/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\John\\Documents\\pidome\\clients\\pidome-client\\PiDome-Client\\src\\android\\aidl\\org\\pidome\\client\\services\\aidl\\client\\ClientCallbacksServiceAidl.aidl
 */
package org.pidome.client.services.aidl.client;
public interface ClientCallbacksServiceAidl extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl
{
private static final java.lang.String DESCRIPTOR = "org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl interface,
 * generating a proxy if needed.
 */
public static org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl))) {
return ((org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl)iin);
}
return new org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl.Stub.Proxy(obj);
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
case TRANSACTION_updateGPSDistance:
{
data.enforceInterface(DESCRIPTOR);
double _arg0;
_arg0 = data.readDouble();
this.updateGPSDistance(_arg0);
return true;
}
case TRANSACTION_updateConnectionStatus:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.updateConnectionStatus(_arg0);
return true;
}
case TRANSACTION_updateClientStatus:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.updateClientStatus(_arg0);
return true;
}
case TRANSACTION_broadcastServerRPCFromStream:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.broadcastServerRPCFromStream(_arg0);
return true;
}
case TRANSACTION_updateUserPresence:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.updateUserPresence(_arg0);
return true;
}
case TRANSACTION_handleUserLoggedIn:
{
data.enforceInterface(DESCRIPTOR);
this.handleUserLoggedIn();
return true;
}
case TRANSACTION_handleUserLoggedOut:
{
data.enforceInterface(DESCRIPTOR);
this.handleUserLoggedOut();
return true;
}
case TRANSACTION_broadcastLoginEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
this.broadcastLoginEvent(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_broadcastConnectionEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
this.broadcastConnectionEvent(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_appIsInForeGround:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.appIsInForeGround();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.pidome.client.services.aidl.client.ClientCallbacksServiceAidl
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
@Override public void updateGPSDistance(double distance) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeDouble(distance);
mRemote.transact(Stub.TRANSACTION_updateGPSDistance, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void updateConnectionStatus(java.lang.String status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(status);
mRemote.transact(Stub.TRANSACTION_updateConnectionStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void updateClientStatus(java.lang.String status) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(status);
mRemote.transact(Stub.TRANSACTION_updateClientStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void broadcastServerRPCFromStream(java.lang.String RPCMessage) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(RPCMessage);
mRemote.transact(Stub.TRANSACTION_broadcastServerRPCFromStream, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void updateUserPresence(int presenceId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(presenceId);
mRemote.transact(Stub.TRANSACTION_updateUserPresence, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void handleUserLoggedIn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_handleUserLoggedIn, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void handleUserLoggedOut() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_handleUserLoggedOut, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void broadcastLoginEvent(java.lang.String status, int errCode, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(status);
_data.writeInt(errCode);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_broadcastLoginEvent, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void broadcastConnectionEvent(java.lang.String status, int errCode, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(status);
_data.writeInt(errCode);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_broadcastConnectionEvent, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public boolean appIsInForeGround() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_appIsInForeGround, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_updateGPSDistance = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_updateConnectionStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_updateClientStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_broadcastServerRPCFromStream = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_updateUserPresence = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_handleUserLoggedIn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_handleUserLoggedOut = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_broadcastLoginEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_broadcastConnectionEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_appIsInForeGround = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
}
public void updateGPSDistance(double distance) throws android.os.RemoteException;
public void updateConnectionStatus(java.lang.String status) throws android.os.RemoteException;
public void updateClientStatus(java.lang.String status) throws android.os.RemoteException;
public void broadcastServerRPCFromStream(java.lang.String RPCMessage) throws android.os.RemoteException;
public void updateUserPresence(int presenceId) throws android.os.RemoteException;
public void handleUserLoggedIn() throws android.os.RemoteException;
public void handleUserLoggedOut() throws android.os.RemoteException;
public void broadcastLoginEvent(java.lang.String status, int errCode, java.lang.String message) throws android.os.RemoteException;
public void broadcastConnectionEvent(java.lang.String status, int errCode, java.lang.String message) throws android.os.RemoteException;
public boolean appIsInForeGround() throws android.os.RemoteException;
}
