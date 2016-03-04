package org.pidome.server;

/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URL;
import java.net.UnknownHostException;
import java.security.Policy;
import java.security.Security;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.pidome.misc.utils.PiDomeLocaleUtils;
import org.pidome.misc.utils.PidControl;

import org.pidome.server.services.ServiceController;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.connector.tools.Platforms;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.extras.SystemExtras;
import org.pidome.server.system.network.Network;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

public class PiDomeServerBoot {

    static Logger LOG = LogManager.getLogger(PiDomeServerBoot.class);
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            System.err.println ("Uncaught exception by " + t + ":");
            e.printStackTrace();
        });
        try {
            for (String s : args) {
                switch (s) {
                    case "debug":
                        SystemConfig.setLogLevel(Level.DEBUG);
                    break;
                    case "trace":
                        SystemConfig.setLogLevel(Level.TRACE);
                    break;
                    default:
                    break;
                }
            }
            System.out.println("Reading platform");
            Platforms.setPlatform();
            System.out.println("Start initialize");
            SystemConfig.initialize();
            if(SystemConfig.getProperty("system", "server.securitymanagerenabled").equals("true")){
                LOG.debug("Going to read policy");
                try {
                    URL policyURL = new URL("file:config/policies/pidome.policy");
                    System.setProperty("java.security.policy", policyURL.toString());
                    Policy.getPolicy().refresh();
                    System.setSecurityManager(new SecurityManager());
                    LOG.warn("Running server with security manager enabled.");
                } catch (Exception ex) {
                    throw new RuntimeException("Could not apply policies: " + ex.getMessage());
                }
                LOG.debug("Done reading policy");
            } else {
                LOG.warn("Running server with security manager disabled. This is the case as long the server is in alpha state.");
            }
            try {
                PiDomeLocaleUtils.setDefaultLocale();
            } catch (ConfigPropertiesException ex){
                LOG.warn("There is no default locale present in the config file, will default to: {}", Locale.getDefault());
            }
            LOG.info("Starting server with build: {} on platform: {} ({})", SystemConfig.getProperty("system", "server.build"), Platforms.getReportedOs(), Platforms.getReportedArch());
            PidControl.createPid();
            DB.init();
            Security.addProvider(new BouncyCastleProvider());
            new TimeUtils().startTimeThread();
            String runSet = null;
            runSet = System.getProperty("com.main.runset");
            if (runSet == null) {
                runSet = "DEFAULT";
            }
            switch (runSet) {
                case "install":
                    throw new ConfigException("Runtime install-system not available yet");
                //break;
                case "update":
                    throw new ConfigException("Runtime update not available yet, and should not start this server, it should start the update process in the shell script");
                //break;
                case "upgrade":
                    throw new ConfigException("Runtime upgrade not available yet, and should not start this server, it should start the upgrade process in the shell script");
                //break;
                default:
                    Network netIface = new Network();
                    PidomeJSONRPC.prepare();
                    SystemExtras systemExtras = new SystemExtras();
                    systemExtras.initializeExtras();
                    ServiceController.initialize();
                    try {
                        netIface.startRoutines();
                    } catch (UnknownHostException e) {
                        LOG.error("Could not start network routines: ", e.getMessage(), e);
                        throw new ConfigException(e.getMessage());
                    }
                    ServiceController.startAllServers();
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            LOG.info("Server shutdown instantiated");
                            ServiceController.stopAllServers();
                            DB.releaseAll();
                            PidControl.shutDown();
                            LOG.info("Goodbye, see you soon");
                        }
                    });
                    break;
            }
            System.gc();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

}
