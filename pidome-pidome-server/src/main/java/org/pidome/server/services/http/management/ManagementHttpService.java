/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.management;

import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.network.Network;
import org.pidome.server.system.network.http.HttpServer;

/**
 *
 * @author John
 */
public final class ManagementHttpService extends HttpServer {
    
    /**
     * Constructs the management http server.
     * @throws Exception 
     */
    public ManagementHttpService() throws Exception {
        super((!SystemConfig.getProperty("system", "userclients.ip").equals("network.autodiscovery")?
                SystemConfig.getProperty("system", "userclients.ip"):
                Network.getIpAddressProperty().get().getHostAddress()),
                Integer.parseInt(SystemConfig.getProperty("system", "userclients.port"))
             );
    }
    
    /**
     * Returns the server name.
     * @return 
     */
    @Override
    public String getServiceName() {
        return "HTTP" + ((this.SSLAvailable())?"S":"") + " Management Webservice";
    }
    
    
}
