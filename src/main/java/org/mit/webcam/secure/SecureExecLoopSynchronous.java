package org.mit.webcam.secure;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class SecureExecLoopSynchronous implements Runnable {
	private Thread execLoop = null;
	
	private Map<String, CtxRunnableSynchronous> handlers;
	private SynchronousQueue<ExecContext> fifoActions;
	private SynchronousQueue<Object> retValues;
	public SecureExecLoopSynchronous(Map<String, CtxRunnableSynchronous> handlers) {
		fifoActions = new SynchronousQueue<ExecContext>();
		retValues = new SynchronousQueue<Object>();
		
		this.handlers = handlers;
		this.execLoop = new Thread(this);
		this.execLoop.start();
	}
	
	private static class ExecContext {
		public String action;
		public Map<String, Object> ctx;
		public ExecContext(String action, Map<String, Object> ctx) {
			this.action = action;
			this.ctx = ctx;
		}
	}
	
	private static class ExceptionWrapper {
		Exception e = null;
		public ExceptionWrapper(Exception e) {
			this.e = e;
		}
	}
	
	public synchronized Object execute(String action, Map<String, Object> ctx) 
			throws Exception {
		
		if(!this.handlers.containsKey(action)) {
			throw new UnsupportedActionException(action + " not supported.");
		}
		
		fifoActions.put(new ExecContext(action, ctx));
		
		Object result = this.retValues.take();
		if(result instanceof ExceptionWrapper) {
			ExceptionWrapper wrapper = (ExceptionWrapper) result;
			throw wrapper.e;
		} else {
			return result;
		}
	}
	
	public void run() {
		while(this.execLoop == Thread.currentThread()) {
			try {
				ExecContext exec = fifoActions.take();
				CtxRunnableSynchronous runnable = this.handlers.get(exec.action);
				Object result = null;
				try {
					result = runnable.handle(exec.ctx);
				} catch(Exception e) {
					result = new ExceptionWrapper(e);
				} finally {
					this.retValues.put(result);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
