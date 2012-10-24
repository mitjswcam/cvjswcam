package org.mit.webcam.secure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public abstract class CtxRunnableSynchronous {
	private Map<String, Object> context = null;
	
	public void setContext(Map<String, Object> context) {
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
	
	public Object handle(Map<String, Object> context) {
		setContext(context);
		return this.run();
	}
	
	public abstract Object run();
}
