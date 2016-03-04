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

package org.pidome.server.services.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import javax.security.auth.x500.X500Principal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.network.Network;

/**
 *
 * @author John
 */
public final class CertGen {
    
    public static boolean available = false;
    
    private KeyPair KPair;
    private String curHost;
    private KeyStore privateKS;
    
    static Logger LOG = LogManager.getLogger(CertGen.class);
    
    public CertGen() throws CertGenException {
        try {
            if(SystemConfig.getProperty("system", "server.enablessl").equals("true")){
                LOG.info("Generating certificate(s), please wait......");
                available = false;
                try {
                    curHost = Network.getIpAddressProperty().get().getHostName();
                    genKeyPair();
                    gen();
                } catch (UnknownHostException ex) {
                    throw new CertGenException("Could not determine host, no certificate creation done");
                } catch (NoSuchAlgorithmException ex) {
                    throw new CertGenException("Could not create key pair for certificate creation, wrong algorithm: " + ex.getMessage());
                } catch (NoSuchProviderException ex) {
                    throw new CertGenException("Could not create key pair for certificate creation, wrong provider: " + ex.getMessage());
                }
            }
        } catch (ConfigPropertiesException ex) {
            LOG.info("Secure SSL services are disabled. Set the 'server.enablessl' directie to true to enable this.");
        }
    }
    
    private void genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");  
        keyPairGenerator.initialize(1024);  
        KPair = keyPairGenerator.generateKeyPair();    
    }
    
    private void gen() throws CertGenException {
        try {
            File store = new File(SystemConfig.getProperty("system", "server.keystore"));
            store.getParentFile().mkdirs();
            if(store.exists()){
                store.delete();
            }
            X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
            certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            certGen.setSubjectDN(new X500Principal("CN=" + curHost));
            certGen.setIssuerDN(new X500Principal("CN=PiDome Server at " + curHost));
            certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
            certGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
            certGen.setPublicKey(KPair.getPublic());
            certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
            
            GeneralName altName = new GeneralName(GeneralName.iPAddress, Network.getIpAddressProperty().get().getHostAddress());
            GeneralNames subjectAltName = new GeneralNames(altName);
            certGen.addExtension(X509Extensions.SubjectAlternativeName, false, subjectAltName); 
            
            X509Certificate cert = certGen.generate(KPair.getPrivate(), "BC");
            
            /// Always, but always create a new keystore. a new keystore pass has is always created.
            privateKS = KeyStore.getInstance("JKS");  
            privateKS.load(null, null);
            
            String storePass = MessageDigest.getInstance("MD5").toString();
            
            privateKS.setKeyEntry("PiDome.Default", KPair.getPrivate(),  
                                  storePass.toCharArray(),  
                                  new java.security.cert.Certificate[]{cert});
            
            store.createNewFile();
            
            privateKS.store(new FileOutputStream(store), storePass.toCharArray());

            System.setProperty("javax.net.ssl.keyStore", SystemConfig.getProperty("system", "server.keystore"));
            System.setProperty("javax.net.ssl.keyStorePassword", storePass);
            
            available = true;
            
            LOG.info("Certificate(s) generated.");
            
        } catch (CertificateEncodingException ex) {
            throw new CertGenException("Could not encode certificate, can not continue: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new CertGenException("Illegal state, can not continue: " + ex.getMessage());
        } catch (NoSuchProviderException ex) {
            throw new CertGenException("No known provider, can not continue: " + ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            throw new CertGenException("No such algorithm, can not continue: " + ex.getMessage());
        } catch (SignatureException ex) {
            throw new CertGenException("Signature problem, can not continue: " + ex.getMessage());
        } catch (InvalidKeyException ex) {
            throw new CertGenException("Invalid key used, can not continue: " + ex.getMessage());
        } catch (KeyStoreException ex) {
            throw new CertGenException("KeyStore problem, can not continue: " + ex.getMessage());
        } catch (IOException ex) {
            throw new CertGenException("KeyStore can not be opened, can not continue: " + ex.getMessage());
        } catch (CertificateException ex) {
            throw new CertGenException("KeyStore certificate problem, can not continue: " + ex.getMessage());
        } catch (ConfigPropertiesException ex) {
            throw new CertGenException("KeyStore location configuration problem, can not continue: " + ex.getMessage());
        }
    }
}
