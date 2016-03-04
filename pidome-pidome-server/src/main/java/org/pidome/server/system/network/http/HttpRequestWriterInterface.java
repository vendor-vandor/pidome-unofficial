/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author John
 */
public interface HttpRequestWriterInterface {
    
    public void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId);

    public void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId, boolean cache);
    
}
