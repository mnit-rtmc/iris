/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 */
package us.mn.state.dot.tms.client.toast;

import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.KeyStroke;
import us.mn.state.dot.tms.utils.WebBrowser;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Help system functionality. A context sensitive web page is started
 * each time the user presses the F1 key. This class provides static
 * help system methods invoked elsewhere.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @see I18N
 */
public class Help {

	/** System default help page name */
	public final static String DEFAULT_HELP_PAGE_NAME = "Help.Default";

	/** get key that initiates help system */
	static KeyStroke getSystemHelpKey() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	}

	/** Invoke help with the specified URL.
	 * @param url HTTP URL of help page to load. */
	static public void invokeHelp(String url) throws IOException {
		WebBrowser.open(url == null ? 
			I18N.get(DEFAULT_HELP_PAGE_NAME) : url);
	}

	/** constructor */
	private Help() { }
}
