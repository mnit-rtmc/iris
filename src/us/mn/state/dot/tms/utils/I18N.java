/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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
	static private final String UNDEFINED = "Undefined I18N string";

	/** Value returned for error reading message */
	static private final String NOT_READ = "Message bundle not read";

	/** Key code to use for lookup failure */
	static private final int FAILURE_CODE = 0;

	/** Char to use for lookup failure */
	static private final char FAILURE_CHAR = '\0';

	/** Resource bundle with language, country and variant */
	static private ResourceBundle lcv_bundle = null;

	/** Resource bundle with language and country */
	static private ResourceBundle lc_bundle = null;

	/** Resource bundle with language only.  Load the "en" bundle to use
	 * before initialize is called. */
	static private ResourceBundle l_bundle = getBundle("en");

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
		lcv_bundle = getBundle(l, c, v);
		lc_bundle = getBundle(l, c);
		l_bundle = getBundle(l);
	}

	/** Get a resource bundle */
	static private ResourceBundle getBundle(String l, String c, String v) {
		if (l != null && c != null && v != null)
			return getBundle(new Locale(l, c, v));
		else
			return null;
	}

	/** Get a resource bundle */
	static private ResourceBundle getBundle(String l, String c) {
		if (l != null && c != null)
			return getBundle(new Locale(l, c));
		else
			return null;
	}

	/** Get a resource bundle */
	static private ResourceBundle getBundle(String l) {
		if (l != null)
			return getBundle(new Locale(l));
		else
			return null;
	}

	/** Get a resource bundle */
	static private ResourceBundle getBundle(Locale loc) {
		try {
			return ResourceBundle.getBundle(BASENAME, loc);
		}
		catch (MissingResourceException e) {
			System.err.println("I18N could not load bundle: " +loc);
			return null;
		}
	}

	/** Get the specified message, with no error messages returned
	 * if there is an error.
	 * @param id Name of I18N string in the bundle.
	 * @return The I18N string cooresponding to id, else null on error */
	static public String getSilent(String id) {
		if (id == null || id.isEmpty())
			return null;
		else
			return getString(id);
	}

	/** Get the specified message.
	 * @param id Name of I18N string in the bundle.
	 * @return The I18N string cooresponding to id, else error message */
	static public String get(String id) {
		if (id == null || id.isEmpty())
			return UNDEFINED;
		else {
			String val = getString(id);
			if (val != null)
				return val;
			else {
				System.err.println("I18N: failed to read (" +
					id + ") from bundle");
				return NOT_READ;
			}
		}
	}

	/** Get a string value from a resource bundle */
	static private String getString(String key) {
		if (lcv_bundle != null) {
			try {
				return lcv_bundle.getString(key);
			}
			catch (Exception e) {
				// fall through
			}
		}
		if (lc_bundle != null) {
			try {
				return lc_bundle.getString(key);
			}
			catch (Exception e) {
				// fall through
			}
		}
		if (l_bundle != null) {
			try {
				return l_bundle.getString(key);
			}
			catch (Exception e) {
				// fall through
			}
		}
		return null;
	}

	/** Return the implied key mnemonic in the specified I18N string.
	 * For example, if the I18N string is "<html><u>B</u>lank</html>"
	 * then VK_B is returned.
	 * @param id Name of I18N string in the bundle.
	 * @return The KeyEvent code else 0 on failure.
	 * @see java.awt.event.KeyEvent */
	static public int getKeyEvent(String id) {
		char c = getKeyEventChar(id);
		if (c == FAILURE_CHAR)
			return FAILURE_CODE;
		else
			return charToKeyCode(c);
	}

	/** Return the implied key mnemonic in the specified I18N
	 * string as a character. For example, if the I18N string
	 * is "<html><u>B</u>lank</html>" then 'B' is returned.
	 * @param id Name of I18N string in the bundle.
	 * @return The 1st underlined char else '\0' on failure. */
	static public char getKeyEventChar(String id) {
		String s = getSilent(id);
		if (s == null)
			return FAILURE_CHAR;
		String utext = extractUnderline(s);
		if (utext == null || utext.isEmpty())
			return FAILURE_CHAR;
		return Character.toUpperCase(utext.charAt(0));
	}

	/** Extract underlined text from the argument.
	 * @return Null on failure or if no underline text exists,
	 *         else the underlined text, which might have length 0. */
	static public String extractUnderline(String xml) {
		final String TAG_OPEN = "<u>";
		final String TAG_CLOSE = "</u>";
		if (xml == null || xml.isEmpty())
			return null;
		int s = xml.indexOf(TAG_OPEN);
		if (s < 0)
			return null;
		int e = xml.indexOf(TAG_CLOSE, s);
		if (e < 0)
			return null;
		if (s >= e)
			return "";
		return xml.substring(s + TAG_OPEN.length(), e);
	}

	/** Return KeyEvent representation of specified character */
	static private int charToKeyCode(char c) {
		c = Character.toUpperCase(c);
		switch (c) {
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
