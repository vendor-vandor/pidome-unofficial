/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import static io.netty.handler.codec.http2.Http2SecurityUtil.CIPHERS;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import java.io.IOException;
import java.net.InetAddress;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.network.sockets.SocketBase;
import org.pidome.server.services.http.Webservice_renderer;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.config.SystemProperties;

/**
 * Runs an http server.
 * @author John
 */
public abstract class HttpServer extends SocketBase {
    
    /**
     * Class logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(HttpServer.class);
    
    /**
     * This is used until everything in the server is replaced.
     * @deprecated 
     */
    protected enum HTML {
        OLD,NEW
    }
    
    /**
     * Document root.
     */
    private static String documentTemplatesRoot;
    
    /**
     * The header file to include, if no header file present leave empty.
     */
    private static String documentTemplatesHeader = null;

    /**
     * the footer file to include, if no footer file is present leave empty.
     */
    private static String documentTemplatesFooter = null;
    
    /**
     * Document classes root.
     */
    private static final String documentClassesRoot    = "org.pidome.server.services.http.management.desktop.Webclient_";
    
    /**
     * A documentroot especially for xml api
     * @deprecated
     * @todo: Move to jsonrpc
     */
    private static final String xmlClassesRoot    = "org.pidome.server.services.http.management.xmlapi";
    
    /**
     * Construct an http server.
     * @param ip The ip address to bind to.
     * @param port the port to bind to.
     * @throws Exception 
     */
    public HttpServer(String ip, int port) throws Exception {
        this(InetAddress.getByName(ip), port);
    }
    
    /**
     * Construct an http server.
     * @param ip The InetAddres to biond the server to.
     * @param port The port to bind to.
     * @throws Exception 
     */
    public HttpServer(InetAddress ip, int port) throws Exception {
        super();
        LOG.info("Starting '{}'", getServiceName());
        documentTemplatesRoot = "resources/http/web/" + SystemConfig.getProperty("system", "userclients.interface").trim();
        try {
            SystemProperties properties = new SystemProperties("resources/http/web/config/",SystemConfig.getProperty("system", "userclients.interface").trim());
            documentTemplatesHeader = documentTemplatesRoot + "/" + properties.getProperty("header");
        } catch (IOException ex){
            LOG.error("No header file", ex);
        }
        try {
            SystemProperties properties = new SystemProperties("resources/http/web/config/",SystemConfig.getProperty("system", "userclients.interface").trim());
            documentTemplatesFooter = documentTemplatesRoot + "/" + properties.getProperty("footer");
        } catch (IOException ex){
            LOG.error("No footer file", ex);
        }
        Webservice_renderer.setConfig(documentTemplatesRoot);
        this.setIpAddress(ip);
        this.setPort(port);
    }
    
    /**
     * Returns the document template root.
     * @return The templates document root.
     */
    public static String getDocumentRoot(){
        return documentTemplatesRoot;
    }
    
    /**
     * Returns the http header.
     * This function may return null which means there is no header file
     * @return 
     */
    public static String getHttpHeader(){
        return documentTemplatesHeader;
    }
    
    /**
     * Returns the http footer.
     * This function may return null which means there is no heade file
     * @return 
     */
    public static String getHttpFooter(){
        return documentTemplatesFooter;
    }
    
    /**
     * Returns the document template root.
     * @return The templates document root.
     */
    public static String getXMLClassesRoot(){
        return xmlClassesRoot;
    }
    
    /**
     * Returns the classes root.
     * @return The document class root based on the type.
     */
    protected static String getDocumentClassRoot(){
        return documentClassesRoot;
    }
    
    /**
     * Start the http server.
     */
    @Override
    public void start() {
        getSocketServerBootstrapContext(1, 15)
                 .childHandler(new HttpServerInitializer())
                 .handler(new LoggingHandler(LogLevel.TRACE))
                 .bind(this.getIpAddress().getValue(), this.getPort().getValue());
        LOG.info("'{}' started", getServiceName());
    }

    /**
     * The http initializer including spdy.
     */
    @Sharable
    public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

        public HttpServerInitializer() {}

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipe = getSocketChannelPipeLine(ch, false);
            if(HttpServer.this.SSLAvailable()){
                try {
                    SSLEngine handler = HttpServer.this.getNettySSLContext()
                            .ciphers(CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                            .applicationProtocolConfig(
                                    new ApplicationProtocolConfig(
                                            Protocol.ALPN,
                                            SelectorFailureBehavior.NO_ADVERTISE,
                                            SelectedListenerFailureBehavior.ACCEPT,
                                            ApplicationProtocolNames.SPDY_3_1,
                                            ApplicationProtocolNames.HTTP_2,
                                            ApplicationProtocolNames.HTTP_1_1)
                            ).build().newEngine(ch.alloc());
                    pipe.addLast(new SslHandler(handler));
                    pipe.addLast(new HttpProtocolNegotiator(
                                        HttpServer.this.SSLAvailable(), 
                                        HttpServer.this.getPort().getValue()
                                     )
                                );
                } catch (ConfigPropertiesException | SSLException ex) {
                    LOG.error("Unable to enable SSL, stopping: {}", ex.getMessage(), ex);
                }
            } else {
                try {
                    HttpProtocolNegotiator.setHttp1PipeLine(pipe ,false, HttpServer.this.getPort().getValue());
                } catch (Exception ex) {
                    LOG.error("Could not configure single http 1.1: {}", ex.getMessage(), ex);
                }
            }
        }
    }
    
    /**
     * Returns the server name.
     * @return 
     */
    @Override
    public abstract String getServiceName();
    
}