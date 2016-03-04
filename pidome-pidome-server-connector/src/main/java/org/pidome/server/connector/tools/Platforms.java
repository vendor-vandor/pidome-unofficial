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

package org.pidome.server.connector.tools;

import java.awt.GraphicsEnvironment;

/**
 *
 * @author John Sirach
 */
public final class Platforms {

    public static final String OS_WINDOWS = "OS_WINDOWS";
    public static final String OS_LINUX   = "OS_LINUX";
    
    public static final String ARCH_64    = "amd64";
    public static final String ARCH_86    = "x86";
    public static final String ARCH_ARM   = "arm";
    
    static String OS            = "UNKNOWN";
    static String ARCH          = "UNKNOWN";
    
    static String REPORTED_OS   = "UNKNOWN";
    static String REPORTED_ARCH = "UNKNOWN";
    
    static Boolean ISHEADLESS = false;
    
    public static void setPlatform() throws PlatformException {
        setReportedOs();
        setReportedArch();
        if(REPORTED_OS.startsWith("Windows")){
            OS = OS_WINDOWS;
            System.setProperty("com.resource.dir", "resources");
        } else if(REPORTED_OS.startsWith("Linux")){
            OS = OS_LINUX;
        } else {
            throw new PlatformException("Your OS '"+REPORTED_OS+"' is unsupported.");
        }
        switch (REPORTED_ARCH) {
            case ARCH_64:
                ARCH = ARCH_64;
                break;
            case ARCH_86:
                ARCH = ARCH_86;
                break;
            case ARCH_ARM:
                ARCH = ARCH_ARM;
                break;
            default:
                throw new PlatformException("Your architecture '"+REPORTED_ARCH+"' on '"+REPORTED_OS+"' is unsupported.");
        }
        if (GraphicsEnvironment.isHeadless()) {
            ISHEADLESS = true;
        } else {
            ISHEADLESS = false;
        }
    }
    
    static void setReportedOs(){
        REPORTED_OS = System.getProperty("os.name");
    }

    static void setReportedArch(){
        REPORTED_ARCH = System.getProperty("os.arch");
    }
    
    public static String getReportedArch(){
        return REPORTED_ARCH;
    }
    
    public static String getReportedOs(){
        return REPORTED_OS;
    }
    
    public static Boolean isHeadLess(){
        return ISHEADLESS;
    }
    
    public static String isOs(){
        return OS;
    }
    
    public static String isArch(){
        return ARCH;
    }
    
}
