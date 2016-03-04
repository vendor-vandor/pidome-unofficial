/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.tools.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connects to http resources.
 * @author John Sirach
 */
public class HTTPConnector {

    public static final String GET = "GET";
    public static final String POST= "POST";
    public static final String PUT = "PUT";
    
    String remoteIp;
    
    boolean secure;
    boolean ssl = false;
    
    String METHOD = GET;
    URL url;
    
    String postData;
    
    static Logger LOG = LogManager.getLogger(HTTPConnector.class);
    
    int timeout = 3000;
    
    Map<String,String> headerCollection = new HashMap<>();
    
    /**
     * Constructor.
     * @param urlWitFullPath
     * @param port
     * @param ssl
     * @throws java.net.MalformedURLException
     */
    public HTTPConnector(String urlWitFullPath, boolean ssl) throws MalformedURLException {
        url = new URL(urlWitFullPath);
        this.ssl = ssl;
    }
    
    /**
     * Sets a timeout. Default is 3 seconds.
     * @param timeout 
     */
    public final void setRequestTimeout(int timeout){
        this.timeout = timeout;
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
     * Non markup post data.
     * @param data 
     */
    public final void setPlainPostData(String data){
        postData = data;
    }
    
    /**
     * Add an http request header.
     * @param name
     * @param value 
     */
    public final void addHeader(String name, String value){
        headerCollection.put(name, value);
    }
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return
     * @throws IOException 
     */
    public final String getData() throws IOException {
        LOG.debug("Retrieving: {} by method: {}", url.toString(), METHOD);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        for(Map.Entry<String,String> header: headerCollection.entrySet()){
            uc.addRequestProperty(header.getKey(), header.getValue());
        }
        uc.setUseCaches(false);
        uc.setDoInput (true);
        uc.setConnectTimeout(timeout);
        uc.setReadTimeout(timeout);
        switch(METHOD){
            case GET:
                uc.setRequestMethod("GET");
                uc.connect();
            break;
            case PUT:
            case POST:
                uc.setRequestMethod(METHOD);
                uc.setDoOutput (true);
                uc.connect();
                try(DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                } catch (Exception ex){
                    LOG.error("Could not send post data: {}", ex.getMessage());
                }
                postData = null;
            break;
        }
        byte[] data;
        int contentLength = uc.getContentLength();
        if(contentLength!=-1){
            int offset = -1;
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
        } else {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                try (InputStream in = new BufferedInputStream(uc.getInputStream())) {
                    for(int byteRead; (byteRead = in.read()) !=-1;){
                        bos.write(byteRead);
                    }
                }
                data = bos.toByteArray();
            }
        }
        uc.disconnect();
        return new String(data, 0, data.length, "UTF-8");
    }
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return
     * @throws IOException 
     */
    public final byte[] getBinaryData() throws IOException {
        LOG.debug("Retrieving: {} by method: {}", url.toString(), METHOD);
        HttpURLConnection uc = (HttpURLConnection)url.openConnection();
        for(Map.Entry<String,String> header: headerCollection.entrySet()){
            uc.addRequestProperty(header.getKey(), header.getValue());
        }
        uc.setUseCaches(false);
        uc.setDoInput (true);
        uc.setConnectTimeout(timeout);
        uc.setReadTimeout(timeout);
        switch(METHOD){
            case GET:
                uc.setRequestMethod("GET");
                uc.connect();
            break;
            case PUT:
            case POST:
                uc.setRequestMethod(METHOD);
                uc.setDoOutput (true);
                uc.connect();
                try(DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                } catch (Exception ex){
                    LOG.error("Could not send post data: {}", ex.getMessage());
                }
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
        uc.disconnect();
        return data;
    }
    
}