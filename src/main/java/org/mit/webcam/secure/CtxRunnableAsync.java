package org.mit.webcam.secure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public abstract class CtxRunnableAsync implements Runnable {
	private Map<String, Object> context = null;
	private SynchronousQueue<Object> ret = null;
	
	public void setContext(SynchronousQueue<Object> ret, Map<String, Object> context) {
		this.ret = ret;
		this.context = context;
	}
	
	public Map<String, Object> getContext() {
		if(this.context == null) {
			this.context = new HashMap<String, Object>();
		}
		return this.context;
	}
	
	public Object getVariable(String key) {
		Map<String, Object> context = this.getContext();
		return context.containsKey(key) ? context.get(key) : null;
	}
	
	@SuppressWarnings("unchecked") //This method will throw errors if default is null
	public <T> T getVariable(String key, T default_value) {
		Map<String, Object> context = this.getContext();
		return context.containsKey(key) ? (T) context.get(key) : default_value;
	}
	
	public void handle(SynchronousQueue<Object> ret, Map<String, Object> context) {
		setContext(ret, context);
		new Thread(this).start();
	}
	
	/***********************************************************************************
	 * Warning: This function should only be called at most one time from a runnable.
	 * Warning: If this function is called, it will block until the value is consumed
	 * by a getReturnValue call on SecureExecLoop instance.
	 ***********************************************************************************/
	public void toReturn(Object o) {
		try {
			this.ret.put(o);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
