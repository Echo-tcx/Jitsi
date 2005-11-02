/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 * File based on:
 * @(#)JavaSoundDetector.java   1.2 01/03/13
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 */
package net.java.sip.communicator.impl.media.configuration;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;


public class JavaSoundDetector {
    
    boolean supported = false;
    
    public JavaSoundDetector() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    null,
                    AudioSystem.NOT_SPECIFIED);
            supported = AudioSystem.isLineSupported(info);
        } catch (Exception ex) {
            supported = false;
        }
    }
    
    public boolean isSupported() {
        return supported;
    }    
}

