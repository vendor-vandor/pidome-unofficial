/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.server.system.network.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderDateFormat;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import java.net.SocketAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author John Sirach
 */
public class HttpClientHandler extends ChannelInboundHandlerAdapter implements HttpRequestWriterInterface {

    /**
     * Spdy header for SPDY response
     */
    private final static String SPDY_STREAM_ID = "X-SPDY-Stream-ID";
    /**
     * SPDY priority level.
     */
    private final static String SPDY_STREAM_PRIO = "X-SPDY-Stream-Priority";

    /**
     * The remote socket address.
     */
    private SocketAddress remoteSocketAddress;
    /**
     * The plain version of the server's ip.
     */
    private String plainIp;
    /**
     * Te port the server runs on.
     */
    private final int port;
    /**
     * Boolean if we are on ssl or not.
     */
    private final boolean ssl;

    /**
     * The channel handler context.
     */
    ChannelHandlerContext chc;
    
    /**
     * SPDY id when in use.
     */
    String spdyId;
    
    /**
     * If logged in.
     */
    boolean loggedIn = false;

    /**
     * Client handler logger.
     */
    static Logger LOG = LogManager.getLogger(HttpClientHandler.class);

    /**
     * If keepalive is enabled.
     */
    boolean keepAlive = false;
    
    /**
     * HTTP 1/1.1 client handler.
     *
     * @param ssl if ssl or not.
     * @param port The port used.
     */
    public HttpClientHandler(boolean ssl, int port){
        this.port = port;
        this.ssl = ssl;
    }

    /**
     * Catch any internal exceptions and closes the channel.
     * @param ctx Channel context.
     * @param cause The cause of the exception.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Webserver fault: {}", cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * Reads channel input.
     * @param chc The channel context.
     * @param msg The current content.
     * @throws Exception 
     */
    @Override
    public void channelRead(ChannelHandlerContext chc, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            this.chc = chc;
            FullHttpRequest request = (FullHttpRequest)msg;
            // Decide whether to close the connection or not.
            keepAlive = HttpUtil.isKeepAlive(request);
            if (request.headers().contains(SPDY_STREAM_ID)) {
                spdyId = request.headers().get(SPDY_STREAM_ID);
            }
            this.remoteSocketAddress = chc.channel().remoteAddress();
            this.plainIp = this.getPlainIp(this.remoteSocketAddress);
            if (HttpUtil.is100ContinueExpected(request)) {
                chc.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, CONTINUE));
            }
            HttpRequestHandler.processManagement(chc, request, this);
        }
    }

    /**
     * Flush after done
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
    
    /**
     * Returns the plain ip.
     * @param socket
     * @return 
     */
    private String getPlainIp(SocketAddress socket){
        return socket.toString().substring(1, socket.toString().indexOf(":"));
    }

    /**
     * Writes the response to the output
     * @param ctx The channel context
     * @param status The response status
     * @param buf The buffer containing the data to send.
     * @param fileType The file type.
     * @param streamId The Stream id (only used in http2)
     */
    @Override
    public final void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId) {
        writeResponse(ctx, status, buf, fileType, streamId, true);
    }

    /**
     * Writes the response to the output
     * @param ctx The channel context
     * @param status The response status
     * @param buf The buffer containing the data to send.
     * @param fileType The file type.
     * @param streamId The Stream id (only used in http2)
     * @param cache (if cache headers should be send).
     */
    @Override
    public final void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] buf, String fileType, String streamId, boolean cache) {
        
        ByteBuf content = ctx.alloc().buffer(buf.length);
        content.writeBytes(buf);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);

        HttpUtil.setContentLength(response, response.content().readableBytes());
        
        // In case of SPDY protocol used.
        if (spdyId!=null) {
            response.headers().set(SPDY_STREAM_ID, spdyId);
            response.headers().set(SPDY_STREAM_PRIO, 0);
            response.headers().set(HttpHeaderNames.SERVER, "PiDome integrated 0.2 SPDY");
        } else {
            response.headers().set(HttpHeaderNames.SERVER, "PiDome integrated 0.2 HTTP1.1");
        }

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpRequestHandler.getContentTypeHeader(fileType));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "http" + ((ssl == true) ? "s" : "") + "://" + plainIp + ((port != 80) ? ":" + port : ""));
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

        if (cache == true) {
            DateTime dt = new DateTime();
            HttpHeaderDateFormat dateFormat = HttpHeaderDateFormat.get();
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3153600");
            response.headers().set(HttpHeaderNames.EXPIRES, dateFormat.format(dt.plusMonths(12).toDate()));
        } else {
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache, must-revalidate");
            response.headers().set(HttpHeaderNames.EXPIRES, "Sat, 26 Jul 1997 05:00:00 GMT");
        }

        if (keepAlive) {
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            ctx.write(response);
        } else {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
