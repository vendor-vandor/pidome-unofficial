/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.sockets;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.services.provider.CertGen;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 * Base socket implementation for all services.
 * SSL is based on TLS.
 * @author John
 */
public abstract class SocketBase extends AbstractSocketService {
    
    /**
     * Class logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SocketBase.class);
    
    /**
     * A secure socket or not.
     */
    private boolean isSecure;
    
    /**
     * The server channel.
     */
    private Channel channel;
    
    /**
     * Try to share a single group.
     */
    private static NioEventLoopGroup workersGroup;
    
    /**
     * Construct a base socket setup.
     * @throws org.pidome.server.system.network.sockets.SocketBaseException When secure is enabled but no certificate is available.
     */
    public SocketBase() throws SocketBaseException {
        try {
            isSecure = Optional.of(Boolean.parseBoolean(SystemConfig.getProperty("system", "server.enablessl"))).get();
            if (isSecure) {
                if(!CertGen.available){
                    throw new SocketBaseException("SSL configured but not available, SSL http wil not start");
                }
            }
        } catch (ConfigPropertiesException ex) {
            LOG.warn("Unable to determine if SSL is enabled, starting insecure");
            isSecure = false;
        }
    }
    
    /**
     * Main parent group.
     */
    private EventLoopGroup bossGroup;
    /**
     * Worker child groups.
     */
    private EventLoopGroup workerGroup;
    
    /**
     * Returns a default with minimal requirements set server bootstrap.
     * @param parents The parent worker controller amount.
     * @param childs The amount of childs per parent worker.
     * @return Server Bootstrap with a socket service channel.
     */
    public final ServerBootstrap getSocketServerBootstrapContext(int parents, int childs){
        if(workersGroup == null){
            workersGroup = new NioEventLoopGroup();
        }
        return new ServerBootstrap().group(workersGroup)
                                    .childOption(ChannelOption.ALLOCATOR, 
                                                 PooledByteBufAllocator.DEFAULT)
                                    .channel(NioServerSocketChannel.class)
                                    .handler(new LoggingHandler(LogLevel.DEBUG));
    }
    
    /**
     * Retutrns true if SSL is available.
     * @return true when SSL ius available.
     */
    public final boolean SSLAvailable(){
        return (isSecure && CertGen.available);
    }
    
    /**
     * Returns a default pipeline setup
     * Use this pipeline for example in an ChannelInitializer&gt;SocketChannel&lt; initializer.
     * if SSL is available this implementation will return a secure socket.
     * 
     * @param ch A socketServer channel.
     * @return a Channel pipeline.
     */
    public ChannelPipeline getSocketChannelPipeLine(Channel ch){
        return getSocketChannelPipeLine(ch, true);
    }
    
    /**
     * Returns a default pipeline setup
     * Use this pipeline for example in an ChannelInitializer&gt;SocketChannel&lt; initializer
     * @param ch A socketServer channel.
     * @param defaultSSl When set to false and SSL is available, you must implement the SSL engine.
     * @return a Channel pipeline.
     */
    public ChannelPipeline getSocketChannelPipeLine(Channel ch, boolean defaultSSl){
        ChannelPipeline p = ch.pipeline();
        if(defaultSSl && SSLAvailable()){
            try {
                SslHandler sslEngine = getNettySSLContext().build().newHandler(ch.alloc());
                p.addLast(sslEngine);
            } catch (ConfigPropertiesException ex) {
                LOG.error("Configuration problem: {}", ex.getMessage(), ex);
            } catch (SSLException ex) {
                LOG.error("SSL problem: {}", ex.getMessage(), ex);
            }
        }
        return p;
    }
    
    /**
     * Returns a Netty compatible SSL Context
     * @return SSL context
     * @throws ConfigPropertiesException When SSL is not available.
     */
    protected SslContextBuilder getNettySSLContext() throws ConfigPropertiesException {
        if(CertGen.available){
            try {

                KeyStore ks = KeyStore.getInstance("JKS");
                File kf = new File(SystemConfig.getProperty("system", "server.keystore"));
                ks.load(new FileInputStream(kf), System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                return SslContextBuilder.forServer(kmf).trustManager(tmf);

            } catch (KeyStoreException | ConfigPropertiesException | NoSuchAlgorithmException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
                throw new ConfigPropertiesException (ex);
            }
        } else {
            throw new ConfigPropertiesException("SSL configured but not available, SSL http wil not start");
        }
    }
    
    /**
     * Are we living?
     * @return 
     */
    @Override
    public boolean isAlive() {
        return (bossGroup!=null && workerGroup!= null) && (!bossGroup.isTerminated() && !workerGroup.isTerminated());
    }
    
    /**
     * Gracefull shutdown.
     */
    protected final void shutdownGracefully(){
        if(bossGroup!=null){
            bossGroup.shutdownGracefully();
        }
        if(workerGroup!=null){
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * Stops the sockets.
     */
    @Override
    public final void interrupt() {
        this.shutdown();
    }
    
    /**
     * Shuts down the sockets.
     */
    protected final void shutdown() {
        if (channel != null) {
            try {
                channel.flush();
                channel.close();
                channel = null;
            } catch (Exception e) {
                LOG.warn("Faulty shutdown, in case of restart restart the server.");
            }
        }
        this.shutdownGracefully();
    }
    
}