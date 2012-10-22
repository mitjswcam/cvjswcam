package org.mit.webcam.applet;

import javax.swing.JApplet;
import java.awt.Canvas;

import java.io.File;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.mit.webcam.media.Camera;
import org.mit.webcam.media.Detector;
import org.mit.webcam.natlib.LibraryLoader;


@SuppressWarnings("serial")
public class Main extends JApplet {
	
	public static final String version = "0.1a";
	
	public static enum ACTIONS {
		START_RECORD, STOP_RECORDING, GRAB_FRAME, UPLOAD, DETECT_VIDEO
	};

	//package scope
	AppletOptions options;
	ResourceTracker resources;
	
	//private scope
	private Canvas c = null;
	private Handlers handlers = null;
	
	///
	/// Applet Overide Methods
	///
	

	@Override
	public void start() {
	}
	
	@Override
	public void stop() {
		
	}
	
	@Override
	public void init() {	
		this.options = new AppletOptions(this);
		LibraryLoader.loadLibraries(options);
		
		this.resources = new ResourceTracker(options);
		
		this.getContentPane().add((c = new Canvas()));

		/****************************************************************
		 * The main thread is running in privileged context. Any
		 * code executed from java-script is inherently sand-boxed,
		 * and will run as if unsigned. Therefore, we pass messages 
		 * to the privileged thread indicating it should execute 
		 * certain actions instead of directing attempting to perform
		 * those actions.
		 ****************************************************************/
		
		this.handlers = new Handlers(this);
		handlers.getSecureExecLoop(); //create it
	}

	@Override
	public void destroy() {
		
	}
	
	@Override
	public String[][] getParameterInfo() {
		return this.options.getParameterInfo();
	}
	
	///
	/// Schedule privileged actions
	///
	
	/**
	 * This method returns immediately, and invokes a handler for a given 
	 * action to be run in a background Thread with higher privileges than
	 * can be accessed directly from javascript. No arguments are supplied, therefore the
	 * handler must supply defaults or abort. 
	 * 
	 * @param action   the action for which a corresponding handler {@link Handlers} should be invoked
	 */
	private void schedule(ACTIONS action) {
		schedule(action, null);
	}
	
	/**
	 * This method return immediately, and invokes a handler for a given 
	 * action to be run in a background Thread with higher privileges than
	 * can be accessed directly from java-script. 
	 * 
	 * @param action   the action for which a corresponding handler {@link Handlers} should be invoked
	 * @param context  the map of arguments which a given action handler can (but is not required) to use
	 * @see CtxRunnable
	 */
	private void schedule(ACTIONS action, Map<String, Object> context) {
		this.handlers.getSecureExecLoop().schedule(action.toString(), context);
	}
	
	/** 
	 * This function must be called after scheduling an action which produces 
	 * a value or else the thread will block. Not all actions are producers, but if
	 * so they must be consumed. See CtxRunnable.toReturn...
	 **/
	private Object consumeScheduledAction() {
		return this.handlers.getSecureExecLoop().getReturnValue();
	}
	
	///
	/// PUBLIC JS INTERFACE -- all wrapped in try catch for better debugging than js console
	///
	
	public void startRecording(boolean useVid, boolean useMic, boolean failOnMic) {
		try {
			schedule(ACTIONS.START_RECORD);
		} catch(Exception e) {
			System.err.println("startRecording():");
			e.printStackTrace();
		}
	}
	
	public void startRecording(boolean useVid, boolean useMic, boolean failOnMic, int width, int height) {
		try {
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("height", height);
			context.put("width", width);
			schedule(ACTIONS.START_RECORD, context);
		} catch(Exception e) {
			System.err.println("startRecording(context):");
			e.printStackTrace();
		}
	}
	
	public boolean detectMic() {
		try {
			return Detector.isMicrophoneAvailable();
		} catch(Exception e) {
			System.err.println("detectMic():");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean detectVideo() {
		try {
			schedule(ACTIONS.DETECT_VIDEO);
			return (Boolean) consumeScheduledAction();
		} catch(Exception e) {
			System.err.println("detectVideo():");
			e.printStackTrace();
		}
		return false;
	}
	
	public String stopRecording() {
		try {
			schedule(ACTIONS.STOP_RECORDING);
			String id = (String) consumeScheduledAction();
			return id;
		} catch(Exception e) {
			System.err.println("stopRecording():");
			e.printStackTrace();
		}
		return null;
	}
	
	public void pageFrameGrab() {
		System.err.println("Implementation removed because it generated a full screen capture and is a potential privacy problem.");
	}
	
	public boolean upload(String ex_id, String json, String[] exemptions) {
		if(ex_id == "" || ex_id == null) return false;
		
		try {
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("exempt_keys", new HashSet<String>(Arrays.asList(exemptions)));
			context.put("json_data", json);
			context.put("experiment_id", ex_id);
			schedule(ACTIONS.UPLOAD, context);
			return true;
		} catch(Exception e) {
			System.err.println("upload():");
			e.printStackTrace();
		}
		return false;
	}
	
	public String camFrameGrab() {
		try {
			schedule(ACTIONS.GRAB_FRAME);
			String id = (String) consumeScheduledAction();
			return id;
		} catch(Exception e) {
			System.err.println("camFrameGrab():");
			e.printStackTrace();
		}
		return null;
	}
	
	public String getFrame(String id) {
		try {
			File image = this.resources.getImage(id);
			//upload image to server and allow page to grab it?
			return image.getAbsolutePath();
		} catch(Exception e) {
			System.err.println("getFrame():");
			e.printStackTrace();
		}
		return null;
	}
	
	///
	///
	///
	
	private Camera camera = null;
	Camera getCamera() {
		if(this.camera == null) {
			this.camera = new Camera(c, this.options.getVideoWidth(), this.options.getVideoHeight());
		}
		return this.camera;
	}
	
	Camera getCamera(int w, int h) {
		if(this.camera != null) {
			this.camera.destroy();
			this.camera = null;
		}
		this.camera = new Camera(c, w, h);
		return this.camera;
	}
	
	
	
	
	
	
	

}