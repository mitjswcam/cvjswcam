package org.mit.webcam.upload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CountingOutputStream extends FilterOutputStream {

	private long transferred;
	private List<UploadProgressListener> listeners;
	
	public CountingOutputStream(OutputStream out) {
		super(out);
		this.transferred = 0;
		this.listeners = new ArrayList<UploadProgressListener>();
	}
	
	public void addUploadProgressListener(UploadProgressListener listener) {
		this.listeners.add(listener);
	}
	
	public void dispatchProgress() {
		for(UploadProgressListener listener : this.listeners) {
			listener.transferred(this.transferred);
		}
	}

	public void write(byte[] bytes, int offset, int len) throws IOException {
		this.out.write(bytes, offset, len);
		this.transferred += len;
		this.dispatchProgress();
	}
	
	public void write(int b) throws IOException {
		this.out.write(b);
		this.transferred++;
		this.dispatchProgress();
	}
	
}
