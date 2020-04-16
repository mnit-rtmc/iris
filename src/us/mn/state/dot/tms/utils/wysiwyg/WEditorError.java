/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * WYSIWYG Editor Error
 * 
 * Contains information about an error that
 * occurs somewhere in the WYSIWYG editor.
 * This will usually be a MULTI error, but
 * can be other things as well.
 * 
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 */

public class WEditorError {
	MultiSyntaxError mse; // MultiSyntaxError
	String shortErrStr;   // short MULTI error name
	String longErrStr;    // human readable explanation
	WToken tok;			  // token associated with the error, if applicable

	/** Constructor for a MultiSyntaxError error */
	public WEditorError(MultiSyntaxError xmse) {
		longErrStr = getI18NErrStr(xmse);
	}
	
	/** Constructor for a MultiSyntaxError associated with a specific token */
	public WEditorError(MultiSyntaxError xmse, WToken t) {
		longErrStr = getI18NErrStr(xmse);
		tok = t;
	}

	/** Constructor for errors that are not
	 *  MultiSyntaxError errors */
	public WEditorError(String em) {
		mse = MultiSyntaxError.other;
		shortErrStr = "other";
		longErrStr = getI18NErrStr(em);
	}
	
	/** Read the human-readable error string from I18N definitions.
	 *  Used when the error is a MultiSyntaxError.
	 */
	private String getI18NErrStr(MultiSyntaxError xmse) {
		shortErrStr = "MultiSyntaxError." + xmse.toString();
		String des = getI18NErrStr(shortErrStr);
		return des;
	}

	/** Read the human-readable error string from I18N definitions.
	 *  Used when the error is a not MultiSyntaxError.
	 */
	private String getI18NErrStr(String em) {
		String des = I18N.get(em);
		if (des.startsWith("I18N: failed to read ("))
			des = em;
		return des;
	}

	//-------------------------------------------
	// Get info about token error
	
	/** Return whether or not this error has a token associated with it. */
	public boolean hasToken() {
		return tok != null;
	}
	
	public WToken getToken() {
		return tok;
	}

	/** Get short error description */
	public String getShortErrStr() {
		return shortErrStr;
	}

	/** Get long error description. If there is a token associated with this
	 *  error, it is included in the description.
	 */
	public String getLongErrStr() {
		if (tok != null)
			return longErrStr + ": " + tok.toString();
		else
			return longErrStr;
	}
	
	/** Provide a short representation of the error. Includes the token if
	 *  there is one.
	 */
	public String toString() {
		if (tok != null)
			return shortErrStr + ": " + tok.toString();
		else
			return shortErrStr;
	}
}
