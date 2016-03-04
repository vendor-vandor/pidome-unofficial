/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.codec.spdy.SpdyFrameCodec;
import io.netty.handler.codec.spdy.SpdyHttpDecoder;
import io.netty.handler.codec.spdy.SpdyHttpEncoder;
import io.netty.handler.codec.spdy.SpdyHttpResponseStreamIdHandler;
import io.netty.handler.codec.spdy.SpdySessionHandler;
import io.netty.handler.codec.spdy.SpdyVersion;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class HttpProtocolNegotiator extends ApplicationProtocolNegotiationHandler {

    /**
     * Negotiator logger.
     */
    static Logger LOG = LogManager.getLogger(HttpProtocolNegotiator.class);
    
    /**
     * If SSL or not, only used with http not spdy
     */
    private final boolean ssl;
    /**
     * The port the services run on.
     */
    private final int port;
    
    /**
     * Upgrade facory to go from http1.1 to http2
     */
    private static final UpgradeCodecFactory upgradeCodecFactory = (CharSequence protocol) -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            return new Http2ServerUpgradeCodec(new HttpToHttp2ConnectionHandlerBuilder().build());
        } else {
            return null;
        }
    };
    
    /**
     * Negotiator constructow.
     * @param ssl Indicator for client handlers if SSL is used.
     * @param port The port used.
     */
    protected HttpProtocolNegotiator(boolean ssl, int port) {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.ssl = ssl;
        this.port = port;
    }

    /**
     * Configures the HTTP pipeline.
     * @param ctx Channel handler context.
     * @param protocol Protocol initiated. either http 1.1 or spdy (http 2)
     * @throws Exception 
     */
    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        LOG.debug("Got protocol: {}", protocol);
        if (ApplicationProtocolNames.SPDY_3_1.equals(protocol)) {
            configureSpdy(ctx, SpdyVersion.SPDY_3_1, port);
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            configureHttp1(ctx, ssl, port);
            return;
        }
        
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            configureHttp2(ctx, port);
            return;
        }
        
        LOG.warn("unknown protocol: " + protocol);
    }

    /**
     * Configures the channel to use spdy.
     * @param ctx The channel handler.
     * @param version The spdy version.
     * @param port The port used.
     * @throws Exception When the channel can not be configured.
     */
    private static void configureSpdy(ChannelHandlerContext ctx, SpdyVersion version, int port) throws Exception {
        ctx.pipeline().addLast(new SpdyFrameCodec(version))
                      .addLast(new SpdySessionHandler(version, true))
                      .addLast(new SpdyHttpEncoder(version))
                      .addLast(new SpdyHttpDecoder(version, 5242880))
                      .addLast(new SpdyHttpResponseStreamIdHandler())
                      .addLast(new HttpClientHandler(true, port))
                      .addLast(new UserEventLogger());
    }

    /**
     * Configures an http2 channel.
     * @param ctx 
     */
    private static void configureHttp2(ChannelHandlerContext ctx, int port) {
        DefaultHttp2Connection connection = new DefaultHttp2Connection(true);
        InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
                .propagateSettings(true).validateHttpHeaders(false)
                .maxContentLength(5242880).build();

        ctx.pipeline().addLast(new HttpToHttp2ConnectionHandlerBuilder()
                .frameListener(listener)
                .connection(connection).build());

        ctx.pipeline().addLast(new Http2ClientHandler(port));
    }
    
    /**
     * Configures the channel to use http.
     * This defaults to http 1.1
     * @param ctx The channel context
     * @param ssl if the channel should be used with ssl enabled.
     * @param port The port the channel lives on.
     * @throws Exception When the http channel can not be configured.
     */
    protected static void configureHttp1(ChannelHandlerContext ctx, boolean ssl, int port) throws Exception {
        setHttp1PipeLine(ctx.pipeline(), ssl, port);
    }
    
    /**
     * Set's the http 1 pipeline.
     * @param pipe The pipeline to configure.
     * @param ssl SSL enabled or not.
     * @param port The port in use.
     */
    protected static final void setHttp1PipeLine(ChannelPipeline pipe, boolean ssl, int port){
        final HttpServerCodec sourceCodec = new HttpServerCodec();
        pipe.addLast(sourceCodec);
        if(ssl){
            pipe.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory));
        }
        //pipe.addLast("encoder", new HttpResponseEncoder())
        //    .addLast("decoder", new HttpRequestDecoder())
        pipe.addLast("aggregator", new HttpObjectAggregator(5242880))
        //    .addLast(new HttpContentCompressor())
            .addLast("handler", new Http1EntryPacketHandler(ssl, port))
            ///.addLast(new WebSocketServerProtocolHandler(HttpWebsocketHelper.WEBSOCKET_ADDRESS))
            .addLast(new UserEventLogger());
    }
    
    /**
     * Class that logs any User Events triggered on this channel.
     */
    private static class UserEventLogger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            LOG.debug("User Event Triggered: " + evt);
            ctx.fireUserEventTriggered(evt);
        }
    }
    
}