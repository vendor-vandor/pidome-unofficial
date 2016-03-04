/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.system.hardware.peripherals.usb.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import org.pidome.server.system.hardware.peripherals.usb.linux.CFuncImpl.LinuxCLib.FDSet;
import org.pidome.server.system.hardware.peripherals.usb.linux.CFuncImpl.LinuxCLib.FDSetImpl;

/**
 *
 * @author John Sirach
 */
public class CFuncImpl {

    static public FDSet newFDSet() {
        return new FDSetImpl();
    }
    
    static public void FD_SET(int fd, LinuxCLib.FDSet set) {
        if (set == null) {
            return;
        }
        LinuxCLib.FDSetImpl p = (LinuxCLib.FDSetImpl) set;
        p.bits[fd / LinuxCLib.FDSetImpl.NFBBITS] |= 1 << (fd % LinuxCLib.FDSetImpl.NFBBITS);
    }
    
    static public void FD_ZERO(FDSet set) {
        if (set == null) {
            return;
        }
        FDSetImpl p = (FDSetImpl) set;
        java.util.Arrays.fill(p.bits, 0);
    }
    
    static public int select(int nfds, FDSet rfds, FDSet wfds, FDSet efds, TimeVal timeout) {
        LinuxCLib.timeval tout = null;
        if (timeout != null) {
            tout = new LinuxCLib.timeval(timeout);
        }

        int[] r = rfds != null ? ((FDSetImpl) rfds).bits : null;
        int[] w = wfds != null ? ((FDSetImpl) wfds).bits : null;
        int[] e = efds != null ? ((FDSetImpl) efds).bits : null;
        return LinuxCLib.INSTANCE.select(nfds, r, w, e, tout);
    }
    
    static public boolean FD_ISSET(int fd, FDSet set) {
        if (set == null) {
            return false;
        }
        FDSetImpl p = (FDSetImpl) set;
        return (p.bits[fd / FDSetImpl.NFBBITS] & (1 << (fd % FDSetImpl.NFBBITS))) != 0;
    }
    
    final static public class TimeVal {

        public long tv_sec;
        public long tv_usec;
    }
    
    public interface LinuxCLib extends Library {

        LinuxCLib INSTANCE = (LinuxCLib) Native.loadLibrary("c", LinuxCLib.class);

        public int select(int n, int[] read, int[] write, int[] error, timeval timeout);
        
        static public class timeval extends Structure {

            public NativeLong tv_sec;
            public NativeLong tv_usec;

            @Override
            protected List getFieldOrder() {
                return Arrays.asList(//
                        "tv_sec",//
                        "tv_usec"//
                );
            }

            public timeval(TimeVal timeout) {
                tv_sec = new NativeLong(timeout.tv_sec);
                tv_usec = new NativeLong(timeout.tv_usec);
            }
        }

        static class FDSetImpl extends FDSet {

            static final int FD_SET_SIZE = 1024;
            static final int NFBBITS = 32;
            int[] bits = new int[(FD_SET_SIZE + NFBBITS - 1) / NFBBITS];

            @Override
            public String toString() {
                return String.format("%08X%08X", bits[0], bits[1]);
            }
        }

        abstract public class FDSet {}

    }    
    
    
}
