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
public class BinaryHttpDataConnector extends HTTPConnector {

    static {
        Logger.getLogger(BinaryHttpDataConnector.class.getName()).setLevel(Level.ALL);
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
    public BinaryHttpDataConnector(String ip, int port, String endPoint, boolean ssl) throws MalformedURLException {
        this.setServerData(ip, port, ssl);
        this.setUrl(endPoint);
    }

    /**
     * Returns binary data via http.
     * It is your own responsibility to identify the data.
     * @param url The full url to get the data from.
     * @param params HTTP Get parameters.
     * @return byte[]
     * @throws java.io.IOException When data can not be fetched.
     */
    public byte[] getHttpBinaryData(String url, Map<String, String> params) throws IOException {
        boolean first = true;
        StringBuilder urlData = new StringBuilder(url);
        if(params !=null){
            for(Map.Entry<String,String> postset: params.entrySet()){
                if(first == true){
                    urlData.append("?").append(postset.getKey()).append("=").append(postset.getValue());
                    first = false;
                } else {
                    urlData.append("&").append(postset.getKey()).append("=").append(postset.getValue());
                }
            }
        }
        Logger.getLogger(BinaryHttpDataConnector.class.getName()).log(Level.FINE, "Executing getHttpBinaryData url: {0}", urlData.toString());
        this.setUrl(urlData.toString());
        byte[] returnData = this.getBinaryData();
        Logger.getLogger(BinaryHttpDataConnector.class.getName()).log(Level.FINE, "Data received from simple http connector: {0}", returnData.length);
        return returnData;
    }
    
}