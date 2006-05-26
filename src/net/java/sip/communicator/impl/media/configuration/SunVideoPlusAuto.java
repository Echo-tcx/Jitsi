/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * File based on:
 * @(#)SunVideoPlusAuto.java 1.6 01/03/13
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 */
package net.java.sip.communicator.impl.media.configuration;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;
import javax.media.protocol.CaptureDevice;

import com.sun.media.protocol.sunvideoplus.*;

public class SunVideoPlusAuto {

    private static String DEVICE_PREFIX = "/dev/o1k";
    private static String PROTOCOL = "sunvideoplus";
    private static String LOCATOR_PREFIX = PROTOCOL + "://";

    private static boolean DO_PAL = false;

    int currentID = -1;

    public SunVideoPlusAuto() {
        /*
         * First remove any old entries
         */
        Vector devices = (Vector) CaptureDeviceManager.
        getDeviceList(null).clone();
        Enumeration enumeration = devices.elements();
        while (enumeration.hasMoreElements()) {
            CaptureDeviceInfo cdi = (CaptureDeviceInfo) enumeration.nextElement();
            String devName = cdi.getLocator().getProtocol();
            if (devName.equals(PROTOCOL))
                CaptureDeviceManager.removeDevice(cdi);
        }

        int nDevices = 0;
        for (int i = 0; i < 10; i++) {
            File fl = new File(DEVICE_PREFIX + i);
            if (fl.exists()) {
                if (DO_PAL) {
                    generalDevice(i, "PAL");
                    // If generating PAL, do both
                    // Garbage collect to release the PAL datasource
                    // otherwise it sometimes hangs before completing NTSC
                    System.gc();
                    generalDevice(i, "NTSC");
                } else {
                    generalDevice(i, null);
                }
                // No longer generate specific configurations,
                // let capture preview handle selection.
                // doDevice(i);
                nDevices++;
            }
        }

        try {
            CaptureDeviceManager.commit();
            System.err.println("SunVideoPlusAuto: Committed ok");
        } catch (java.io.IOException ioe) {
            System.err.println("SunVideoPlusAuto: error committing cdm");
        }
    }

    protected void generalDevice(int id, String signal) {
        // Add the general device
        javax.media.protocol.DataSource dsource = null;
        String url = LOCATOR_PREFIX + id;
        if (signal != null)
            url += "////" + signal.toLowerCase();
        try {
            dsource = Manager.createDataSource(new MediaLocator(url));
        } catch (Exception ex) {
        }
        if (dsource != null && dsource instanceof
                com.sun.media.protocol.sunvideoplus.DataSource) {
            CaptureDeviceInfo cdi = ((CaptureDevice)dsource).
            getCaptureDeviceInfo();
            if (cdi != null) {
                String name = cdi.getName();
                if (signal == null) {
                    CaptureDeviceManager.addDevice(cdi);
                } else {
                    name = cdi.getName() + " (" + signal + ")";
                    CaptureDeviceManager.addDevice(new CaptureDeviceInfo(name,
                            cdi.getLocator(), cdi.getFormats()));
                }
                System.err.println("CaptureDeviceInfo = "
                        + name + " "
                        + cdi.getLocator());
            }
            dsource.disconnect();
        }
    }

    protected void doDevice(int id) {
        currentID = id;
        FormatSetup fd = new FormatSetup(currentID);
        Vector cdiv = fd.getDeviceInfo();
        if (cdiv != null && cdiv.size() > 0) {
            for (int i = 0; i < cdiv.size(); i++) {
                CaptureDeviceInfo cdi =
                    (CaptureDeviceInfo) cdiv.elementAt(i);
                // At the moment, the name and locator are identical
                System.err.println("CaptureDeviceInfo = "
                        + cdi.getName());
//              System.err.println("CaptureDeviceInfo = "
//              + cdi.getName() + " "
//              + cdi.getLocator());
            }
        }
    }

    class FormatSetup {

        int id;

        boolean fullVideo = false;
        boolean anyVideo = true;

        String sAnalog, sPort, sVideoFormat, sSize;

        Hashtable videoFormats = new Hashtable();

        OPICapture opiVidCap = null;

        public FormatSetup(int id) {
            this.id = id;
            opiVidCap = new OPICapture(null);
            if (!opiVidCap.connect(id)) {
                throw new Error("Unable to connect to device");
            }

        }

        private void addVideoFormat(Format fin) {
            String sVideo = sPort + "/" + sVideoFormat + "/"
            + sSize + "/"
            + sAnalog;
            System.err.println("New format " + sVideo + " = " + fin);
            videoFormats.put(sVideo, fin);
        }

        public void mydispose() {
            opiVidCap.disconnect();
            System.err.println("Disconnected driver");
        }

        public void doFormat() {
            if (anyVideo) {
                doVideoFormats();
            }
        }

        public void doVideoFormats() {
            if (!anyVideo) {
                // add a dummy format entry
                videoFormats.put("off", new VideoFormat(VideoFormat.RGB));
            }

            sAnalog = "ntsc";
            if (DO_PAL)
                sAnalog = "pal";
            if (!opiVidCap.setSignal(sAnalog)) {
                System.err.println("Video analog signal not recognized");
                return;
            }
            int port = 1;
            if (!opiVidCap.setPort(port)) {
                System.err.println("Video source not recognized on port");
                return;
            }
            sPort = "" + port;
            opiVidCap.setScale(2);
            sSize = "cif";
            getVideoFormats();
        }

        private void getVideoFormats() {
            sVideoFormat = "h261";
            getH261Format();
            sVideoFormat = "h263";
            getH263Format();
            sVideoFormat = "jpeg";
            getJpegFormat();
            sVideoFormat = "rgb";
            getRGBFormat();
            sVideoFormat = "yuv";
            getYUVFormat();
        }

        private void getRGBFormat() {
            if (!opiVidCap.setCompress("RGB"))
                return;
            /*
             * If sizes are wanted, the only valid sizes are
             *  NTSC
             *      fcif    (640 x 480)
             *      cif     (320 x 240)
             *      qcif    (160 x 120)
             *  PAL
             *      fcif    (768 x 576)
             *      cif     (384 x 288)
             *      qcif    (192 x 144)
             */
            Dimension size = new Dimension(opiVidCap.getWidth(),
                    opiVidCap.getHeight());
            addVideoFormat(new RGBFormat(size, Format.NOT_SPECIFIED,
                    Format.byteArray,
                    Format.NOT_SPECIFIED,
                    16,
                    0xF800, 0x7E0, 0x1F, 2,
                    Format.NOT_SPECIFIED,
                    Format.FALSE,
                    Format.NOT_SPECIFIED));
        }

        private void getYUVFormat() {
            if (!opiVidCap.setCompress("YUV"))
                return;
            /*
             * If sizes are wanted, the only valid sizes are
             *  NTSC
             *      fcif    (640 x 480)
             *      cif     (320 x 240)
             *      qcif    (160 x 120)
             *  PAL
             *      fcif    (768 x 576)
             *      cif     (384 x 288)
             *      qcif    (192 x 144)
             *
             * The capture stream is actually interleaved YVYU format.
             * This is defined in the offset values below.
             */
            Dimension size = new Dimension(opiVidCap.getWidth(),
                    opiVidCap.getHeight());
            addVideoFormat(new YUVFormat(size, Format.NOT_SPECIFIED,
                    Format.byteArray,
                    Format.NOT_SPECIFIED,
                    YUVFormat.YUV_YUYV,
                    Format.NOT_SPECIFIED,
                    Format.NOT_SPECIFIED,
                    0, 3, 1));
        }

        private void getJpegFormat() {
            if (!opiVidCap.setCompress("Jpeg"))
                return;
            /*
             * If sizes are wanted, the only valid sizes are
             *  NTSC
             *      cif     (320 x 240)
             *      qcif    (160 x 120)
             *  PAL
             *      cif     (384 x 288)
             *      qcif    (192 x 144)
             */
            Dimension size = new Dimension(opiVidCap.getWidth(),
                    opiVidCap.getHeight());
            addVideoFormat(new VideoFormat(VideoFormat.JPEG, size,
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    Format.NOT_SPECIFIED));
        }

        private void getH261Format() {
            if (!opiVidCap.setCompress("H261"))
                return;
            /*
             * If sizes are wanted, the only valid sizes are
             *      cif     (352 x 288)
             *      qcif    (176 x 144)
             */
            Dimension size = new Dimension(opiVidCap.getWidth(),
                    opiVidCap.getHeight());
            addVideoFormat(new VideoFormat(VideoFormat.H261, size,
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    Format.NOT_SPECIFIED));
        }

        private void getH263Format() {
            if (!opiVidCap.setCompress("H263"))
                return;
            /*
             * If sizes are wanted, the only valid sizes are
             *      cif     (352 x 288)
             *      qcif    (176 x 144)
             */
            Dimension size = new Dimension(opiVidCap.getWidth(),
                    opiVidCap.getHeight());
            addVideoFormat(new VideoFormat(VideoFormat.H263, size,
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    Format.NOT_SPECIFIED));
        }


        public void issueError(String err) {
            System.err.println(err);
            Toolkit.getDefaultToolkit().beep();
        }

        public Enumeration sortedFormats(Hashtable formats) {
            Vector sorted = new Vector();
            keyloop: for (Enumeration en = formats.keys();
            en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                for (int i = 0; i < sorted.size(); i++) {
                    if (key.compareTo((String)sorted.elementAt(i)) < 0) {
                        sorted.insertElementAt(key, i);
                        continue keyloop;
                    }
                }
                sorted.addElement(key);
            }
            return sorted.elements();
        }


        public Vector getDeviceInfo() {
            doFormat();
            mydispose();

            String locatorPrefix = LOCATOR_PREFIX + id;
            Vector devices = new Vector();
            if (anyVideo) {

                for (Enumeration ve = sortedFormats(videoFormats);
                ve.hasMoreElements(); ) {
                    String vKey = (String) ve.nextElement();
                    Format vForm = (VideoFormat)videoFormats.get(vKey);
                    Format[] farray = null;
                    farray = new Format[1];
                    farray[0] = vForm;
                    String name = locatorPrefix + "/" + vKey;
                    CaptureDeviceInfo cdi = new CaptureDeviceInfo(name,
                            new MediaLocator(name), farray);
                    CaptureDeviceManager.addDevice(cdi);
                    devices.addElement(cdi);
                }
            }
            return devices;
        }

    }

    public static void setPALSignal(boolean pal) {
        DO_PAL = pal;
    }

    public static void main(String [] args) {
        if (args.length > 0) {
            if (args.length > 1) {
                System.err.println(
                "Usage: java SunVideoPlusAuto [ ntsc | pal ]");
                System.exit(1);
            }
            if (args[0].equalsIgnoreCase("ntsc")) {
                SunVideoPlusAuto.setPALSignal(false);
            } else if (args[0].equalsIgnoreCase("pal")) {
                SunVideoPlusAuto.setPALSignal(true);
            } else {
                System.err.println(
                "Usage: java SunVideoPlusAuto [ ntsc | pal ]");
                System.exit(1);
            }
        }
        SunVideoPlusAuto m = new SunVideoPlusAuto();
        System.exit(0);
    }
}

