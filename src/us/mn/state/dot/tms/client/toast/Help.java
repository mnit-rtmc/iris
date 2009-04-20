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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import us.mn.state.dot.tms.utils.WebBrowser;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Help system functionality. A context sensitive web page is started
 * each time the user presses the F1 key. This class provides 1) static
 * help system methods invoked elsewhere and 2) the action listener
 * that handles keystroke invocations.
 * @author Michael Darter
 * @see SmartDesktop, AbstractForm, I18NMessages
 */
public class Help implements ActionListener {

	/** system default help page name */
	public final static String defaultHelpPageName = "Help.Default";

	/** smart desktop */
	static SmartDesktop m_sd = null;

	/** constructor */
	public Help(SmartDesktop sd) {
		m_sd = sd;
	}

	/** get key that initiates help system */
	static KeyStroke getSystemHelpKey() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
	}

	/** Invoke help with the specified URL.
	 *  @param url HTTP URL of help page to load. */
	static public void invokeHelpWithUrl(String url) {
 		// URL to load when no URL found
		String DEFAULT_ERROR_URL = "NO_URL_SPECIFIED_IN_HELP";
 		WebBrowser.open(url == null ? 
 			DEFAULT_ERROR_URL : url);
 	}

	/** Invoke help with the specified i18n page name.
 	 *  @param pn Page name (I18N string tag) of help page to load. */
 	static public void invokeHelpWithPageName(String pn) {
		invokeHelpWithUrl(I18NMessages.get(pn == null ? 
			defaultHelpPageName : pn));
 	}

	/** Invoke help. The help URL is set by each form. */
	static public void invokeHelp() {
		AbstractForm cf = m_sd.findTopFrame();
		//System.err.println("cf="+(cf == null ? "null" : cf.title));
		String url = (cf == null ? 
			I18NMessages.get(defaultHelpPageName) :
			cf.getHelpPageUrl());
		WebBrowser.open(url == null ? 
			"http://iris.dot.state.mn.us/" : url);
	}

	/** Handle keystroke via ActionListener interface.
	 *  @see SmartDesktop */
	public void actionPerformed(ActionEvent e) {
		Help.invokeHelp();
	}
}
