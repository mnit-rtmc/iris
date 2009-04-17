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
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import javax.swing.JOptionPane;

/**
 * Simple class to open a web browser in another process
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WebBrowser {

	/** Execute a subprocess with a web browser at the given URL */
	static public void open(URL url) throws IOException {
		if(url == null)
			return;
		open(url.toString());
	}

	/**
	 *  Bare Bones Browser Launch
	 *  Version 1.5 (December 10, 2005)
	 *  Dem Pilafian
	 *  Supports: Mac OS X, GNU/Linux, Unix, Windows XP
	 *  Originally released into the public domain
	 */
	public static void open(String url) {
		String osName = System.getProperty("os.name");
		try {
			// mac
			if(osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName(
					"com.apple.eio.FileManager");
				Method openURL = 
					fileMgr.getDeclaredMethod("openURL",
				new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});

			// windows
			} else if(osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
				"rundll32 url.dll,FileProtocolHandler " + url);

			// linux
			} else {
				String[] browsers = {"firefox", "opera", 
					"konqueror", "epiphany", "mozilla", 
					"netscape"};
				String browser = null;
				for(int count = 0; count < browsers.length && 
					browser == null; count++) 
				{
					if(Runtime.getRuntime().exec(
						new String[] {"which", 
						browsers[count]}).waitFor() 
						== 0)
					{
						browser = browsers[count];
					}
				}
				if(browser == null)
					throw new Exception(
						"Could not find web browser");
				else
					Runtime.getRuntime().exec(
						new String[] {browser, url});
			}
		} catch(Exception e) {
			String m = "There was a problem starting the " +
				"web browser: " + e;
			JOptionPane.showMessageDialog(null, m);
		}
	}
}
