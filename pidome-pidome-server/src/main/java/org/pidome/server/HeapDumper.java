/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;

/**
 *
 * @author John
 */
public class HeapDumper {

    // This is the name of the HotSpot Diagnostic MBean

    private static final String HOTSPOT_BEAN_NAME
            = "com.sun.management:type=HotSpotDiagnostic";

    // field to store the hotspot diagnostic MBean 
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    ///dumpHeap(fileName, live);
    /**
     * Dump the heap!
     *
     * @param fileName
     * @param live
     */
    public static void dumpHeap(String fileName, boolean live) {
        /// Terrible, i know, but clearing stuff up would be nice before making a dump.
        System.gc();
        System.runFinalization();
        // initialize hotspot diagnostic MBean
        initHotspotMBean();
        try {
            hotspotMBean.dumpHeap(fileName, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    // initialize the hotspot diagnostic MBean field
    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized (HeapDumper.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    // get the hotspot diagnostic MBean from the
    // platform MBean server
    private static HotSpotDiagnosticMXBean getHotspotMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean bean
                    = ManagementFactory.newPlatformMXBeanProxy(server,
                            HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

}
