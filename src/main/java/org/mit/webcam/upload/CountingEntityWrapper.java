package org.mit.webcam.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.BasicHttpEntity;

public class CountingEntityWrapper implements HttpEntity {

	private HttpEntity wrapped;
	
	private List<UploadProgressListener> listeners = new ArrayList<UploadProgressListener>();
	
	public CountingEntityWrapper(HttpEntity entity) {
		this.wrapped = entity;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void consumeContent() throws IOException {
		this.wrapped.consumeContent();
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return this.wrapped.getContent();
	}

	@Override
	public Header getContentEncoding() {
		return this.wrapped.getContentEncoding();
	}

	@Override
	public long getContentLength() {
		return this.wrapped.getContentLength();
	}

	@Override
	public Header getContentType() {
		return this.wrapped.getContentType();
	}

	@Override
	public boolean isChunked() {
		return this.wrapped.isChunked();
	}

	@Override
	public boolean isRepeatable() {
		return this.wrapped.isRepeatable();
	}

	@Override
	public boolean isStreaming() {
		return this.wrapped.isStreaming();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		CountingOutputStream stream = new CountingOutputStream(out);
		for(UploadProgressListener listener : this.listeners) {
			stream.addUploadProgressListener(listener);
		}
		this.wrapped.writeTo(stream);
	}
	
	public void addUploadProgressListener(UploadProgressListener up) {
		this.listeners.add(up);
	}
	
	public void removeUploadProgressListener(UploadProgressListener up) {
		this.listeners.remove(up);
	}

}
