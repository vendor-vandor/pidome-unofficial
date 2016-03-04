/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.utilities;

/**
 *
 * @author John
 */
public class WaitRunnable implements Runnable {

    private final Runnable toRun;
    private boolean done;

    public WaitRunnable(Runnable toRun) {
        this.toRun = toRun;
    }

    @Override
    public void run() {
        toRun.run();
        synchronized (this) {
            done = true;
            notifyAll();
        }
    }

    public void waitForComplete() {
        synchronized (this) {
            while (!done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

    }
}
