package org.mit.webcam.media;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;

import static com.googlecode.javacv.cpp.videoInputLib.*;

public class Detector {

	public static boolean isMicrophoneAvailable() {
		try {
            if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
                return false;
            } else {
                return true;
            }
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }
        return false;
	}
	
	public static boolean isWebcamAvailable() {
		int n = videoInput.listDevices();
		System.out.println("Enumerating " + n + " video input devices");
		for(int i = 0; i < n; i++) {
			System.out.println(videoInput.getDeviceName(i));
		}
		
		return n > 0;
	}
}
