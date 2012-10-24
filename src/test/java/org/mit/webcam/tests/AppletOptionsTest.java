package org.mit.webcam.tests;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.mit.webcam.applet.AppletOptions;
import org.mit.webcam.applet.Parameterized;

public class AppletOptionsTest {

	public static Map<String, String> parameters = new HashMap<String, String>();
	
	static {
		parameters.put("lib_path", "lib");
		parameters.put("base_path", "/base/");
		parameters.put("upload_path", "upload.php");
		parameters.put("rec_width", "1920");
		parameters.put("rec_height", "1200");
		parameters.put("dll_archive_64", "windows_64.jar");
		parameters.put("dll_archive_32", "windows_32.jar");
		parameters.put("so_archive_64", "unix_64.jar");
		parameters.put("so_archive_32", "unix_32.jar");
		parameters.put("dylid_archive_64", "osx_64.jar");
	}
	
	private Parameterized mockApplet;
	private AppletOptions options;

	@Before
	public void setUp() throws Exception {
		mockApplet = new Parameterized(){
			public String getParameter(String key) {
				if(AppletOptionsTest.parameters.containsKey(key)) {
					return AppletOptionsTest.parameters.get(key);
				}
				return null;
			}
			public URL getCodeBase() {
				try {
					return new URL("http://my-rackspace-server.com:8080");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		options = new AppletOptions(mockApplet);
	}

	@Test
	public void testPathSimple() {
		String path1 = "/webcam/";
		String path2 = "libs/";
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "/webcam/libs/");
	}
	
	@Test 
	public void testPathSimple2() {
		String path1 = "/webcam";
		String path2 = "libs/";
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "/webcam/libs/");
	}
	
	@Test
	public void testRootPathOverride() {
		String path1 = "/webcam/";
		String path2 = "/libs";
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "/libs");
	}
	
	@Test
	public void testPath1Null() {
		String path1 = null;
		String path2 = "libs/";
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "libs/");
	}
	
	@Test
	public void testPath2Null() {
		String path1 = "webcam/";
		String path2 = null;
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "webcam/");
	}
	
	@Test
	public void testPathsNull() {
		String path1 = null;
		String path2 = null;
		String path3 = AppletOptions.joinURLPath(path1, path2);
		assertEquals(path3, "");
	}
	
	@Test
	public void testArchiveConfig() {
		String url = this.options.getLibraryURL().toString();
		//testing on unix 64
		assertEquals(url, mockApplet.getCodeBase().toString() + "/base/lib/unix_64.jar");
	}

}
