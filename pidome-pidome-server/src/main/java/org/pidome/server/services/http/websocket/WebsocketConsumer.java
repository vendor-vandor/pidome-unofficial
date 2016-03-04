/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;

/**
 *
 * @author John
 */
public final class WebsocketConsumer extends RemoteClient {
 
    /**
     * Local logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(WebsocketConsumer.class);
    
    /**
     * The remote channel.
     */
    private ChannelHandlerContext socket;
    
    /**
     * The websocket constructor.
     * @param socket 
     */
    public WebsocketConsumer(ChannelHandlerContext socket){
        super(RemoteClient.Type.WEBSOCKET);
        this.socket = socket;
    }
    
    /**
     * Returns if the given WebSocket is this client's socket.
     * @param ws
     * @return 
     */
    public final boolean isSocket(ChannelHandlerContext ws){
        return (socket!=null)?ws==socket:false;
    }
    
    /**
     * Sends the message over the socket.
     * @param nameSpace
     * @param message
     * @return 
     */
    @Override
    public boolean sendSocket(String nameSpace, byte[] message) {
        Runnable send = () -> {
            try {
                TextWebSocketFrame msg = new TextWebSocketFrame(new String(message, "UTF-8"));
                if(throttled()){
                    if(RemoteClientsConnectionPool.getNonThrottledServices().contains(nameSpace)){
                        socket.channel().writeAndFlush(msg);
                    }
                } else {
                    socket.channel().writeAndFlush(msg);
                }
            } catch (UnsupportedEncodingException ex) {
                LOG.error("Message could not be utf8 encoded: {} ({})", new String(message), ex.getMessage());
            }
        };
        send.run();
        return true;
    }

    /**
     * Returns the remote address from the socket.
     * @return 
     */
    @Override
    public String getRemoteSocketAddress() {
        return ((InetSocketAddress)socket.channel().remoteAddress()).getAddress().getHostAddress();
    }

    /**
     * Closes the socket and unsets the local channel.
     */
    @Override
    public void finish() {
        if(socket !=null){
            socket.close();
            this.socket = null;
        }
    }
    
}