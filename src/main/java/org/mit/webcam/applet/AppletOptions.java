package org.mit.webcam.applet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class AppletOptions {

	public static final String DEFAULT_BASEPATH = "/";
	
	public static final String DEFAULT_LIB_DL_PATH    = "libs/";
	
	public static final String DEFAULT_DLL_ARCHIVE_64   = "win64.zip";
	public static final String DEFAULT_SO_ARCHIVE_64    = "nix64.zip";
	public static final String DEFAULT_DYLIB_ARCHIVE_64 = "mac64.zip";
	public static final String DEFAULT_DLL_ARCHIVE_32   = "win32.zip";
	public static final String DEFAULT_SO_ARCHIVE_32    = "nix32.zip";
	
	public static final String DEFAULT_UPLOAD_PATH  = "upload.php";
	public static final int    DEFAULT_VIDEO_WIDTH  = 640;
	public static final int    DEFAULT_VIDEO_HEIGHT = 480;
	
	public static final String DEFAULT_VIDEO_EXT = "mp4";
	public static final String DEFAULT_IMAGE_EXT = "png";
	
	public static String joinFilePath(String path1, String path2) {
		return AppletOptions.joinPath(path1, path2, File.separator);
	}
	
	public static String joinURLPath(String path1, String path2) {
		return AppletOptions.joinPath(path1, path2, "/"); 
	}
	
	public static String joinPath(String path1, String path2, String separator) {
		if(path1 == null || path1 == "") {
			return (path2 == null || path2 == "") ? "" : path2;
		} else {
			if(path2 == null || path2 == "") 
				return path1;
		}
		
		int sLen = separator.length(), p1Len = path1.length(), p2Len = path2.length();
		int maxSize = p1Len + p2Len + sLen;
		StringBuffer buffer = new StringBuffer(maxSize);
		
		/***
		 * If we attempt to join a new path beginning with a separator, 
		 * treat it as a root path and ignore the first.
		 * 
		 * This allows us to specify parameters outside of the base url
		 * path if necessary. (eg. upload_path be in a separate path 
		 * outside of the base path specified for the deployment)
		 */
		int index = path2.indexOf(separator);
		if(index == 0) { // && !path1.contains("://") protocol specifier
			return path2;
		}
		
		//remove any trailing separators
		index = path1.lastIndexOf(separator);
		if(index == path1.length() - separator.length()) {
			path1 = path1.substring(0, index);
		}
		
		buffer.append(path1);
		buffer.append(separator);
		buffer.append(path2);
		return buffer.toString();
	}
	
	private Parameterized applet = null;
	public AppletOptions(Parameterized applet) {
		this.applet = applet;
	}
	
	//util methods
	
	public URL getServer() {
		return this.getServer("");
	}
	
	public URL getServer(String path) {
		String base = getBasePath();
		path = AppletOptions.joinURLPath(base, path);
		return getServerAbsolute(path);
	}
	
	public URL getServerAbsolute(String path) {
		URL cb = this.applet.getCodeBase();
		URL server = null;
		try {
			//TODO: so that if path specifies a whole url it just uses that
			server = new URL(cb.getProtocol(), cb.getHost(), cb.getPort(), path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return server;
	}
	
	public URL getLibraryURL() {
		return this.getServer(this.getLibArchivePath());
	}
	
	public URL getUploadURL() {
		return this.getServer(this.getUploadPath());
	}

	public int getVideoWidth() {
		return this.getVideoDim("rec_width", AppletOptions.DEFAULT_VIDEO_WIDTH);
	}
	
	public int getVideoHeight() {
		return this.getVideoDim("rec_height", AppletOptions.DEFAULT_VIDEO_HEIGHT);
	}
	
	public String getVideoFormat() {
		return AppletOptions.DEFAULT_VIDEO_EXT;
	}
	
	public String getImageFormat() {
		return AppletOptions.DEFAULT_IMAGE_EXT;
	}
	
	private File tmpdir = null;
	public File getTmpDir() {
		if(this.tmpdir == null) {
			String path = System.getProperty("java.io.tmpdir");
			String version = Main.version;
			String folder = "webcam-" + version;
			this.tmpdir = new File(path, folder);
		}
		return this.tmpdir;
	}
	
	///
	/// Param Accessors
	///
	
	private String getUploadPath() {
		String path = this.applet.getParameter("upload_path");
		if(path == null) {
			path = DEFAULT_UPLOAD_PATH;
		}
		return path;
	}
	
	private String getBasePath() {
		String path = this.applet.getParameter("base_path");
		if(path == null) {
			path = AppletOptions.DEFAULT_BASEPATH;
		}
		return path;
	}
	
	private int getVideoDim(String tag, int defaultValue) {
		int v = defaultValue;
		String tagValue = this.applet.getParameter(tag);
		if(tagValue != null) {
			try {
				v = Integer.parseInt(tagValue);
			} catch(NumberFormatException e) {
				System.err.println("Failed to parse " + tag + " param: " + tagValue + ". Using default: " + defaultValue);
				e.printStackTrace();
			}
		}
		return v;
	}
	
	private String getLibraryPath() {
		String path = this.applet.getParameter("lib_path");
		if(path == null) {
			path = DEFAULT_LIB_DL_PATH;
		}
		return path;
	}
	
	private String getLibArchivePath() {
		String osName = System.getProperty("os.name").toLowerCase();
		String bitness = System.getProperty("sun.arch.data.model");
		
		//System.out.println("OS: " + osName + ", bitness: " + bitness);

		String archive = null;
		
		//TODO: parameter map
		
		if(osName.indexOf("win") >= 0) {
			archive = applet.getParameter("dll_archive_" + bitness);
			if(archive == null)
				archive = (bitness == "64") ? DEFAULT_DLL_ARCHIVE_64 : DEFAULT_DLL_ARCHIVE_32;
		} else if(osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0) {
			archive = applet.getParameter("so_archive_" + bitness);
			if(archive == null)
				archive = bitness == "64" ? DEFAULT_SO_ARCHIVE_64 : DEFAULT_SO_ARCHIVE_32;
		} else if(osName.indexOf("mac") >= 0) {
			archive = applet.getParameter("dylib_archive" + bitness);
			if(archive == null) 
				archive = DEFAULT_DYLIB_ARCHIVE_64;
		} else {
			throw new Error("Fatal Error: Could not retrieve valid native library for you platform: " + osName + ", " + bitness + " bit.");
			//for now:
			//System.err.println("messed up with detecting archive type, defaulting to win32");
			//archive = "windows-x86.jar";
		}
		
		return AppletOptions.joinURLPath(this.getLibraryPath(), archive);
	}
	
	///
	///
	///
	
	public String[][] getParameterInfo() {
		return new String[][] {
				{"lib_path", "Path on a hosting server where native libraries can be retrieved."},
				{"dll_archive_32", "Archive containing 32 bit Windows native libraries."},
				{"dll_archive_64", "Archive containing 64 bit Windows native libraries."},
				{"so_archive_32", "Archive containing 32 bit Linux native shared libraries."},
				{"so_archive_64", "Archive containing 64 bit Linux native shared libraries."},
				{"dylib_archive_64", "Archive containing 64 bit OSX native shared libraries."},
				{"upload_path", "Path to upload script or endpoint."},
				{"rec_width", "Default width in pixels of recorded files."},
				{"rec_height", "Default height in pixels of recorded files."}
		};
		
	}
}
