/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderDateFormat;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import static org.pidome.server.system.network.http.Http2Util.firstValue;
import static org.pidome.server.system.network.http.Http2Util.toInt;

/**
 *
 * @author John
 */
public class Http2ClientHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements HttpRequestWriterInterface {

    /**
     * Class logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Http2ClientHandler.class);
    
    private static final String LATENCY_FIELD_NAME = "latency";
    private static final int MIN_LATENCY = 0;
    private static final int MAX_LATENCY = 1000;

    private final int port;
    
    protected Http2ClientHandler(int port){
        super();
        this.port = port;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder queryString = new QueryStringDecoder(request.uri());
        String streamId = streamId(request);
        int latency = toInt(firstValue(queryString, LATENCY_FIELD_NAME), 0);
        if (latency < MIN_LATENCY || latency > MAX_LATENCY) {
            sendBadRequest(ctx, streamId);
            return;
        }
        HttpRequestHandler.processManagement(ctx, request, this, streamId);
    }

    private void sendBadRequest(ChannelHandlerContext ctx, String streamId) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, BAD_REQUEST, EMPTY_BUFFER);
        streamId(response, streamId);
        ctx.writeAndFlush(response);
    }

    private String streamId(FullHttpRequest request) {
        return request.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
    }

    private void streamId(FullHttpResponse response, String streamId) {
        response.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId) {
        writeResponse(ctx, status, buf, fileType, streamId, false);
    }

    @Override
    public void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId, boolean cache) {
        
        String plainIp = HttpRequestHandler.getPlainIp(ctx.channel().localAddress());
        
        ByteBuf content = ctx.alloc().buffer(buf.length);
        content.writeBytes(buf);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        streamId(response, streamId);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, fileType);
        
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpRequestHandler.getContentTypeHeader(fileType));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "https://" + plainIp + ((port != 80) ? ":" + port : ""));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.headers().set(HttpHeaderNames.SERVER, "PiDome integrated 0.2 HTTP2");
        
        if (cache == true) {
            DateTime dt = new DateTime();
            HttpHeaderDateFormat dateFormat = HttpHeaderDateFormat.get();
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3153600");
            response.headers().set(HttpHeaderNames.EXPIRES, dateFormat.format(dt.plusMonths(12).toDate()));
        } else {
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache, must-revalidate");
            response.headers().set(HttpHeaderNames.EXPIRES, "Sat, 26 Jul 1997 05:00:00 GMT");
        }
        
        ctx.writeAndFlush(response);
    }
}