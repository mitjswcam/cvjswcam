package org.mit.webcam.media;

import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static com.googlecode.javacv.cpp.avcodec.*;
import static com.googlecode.javacv.cpp.avutil.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.mit.webcam.applet.AppletOptions;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.Frame;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import static com.googlecode.javacv.cpp.opencv_core.*;

public class Camera implements Runnable {

	
	public static final short FRAMES = 30;

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
	
	private AudioFormat audio_format = null;
	private TargetDataLine line = null;
	
	private volatile Thread singletonThread = null;
	
	public Camera(Canvas canvas, int width, int height) {
		this.width = width;
		this.height = height;
		this.canvas = canvas;
		
		this.audio_format = new AudioFormat(192000f, 8, 1, true, true);
	}
	
	public void record() {
		this.record(null);
	}
	
	public void record(File dest) {
		this.dest = dest;
		this.singletonThread = new Thread(this, "Camera Thread");
		this.singletonThread.start();
	}
	
	public File stop() {
		Thread tmp = this.singletonThread;
		this.singletonThread = null;
		if(tmp != null) {
			tmp.interrupt();
		}
		return this.dest;
	}
	
	public synchronized boolean grabFrame(File output) {
		boolean destroyOnExit = this.grabber == null;
		try {
			IplImage image = getOrStartGrabber().grab();
			ImageIO.write(image.getBufferedImage(), "png", output);
			return true;
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(destroyOnExit && this.grabber != null) {
				try {
					this.grabber.stop();
				} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
					e.printStackTrace();
				}
				this.grabber = null;
			}
		}
		return false;
	}
	
	public void start() {
	}
	
	
	private OpenCVFrameGrabber getOrStartGrabber() throws com.googlecode.javacv.FrameGrabber.Exception {
		if(this.grabber == null) {
			this.grabber = new OpenCVFrameGrabber(0);
			this.grabber.setImageHeight(this.height);
			this.grabber.setImageWidth(this.width);
			this.grabber.start();
		}
		return grabber;
	}
	

	@Override
	public void run() {
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		AudioInputStream ais = null;
		byte[] buffer = null;
		
		try {
			OpenCVFrameGrabber grabber = this.getOrStartGrabber();
			
			if(this.dest != null) {
				try {
					DataLine.Info info = new DataLine.Info(TargetDataLine.class, audio_format);
					line = (TargetDataLine) AudioSystem.getLine(info);
					line.open(audio_format);
					line.start();

					ais = new AudioInputStream(line);
				} catch (LineUnavailableException ex) {
					line = null;
					System.err.println("Line unavailable");
				}
				
				
				recorder = FrameRecorder.createDefault(this.dest, width, height);
				recorder.setVideoCodec(CODEC_ID_MPEG4);
			    recorder.setPixelFormat(PIX_FMT_YUV420P);
			    
				//recorder.setAudioCodec(CODEC_ID_AAC);
				//recorder.setAudioChannels(audio_format.getChannels());
				//int bitrate = (int) (audio_format.getSampleSizeInBits() * audio_format.getSampleRate() * audio_format.getChannels());
				//recorder.setAudioBitrate(bitrate);
				recorder.start();
			}
			
			long lastFrameNano = 0;
			long currNano;
			long nanoFrameDelta = 1000000000 / FRAMES;
			Thread current = Thread.currentThread();
			while(this.singletonThread == current) {
				/*int size = Math.min(ais.available(), buffer.length);
				int count = ais.read(buffer, 0, size);
				if(count > 0) {
					//recorder.record(ByteBuffer.wrap(buffer, 0, count).asShortBuffer()); //16 bit depth
					recorder.record(ByteBuffer.wrap(buffer, 0, count)); //8 bit depth
				}*/
				
				currNano = System.nanoTime();
				if((currNano - lastFrameNano) >= nanoFrameDelta) {
					lastFrameNano = currNano;
					
					image = grabber.grab();
					EventQueue.invokeLater(new GuiUpdater(image.getBufferedImage(), canvas, canvasW, canvasH));
					
					/*int size = ais.available();
					buffer = new byte[size];
					int count = ais.read(buffer, 0, size);
					if(count > 0) {
						recorder.record(ByteBuffer.wrap(buffer, 0, count));
					}*/
					recorder.record(image);
					
					Thread.yield();
				}
				
				if(Thread.interrupted()) {
					System.out.println("Interrupted");
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			destroy();
		}
	}
	
	public void destroy() {
		if(grabber != null) {
			try {
				grabber.stop();
				grabber = null;
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				e.printStackTrace();
			}
		}
		if(image != null) {
			image = null;
		}
		if(recorder != null) {
			try {
				recorder.stop();
			} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
				e.printStackTrace();
			}
			recorder = null;
		}
		if(line != null) {
			line.stop();
			line.close();
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
			//System.out.println("PAINTING");
			c.getGraphics().drawImage(image, 0, 0, w, h, c);
		}
	}
}
