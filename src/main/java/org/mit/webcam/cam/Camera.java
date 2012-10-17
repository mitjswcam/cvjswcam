package org.mit.webcam.cam;

import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.Frame;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class Camera implements Runnable {

	private static final String validCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static String getRandomName(int len) {
		len = Math.max(1, Math.min(len, 64));
		StringBuffer buffer = new StringBuffer(len);
		int index;
		for(int i = 0; i < len; i++) {
			index = (int)(Math.random() * validCharacters.length()); 
			buffer.append(validCharacters.charAt(index));
		}
		buffer.append(".avi");
		return buffer.toString();
	}
	
	public static final short FRAMES = 30;
	public static final long MILLIS = 1000/FRAMES;

	///
	/// Instance Variables
	///
	
	private Canvas canvas;
	private File dest;
	
	private int width;
	private int height;
	
	private OpenCVFrameGrabber grabber = null;
	private IplImage image = null;
	private FrameRecorder recorder = null;
	
	private AudioFormat audio_format;
	//private DataLine.Info info;
	private TargetDataLine line;
	
	public Camera(Canvas canvas, File dest, int width, int height) {
		this.width = width;
		this.height = height;
		this.canvas = canvas;
		this.dest = dest;
		
		this.audio_format = new AudioFormat(8000.0f, 16, 1, true, true);
		//this.info = new DataLine.Info(TargetDataLine.class, audio_format);
		
	}
	
	public Thread record() {
		Thread t = new Thread(this);
		t.start();
		return t;
	}
	
	public void start() {
	}

	@Override
	public void run() {
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		
		try {
			grabber = new OpenCVFrameGrabber(0); 
			grabber.setImageWidth(width);
			grabber.setImageHeight(height);
			grabber.start();
			//image = grabber.grab();
			
			if(this.dest != null) {
				/*if (!AudioSystem.isLineSupported(info)) {
					System.err.println("Line Not Supported");
					this.line = null;
				}*/
				/*
				try {
					line = AudioSystem.getTargetDataLine(audio_format);
					line.open();
					line.start();
					System.out.println("Line started");
				} catch (LineUnavailableException ex) {
					line = null;
					System.err.println("Line unavailable");
				}*/
				
				//recorder = FrameRecorder.createDefault(this.dest, width, height);	
				//recorder.setAudioChannels(audioChannels);
				recorder = new FFmpegFrameRecorder(this.dest, width, height);
				/*recorder.setAudioChannels(audio_format.getChannels());
				System.out.println("setting audio channels: " + recorder.getAudioChannels());
				recorder.setAudioBitrate((int)audio_format.getSampleRate()*audio_format.getSampleSizeInBits());
				System.out.println("setting audio bitrate: " + recorder.getAudioBitrate());*/
				recorder.start();
			}
			
			while(!Thread.interrupted()) {
				image = grabber.grab();
				EventQueue.invokeLater(new GuiUpdater(image.getBufferedImage(), canvas, canvasW, canvasH));
				if(this.dest != null) {
					/*if(this.line != null ) {
						int len = line.available();
						byte[] b = new byte[len];
						Buffer buffer = ByteBuffer.wrap(b, 0, len);
						recorder.record(buffer);
					}*/
					recorder.record(image);
					
				}
				
				try {
					Thread.sleep(Camera.MILLIS);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			destroy();
			line.stop();
		}
	}
	
	public void destroy() {
		if(grabber != null) {
			try {
				grabber.stop();
				//grabber.release();
				grabber = null;
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				e.printStackTrace();
			}
		}
		if(image != null) {
			image.release();
			image = null;
		}
		if(recorder != null) {
			try {
				recorder.stop();
				//recorder.release();
			} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
				e.printStackTrace();
			}
			recorder = null;
		}
	}
	
	private static class GuiUpdater implements Runnable {
		BufferedImage image;
		Canvas c;
		int w;
		int h;
		public GuiUpdater(BufferedImage image, Canvas c, int w, int h) {
			this.image = image;
			this.c = c; 
			this.w = w;
			this.h = h;
		}
		public void run() {
			c.getGraphics().drawImage(image, 0, 0, w, h, c);
		}
	}
}
