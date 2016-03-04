/*
 * Copyright 2014 John.
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

package org.pidome.pcl.networking;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;

/**
 * Certificate handler.
 * @author John
 */
public class CertHandler {
    
    static {
        Logger.getLogger(CertHandler.class.getName()).setLevel(Level.ALL);
    }
    
    private final static BooleanPropertyBindingBean available = new BooleanPropertyBindingBean(false);
    
    /**
     * Nasty.
     */
    private static TrustManager[] trustAllCerts;
    /**
     * SSL context.
     */
    private static SSLContext sc;
    
    public static void init() throws CertHandlerException {
        if(trustAllCerts==null){
            try {
                trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        @Override
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        @Override
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };
                sc = SSLContext.getInstance("TLSv1.2");
                
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                
                KeyStore privateKS = KeyStore.getInstance("JKS");  
                privateKS.load(null, null);
                
                kmf.init(privateKS, "justsomethingtotest".toCharArray());
                
                sc.init(kmf.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                available.setValue(true);
                
            } catch (NoSuchAlgorithmException | KeyManagementException | IOException | CertificateException | KeyStoreException | UnrecoverableKeyException ex) {
                throw new CertHandlerException("SSL/TLS not available: " + ex.getMessage());
            }
        }
    }
    
    public static SSLContext getContext() throws CertHandlerException {
        if(!available.getValue()){
            throw new CertHandlerException("SSL/TLS not available");
        } else {
            return sc;
        }
    }
    
    public static ReadOnlyBooleanPropertyBindingBean available(){
        return available.getReadOnlyBooleanPropertyBindingBean();
    }
    
}
