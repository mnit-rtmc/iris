/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.KeyStroke;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;

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
	static private final String DEFAULT_HELP_PAGE_NAME = "Help.Default";

	/** get key that initiates help system */
	static public KeyStroke getSystemHelpKey() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	}

	/** Invoke help with the specified page name.
	 * @param name Name of help page to load. */
	static public void invokeHelp(String name) {
		invokeHelpUrl(I18N.get(pageName(name)));
	}

	/** Get a help page name */
	static private String pageName(String name) {
		return (name != null) ? name : DEFAULT_HELP_PAGE_NAME;
	}

	/** Invoke help with the specified URL.
	 * @param url URL of help page to load. */
	static public void invokeHelpUrl(final String url) {
		runSwing(new Invokable() {
			public void invoke() throws IOException {
				WebBrowser.open(url);
			}
		});
	}

	/** constructor */
	private Help() { }
}
