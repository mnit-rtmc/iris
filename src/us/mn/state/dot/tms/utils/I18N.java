/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Convenience class to handle I18N messages.
 *
 * @author P.W. Wong, AHMCT
 * @author Michael Darter
 * @author Douglas Lau
 */
public class I18N {

	/** Base name for resource bundles */
	static private final String BASENAME = "messages";

	/** Value returned for an undefined string */
	static protected final String UNDEFINED = "Undefined I18N string";

	/** Value returned for error reading message */
	static protected final String NOT_READ = "Message bundle not read";

	/** Key code to use for lookup failure */
	static protected final int FAILURE_CODE = 0;

	/** Char to use for lookup failure */
	static protected final char FAILURE_CHAR = '\0';

	/** The resource bundle */
	static private ResourceBundle m_bundle = null;

	/** Class can't be instantiated */
	private I18N() {
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
		String l = props.getProperty("language");
		String c = props.getProperty("country");
		String v = props.getProperty("variant");
		if(l == null || c == null || v == null) {
			Log.severe("Error: I18N properties (language, " +
				"country, variant) not set");
			return;
		}
		Log.finest("Note: I18N: opening resources: Language=" +
			l + ", Country=" + c + ", Variant=" + v);
		try {
			Locale loc = new Locale(l, c, v);
			m_bundle = ResourceBundle.getBundle(BASENAME, loc);
		}
		catch(MissingResourceException ex) {
			Log.severe("Error: I18N could not load message " +
				"bundle: " + ex);
		}
	}

	/** Get the specified message, with no error messages returned
	 *  if there is an error.
	 *  @param id Name of I18N string in the bundle.
	 *  @return The I18N string cooresponding to id, else null on error */
	static public String getSilent(String id) {
		if(id == null || id.isEmpty())
			return null;
		if(m_bundle == null)
			return null;
		try {
			return m_bundle.getString(id);
		}
		catch(Exception ex) {
			return null;
		}
	}

	/** Get the specified message.
	 *  @param id Name of I18N string in the bundle.
	 *  @return The I18N string cooresponding to id, else error message */
	static public String get(String id) {
		if(id == null || id.isEmpty())
			return UNDEFINED;
		if(m_bundle == null) {
			Log.severe("Error: message bundle not loaded.");
			return NOT_READ;
		}
		try {
			return m_bundle.getString(id);
		}
		catch(Exception ex) {
			Log.warning("Error: attempting to read id (" + id +
				") from bundle, ex=" + ex);
			return NOT_READ;
		}
	}

	/** Return the implied key mnemonic in the specified I18N string.
	 *  For example, if the I18N string is "<html><u>B</u>lank</html>"
	 *  then VK_B is returned. 
	 *  @param id Name of I18N string in the bundle.
	 *  @return The KeyEvent code else 0 on failure.
	 *  @see java.awt.event.KeyEvent */
	static public int getKeyEvent(String id) {
		char c = getKeyEventChar(id);
		if(c == '\0')
			return FAILURE_CODE;
		else
			return charToKeyCode(c);
	}

	/** Return the implied key mnemonic in the specified I18N
	 *  string as a character. For example, if the I18N string
	 *  is "<html><u>B</u>lank</html>" then 'B' is returned.
	 *  @param id Name of I18N string in the bundle.
	 *  @return The 1st underlined char else '\0' on failure. */
	static public char getKeyEventChar(String id) {
		String s = getSilent(id);
		if(s == null)
			return FAILURE_CHAR;
		String utext = SXml.extractUnderline(s);
		if(utext == null || utext.isEmpty())
			return FAILURE_CHAR;
		return Character.toUpperCase(utext.charAt(0));
	}

	/** Return KeyEvent representation of specified character */
	static private int charToKeyCode(char c) {
		c = Character.toUpperCase(c);
		switch(c) {
			case 'A': return KeyEvent.VK_A;
			case 'B': return KeyEvent.VK_B;
			case 'C': return KeyEvent.VK_C;
			case 'D': return KeyEvent.VK_D;
			case 'E': return KeyEvent.VK_E;
			case 'F': return KeyEvent.VK_F;
			case 'G': return KeyEvent.VK_G;
			case 'H': return KeyEvent.VK_H;
			case 'I': return KeyEvent.VK_I;
			case 'J': return KeyEvent.VK_J;
			case 'K': return KeyEvent.VK_K;
			case 'L': return KeyEvent.VK_L;
			case 'M': return KeyEvent.VK_M;
			case 'N': return KeyEvent.VK_N;
			case 'O': return KeyEvent.VK_O;
			case 'P': return KeyEvent.VK_P;
			case 'Q': return KeyEvent.VK_Q;
			case 'R': return KeyEvent.VK_R;
			case 'S': return KeyEvent.VK_S;
			case 'T': return KeyEvent.VK_T;
			case 'U': return KeyEvent.VK_U;
			case 'V': return KeyEvent.VK_V;
			case 'W': return KeyEvent.VK_W;
			case 'X': return KeyEvent.VK_X;
			case 'Y': return KeyEvent.VK_Y;
			case 'Z': return KeyEvent.VK_Z;
			case '0': return KeyEvent.VK_0;
			case '1': return KeyEvent.VK_1;
			case '2': return KeyEvent.VK_2;
			case '3': return KeyEvent.VK_3;
			case '4': return KeyEvent.VK_4;
			case '5': return KeyEvent.VK_5;
			case '6': return KeyEvent.VK_6;
			case '7': return KeyEvent.VK_7;
			case '8': return KeyEvent.VK_8;
			case '9': return KeyEvent.VK_9;
			default: return FAILURE_CODE;
		}
	}
}
