package org.mit.webcam.applet;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;

@SuppressWarnings("serial")
public class ImageViewer extends JApplet implements Runnable {

	private Canvas c;
	private String path = null;
	private boolean stale = false;
	
	private BufferedImage image = null;
	public void init() {
		c = new Canvas();
		
		this.getContentPane().add(c);
		
		new Thread(this).start();
	}
	
	public synchronized void setPath(String path) {
		this.path = path;
		this.stale = true;
	}
	
	private void loadImage() {
		try {
			this.image = ImageIO.read(new File(this.path));
			this.stale = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void paint(Graphics g) {
		if(this.image != null) {
			Graphics g2 = c.getGraphics();
			int w = this.getWidth();
			int h = this.getHeight();
			g2.drawImage(this.image, 0, 0, w, h, c);
		}
	}
	
	public void run() {
		System.out.println("running");
		while(true) {
			if(stale) {
				System.out.println("Stale im to load");
				loadImage();
			}
			
			repaint();
			
			try {
				Thread.sleep(750);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			if(Thread.interrupted()) {
				System.out.println("Interrupted Viewer");
				break;
			}
		}
	}
}
