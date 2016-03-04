/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.threads;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author John Sirach
 */
public class SystemThreads {

    MinuteThread minuteThread = new MinuteThread();
    
    static List _minuteListeners = new ArrayList();
    
    public SystemThreads(){}
    
    public final void start(){
        minuteThread.start();
    }
    
    public final void stop(){
        minuteThread.interrupt();
    }
    
    public static synchronized void addMinuteListener(MinuteThreadListener l){
        _minuteListeners.add(l);
    }
    
    public static synchronized void removeMinuteListener(MinuteThreadListener l){
        _minuteListeners.remove(l);
    }
    
    final synchronized void _fireMinuteEvent(){
        Iterator listeners = _minuteListeners.iterator();
        while (listeners.hasNext()) {
            ((MinuteThreadListener) listeners.next()).handleMinuteThread();
        }
    }
    
    private class MinuteThread extends Thread {

        private MinuteThread() {
        }

        @Override
        @SuppressWarnings("deprecation")
        public void run() {
            Thread.currentThread().setName("SYSTHREAD:MinuteThread");
            while (true) {
                Date oDate = new Date();
                Integer nextrun = 60 - oDate.getSeconds();
                try {
                    Thread.sleep(nextrun * 1000);
                    _fireMinuteEvent();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    
}
