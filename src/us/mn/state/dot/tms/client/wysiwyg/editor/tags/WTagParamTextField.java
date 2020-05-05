/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor.tags;


/** Tag parameter input class to take text that will be substituted into
 *  message text.
 */
@SuppressWarnings("serial")
class WTagParamTextField extends WTagParamField {
	
	/** Invalid characters that will not be allowed */
	private static final String invalidChars = "`";
	
	/** Range of allowed ASCII values */
	private static final int asciiMin = 32;
	private static final int asciiMax = 122;
	
	public WTagParamTextField(String text, int columns, boolean required) {
		super(text, columns, required);
	}
	
	/** The string taken from this parameter must be suitable for use in a
	 *  message text (upper-case ASCII, numbers, and some special characters).
	 */
	@Override
	protected boolean validateString() {
		// get the text from the box and check each character individually
		String txt = getText();
		for (char c: txt.toCharArray()) {
			// if any character is invalid, return false
			int i = (int) c;
			if (i < asciiMin)
				return false;
			else if (i > asciiMax)
				return false;
			else if (invalidChars.indexOf(c) >= 0)
				return false;
			else if (Character.isLowerCase(c))
				return false;
		}
		return true;
	}
}
