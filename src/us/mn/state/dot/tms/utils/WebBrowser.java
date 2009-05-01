/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Some parts of this file were based on:
 *   Bare Bones Browser Launch
 *   Version 1.5 (December 10, 2005)
 *   Dem Pilafian
 *   Supports: Mac OS X, GNU/Linux, Unix, Windows XP
 *   Originally released into the public domain
 */
package us.mn.state.dot.tms.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Simple class to open a web browser in another process.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WebBrowser {

	/** Exception for web browser not found */
	static protected final IOException BROWSER_NOT_FOUND =
		new FileNotFoundException("Could not find web browser");

	/** Linux web browsers */
	static protected final String[] LINUX_BROWSERS = {
		"firefox", "epiphany", "konqueror", "mozilla", "opera"
	};

	/** Execute a subprocess with a web browser at the given URL */
	static public void open(URL url) throws IOException {
		if(url == null)
			return;
		open(url.toString());
	}

	/** Open a URL in a web browser */
	static public void open(String url) throws IOException {
		String osName = System.getProperty("os.name");
		if(osName.startsWith("Mac OS"))
			openMac(url);
		else if(osName.startsWith("Windows"))
			openWindows(url);
		else
			openLinux(url);
	}

	/** Open a web browser on a Mac computer */
	static protected void openMac(String url) throws IOException {
		try {
			Class mgr = Class.forName("com.apple.eio.FileManager");
			Method openURL = mgr.getDeclaredMethod("openURL",
				new Class[] { String.class }
			);
			openURL.invoke(null, new Object[] { url });
		}
		catch(ClassNotFoundException e) {
			throw BROWSER_NOT_FOUND;
		}
		catch(NoSuchMethodException e) {
			throw BROWSER_NOT_FOUND;
		}
		catch(IllegalAccessException e) {
			throw BROWSER_NOT_FOUND;
		}
		catch(InvocationTargetException e) {
			throw BROWSER_NOT_FOUND;
		}
	}

	/** Open a web browser on a Windows computer */
	static protected void openWindows(String url) throws IOException {
		Runtime.getRuntime().exec(
			"rundll32 url.dll,FileProtocolHandler " + url);
	}

	/** Open a web browser on a Linux computer */
	static protected void openLinux(String url) throws IOException {
		String browser = locateBrowser();
		Runtime.getRuntime().exec(new String[] {
			browser, url
		});
	}

	/** Locate a browser on a Linux computer */
	static protected String locateBrowser() throws IOException {
		for(String browser: LINUX_BROWSERS)
			if(browserExists(browser))
				return browser;
		throw BROWSER_NOT_FOUND;
	}

	/** Check if a browser exists */
	static protected boolean browserExists(String name) throws IOException {
		Process p = Runtime.getRuntime().exec(
			new String[] { "which", name }
		);
		try {
			return p.waitFor() == 0;
		}
		catch(InterruptedException e) {
			return false;
		}
	}
}
