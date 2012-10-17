package org.mit.webcam.natlib;

import java.applet.Applet;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.mit.webcam.applet.AppletOptions;

/**
 * 
 * <param name="dll_archive_64" value="windows64.jar" /> Required for an applet to work on windows systems
 * <param name="so_archive_64"  value="unix64.jar /> Required for an applet to work on unix systems
 * <param name="dylib_archive_64" value="mac64.jar" /> Required for an applet to work on mac systems
 * <param name="dll_archive_32" value="windows.jar" /> 
 * <param name="so_archive_32" value="unix.jar" />
 * 
 * <param name="lib_path"    value="/webcam/libs/" /> Required location of dll_archive & so_archive
 */
public class LibraryLoader {
	
	public static void loadLibraries(AppletOptions options) {
		URL link = options.getLibraryURL();
		System.out.println("From: " + link);
		
		File local = options.getTmpDir(); 
		System.out.println("tmpDir: " + local);
		
		//File path = new File(local);
		if(!local.exists() && !local.mkdirs()) 
			throw new Error("Fatal Error: Could not create local library directory: " + local);

		setLibraryPath(local);
		
		try {
			fetchArchive(link, local);
			extractArchive(local);
		} catch(Exception e) {
			e.printStackTrace();			
		}
	}

    public static void setLibraryPath(File path) {
    	String LIB_PATH = path.getAbsolutePath() + File.pathSeparator + System.getProperty("java.library.path");
		System.setProperty("java.library.path", LIB_PATH);

		try {
			//Force system property cache to be flushed and reloaded
			Field sysPath = ClassLoader.class.getDeclaredField("sys_paths");
			sysPath.setAccessible(true);
			sysPath.set(null, null);
		} catch(Exception e) {
			System.err.println("Could not access sys_paths field");
			e.printStackTrace();
		}
    }
	
	private static void fetchArchive(URL archive, File path) throws MalformedURLException, IOException {
		fetchArchive(archive, path, 1024);
	}
	
	private static void fetchArchive(URL archive, File path, int bufferSize) throws MalformedURLException, IOException {
		System.out.println("Archive: " + archive.toString());
		
		InputStream is = archive.openStream();
		OutputStream os = new FileOutputStream(new File(path, "archive.jar"));
		
		int byteCount = 0;
		byte[] buffer = new byte[bufferSize];
		while( (byteCount = is.read(buffer)) > 0) {
			os.write(buffer, 0, byteCount);
		}
		os.flush();
		os.close();
		is.close();
	}
	
	private static void extractArchive(File path) throws IOException {
		extractArchive(path, 1024);
	}
	
	private static void extractArchive(File path, int bufferSize) throws IOException {
		File archive = new File(path, "archive.jar");
		ZipFile jar = new ZipFile(archive);
		
		Enumeration<? extends ZipEntry> entries = jar.entries();
		while(entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			System.out.println("ZipEntry: " + entry);
			if(entry.isDirectory()) continue;
			
			
			File lib = new File(path, entry.getName());
			lib = new File(path, lib.getName());
			OutputStream os = new FileOutputStream(lib);
			InputStream is = jar.getInputStream(entry);
			int byteCount = 0;
			byte[] buffer = new byte[bufferSize];
			while( (byteCount = is.read(buffer)) > 0) {
				os.write(buffer, 0, byteCount);
			}
			os.flush();
			os.close();
			is.close();
			
		}
		jar.close();
		
		archive.delete();
		if(archive.exists()) 
			archive.deleteOnExit();
	}
}
