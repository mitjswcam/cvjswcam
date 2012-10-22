package org.mit.webcam.applet;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ResourceTracker {
	
	private static final String validCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static String getRandomName(int len) {
		return getRandomName(len, "");
	}
	public static String getRandomName(int len, String extension) {
		len = Math.max(1, Math.min(len, 64));
		StringBuffer buffer = new StringBuffer(len);
		int index;
		for(int i = 0; i < len; i++) {
			index = (int)(Math.random() * validCharacters.length()); 
			buffer.append(validCharacters.charAt(index));
		}
		if(extension != null && extension != "") {
			buffer.append('.');
			buffer.append(extension);
		}
		return buffer.toString();
	}
	
	public static String getUniqueRandomName(Set<String> s, int len) {
		return getUniqueRandomName(s, len, "");
	}
	
	public static String getUniqueRandomName(Set<String> s, int len, String extension) {
		String hash = null;
		do {
			hash = getRandomName(len, extension);
		} while(s.contains(hash));
		return hash;
	}
	
	private AppletOptions options;
	private Map<String, File> videos, images;
	
	public ResourceTracker(AppletOptions options) {
		this.options = options;
		
		videos = new HashMap<String, File>();
		images = new HashMap<String, File>();
	}
	
	public File getUniqueTempVideo() {
		Set<String> usedFilenames = this.getUnionedFilenameSet();
		String name = ResourceTracker.getUniqueRandomName(usedFilenames, 8, this.options.getVideoFormat());
		return new File(this.options.getTmpDir(), name);
	}
	
	public List<File> filterFiles(List<String> exemptKeys) {
		return filterFiles(new HashSet<String>(exemptKeys));
	}
	
	public List<File> filterFiles(Set<String> exemptKeys) {
		List<File> files = new LinkedList<File>();
		Set<Entry<String, File>> entries = new HashSet<Entry<String, File>>();
		entries.addAll(videos.entrySet());
		entries.addAll(images.entrySet());
		Iterator<Entry<String,File>> i = entries.iterator();
		while(i.hasNext()) {
			Entry<String, File> entry = i.next();
			if(!exemptKeys.contains(entry.getKey())) {
				files.add(entry.getValue());
			}
		}
		return files;
	}
	
	public File getImage(String key) {
		return this.images.get(key);
	}
	
	public File getUniqueTempImage() {
		Set<String> usedFilenames = this.getUnionedFilenameSet();
		String name = ResourceTracker.getUniqueRandomName(usedFilenames, 8, this.options.getImageFormat());
		return new File(this.options.getTmpDir(), name);
	}
	
	//todo: specific exceptions
	public String addVideo(File video) throws Exception {
		return this.addFile(this.videos, video);
	}
	
	public String addImage(File image) throws Exception {
		return this.addFile(this.images, image);
	}
	
	private String addFile(Map<String, File> map, File f) throws Exception {
		Set<String> usedFilenames = this.getUnionedFilenameSet();
		if(usedFilenames.contains(f.getName())) {
			throw new Exception("Duplicate File Not Added: " + f.getName() );
		}
		
		Set<String> usedKeys = this.getUnionedKeySet();
		String key = ResourceTracker.getUniqueRandomName(usedKeys, 8);
		map.put(key, f);
		return key;
	}
	
	private Set<String> getUnionedKeySet() {
		HashSet<String> set = new HashSet<String>();
		set.addAll(videos.keySet());
		set.addAll(images.keySet());
		return set;
	}
	
	private Set<String> getUnionedFilenameSet() {
		HashSet<String> set = new HashSet<String>();
		Collection<File> c = new HashSet<File>();
		c.addAll(videos.values());
		c.addAll(images.values());
		Iterator<File> i = c.iterator();
		while(i.hasNext()) {
			set.add(i.next().getName());
		}
		return set;
	}
}
