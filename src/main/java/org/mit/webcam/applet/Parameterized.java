package org.mit.webcam.applet;

import java.net.URL;

//Interface for test coverage of applet parameters
public interface Parameterized {
	public String getParameter(String key);
	public URL getCodeBase();
}
