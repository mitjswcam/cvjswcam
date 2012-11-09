package org.mit.webcam.upload;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mit.webcam.applet.Main;



public class FileUploader implements Runnable{
	
	private static String experimentId = null;
	public static void setExperiment(String ex_id) {
		FileUploader.experimentId = ex_id;
	}
		
	public static String getExperiment()  {
		if(FileUploader.experimentId == null) throw new NullPointerException();
		return FileUploader.experimentId;
	}
		
	private static String userId = null;
	public static void setUser(String user_id) {
		FileUploader.userId = user_id;
	}
		
	public static String getUser() {
		if(FileUploader.userId == null) throw new NullPointerException();
		return FileUploader.userId;
	}
	
	public static void uploadMongo(URL upURL, String json) {
		System.out.println("Deprecated: Upload mongo json in javascript ajax call");
	}
	
	public static void uploadFiles(URL upURL, List<File> files) {
		for(File f : files) {
			FileUploader.dispatchUpload(upURL, f);
		}
	}
		
	//TODO: switch these to non-blocking async requests
	public static void dispatchUpload(URL upURL, File localFile) {
		System.out.println("Uploading " + localFile.getAbsolutePath() + " to " + upURL.toString());
		FileUploader uploader = new FileUploader(upURL, localFile);
		uploader.start();
	}
	
	
	
	private File file;
	private URL uploadUrl;
	
	public FileUploader(URL uploadUrl, File f) {
		this.file = f;
		this.uploadUrl = uploadUrl;
	}
	
	public void start() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(this.uploadUrl.toString());
		post.setHeader("content-disposition", "form-data");
		
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
		entity.addPart("experiment_id", new org.apache.http.entity.mime.content.StringBody(FileUploader.getExperiment()));
		entity.addPart("user_id", new org.apache.http.entity.mime.content.StringBody(FileUploader.getUser()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		entity.addPart("fileFromJava", new FileBody(this.file));
		CountingEntityWrapper toPost = new CountingEntityWrapper(entity);
		
		final String name = file.getName();
		final long length = entity.getContentLength();
		toPost.addUploadProgressListener(new UploadProgressListener() {
			public void transferred(long num) {
				String s = String.format("%s: %.2f%% (%d / %d)", name, (100f*num/length), num, length);
				System.out.println(s);
			}
		});	
		post.setEntity(toPost);
		try {
			//org.apache.http.HttpResponse resp = client.execute(post);
			//String output = org.apache.http.util.EntityUtils.toString(resp.getEntity());
			//System.out.println("HttpResponse: " + output);
			client.execute(post);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished uploading " + this.file.getAbsolutePath());
	}
}
