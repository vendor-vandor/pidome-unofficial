/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network.connectors;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javafx.scene.image.Image;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.network.CertHandler;
import org.pidome.client.system.network.CertHandlerException;

/**
 * Connects to http resources.
 * @author John Sirach
 */
public class HTTPConnector {

    public static final String GET = "GET";
    public static final String POST= "POST";
    
    String remoteIp;
    int remotePort;
    
    boolean secure;
    boolean ssl = false;
    
    String METHOD = GET;
    URL url;
    
    String postData;
    
    static Logger LOG = LogManager.getLogger(HTTPConnector.class);
    
    /**
     * Constructor.
     */
    public HTTPConnector(){
        ssl = CertHandler.available().get();
    }
    
    /**
     * Set's the initial server data.
     * @param ip
     * @param port 
     * @param ssl 
     */
    public final void setServerData(String ip, int port, boolean ssl){
        remoteIp = ip;
        remotePort = port;
        //if(ssl) this.ssl = CertHandler.available().get();
        this.ssl = ssl;
    }
    
    /**
     * Sets the url used for get or post.
     * @param urlWitFullPath
     * @throws MalformedURLException 
     */
    public final void setUrl(String urlWitFullPath) throws MalformedURLException {
        url = new URL("http"+((this.ssl)?"s":"")+"://"+remoteIp+":"+remotePort+urlWitFullPath);
    }
    
    /**
     * Sets the method to be used.
     * @param method 
     */
    public final void setHTTPMethod(String method){
        METHOD = method;
    }
    
    /**
     * Sets the post data.
     * @param data 
     */
    public final void setPostData(Map<String,String> data) {
        data.keySet().stream().forEach((key) -> {
            if(postData == null){
                postData = key+"="+data.get(key);
            } else {
                postData += "&"+key+"="+data.get(key);
            }
        });
        LOG.debug("Post data: {}", postData);
    }
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return
     * @throws IOException 
     */
    public final String getData() throws IOException {
        if(ssl) return getSSLData();
        LOG.debug("Retrieving: {} by method: {}", url.toString(), METHOD);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        uc.setUseCaches(false);
        uc.setDoInput (true);
        switch(METHOD){
            case GET:
                uc.setRequestMethod("GET");
                uc.connect();
            break;
            case POST:
                uc.setRequestMethod("POST");
                uc.setDoOutput (true);
                uc.connect();
                DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                postData = null;
            break;
        }
        byte[] data;
        int contentLength = uc.getContentLength();
        int offset;
        try (InputStream in = new BufferedInputStream(uc.getInputStream())) {
            data = new byte[contentLength];
            int bytesRead = 0;
            offset = 0;
            while (offset < contentLength) {
              bytesRead = in.read(data, offset, data.length - offset);
              if (bytesRead == -1)
                break;
              offset += bytesRead;
            }
        }
        if (offset != contentLength) {
          throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }
        return new String(data, 0, data.length, "UTF-8");
    }
    
    public final String getSSLData() throws IOException {
        try {
            
            LOG.debug("Retrieving: {} by method: {} (SSL)", url.toString(), METHOD);
            HttpsURLConnection uc = (HttpsURLConnection)url.openConnection();
            SSLSocketFactory sf = CertHandler.getContext().getSocketFactory();
            uc.setSSLSocketFactory(sf);
            uc.setDoInput (true);
            switch(METHOD){
                case GET:
                    uc.setRequestMethod("GET");
                    uc.connect();
                break;
                case POST:
                    uc.setRequestMethod("POST");
                    uc.setDoOutput (true);
                    uc.setRequestProperty("Content-Type", "application/json");
                    uc.setRequestProperty("Content-Length", String.valueOf(postData.getBytes("UTF-8").length));
                    uc.setRequestProperty("Connection","Close");
                    uc.connect();
                    DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                break;
            }
            InputStream in = new BufferedInputStream(uc.getInputStream());
            int contentLength = uc.getContentLength();
            byte[] data = new byte[contentLength];
            int offset = 0;
            int bytesRead = 0;
            while (offset < contentLength) {
                LOG.debug("Offset: {}, contentLength: {}", offset, contentLength);
                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1)
                    break;
                offset += bytesRead;
            }
            if (offset != contentLength) {
                throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
            }
            in.close();
            String returnData = new String(data, 0, data.length, "UTF-8");
            LOG.debug("having data: {}", returnData);
            return returnData;
        } catch (CertHandlerException ex) {
            LOG.error("Could not handle certificate: {}", ex.getMessage());
        }
        return "";
    }
    
    
    
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return
     * @throws IOException 
     */
    public final byte[] getBinaryData() throws IOException {
        if(ssl) return getBinarySSLData();
        LOG.debug("Retrieving: {} by method: {}", url.toString(), METHOD);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        uc.setUseCaches(false);
        uc.setDoInput (true);
        switch(METHOD){
            case GET:
                uc.setRequestMethod("GET");
                uc.connect();
            break;
            case POST:
                uc.setRequestMethod("POST");
                uc.setDoOutput (true);
                uc.connect();
                DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                postData = null;
            break;
        }
        byte[] data;
        int contentLength = uc.getContentLength();
        int offset;
        try (InputStream in = new DataInputStream(uc.getInputStream())) {
            data = new byte[contentLength];
            int bytesRead = 0;
            offset = 0;
            while (offset < contentLength) {
              bytesRead = in.read(data, offset, data.length - offset);
              if (bytesRead == -1)
                break;
              offset += bytesRead;
            }
        }
        if (offset != contentLength) {
          throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }
        return data;
    }
    
    public final byte[] getBinarySSLData() throws IOException {
        try {
            LOG.debug("Retrieving: {} by method: {} (SSL)", url.toString(), METHOD);
            HttpsURLConnection uc = (HttpsURLConnection)url.openConnection();
            SSLSocketFactory sf = CertHandler.getContext().getSocketFactory();
            uc.setSSLSocketFactory(sf);
            uc.setDoInput (true);
            switch(METHOD){
                case GET:
                    uc.setRequestMethod("GET");
                    uc.connect();
                break;
                case POST:
                    uc.setRequestMethod("POST");
                    uc.setDoOutput (true);
                    uc.setRequestProperty("Content-Type", "application/json");
                    uc.setRequestProperty("Content-Length", String.valueOf(postData.getBytes("UTF-8").length));
                    uc.setRequestProperty("Connection","Close");
                    uc.connect();
                    DataOutputStream wr = new DataOutputStream(uc.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                break;
            }
            InputStream in = new DataInputStream(uc.getInputStream());
            int contentLength = uc.getContentLength();
            byte[] data = new byte[contentLength];
            int offset = 0;
            int bytesRead = 0;
            while (offset < contentLength) {
                LOG.debug("Offset: {}, contentLength: {}", offset, contentLength);
                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1)
                    break;
                offset += bytesRead;
            }
            if (offset != contentLength) {
                throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
            }
            in.close();
            return data;
        } catch (CertHandlerException ex) {
            LOG.error("Could not handle certificate: {}", ex.getMessage());
        }
        throw new IOException("No data");
    }
    
    public final Image loadRemoteImage() throws IOException {
        return new Image(new ByteArrayInputStream(getBinaryData()));
    }
    
    
}
