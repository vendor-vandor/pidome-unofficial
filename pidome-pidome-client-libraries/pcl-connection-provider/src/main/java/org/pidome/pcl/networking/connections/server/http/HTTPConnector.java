/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.networking.connections.server.http;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.pidome.pcl.networking.CertHandler;
import org.pidome.pcl.networking.CertHandlerException;

/**
 * Connects to http resources.
 * @author John Sirach
 */
public class HTTPConnector {

    static {
        Logger.getLogger(HTTPConnector.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * http methods.
     */
    public enum Method {
        /**
         * Use GET.
         */
        GET,
        /**
         * Use post.
         */
        POST;
    }
    
    private String remoteIp;
    private int remotePort;
    
    private boolean ssl = false;
    
    Method METHOD = Method.GET;
    private URL url;
    
    private String postData;
    
    /**
     * Constructor.
     */
    public HTTPConnector(){
        ssl = CertHandler.available().get();
    }
    
    /**
     * Set's the initial server data.
     * @param ip server remote ip.
     * @param port server remote port.
     * @param ssl true if the remote port is an ssl port.
     */
    public final void setServerData(String ip, int port, boolean ssl){
        remoteIp = ip;
        remotePort = port;
        //if(ssl) this.ssl = CertHandler.available().get();
        this.ssl = ssl;
    }
    
    /**
     * Sets the url used for get or post.
     * @param urlWitFullPath The url to reach from trailing slash (/) to the file requested.
     * @throws MalformedURLException When the url created ain't correct.
     */
    public final void setUrl(String urlWitFullPath) throws MalformedURLException {
        url = new URL("http"+((this.ssl)?"s":"")+"://"+remoteIp+":"+remotePort+urlWitFullPath);
    }
    
    /**
     * Sets the method to be used.
     * @param method Set POST or GET method.
     */
    public final void setHTTPMethod(Method method){
        METHOD = method;
    }
    
    /**
     * Sets the post data.
     * @param data Map with key and values as string to be used as post data.
     */
    public final void setPostData(Map<String,String> data) {
        for(Map.Entry<String,String> postset: data.entrySet()){
            if(postData == null){
                postData = postset.getKey()+"="+postset.getValue();
            } else {
                postData += "&"+postset.getKey()+"="+postset.getValue();
            }
        }
    }
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return Returns the requested data.
     * @throws IOException When the remote host is unreachable or a timeout is reached.
     */
    public final String getData() throws IOException {
        if(ssl) return getSSLData();
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
                try (DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                } catch (Exception ex){
                    Logger.getLogger(HTTPConnector.class.getName()).log(Level.FINE, "Data posting failed: {0}", ex.getMessage());
                }
                postData = null;
            break;
        }
        byte[] data;
        int contentLength = uc.getContentLength();
        int offset;
        try (InputStream input = uc.getInputStream();
             InputStream in = new BufferedInputStream(input)) {
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
        return new String(data, 0, data.length, "UTF-8");
    }
    
    /**
     * Returns data as getData() using ssl.
     * @return Returns the data of the requested url.
     * @throws IOException When the remote host is unreachable or a timeout is reached.
     */
    public final String getSSLData() throws IOException {
        try {
            SSLSocketFactory sf = CertHandler.getContext().getSocketFactory();
            HttpsURLConnection uc = (HttpsURLConnection)url.openConnection();
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
                    try (DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                        wr.writeBytes(postData);
                        wr.flush();
                        wr.close();
                    } catch (Exception ex){
                        Logger.getLogger(HTTPConnector.class.getName()).log(Level.FINE, "Data posting failed: {0}", ex.getMessage());
                    }
                break;
            }
            byte[] data;
            try (InputStream input = uc.getInputStream(); InputStream in = new BufferedInputStream(input)) {
                int contentLength = uc.getContentLength();
                data = new byte[contentLength];
                int offset = 0;
                int bytesRead = 0;
                while (offset < contentLength) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1)
                        break;
                    offset += bytesRead;
                }   if (offset != contentLength) {
                    throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
                }
            }
            String returnData = new String(data, 0, data.length, "UTF-8");
            uc.disconnect();
            return returnData;
        } catch (CertHandlerException ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Returns data via the method set in setHTTPMethod.
     * @return Binary data from a remote host.
     * @throws IOException When remote host is unreachable or a timeout is received.
     */
    public final byte[] getBinaryData() throws IOException {
        if(ssl) return getBinarySSLData();
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
                try (DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                } catch (Exception ex){
                    Logger.getLogger(HTTPConnector.class.getName()).log(Level.FINE, "Data posting failed: {0}", ex.getMessage());
                }
                postData = null;
            break;
        }
        byte[] data;
        int contentLength = uc.getContentLength();
        int offset;
        try (InputStream input = uc.getInputStream(); InputStream in = new BufferedInputStream(input)) {
            data = new byte[contentLength];
            int bytesRead = 0;
            offset = 0;
            while (offset < contentLength) {
              bytesRead = in.read(data, offset, data.length - offset);
              if (bytesRead == -1)
                break;
              offset += bytesRead;
            }
            in.close();
        }
        if (offset != contentLength) {
          throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }
        uc.disconnect();
        return data;
    }
    
    /**
     * Returns data as getBinaryData() utilizing ssl.
     * @return Returns remote file data.
     * @throws IOException When unreachable or timeout is reached.
     */
    public final byte[] getBinarySSLData() throws IOException {
        try {
            SSLSocketFactory sf = CertHandler.getContext().getSocketFactory();
            HttpsURLConnection uc = (HttpsURLConnection)url.openConnection();
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
                    uc.setRequestProperty("Content-Length", String.valueOf(postData.getBytes("UTF-8").length));
                    uc.setRequestProperty("Connection","Close");
                    uc.connect();
                    try (DataOutputStream wr = new DataOutputStream(uc.getOutputStream())){
                        wr.writeBytes(postData);
                        wr.flush();
                        wr.close();
                    } catch (Exception ex){
                        Logger.getLogger(HTTPConnector.class.getName()).log(Level.FINE, "Data posting failed: {0}", ex.getMessage());
                    }
                break;
            }
            byte[] data;
            try (InputStream input = uc.getInputStream(); InputStream in = new BufferedInputStream(input)) {
                int contentLength = uc.getContentLength();
                data = new byte[contentLength];
                int offset = 0;
                int bytesRead = 0;
                while (offset < contentLength) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1)
                        break;
                    offset += bytesRead;
                }   if (offset != contentLength) {
                    throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
                }
            }
            uc.disconnect();
            return data;
        } catch (CertHandlerException ex) {
            throw new IOException(ex);
        }
    }
    
}
