/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.networking.connections.server.http.HTTPConnector;

/**
 *
 * @author John
 */
public class SimpleHttpDataConnector extends HTTPConnector {

    static {
        Logger.getLogger(SimpleHttpDataConnector.class.getName()).setLevel(Level.ALL);
    }
    
    String ip;
    
    /**
     * Constructor.
     * @param ip remote host ip.
     * @param port remote host port.
     * @param endPoint the url path to the json connector.
     * @param ssl if secure set true.
     * @throws MalformedURLException When the generated url fails.
     */
    public SimpleHttpDataConnector(String ip, int port, String endPoint, boolean ssl) throws MalformedURLException {
        this.setServerData(ip, port, ssl);
        this.setUrl(endPoint);
    }

    /**
     * Returns data via http.
     * @param url The full url from server root.
     * @param params HTTP Get parameters.
     * @return String.
     * @throws java.io.IOException When data can not be fetched.
     */
    public String getHttpData(String url, Map<String, String> params) throws IOException {
        boolean first = true;
        StringBuilder urlData = new StringBuilder(url);
        for(Map.Entry<String,String> postset: params.entrySet()){
            if(first == true){
                urlData.append("?").append(postset.getKey()).append("=").append(postset.getValue());
                first = false;
            } else {
                urlData.append("&").append(postset.getKey()).append("=").append(postset.getValue());
            }
        }
        Logger.getLogger(SimpleHttpDataConnector.class.getName()).log(Level.FINE, "Executing SimpleHttpDataConnector url: {0}", urlData.toString());
        this.setUrl(urlData.toString());
        String returnData = this.getData();
        Logger.getLogger(SimpleHttpDataConnector.class.getName()).log(Level.FINE, "Data received from simple http connector: {0}", returnData);
        return returnData;
    }
    
}
