package org.mit.webcam.secure;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class SecureExecLoop implements Runnable {
	
	private Map<String, CtxRunnable> handlers;
	private SynchronousQueue<ExecContext> fifoActions;
	private SynchronousQueue<Object> retValues;
	public SecureExecLoop(Map<String, CtxRunnable> handlers) {
		fifoActions = new SynchronousQueue<ExecContext>();
		retValues = new SynchronousQueue<Object>();
		
		this.handlers = handlers;
		new Thread(this).start();
	}
	
	private static class ExecContext {
		public String action;
		public Map<String, Object> ctx;
		public ExecContext(String action, Map<String, Object> ctx) {
			this.action = action;
			this.ctx = ctx;
		}
	}
	
	public synchronized boolean schedule(String action, Map<String, Object> ctx) {
		if(!this.handlers.containsKey(action)) {
			return false;
		}
		
		try {
			fifoActions.put(new ExecContext(action, ctx));
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public synchronized Object getReturnValue() {
		try {
			return this.retValues.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void run() {
		while(!Thread.interrupted()) {
			try {
				ExecContext exec = fifoActions.take();
				handle(this.handlers.get(exec.action), exec.ctx);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handle(CtxRunnable handler, Map<String, Object> ctx) {
		handler.handle(this.retValues, ctx);
	}
}
