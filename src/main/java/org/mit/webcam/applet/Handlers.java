package org.mit.webcam.applet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mit.webcam.applet.Main.ACTIONS;
import org.mit.webcam.media.Camera;
import org.mit.webcam.media.Detector;
import org.mit.webcam.secure.CtxRunnable;
import org.mit.webcam.secure.SecureExecLoop;
import org.mit.webcam.upload.FileUploader;

public class Handlers {
	private final Main applet;
	private SecureExecLoop exec = null;
	public Handlers(Main applet) {
		this.applet = applet;
	}
	
	
	public SecureExecLoop getSecureExecLoop() {
		if(this.exec == null) {
			Map<String, CtxRunnable> handlers = new HashMap<String, CtxRunnable>();
			handlers.put(ACTIONS.START_RECORD.toString(), this.createStartRecordHandler());
			handlers.put(ACTIONS.STOP_RECORDING.toString(), this.createStopRecordHandler());
			handlers.put(ACTIONS.GRAB_FRAME.toString(), this.createGrabFrameHandler());
			handlers.put(ACTIONS.UPLOAD.toString(), this.createUploadHandler());
			handlers.put(ACTIONS.DETECT_VIDEO.toString(), this.createDetectVideoHandler());
			this.exec = new SecureExecLoop(handlers);
		}
		return this.exec;
	}
	
	private CtxRunnable createDetectVideoHandler() {
		CtxRunnable runnable = new CtxRunnable() {
			public void run() {
				boolean test = Detector.isWebcamAvailable();
				this.toReturn(test);
			}
		};
		return runnable;
	}

	private CtxRunnable createStartRecordHandler() {
		CtxRunnable handler = new CtxRunnable() {
			public void run() {
				int width = this.getVariable("width", applet.options.getVideoWidth());
				int height = this.getVariable("height", applet.options.getVideoHeight());
				Camera c = applet.getCamera(width, height);
				File videoFile = applet.resources.getUniqueTempVideo();
				c.record(videoFile);
			}
		};
		return handler;
	}
	
	private CtxRunnable createStopRecordHandler() {
		CtxRunnable handler = new CtxRunnable() {
			public void run() {
				File video = applet.getCamera().stop();
				try {
					String id = applet.resources.addVideo(video);
					this.toReturn(id);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		return handler;
	}
	
	private CtxRunnable createGrabFrameHandler() {
		CtxRunnable handler = new CtxRunnable() {
			public void run() {
				Camera c = applet.getCamera();
				File f = applet.resources.getUniqueTempImage();
				String id = "";
				if(c.grabFrame(f)) {
					try {
						id = applet.resources.addImage(f);
					} catch (Exception e) {
						System.out.println("Since we are getting our file from resources, this should never happen");
						e.printStackTrace();
					}
				}
				this.toReturn(id);
			}
		};
		return handler;
	}
	
	private CtxRunnable createUploadHandler() {
		CtxRunnable handler = new CtxRunnable() {
			public void run() {
				String experiment_id = (String) this.getVariable("experiment_id");
				if(experiment_id == null) {
					System.err.println("(experiment_id) must be specified");
				}
				
				String json_data = (String) this.getVariable("json_data");
				json_data = (json_data != null) ? json_data : "";
				
				Set<String> exempt_keys = this.getVariable("exempt_keys", new HashSet<String>());
				
				List<File> files = applet.resources.filterFiles(exempt_keys);
				
				//files.add( NEW JSON DATA FILE )
				//FileUploader.uploadExperiment(applet.options.getUploadURL(), experiment_id, (File[])files.toArray());
				FileUploader.uploadFiles(applet.options.getUploadURL(), files);// (File[])files.toArray());
			}
		};
		return handler;
	}
}
