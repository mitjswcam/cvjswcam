package org.mit.webcam.applet;

import javax.swing.JApplet;

import java.awt.Canvas;
import java.io.File;

import org.mit.webcam.cam.Camera;
import org.mit.webcam.natlib.LibraryLoader;

//import org.mit.webcam.upload.FileUploader;

public class Main extends JApplet {
	
	public static final String version = "0.1a";
	
	public static final long serialVersionUID = 0101010101;
	

	private AppletOptions options;
	
	private Canvas c = null;
	@Override
	public void init() {	
		this.options = new AppletOptions(this);
		LibraryLoader.loadLibraries(options);
		
		c = new Canvas();
		getContentPane().add(c);
		
		startRecording(true, true, true);
	}
	
	public Thread startRecording(boolean useVid, boolean useMic, boolean failOnMic) {
		int width = this.options.getVideoWidth();
		int height = this.options.getVideoHeight();
		return startRecording(useVid, useMic, failOnMic, width, height);
	
	}
	
	public Thread startRecording(boolean useVid, boolean useMic, boolean failOnMic, int width, int height) {
		Camera c = this.getCamera(width, height);
		//new Thread(c).start();
		return c.record();
	}
	
	public void stopRecording() {
		
	}
	
	public void pageFrameGrab() {
		
	}
	
	public void upload(String ex_id, String json, String[] exemptions) {
		
	}
	
	public void camFrameGrab() {
		
	}
	
	public String getFrame(String id) {
		return null;
	}
	
	private Camera camera = null;
	private Camera getCamera() {
		if(this.camera == null) {
			File dest = new File(options.getTmpDir(), Camera.getRandomName(8));
			this.camera = new Camera(c, dest, this.options.getVideoWidth(), this.options.getVideoHeight());
		}
		return this.camera;
	}
	
	private Camera getCamera(int w, int h) {
		if(this.camera != null) {
			this.camera.destroy();
			this.camera = null;
		}
		File dest = new File(options.getTmpDir(), Camera.getRandomName(8));
		this.camera = new Camera(c, dest, w, h);
		return this.camera;
	}
	
	
	@Override
	public void start() {
	}

	
	@Override
	public String[][] getParameterInfo() {
		return this.options.getParameterInfo();
	}
}