/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author John
 */
public class Http1EntryPacketHandler extends HttpClientHandler {

    /**
     * If SSL or not.
     */
    private final boolean ssl;
    
    /**
     * The websocket handshaker component.
     */
    private WebSocketServerHandshaker handshaker;
    

    /**
     * Class logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Http1EntryPacketHandler.class);
    
    /**
     * Constructor for handling http 1.1 packages
     * @param ssl if ssl or not
     * @param port The port used to connect
     */
    public Http1EntryPacketHandler(boolean ssl, int port) {
        super(ssl, port);
        this.ssl = ssl;
    }

    /**
     * Reads incomming data via the channel.
     * @param ctx The channel handler context.
     * @param msg The message passe in.
     * @throws Exception 
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = ( FullHttpRequest)msg;
            if (!req.getDecoderResult().isSuccess()) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                req.release();
                return;
            }
            LOG.debug("HTTP1.1 request: {}", req.uri());
            if (req.uri().equals(HttpWebSocketHandler.WEBSOCKET_ADDRESS)) {
                // Handshake
                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(HttpWebSocketHandler.getWebSocketLocation(req, ssl), null, true);
                handshaker = wsFactory.newHandshaker(req);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    LOG.error("Requested invalid websocket version: {}", req.headers());
                } else {
                    handshaker.handshake(ctx.channel(), req);
                    LOG.debug("Done websocket handshake, result: {} ({}).", handshaker.version().toHttpHeaderValue(), handshaker.uri());
                }
            } else {
                super.channelRead(ctx, req);
            }
            req.release();
        } else if (msg instanceof WebSocketFrame) {
            LOG.debug("Websocket frame: {}", msg.getClass().getName());
            HttpWebSocketHandler.handleWebSocketFrame(handshaker, ctx, (WebSocketFrame) msg);
        }
    }
    
}
