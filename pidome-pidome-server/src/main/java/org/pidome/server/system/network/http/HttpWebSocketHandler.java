/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.remoteclient.AuthenticationException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.http.rpc.PiDomeJSONRPCAuthentificationParameters;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;
import org.pidome.server.services.http.websocket.WebsocketConsumer;
import org.pidome.server.services.messengers.ClientMessenger;

/**
 *
 * @author John
 */
public class HttpWebSocketHandler {

    private static List<WebsocketConsumer> authUsers = new ArrayList<>();
    
    /**
     * Websocket endpoint address.
     */
    protected final static String WEBSOCKET_ADDRESS = "/stream";
    
    /**
     * Class logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(HttpWebSocketHandler.class);

    /**
     * Returns the current websocket location.
     *
     * @param req The Http request
     * @param ssl if the request is done via ssl.
     * @return
     */
    protected static String getWebSocketLocation(HttpRequest req, boolean ssl) {
        String location = req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_ADDRESS;
        if (ssl) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    /**
     * Handles incoming websocket frames.
     *
     * @param handshaker Websocket handshake handler.
     * @param ctx The Channel handler.
     * @param frame A webosckert frame.
     * @throws UnsupportedOperationException When a websocket frame is not a
     * text based frame.
     */
    protected static void handleWebSocketFrame(WebSocketServerHandshaker handshaker, ChannelHandlerContext ctx, WebSocketFrame frame) throws UnsupportedOperationException {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            finish(ctx);
            LOG.debug("Closed websocket channel: {}", frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            LOG.debug("ping/pong websocket channel: {}", frame);
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame type not supported", frame.getClass().getName()));
        }
        
        String request = ((TextWebSocketFrame) frame).text();
        
        PidomeJSONRPC pidomeJSONRPC;
        try {
            if(request!=null && !request.isEmpty()){
                pidomeJSONRPC = new PidomeJSONRPC(URLDecoder.decode(request,"UTF-8"));
                Map<String, Object> result = new HashMap<>();
                Map<String, Object> data = new HashMap<>();
                result.put("success", true);
                result.put("message", "");
                if (!isAuthorized(ctx)) {
                    data.put("key", "");
                    data.put("auth", false);
                    try {
                        if(pidomeJSONRPC.getMethod().equals("ClientService.signOff")){
                            ctx.channel().disconnect();
                            finish(ctx);
                        } else if(pidomeJSONRPC.getMethod().equals("ClientService.resume")){
                            WebsocketConsumer client = new WebsocketConsumer(ctx);
                            PiDomeJSONRPCAuthentificationParameters authObjects = pidomeJSONRPC.getAuthenticationParameters();
                            if(authObjects.getKey()!=null){
                                for(RemoteClient remoteClient:RemoteClientsConnectionPool.getConnectedClients()){
                                    if(remoteClient.getKey().equals(authObjects.getKey())){
                                        PersonsManagement.getInstance().getPersonByRemoteClient(remoteClient).addRemoteClient(client);
                                        authUsers.add(client);
                                        data.put("code", 200);
                                        data.put("auth", true);
                                        data.put("message", "Authorized by legal web user.");
                                        LOG.debug("Client at {} is authorized as client for web interface", client.getRemoteSocketAddress());
                                        break;
                                    }
                                }
                                if(!data.containsKey("code") || (int)data.get("code") != 200){
                                    data.put("code", 401);
                                    data.put("message", "Authentication failed");
                                    LOG.warn("Client at {} is not authorized as client for web interface", client.getRemoteSocketAddress());
                                }
                            } else {
                                data.put("code", 401);
                                data.put("message", "Authentication failed");
                                LOG.error("Client at {} is not authorized is missing client key", client.getRemoteSocketAddress());
                            }
                        } else if(!pidomeJSONRPC.getMethod().equals("ClientService.signOn")){
                            throw new AuthenticationException("Not authorized");
                        } else if(pidomeJSONRPC.getMethod().equals("ClientService.signOn")){
                            WebsocketConsumer client = new WebsocketConsumer(ctx);
                            PiDomeJSONRPCAuthentificationParameters authObjects = pidomeJSONRPC.getAuthenticationParameters();
                            RemoteClientsAuthentication.AuthResult authResult = RemoteClientsAuthentication.authenticateWebsocketClient(client, authObjects);
                            switch(authResult){
                                case OK:
                                    data.put("auth", true);
                                    data.put("code", 200);
                                    data.put("throttled", client.throttled());
                                    data.put("key", client.getKey());
                                    data.put("message", "Authentication ok");
                                    authUsers.add(client);
                                    LOG.info("Client at {} is authorized as {}", client.getRemoteSocketAddress(), client.getClientName());
                                    broadcastSignon(client);
                                break;
                                case WAIT:
                                    data.put("auth", true);
                                    data.put("code", 202);
                                    data.put("key", client.getKey());
                                    data.put("message", "Authentication needs to be verified");
                                    LOG.info("Client at {} needs to be authorized", client.getRemoteSocketAddress(), client.getClientName());
                                    RemoteClientsConnectionPool.addWaitingDevice(client.getClientName(), client);
                                break;
                                default:
                                    data.put("code", 401);
                                    data.put("message", "Authentication failed");
                                    LOG.info("Client at {} is not authorized as {}", client.getRemoteSocketAddress(), client.getClientName());
                                break;
                            }
                        } else {
                            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
                        }
                    } catch (AuthenticationException ex){
                        data.put("code", 401);
                        data.put("message", ex.getMessage());
                    }
                    try {
                        Thread.sleep(200); /// Sometimes We are to fast for some mobiles confirming/denying access.
                    } catch (InterruptedException ex) {
                        LOG.error("Could not wait for sending to {}: {}", ctx.channel().remoteAddress(), ex.getMessage());
                    }
                    result.put("data", data);
                    LOG.debug("Sending auth result {} to {}", pidomeJSONRPC.constructResponse(result), ctx.channel().remoteAddress());
                    ctx.channel().write(new TextWebSocketFrame(pidomeJSONRPC.constructResponse(result)));
                } else {
                    boolean found = false;
                    for(WebsocketConsumer client: authUsers){
                        if(client.isSocket(ctx)){
                            found = true;
                            pidomeJSONRPC.handle(RemoteClientsConnectionPool.getClientBaseByConnection(client), client);
                            ctx.channel().write(new TextWebSocketFrame(pidomeJSONRPC.getResult()));
                        }
                    }
                    if(!found) throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
                }
            } else {
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
            }
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Problem with JSON-RPC: {}", ex.getMessage(), ex);
            ctx.channel().write(new TextWebSocketFrame(ex.getJsonReadyMessage()));
        } catch (UnsupportedEncodingException ex) {
            ctx.channel().write(new TextWebSocketFrame("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+PidomeJSONRPCException.JSONError.PARSE_ERROR.toLong()+", \"message\": \"Parse error, could not encode url: "+ex.getMessage()+"\"}, \"id\": null}"));
        } catch (Exception ex) {
            LOG.error("Error in handling JSON-RPC requests (send/receive): {}", ex.getMessage(), ex);
            ctx.channel().write(new TextWebSocketFrame("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+PidomeJSONRPCException.JSONError.SERVER_ERROR.toLong()+", \"message\": \"an internal server error occurred: "+ex.getMessage()+"\"}, \"id\": null}"));
        }
        LOG.info("{} received {}", ctx.channel(), request);
    }
    
    private static void finish(final ChannelHandlerContext ws){
        WebsocketConsumer removable = null;
        for(WebsocketConsumer client:authUsers){
            if(client.isSocket(ws)){
                removable = client;
                client.finish();
            }
        }
        if(removable!=null){
            /// Take care of the cleanup our self.
            RemoteClientsConnectionPool.removeClient(removable);
            RemoteClientsAuthentication.deAuthorize(removable);
            authUsers.remove(removable);
        }
    }
    
    /**
     * Broadcast the clients signon to other clients.
     * @param client
     */
    private static void broadcastSignon(RemoteClient client){
        ClientMessenger.send("ClientService", "signOn", 0, new HashMap<String,String>(){{put("name",client.getClientName());}});
    }
    
    /**
     * Broadcasts a signoff.
     * @param client 
     */
    private static void broadcastSignoff(RemoteClient client){
        ClientMessenger.send("ClientService", "signOff", 0, new HashMap<String,String>(){{put("name",client.getClientName());}});
    }
    
    /**
     * Check if the current connection is authorized.
     * This checks if a connection is made by a web authorized user or a pure socket user.
     * @param ws
     * @return 
     */
    private static boolean isAuthorized(final ChannelHandlerContext ws){
        for(WebsocketConsumer client:authUsers){
            if(client.isSocket(ws)){
                return RemoteClientsAuthentication.isAuthorized(client);
            }
        }
        return false;
    }
    
    /**
     * Broadcasts messages to allowed clients present in the socket server.
     * This function sends messages to non registered socket clients like the websocket connections via the web interface.
     * @param nameSpace
     * @param message 
     */
    public static void broadcastMessage(String nameSpace, String message){
        authUsers.stream().forEach((wc) -> {
            wc.sendMsg(nameSpace, message.getBytes());
        });
    }
    
}