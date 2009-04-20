/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Convenience class to handle I18N messages.
 *
 * @version Initial release, 06/03/08
 * @author p.w.wong, AHMCT
 * @author Michael Darter
 */
public class I18NMessages {

	/** Base name for resource bundles */
	static private final String BASENAME = "MessagesBundle.MessagesBundle";

	/** Value returned for an undefined string */
	static protected final String UNDEFINED = "Undefined I18N string";

	/** Value returned for error reading message */
	static protected final String NOT_READ = "Message bundle not read";

	/** The resource bundle */
	static private ResourceBundle m_i18NMessages = null;

	/** Class can't be instantiated */
	private I18NMessages() {
		assert false;
	}

	/**
	 * Read resource bundle and assign to public static field.
	 * This method should be called once at program startup.
	 *
	 * @param props Opened property file which contains the 
	 * 		language, country, and variant.
	 */
	static public void initialize(Properties props) {
		String l = PropertyFile.get(props, "language");
		String c = PropertyFile.get(props, "country");
		String v = PropertyFile.get(props, "variant");

		System.err.println("I18N: opening I18N resources: Language=" +
			l + ", Country=" + c + ", Variant=" + v);
		try {
			if(v != null && l != null && c != null) {
				Locale loc = new Locale(l, c, v);
				m_i18NMessages = ResourceBundle.getBundle(
					BASENAME, loc);
			}
		}
		catch(Exception ex) {
			System.err.println("I18N: error: could not load " +
				" message bundle: " + ex);
			m_i18NMessages = null;
		}
		if(m_i18NMessages == null) {
			System.err.println("Error: I18NMessages: can't do a " +
				"get() before the message bundle loaded.");
			return "NO_I18N_BUNDLE";
 		}
	}

	/** Get string using id */
	static public String get(String id) {
		if(id == null || id.length() == 0)
			return UNDEFINED;
		if(m_i18NMessages == null) {
			System.err.println("Error: message bundle not loaded.");
			return NOT_READ;
		}
		try {
			return m_i18NMessages.getString(id);
		}
		catch(Exception ex) {
			System.err.println(
				"I18N: error while reading id (" + id +
				") from bundle, ex=" + ex);
			return NOT_READ;
		}
	}
}
