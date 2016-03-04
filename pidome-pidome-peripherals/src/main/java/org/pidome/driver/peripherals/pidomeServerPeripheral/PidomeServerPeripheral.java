/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.peripherals.pidomeServerPeripheral;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;


/**
 * This driver is only here for consistency purposes. 
 * Later on this will be handling the real device data to dispatch
 * @author John
 */
public class PidomeServerPeripheral extends PeripheralHardwareDriver implements PeripheralHardwareDriverInterface {

    ScheduledExecutorService scheduledServiceExecutor;
    DecimalFormat df = new DecimalFormat("#.##");
    
    OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    
    static Logger LOG = LogManager.getLogger(PidomeServerPeripheral.class);
    
    /**
     * Uptime parser
     */
    private static final Pattern uptimePattern = Pattern.compile("^([\\d\\.]+)\\s+([\\d\\.]+)$");
    
    public PidomeServerPeripheral()  throws PeripheralHardwareException {
        prepare();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
    }
    
    @Override
    public void initDriver() throws PeripheralHardwareException {
        /// We do not need to init, our default driver is the server driver, see: getSoftwareId;
    }
    
    @Override
    public PeripheralVersion getSoftwareId() throws PeripheralHardwareException {
        createPeripheralVersion("NATIVE_PIDOMESERVERDEVICE_DRIVER_0.0.1");
        return getVersion();
    }
    
    @Override
    public void startDriver() throws PeripheralHardwareException {
        scheduledServiceExecutor = Executors.newScheduledThreadPool(1);
        Runnable runnable = getServerChecksRunnable ();
        scheduledServiceExecutor.schedule(runnable, 10, TimeUnit.SECONDS);
        scheduledServiceExecutor.scheduleWithFixedDelay(runnable, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Server check executable.
     * @return 
     */
    private Runnable getServerChecksRunnable (){
        return () -> {
            StringBuilder dataBuilder = new StringBuilder();
                try {
                    //Build command for CPU temp
                    String[] cmdString = {"/opt/vc/bin/vcgencmd", "measure_temp"};
                    Runtime rt = Runtime.getRuntime();
                    Process pr;
                    try {
                        pr = rt.exec(cmdString);
                        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line=null;
                        while((line=input.readLine()) != null) {
                            String data = line.trim().replace("\n", "").replace("temp=", "").replace("'C","");
                            dataBuilder.append("procheat").append(":").append(data).append(";");
                        }
                        pr.waitFor();
                    } catch (IOException ex) {
                        /// not supported on windows
                    } catch (InterruptedException ex) {
                        LOG.error("Could not get temperature: {}", ex.getMessage());
                    }
                    
                    //Build command for uptime
                    String[] uptimeString = {"/bin/cat","/proc/uptime"};
                    Process prExecResult;
                    try {
                        prExecResult = rt.exec(uptimeString);
                        try (BufferedReader input = new BufferedReader(new InputStreamReader(prExecResult.getInputStream()))){
                            String line=null;
                            while((line=input.readLine()) != null) {
                                String data = line.trim();
                                Matcher m = uptimePattern.matcher(data);
                                if (m.matches()) {
                                    dataBuilder.append("uptime").append(":").append(m.group(1)).append(";");
                                }
                            }
                        }
                        prExecResult.waitFor();
                    } catch (IOException ex) {
                        LOG.error("Nocando uptime: {}", ex.getMessage(), ex);
                    } catch (InterruptedException ex) {
                        LOG.error("Could not get uptime: {}", ex.getMessage());
                    }
                    dataBuilder.append("cpuusage").append(":").append(df.format(operatingSystemMXBean.getSystemCpuLoad()*100.0)).append(";");
                    
                    //// Saving this one for future multiple plots on single graph Double MEMUsage = Double.parseDouble(df.format(operatingSystemMXBean.getCommittedVirtualMemorySize()/1048576.0));
                    dataBuilder.append("memusage").append(":").append(df.format(((rt.totalMemory() - rt.freeMemory()) / 1048576.0))).append(";");
                    
                    dataBuilder.append("diskspace").append(":").append(df.format(new File("/").getUsableSpace()/1048576.0)).append(";");
                    
                    dispatchData(dataBuilder.toString(), dataBuilder.toString());
                    
                } catch (Exception ex){
                    LOG.error("An error occured during server data retrieval: {}", ex.getMessage(), ex);
                }
            };
    }
    
    final void dispatchData(String control, String data){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1);
            DataOutputStream dos = new DataOutputStream(bos);
            dos.write(1);
            dispatchData(PeripheralHardwareDataEvent.DATA_RECEIVED, control, bos);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PidomeServerPeripheral.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void stopDriver() {
        scheduledServiceExecutor.shutdownNow();
    }

    @Override
    public String readPort() throws IOException {
        return "";
    }

    @Override
    public void writePort(byte[] bytes) throws IOException {
        throw new IOException("Not applicable");
    }
    
    @Override
    public void releaseDriver() {
        /// I'm also empty
    }

}
